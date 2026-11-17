# 架构与开发上下文（Architecture & Development Context）

本文是 MOCO/MOCA 仓库的**开发上下文**：模块地图、数据流、关键契约与代码事实。
任何贡献者（人类或编码 Agent）在修改代码前应先读完本文，避免破坏既有契约。

> 事实基线：本文描述的行为以 `main` 分支 + 通过 `mvn verify` 的 122 个测试为准。
> 已知的遗留缺陷与风险统一记录在 [CODE_REVIEW.md](../CODE_REVIEW.md)，本文不重复展开，
> 只在受影响位置用 `⚠（CR-x）` 标注并链接。

## 1. 项目是什么

MOCA（代码根包名 `MOCO`，论文中称 MOCA）是一个维护 IoT 系统运行时一致性的工具，
核心思路：

1. **学习**：用主动自动机学习（AAL）对（虚拟/真实）设备做状态空间探索，得到设备**行为模型**
   （有向图：状态节点 + API 边）。
2. **执行前检查**：收到应用下发的命令时，用模型判定当前状态是否允许该命令（preCheck）。
3. **执行后校验**：命令执行后比较"物理设备状态 / 数字孪生状态 / 模型预测状态"三方是否一致，
   不一致则计算修复动作并入队（postCheck）。

技术栈：Java 8 语法级别（`maven.compiler.source/target=8`）、Maven 单模块、
JUnit 4、Log4j2、fastjson（生产代码 JSON 序列化）、Gson（模型文件解析）、oshi（系统监控，仅 `Main` 演示用）。
CI 在 **JDK 17** 上构建运行（源码保持 8 语法兼容，见 [coding-standards.md](coding-standards.md)）。

## 2. 模块地图

```
src/main/java/
├── graph/          通用有向图库（独立、无业务依赖）：Graph、Edge、Node、Utils、
│   └── algorithms/ Tarjan、Topsort、IsAcyclic、FindCycles、Components
├── MOCO/           核心一致性引擎
│   ├── API / Edge / Node / Utils        行为模型的数据结构与 IO
│   ├── MessageProxy                     应用 → MOCA 的入口（代理开关）
│   ├── ExecutionChecker                 preCheck / postCheck / 修复路径计算
│   ├── Learner/BehaviorModelLearner     AAL 公共骨架（状态探索、收敛、编号、导出）
│   ├── Learner/*_Model                  14 个具体设备学习器（其中 *_real_Model 4 个对应真实设备）
│   └── modelFiles/                      学习到的行为模型 golden 文件（json + dot）
├── IoTSystem/      被代理的 IoT 侧
│   ├── Main                             演示入口（初始化 5 设备 → 跑消息 → 性能采样）
│   ├── TaskScheduler / MessageQueue / MessageSender / Message / DelayedMessage
│   ├── DeviceController/                5 个控制器 + AbstractDeviceController 基类
│   └── DeviceTwin/                      5 个数字孪生
├── VirtualDevice/  10 个虚拟设备（确定性状态机，无 I/O）
└── RealDevice/     真实设备脚本（Python）+ deviceTwins4Real/（fastjson 状态解析）
```

依赖方向（**新代码只能自上而下，不得反向**；存量中 `MOCO`→`IoTSystem` 的既有耦合见注）：

```
IoTSystem ──▶ MOCO ──▶ VirtualDevice ──▶ (无)
IoTSystem ──▶ VirtualDevice
MOCO ──────▶ graph            （graph 不依赖任何业务包，保持可独立测试）
MOCO.Learner ──▶ VirtualDevice / RealDevice
```

- `graph/` 是最底层库，不允许 import `MOCO`/`IoTSystem`/`VirtualDevice`/`RealDevice` 中任何类。
- 存量例外：`MOCO.ExecutionChecker` / `MOCO.MessageProxy` 引用了 `IoTSystem` 的
  Message/Controller/Twin（与图中方向相反）。**新代码不得扩大**这组反向依赖，
  收窄（消除）是欢迎的，但属于行为/结构变更，需按 [workflow.md](workflow.md) 走设计层。

## 3. 核心数据流

### 3.1 离线：学习 → golden 模型文件

```
*_Model.main()
  → exploreEdges()（多轮重复探索取并集）
  → 状态以 toSystemString() 文本为节点、API 名为边 → 归一化编号 S0..Sn
  → modelFiles/<device>.json + <device>_output.dot
```

