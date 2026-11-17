# Design (as-built): 行为模型学习器（Behavior Model Learner）

[English](2026-11-17-as-built-behavior-model-learner.md) | [简体中文](2026-11-17-as-built-behavior-model-learner.zh-CN.md)

- **日期**：2026-11-17
- **组件**：`MOCO/Learner/BehaviorModelLearner`（骨架）+ 14 个 `*_Model` 学习器 + `MOCO/API/Edge/Node` + `MOCO.Utils`（导出/加载）
- **状态**：Implemented（as-built 记录，逆向自 `main` 现有代码）
- **关联 Issue**：#5

## 问题与背景

MOCA 的第一支柱是"学习设备行为模型"：对虚拟设备（内存确定状态机）或真实设备
（Python 脚本驱动的米家设备）做主动探索，产出**运行时直接使用**的行为图
（`modelFiles/*.json` + `*.dot`）。学习器的正确性直接决定 preCheck/postCheck 的正确性，
因此其探索语义、编号规则、导出格式都是**必须钉死的知识**。本文沉淀 as-built 设计。

## 目标 / 非目标

- 目标：完整、确定地枚举设备可达状态空间，产出可复现、可校验的行为模型文件。
- 非目标：W-method/等价查询等严格 AAL 理论实现（当前是"全 API 字母表 × 状态空间"的
  主动探索 + 收敛判据）；在线学习（模型是离线产物）。

## 方案（as-built）

### 两套学习器形态

| 形态 | 类 | 设备交互 | 收敛 |
| ---- | ---- | ---- | ---- |
| 虚拟设备学习器 | 12 个（`Yeelight_Model` 等，继承 `BehaviorModelLearner<D>`） | 内存对象，`executeApi` 直接调方法 | 重复探索取并集（2 轮 / 或 `learnEdges` 收敛循环） |
| 真实设备学习器 | 4 个（`Fan_real_Model` 等，**独立类**不继承骨架） | `Utils.executePythonScript` 驱动 miio 脚本 | 单轮探索 + 失败重试（MAX_RETRIES=5） |

> 虚拟学习器走统一骨架（历史上 11 份重复代码已收敛到 `BehaviorModelLearner`，
> 见 [../../CODE_REVIEW.md](../../CODE_REVIEW.md) 已修复部分）；真实设备学习器因
> I/O 语义不同（reset-replay-observe）保持每设备独立实现。

### 虚拟设备学习骨架（`BehaviorModelLearner<D>`）

五个抽象契约（子类必须实现）：

```
getAllPossibleAPIs()  → List<API>：探索字母表（API 名 + preemptive + humanIntervention 标志）
executeApi(api, dev)  → String：执行 API；返回 "skip" 表示被拒绝，否则返回 dev.toString()
fromString(state)     → D：从 toString 状态重建设备（深拷贝手段）
toSystemState(dev)    → String：抽象系统态（= 图节点内容，如 toSystemString()）
toStringState(dev)    → String：完整状态（设备克隆用）
newInstance()         → BehaviorModelLearner<D>：新建未探索实例（收敛循环用）
```

**探索算法**（`generateGraph`/`exploreState`，递归 DFS）：

1. 从初始设备出发，对字母表中每个 API：`fromString` 克隆当前设备 → `executeApi`。
2. 返回 `"skip"` 的 API 不产生边；否则记录边 `Edge(systemState, nextSystemState, api.toString())`。
   注意 **`api.toString()` 带 `|preemptive|humanIntervention` 元数据**
   （如 `"turnOn|true|false"`）——这是边名的既定口径，与 preCheck/postCheck 的匹配
   语义纠缠（见"已知缺陷"）。
3. **定点追踪（chaseFixedPoint）**：虚拟设备的派生标志位分两步更新（API 先改可见
   状态、派生标志随后一次调用才跟上）。当"系统态未变但完整态变了"时，重放同一 API
   直到收敛，中间边也记录。上界 `MAX_MODE=20` 防御不收敛设备。
4. 新系统态递归探索；已见状态不重入（`stateTransitions` 去重）。

