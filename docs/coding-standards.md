# Java 编码规范（Coding Standards）

适用范围：`src/main/java` 与 `src/test/java` 全部 Java 代码。目标是一致性与可审性：
让评审者看到 diff 时，注意力全部落在逻辑上，而不是风格上。
规范参考 qwen-code 等开源仓库的"约定优先于配置"思路，按本仓库现状（Java 8 语法级别、
Maven、无 linter/format 工具链）落地为人工约定。

> 与本规范冲突的存量代码：**不要求顺手大规模重排**（会产生噪声 diff），
> 但你正在修改的文件/方法应向规范靠拢；纯风格大扫除需要独立 PR 并在 issue 中说明。

## 1. 语言与构建

- 源码语法级别 **Java 8**（`pom.xml` 的 `maven.compiler.source/target=8`）。
  禁用 Java 9+ 语法（`var`、switch 表达式、record、text block 等）；
  Stream/Optional 允许，但团队代码以显式循环为主，沿用即可。
- CI 在 JDK 17 编译运行 ⇒ 依赖 API 不得超过 JDK 8 字节码/语言范围之外的新运行时行为
  （例如不要用 JDK 11+ 才有的 `String.isBlank`）。
- 依赖变更：改 `pom.xml` 后必须本地 `mvn verify` 一次确认可用；新增依赖需在
  issue/PR 说明用途（当前仅 log4j2、fastjson、gson、oshi、junit4 五个）。

## 2. 包与文件组织

- 包名沿用现状：`graph` / `MOCO` / `MOCO.Learner` / `MOCO.modelFiles`（资源目录）/
  `IoTSystem`（含 `DeviceController`、`DeviceTwin`）/ `VirtualDevice` / `RealDevice`。
- 依赖方向（见 [architecture.md](architecture.md) 第 2 节）：`graph` 零业务依赖；
  现状中 `MOCO.ExecutionChecker` / `MOCO.MessageProxy` 已存在对 `IoTSystem` 的反向引用，
  **新代码不得再扩大**这种耦合（收窄是欢迎的，但属结构变更需走设计层）。
- 一个类一个文件，文件名 = 公共类名（现状类名不规整如 `Yeelight_Model`，保留；新类用
  无下划线的 `UpperCamelCase`）。

## 3. 命名

| 对象 | 约定 | 示例 |
| ---- | ---- | ---- |
| 类/接口 | UpperCamelCase，新类名不含 `_` | `ExecutionChecker` |
| 方法/字段/局部变量 | lowerCamelCase | `preCheck`、`delayedQueue` |
| 常量 | `UPPER_SNAKE_CASE`，`static final` | `MAX_MODE` |
| 测试类 | 被测类名 + `Test` | `GraphTest` |
| 测试方法 | `被测行为_条件_期望` 的简明陈述句 | `regeneratedModelMatchesGoldenFile` |

- 学习器与模型文件的既有对应关系保持：`Xxx_Model.java` ↔ `modelFiles/xxx.json` +
  `xxx_output.dot`（小写开头是文件现状，保留）。

## 4. 日志

- 统一 Log4j2：`private static final Logger LOGGER = LogManager.getLogger();`
  （沿用现状的类内常量风格）。
- 级别语义：`ERROR` 需要人介入的失败；`WARN` 可自动恢复但值得知道；`INFO` 生命周期与
  业务里程碑；`DEBUG` 过程细节。log4j2.xml 已按级别分文件落盘 `log/`。
- **禁止新代码使用 `System.out/System.err`**（存量 16 处，不要求一次性清理）。
- 消息拼接：简单场景用 `+` 拼接（现状风格）；热点路径可用 `LOGGER.info("x={}", x)`
  占位符形式，二者可接受，不要在同一个类里混两种风格。
- 日志必须能定位：含设备 id / 状态 / API 名等上下文，而不是裸 `"failed"`。

## 5. JSON 使用

- 生产状态序列化用 **fastjson**（`JSON`/`JSONObject`，沿用 VirtualDevice/RealDevice 现状）；
  模型文件解析用 **Gson**（`MOCO.Utils`，沿用现状）。**不要在既定场景外引入第二选择**
  （不要用 Gson 序列化设备状态，也不要用 fastjson 读模型文件）。
- 跨进程/跨语言的字符串状态协议（`toString` 家族）属于**行为契约**：任何格式变化都可能
  破坏按字符串比对的一致性检查（见 [architecture.md](architecture.md) 第 4 节），
  需按行为变更流程走。

## 6. 注释与文档

- 默认**不写**注释；只在"为什么这么做"不明显时写，解释 why 而不是 what。
- 保持现状里的类级 Javadoc 风格（如 `ExecutionChecker`、`BehaviorModelLearner` 的
  契约式 Javadoc）。公共工具方法（`graph/`、`MOCO.Utils`）新方法应有一句话 Javadoc。
- 不删除仍然成立的历史注释；不再适用的注释应在同一次修改中删除。
- TODO 格式：`// TODO <github用户名或issue编号>: 说明`，必须可追踪。

## 7. 异常与空值

- 不吞异常：`catch` 后必须至少 `LOGGER.warn` 以上记录；静默吞掉会破坏一致性检查的
  可观测性（存量 `MessageQueue` 中断返回 null 的行为见 CODE_REVIEW.md，属已知项）。
- 对外 API（`MessageProxy.addMessage`、`ExecutionChecker.preCheck` 等）保持现有
  布尔/无异常返回语义；失败路径要打日志。
- 参数校验：公共入口对 null/非法输入早返回并记录，不抛出未被既有调用方处理的
  RuntimeException。

## 8. 并发

- 现状：单线程 `MessageQueue` 消费 + `DelayQueue` 修复重放 + 少量 latch（未生效，已知项）。
- 新增共享可变状态前先读 [architecture.md](architecture.md) 第 3.2 节与 CODE_REVIEW.md
  第 4/5 条；能避免共享就避免（学习器每轮新建实例的既定模式即为此设计）。
- 演示代码中的 `Thread.sleep`/轮询仅限 `Main`/`TaskScheduler` 现状模式，不扩散到核心
  检查逻辑。

## 9. 资源与 IO

- 文件路径一律相对项目根（`src/main/java/MOCO/modelFiles/...`），与现状一致
  ⇒ 测试必须从根目录运行（已在测试断言里防御）。
- 读文件用 `Files.readAllBytes` + `StandardCharsets.UTF_8`（现状风格）；
  写文件后确保内容可被 `Utils.loadGraphFromFile` 读回（json 与 dot 的双写契约）。
- Python 脚本调用（`Utils.executePythonScript`）存在已知的流死锁风险（CR 记录），
  在其修复前，新代码不要依赖该方法的大输出场景。

## 10. 测试代码风格

详见 [testing-standards.md](testing-standards.md)。编码侧配合约束：

- 测试与被测包同名（`src/test/java` 镜像 `src/main/java` 的包结构）。
- 断言消息必须解释失败时该看什么（现状测试是好例子：`"golden yeelight.json not found; 
  run tests from the project root: " + path`）。
- 不为覆盖率写空转断言；每个测试方法回答一个明确问题。

## 11. Git 提交

- **Conventional Commits**：`feat|fix|refactor|docs|test|build|chore(scope): 描述`，
  scope 建议用包名（`feat(learner): ...`、`fix(execution-checker): ...`）。
- golden 模型有意变更的 commit 必须在 message 中写明原因（README 已有此约定，继续执行）。
- 一个 commit 一件事；不把无关重构混进功能/修复提交。
