# 测试规范（Testing Standards）

本仓库的测试体系围绕一个事实构建：**学习到的行为模型（golden 文件）是运行时行为的
一部分**，所以测试除了守代码，还要守数据。CI 与本地完全等价：`mvn verify`。

## 1. 命令与运行方式

```bash
mvn verify                     # 全量：编译 + 全部测试 + jar（与 CI 一致，~10s）
mvn test                       # 只跑测试阶段
mvn test -Dtest=GraphTest      # 单个测试类
mvn test -Dtest=GraphTest#testAddNode   # 单个测试方法
```

- **必须从项目根目录运行**：golden 文件与模型加载路径相对根目录
  （`src/main/java/MOCO/modelFiles/...`），测试内有防御性断言提示。
- surefire 已固定 `-Dfile.encoding=UTF-8`，golden IO 跨平台确定。

## 2. 测试分层与现状基线

| 层 | 内容 | 现状 |
| ---- | ---- | ---- |
| 单元测试 | `graph/` 数据结构与算法 | `GraphTest`(309 行)、`GraphAlgorithmsTest`(170 行) |
| 单元测试 | 虚拟设备语义 | `VirtualDeviceTest`、`YeelightTest`、`CoffeeMachineTest` |
| 单元测试 | 工具与检查器 | `UtilsTest`、`ExecutionCheckerTest` |
| 特征测试 | IoT 系统组装（Twin/Controller/调度） | `IoTSystemTest` |
| golden 校验 | 模型文件结构 + 基线数量 | `GoldenModelFilesTest`（14 个模型） |
| 端到端回归 | 重学 Yeelight 与 golden 逐一比对 | `YeelightModelRegressionTest` |

基线：**122 个测试全部通过**是 `main` 的最低门槛；任何 PR 不得使通过数下降
（增加行为 ⇒ 增加测试 ⇒ 通过数通常上升）。

## 3. 新增测试的要求

按变更类型分层要求（与 [workflow.md](workflow.md) 的矩阵对应）：

1. **Bug 修复**：先写复现测试（红）→ 修复 → 绿。测试名陈述被破坏的行为，
   例如 `preCheckRejectsApiWhenStateUnknown`。提交的 PR 中该测试必须曾经红过
   （在 PR 描述的验证记录里写明）。
2. **行为变更**：至少一个断言**新行为**的测试 + 保留（或调整）断言旧契约的测试。
3. **新虚拟设备/学习器**：虚拟设备语义单测 + `GoldenModelFilesTest.EXPECTED_COUNTS`
   新增条目 + （确定性设备）端到端重学回归，模式照抄 `YeelightModelRegressionTest`。
4. **纯重构**：不新增测试，但**既有特征测试必须保持绿**且不得修改其断言
   （修改断言 = 行为变更，回到上一条）。

## 4. golden 模型变更流程（最重要的一节）

改虚拟设备语义、Learner 探索逻辑、导出格式 ⇒ golden 基线必然漂移，`mvn verify` 会红。
**这是设计好的防回归机制，不是障碍。** 处理顺序：

1. 确认漂移是**有意的**（能说出对应 issue/设计文档）。意外漂移 = 探索逻辑被破坏，
   先修代码而不是改基线。
2. 同一 commit 内：
   - 运行对应学习器 `main` 重新生成 `modelFiles/<device>.json` + `<device>_output.dot`；
   - 更新 `GoldenModelFilesTest.EXPECTED_COUNTS` 的 `[nodeCount, uniqueTransitionCount]`；
   - Yeelight 另需同步 `YeelightModelRegressionTest` 里钉住的 8/16；
   - commit message 写明模型变更原因（Conventional Commits）。
3. PR 描述中给出新旧基线数字对比与解释。

## 5. 测试代码规范

- 框架 JUnit 4（`org.junit.Test` + `Assert.*` 静态导入），不引入 JUnit 5/TestNG。
- 测试类与被测类同包；一个测试类覆盖一个被测类或一个紧密场景。
- 断言三要素：实际值、期望值、**人能读懂的失败消息**（说明"去哪看"）。
- golden/文件比较按**集合语义**（`assertEquals(Set, Set)`），dot 行序来自 HashSet 迭代，
  永不比较行序或整文件字符串。
- 涉及时间（`DelayQueue`、100ms 修复延迟）的测试不真实睡眠等待：用确定性的
  直接方法调用/可注入时钟（现状 `ExecutionCheckerTest` 的做法），避免脆弱测试。
- 测试内禁止 `System.out` 打印诊断信息（失败信息写在断言消息里）。
- 不写相互依赖顺序的测试方法（JUnit 执行顺序不保证）。

## 6. CI

- `.github/workflows/ci.yml`：push 到 `main` + 所有 PR → `mvn -B -ntp verify`
  （ubuntu-22.04，Temurin JDK 17，Maven 缓存），失败上传 surefire 报告。
- 本地 `mvn verify` 与 CI **完全同一条命令**：本地绿了 CI 基本就绿
  （平台差异已由编码固定消除）。CI 红时优先怀疑环境与真实回归，而不是重跑碰运气。
- 修改 CI workflow 属于流程变更：在 PR 中说明动机，并保持"本地=CI"的不变量。