**收敛循环**（`learnEdges(warmup, maxRuns)`）：重复 `newInstance().generateGraph`，
边集并集增长；连续 `HISTORY_SIZE=20` 次边数不变即停。`CoffeeMachine_Model` 用
`learnEdges(100, 9999)`（因 addWater 等 API 有 `Random` 注水量，状态空间需多轮采样
才能饱和）；其余确定性虚拟设备用固定 2 轮并集（`Yeelight_Model.exploreEdges` 模式）。

**节点编号**（`numberNodes` / 各 `*_Model.main` 内联版本）：全部状态进
`TreeSet(CASE_INSENSITIVE_ORDER)`，依次编号 `S0..Sn`。**大小写不敏感排序**是历史
选择，保证 golden 文件字节级可比——改动即破坏全部基线。

### 导出与加载（`MOCO.Utils`）

- `export(edges, dotFile, jsonFile)`：`Graph(true, true, false)` 装载（节点内容是
  `Node{nodeId, content}` 的 toString，即 `Node{"nodeId":"S3", "content":"Yeelight{...}"}`）；
  同一 (target, api) 去重后写 dot（`generateDotFile`，HashSet 遍历 ⇒ **行序无意义**）
  与 json（`saveEdgesToFile`，**Gson** 序列化 `graph.Edge` 集合）。
- `loadGraphFromFile`（Gson 读回）→ `loadOutEdgeApis`（按节点 content 建立状态 →
  可用 API 集合索引，供 preCheck）。
- **双写契约**：json 与 dot 必须描述同一边集（`GoldenModelFilesTest` 校验）。

### 真实设备学习器（以 `Fan_real_Model` 为模板）

- `XxxStatus.parse(脚本输出)`：miio 设备 status JSON → 抽象状态对象
  （如 FanStatus 的 power/speed_level/angle 三字段）。
- 探索前 **reset-replay**：执行 reset 脚本 → 按目标状态回放 turnOn/setSpeed/setAngle
  → 再执行被测 API → 观察新状态。每个 (state, api) 组合最多重试 5 次。
- 输出与虚拟学习器同构：`Xxx_real.json` + dot，同样走 S0..Sn 编号。

### `API` 元数据语义

`API(name, preemptive, humanIntervention)` 的两个布尔记录"该命令是否抢占式 /
是否需要人工介入"，被编码进边名（`name|p|h`）。**当前运行时（preCheck/postCheck）
并未消费这两个标志的语义**，只在字符串匹配时受其影响——这是元数据设计的
"半实现"现状，如实记录。

## 已知缺陷与耦合（不在本文修复，入口见 CODE_REVIEW.md）

- 边名带元数据 + `getTargetStateBasedOnBehaviourModels` 用 `equals(cmd)` 匹配裸
  API 名 ⇒ 运行期 targetState 几乎恒为 `"null"`（CR-3）。
- `preCheck` 用 `contains` 子串匹配 ⇒ `"turn"` 可命中 `"turnOn|..."`（CR-3 关联）。
- `Utils.executePythonScript` 先 `reader.close()` 后 `waitFor()`，大输出可能死锁；
  不读 stderr（已 `redirectErrorStream(true)` 合并，缓解但未消除）（CR-6）。

## 验收标准（as-built 校验）

- `GoldenModelFilesTest`：14 个模型文件结构 + 数量基线（node/uniqueTransition）钉死。
- `YeelightModelRegressionTest`：Yeelight 端到端重学 == golden（8 节点 16 边），
  且两次运行边集相等（确定性）。
- `UtilsTest`：导出/加载/dot 生成的单元契约。
- `mvn verify` 全绿（122 测试）。

## 风险与兼容性

- **改任何一处**（虚拟设备语义 / API 字母表 / 编号排序 / 边名格式 / 导出顺序）都会
  漂移 golden 基线：必须同一 commit 重新生成 + 更新基线（流程见
  [../testing-standards.md](../testing-standards.md) 第 4 节）。
- 真实设备学习器依赖局域网内设备 IP/token（脚本硬编码），不可在 CI 复现；
  golden 文件因此只校验结构，不重学。

## 开放问题

- 真实设备学习器是否应收敛到统一骨架（与虚拟学习器共享编号/导出）？
  属结构性重构，需独立设计文档；现状记录为"有意不统一"。
