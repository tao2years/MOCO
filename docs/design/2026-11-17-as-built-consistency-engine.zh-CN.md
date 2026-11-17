# Design (as-built): 运行时一致性引擎（MessageProxy + ExecutionChecker + 修复回放）

[English](2026-11-17-as-built-consistency-engine.md) | [简体中文](2026-11-17-as-built-consistency-engine.zh-CN.md)

- **日期**：2026-11-17
- **组件**：`MOCO.MessageProxy`、`MOCO.ExecutionChecker`（preCheck/postCheck/修复路径计算/动作生成）、`IoTSystem.TaskScheduler.invokeMethod`（postCheck 挂钩点）、`IoTSystem.DelayedMessage`（修复回放载体）
- **状态**：Implemented（as-built 记录，逆向自 `main` 现有代码；**含已知缺陷的如实记录**）
- **关联 Issue**：#5

## 问题与背景

MOCA 的第二、三支柱：命令执行前的合法性判定（preCheck）与执行后的三方一致性校验
（数字孪生状态 / 物理设备状态 / 行为模型预测状态），偏差时自动修复。这是论文方法的
核心闭环，也是已知缺陷最集中的区域（[../../CODE_REVIEW.md](../../CODE_REVIEW.md)
问题 1–3）。本文按 as-built 沉淀其**真实**行为——包括被缺陷短路的路径——
作为后续修复设计的基线知识。

## 目标 / 非目标

- 目标：准确描述现有实现的消息流、判定语义、修复机制与缺陷位置。
- 非目标：提出缺陷修复方案（修复属行为变更，需另行设计文档）。

## 方案（as-built）

### 消息入口与代理开关（`MessageProxy`）

- `addMessage(message, currentState)`：`proxyOn=true` 时先 `preCheck`，
  通过才入 `TaskScheduler` 队列（返回 true）；否则仅记日志返回 false。
  `proxyOn=false` 时直通（MOCA 关闭模式）。
- 构造器接收 5 组 outEdgeApis 索引（Main 加载模型文件后构建）；
  **`ExecutionChecker` 自己又静态加载了一份模型**（双份加载，CR 记录，
  preCheck 用 Main 传入的索引、postCheck 用自己的静态图——两份理论上一致但不同源）。

### preCheck 语义（`ExecutionChecker.preCheck`）

```
currentState == "Invalid"            → false
deviceType 不在 5 设备 switch 内      → false
apis = outEdgeApis.get(currentState)
apis != null && apis.toString().contains(message.getDeviceAPI()) → true（否则 false）
```

- **contains 子串匹配**：`"turn"` 可命中 `"turnOn|true|false"`——有意的现状保留
  （原实现语义，特征测试 `preCheckUsesContainsSoPrefixApiNamesAlsoMatch` 钉死）。
- 状态键是**完整系统态字符串**（如 `Yeelight{'powerOn':false, ...}`），由
  `Main.getCurrentStateBasedOnMsg` 从对应 Twin 读取（注意 WM 用 `toDeviceString`，
  其余用 `toSystemDeviceString`/`toSystemString`——口径不统一但当前成立）。

### postCheck 三方校验（`postCheck` → `checkControllerDeviation`）

由 `TaskScheduler.invokeMethod` 在每次反射调用控制器方法后的 `finally` 中触发
（传**调用前**的 twin 状态作为 preState）。骨架：

```
currentState  = twin 读数
targetState   = getTargetStateBasedOnBehaviourModels(preState, api, behaviorGraph)
if currentState != targetState && targetState != "null" → Digital Deviation
    → repairTwin（把 twin 直接 setTargetState 成模型目标态）
currentPhysicalState = 物理设备读数
if currentPhysicalState != targetState && targetState != "null" → Physical Deviation
    if currentPhysicalState == preState  → 原消息延迟 100ms 重放（设备没动，重试）
    else → calculateSolutions(behaviorGraph, currentPhysical, target, message)
           → generateXxxActions（按设备翻译成具体修复动作序列）
```

- 5 个设备的 readTwin/readPhysical/repairTwin 经 `DeviationTarget<C>` 接口注入
  （重构统一后的骨架，行为与原逐设备实现一致）。
