# Code Review Findings (整体代码逻辑审查)

本文件记录对 MOCO/MOCA 代码库的整体审查结论，供后续维护参考。
（This file records the overall code-logic review conclusions for future maintenance.）

## 审查范围

- `MOCO/`：MessageProxy、ExecutionChecker、Utils、API/Edge/Node、Learner（13 个学习器）
- `IoTSystem/`：Main、TaskScheduler、MessageSender、MessageQueue、Message、DelayedMessage、DeviceController、DeviceTwin
- `VirtualDevice/`：10 个虚拟设备
- `graph/`：图数据结构与算法（未改动，仅审查）

## 已在本次重构中修复 / 去重的部分

1. **Learner 大规模重复**：11 个虚拟设备学习器各复制约 200 行相同的状态探索、迭代收敛、节点编号、dot/json 导出逻辑 → 抽取到 `MOCO/Learner/BehaviorModelLearner`（行为与重构前逐一比对，边集完全一致）。
2. **Controller 样板代码**：5 个控制器重复 deviceId/device/twin 存取与 `printInternalState` → 抽取到 `IoTSystem/DeviceController/AbstractDeviceController`。
3. **MessageSender 逐条复制**：每个消息 5 行复制代码 → 统一 `send()` 帮助方法（保留了原代码中“用第一条消息取状态”的既有行为）。
4. **ExecutionChecker.checkDeviations**：5 个 case 各 40 行相同骨架 → 统一 `checkControllerDeviation`；`preCheck` 的 switch 简化；`calculateSolutions` 的三段重复选择合并为单次遍历（选择优先级保持不变）。
5. `generateGWActions` 中删除了一条创建后即丢弃的 `new Message(...)` 无效语句（无行为影响）。

## 审查发现的问题（未在本次修复，属设计/行为变更，需要单独决策）

按严重程度排列：

### 1. postCheck 的“物理偏差检测”实际上大部分被硬编码覆盖（CM 分支）

`checkDeviations` 的 CoffeeMachine 分支中：

```java
String currentPhysicalState = ((CMController) controller).getCoffeeMachine().toSystemStateString();
currentPhysicalState = "CoffeeMachine{'waterReady':'true', ...}";  // 立即被覆盖
```

真实读取的物理状态被一行硬编码字符串覆盖。这意味着 CM 的物理偏差检测永远拿一个固定状态和目标状态比较，属于明显的调试残留。本 PR 保持了该行为（重构以不改变行为为前提），但该逻辑应当被清理或用真实设备状态。

### 2. 修复路径计算对 4/5 的设备用了错误的模型

`checkDeviations` 中除 CoffeeMachine 外，Gateway/Light/VC/WM 分支计算修复方案时传入的都是 `cm_behaviorGraph`（咖啡机模型），而不是各自设备的行为模型：

```java
List<String> actionLists = calculateSolutions(cm_behaviorGraph, currentGateway, targetState_gateway, message);
```

这看起来是复制粘贴错误：非 CM 设备发生物理偏差时，会在咖啡机模型上查找修复路径。同样属于行为变更范围，本次保持原样并在测试中记录。

### 3. preCheck / getTargetState 的 API 名称匹配不一致

- 模型中边名带元数据：`"turnOn|true|false"`；
- `preCheck` 用 `apis.toString().contains(apiName)` —— 子串匹配，`"turn"` 也能命中 `"turnOn|..."`；
- `getTargetStateBasedOnBehaviourModels` 用 `edge.getName().equals(cmd)` —— 全等匹配，运行期传入的是裸 API 名（`"turnOn"`），永远不等于带元数据的边名，因此 targetState 几乎总是 `"null"`，数字孪生偏差检测在多数运行中实际不触发。

这解释了为何 TaskScheduler 冒烟运行中几乎看不到 "Digital Deviation"。修复需要统一命名口径（运行期用完整名，或模型加载时去掉元数据），属于跨模块行为变更。

### 4. TaskScheduler 静态 DelayQueue 与双构造器

- `delayedQueue` 是 `static` 字段却在实例构造器中赋值；两个构造器中第一个是残留（只初始化 CM 控制器），调用它会留下 4 个 null 控制器并在 dispatch 时 NPE。Main 只用第二个构造器，风险潜伏。
- `invokeMethod` 中 `CountDownLatch latch` 创建并 countDown 但从不 await（原代码注释掉了 await），线程等待逻辑实际未生效。
- `MessageQueue.getMessage()` 在被中断时返回 null，`dispatchMessage(null)` 会被静默吞掉（有空判断，安全但信息丢失）。

### 5. 并发安全

- `delayedQueue` 为 static 且从未清理，重试消息在全部分享；当前单进程演示可接受。
- Controller 的 device/twin 字段在 `START_FROM_ORIGIN` 修复时被整体替换，与正在执行的线程之间没有同步；演示场景下影响有限，但值得注意。

### 6. 其他小问题（低风险，多为记录）

- `Gateway` 的 `toString` 与 `toSystemString` 语义差异：系统视图将 `lightBrightness` 折叠为布尔值；`Main.getCurrentStateBasedOnMsg` 对 `WashingMachine` 用 `toDeviceString()`（返回 `WashingMachine{...}` 前缀），而 TaskScheduler 分发时用 `wmTwin.toString().replace("WMTwin","WashingMachine")` —— 两种来源的字符串格式需要在运行期恰好一致，脆弱但当前成立。
- `Yeelight.setBrightness(0)` 被接受（0..100 范围含 0），与 `toSystemString` 的 `brightness > 0` 判定不完全一致。
- `Gateway.addDevice` 的上界是 `deviceList < 3`，即最多 3 台；`removeDevice` 不校验成员身份，只看计数器。
- `Edge.fromString` 期望 JSON 对象语法，与 `toString()` 输出格式不兼容（已在测试中记录）。
- `MessageProxy` 构造器接收 5 个独立的 outEdgeApis Map，与 ExecutionChecker 的静态模型文件加载重复（模型文件被加载两份：Main 一次、ExecutionChecker 静态一次）。
- `Utils.executePythonScript` 中 `reader.close()` 在 `waitFor()` 之前调用，标准输出未读完时可能死锁（real-device 学习器路径）；另外不读取 stderr 流。
- `CMTwin.setTargetState` 只更新 7 个布尔字段，忽略 3 个体积字段，与 `updateAllFromString` 不对称。
- 大量 `main` 方法中的演示代码（原样保留），学习器迭代次数各不相同（CM 100+轮，其他 2 轮），收敛判据只有 CM 实现了 `checkSizeConsistency`。

## 结论

核心数据流（学习器 → 模型 JSON → preCheck/postCheck → 修复队列）在演示场景下可用；重构后行为与重构前一致（全部 62 个特征测试通过、各学习器输出与提交在库中的模型文件边集一致）。上面 1–3 号问题说明 post-execution 一致性检查的“物理偏差/数字偏差”检测在真实运行中大部分被短路，若要恢复论文描述的完整功能需要一次有意识的行为修复，建议作为后续 issue 单独处理。