- 学习器实例有状态（visited set），**每轮探索新建实例**。
- golden 文件由 `mvn verify` 三重校验：结构合法（唯一转移、S0..Sn 无洞、json↔dot 一致）＋
  数量基线（`GoldenModelFilesTest.EXPECTED_COUNTS`）＋ Yeelight 端到端重学比对。
- **契约**：改虚拟设备语义或 Learner ⇒ golden 变化 ⇒ 同一 commit 里重新生成模型并更新基线数字。

### 3.2 在线：消息代理 → 执行 → 一致性检查

```
MessageSender.sendMessage*()
  → MessageProxy.addMessage(message, currentState)        [代理开: preCheck，代理关: 直通]
      preCheck: 用 outEdgeApis 索引判断当前状态是否允许该 API
  → TaskScheduler.addMessage() → MessageQueue（单线程消费）
  → dispatchMessage → DeviceController.invoke(api)        [更新物理设备 + DeviceTwin]
  → ExecutionChecker.postCheck(api, preState, controller, delayedQueue, message)
      checkDeviations → 数字孪生偏差 / 物理偏差
      偏差 → calculateSolutions(behaviorGraph, ...) 算修复动作
           → DelayedMessage 入 DelayQueue（100ms）→ 重放
```

⚠（CR-1/2/3）当前 postCheck 的物理偏差检测存在硬编码覆盖、非 CM 设备用了 CM 模型、
API 名匹配口径不一致三个已知缺陷，详见 [CODE_REVIEW.md](../CODE_REVIEW.md)。
**修复它们属于行为变更，需走 [workflow.md](workflow.md) 的 design 层，不要顺手改。**

## 4. 关键代码契约（改动前必读）

| 契约 | 位置 | 说明 |
| ---- | ---- | ---- |
| golden 模型基线 | `GoldenModelFilesTest.EXPECTED_COUNTS` | 任何影响学习结果的改动都要同步再生成 + 更新 |
| 学习确定性 | `YeelightModelRegressionTest` | Yeelight 虚拟设备全确定 ⇒ 两次学习边集必须相等；改探索逻辑/HashSet 语义会打破 |
| 模型文件被加载两次 | `Main`（一次）与 `ExecutionChecker` 静态块（一次） | 路径 `src/main/java/MOCO/modelFiles/*.json` 相对项目根 ⇒ **测试必须从根目录跑** |
| dot 行序无意义 | `generateDotFile` 遍历 `HashSet` | 所有 dot 比较必须按**边集合**而非行序 |
| 状态即字符串 | Twin/Device 的 `toString`/`toSystemString`/`toDeviceString` | 多处按字符串比对/替换前缀（如 `WMTwin`→`WashingMachine`），格式变化=行为变化 |
| `graph/` 零业务依赖 | `graph/**` | 保持库纯净；业务类型通过泛型/`Object` 进入 |
| JDK 兼容 | `pom.xml` | 源码 Java 8 语法（无 var/switch 表达式/stream API 放开限制前先评审），CI 运行于 17 |

## 5. 构建与测试事实

- `mvn verify`（根目录）= CI 完全一致：编译 60+ 源文件 + 122 个测试（10 个测试类），
  本地耗时约 10s。
- 单测：`mvn test -Dtest=ClassName`（surefire 3.2.5，JUnit 4）。
- 测试文件编码经 surefire `argLine -Dfile.encoding=UTF-8` 固定，保证 golden IO 跨平台确定。
- 日志：Log4j2 `src/main/resources/log4j2.xml`，控制台 INFO+，落盘 `log/`（已 git-ignore）。
  代码中仍有少量 `System.out`（16 处，历史遗留），新代码一律用 LOGGER。

## 6. 仓库工程化现状

- 分支保护：`main` 只接受 PR，CI 必须绿。
- CI（`.github/workflows/ci.yml`）：push/PR → `mvn -B -ntp verify`（ubuntu-22.04, JDK 17 temurin），
  失败时上传 surefire 报告。
- PR 模板：`.github/pull_request_template.md`。
- Agent 约定：根目录 [AGENTS.md](../AGENTS.md)（供编码 Agent 读取的英文约定）。

## 7. 已知遗留问题入口

按严重程度与修复建议见 [CODE_REVIEW.md](../CODE_REVIEW.md)。处理其中任一问题前：
先在 issue 层确认范围 → 按 [workflow.md](workflow.md) 判断是否需要 design 文档
（涉及行为/跨模块的，一律需要）。