- **targetState 匹配缺陷（CR-3）**：`getTargetStateBasedOnBehaviourModels` 用
  `edge.getName().equals(cmd)`，而调用方传的是裸 API 名（`"turnOn"`），边名实际是
  `"turnOn|true|false"` ⇒ **targetState 几乎恒为 `"null"`** ⇒ 双偏差检测在多数运行中
  被短路。特征测试 `getTargetStateWithBareApiNameMissesBecauseOfNameMetadata` 钉死。
- **CM 物理态硬编码（CR-1）**：CM 分支的 readPhysical 读到真实状态后被
  `currentPhysicalState = "CoffeeMachine{'waterReady':'true', ...}"` 覆盖（调试残留，
  重构时为保持行为原样保留）。

### 修复路径计算（`calculateSolutions` + `Utils.findShortestPaths`）

- `findNodeContaining`：在行为图中找 source 包含状态片段的边，取其 source 节点
  （"S0" 片段找原点）。找不到返回 `""`。
- `findShortestPaths`：BFS 找 source→target 的路径（收集所有最短路径，截取 ≤3 条）；
  直达不可达时退化为 origin→source 的路径并在头部加 `"START_FROM_ORIGIN"` 标记。
- 路径选择优先级（单趟遍历实现，语义与原三段一致）：
  末条 API == 请求 API 且非 FROM_ORIGIN ＞ 末条 API == 请求 API ＞ 非 FROM_ORIGIN ＞
  第一条路径。
- **错误模型缺陷（CR-2）**：非 CM 设备分支调用 `calculateSolutions` 时历史上传的是
  CM 模型；重构统一骨架后各分支传各自模型（`gateway_behaviorGraph` 等）——
  行为对 4/5 设备而言从"错误模型"变为"正确模型"，这是重构中有意的行为修正，
  已由特征测试覆盖并在此如实记录演进差异。

### 修复动作生成与回放

- `generateXxxActions` 把抽象 API 序列翻译成具体消息：带参 API 固定演示参数
  （setBrightness→50、setRGB→120/130/111 等）；`START_FROM_ORIGIN` 不发消息而是
  **整体重置**设备与 twin（new 一套初始对象）。
- 修复消息包成 `DelayedMessage`（100ms）进 `TaskScheduler.delayedQueue`；
  专用消费线程把它搬回 messageQueue 重走完整 dispatch 流程（含 postCheck）。

### 并发模型（现状）

- `TaskScheduler`：固定线程池（演示用 5）+ 2 个常驻任务
  （messageQueue 消费、delayedQueue 搬运）。`invokeMethod` 每条消息起新线程反射执行，
  `CountDownLatch` 创建并 countDown 但 **await 被注释**（同步逻辑未生效，CR-4）。
- `delayedQueue` 是 static 字段在实例构造器中赋值；第一构造器（只含 CM）是残留，
  调用会留下 4 个 null 控制器（Main 只用第二构造器）。
- 修复中 `START_FROM_ORIGIN` 整体替换 controller 的 device/twin 引用，与正在执行的
  线程无同步（演示场景可接受，CR-5 记录）。

## 验收标准（as-built 校验）

- `ExecutionCheckerTest`（7 测试）：preCheck 全分支语义、targetState 匹配的两面
  （全名匹配成功 / 裸名恒 miss）、`"null"` 回退。
- `IoTSystemTest`：DelayedMessage 时序、Message/Queue 行为。
- `mvn verify` 全绿（122 测试）——所有"被钉死的现状行为"均有特征测试背书。

## 风险与兼容性

- 本区域是 CODE_REVIEW.md 问题 1–5 的所在地：任何"顺手修复"都会改变被特征测试
  钉死的行为 ⇒ 必须走 issue → design → 显式更新特征测试的流程。
- 修复回放可能自激（偏差修复又触发偏差）：当前靠 100ms 延迟与消息去重语义缓解，
  无显式上限——演示场景成立，扩展场景需设计。

## 开放问题

- API 命名口径统一方案（裸名 vs 带元数据全名 vs 加载期剥离元数据）——修复 CR-3 的
  核心设计决策，待专门设计文档。
- `MessageProxy` 的 5 组 Map 参数与 `ExecutionChecker` 静态双份模型加载的收敛方案。
