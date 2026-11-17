# Design (as-built): IoTSystem 运行时（TaskScheduler / DeviceController / DeviceTwin / VirtualDevice）

[English](2026-11-17-as-built-iot-runtime.md) | [简体中文](2026-11-17-as-built-iot-runtime.zh-CN.md)

- **日期**：2026-11-17
- **组件**：`IoTSystem`（Main / TaskScheduler / MessageQueue / MessageSender / Message / DelayedMessage）、`IoTSystem.DeviceController.*`（5 控制器 + `AbstractDeviceController`）、`IoTSystem.DeviceTwin.*`（5 孪生）、`VirtualDevice.*`（10 虚拟设备）
- **状态**：Implemented（as-built 记录，逆向自 `main` 现有代码）
- **关联 Issue**：#5

## 问题与背景

IoTSystem 是被 MOCA 代理的"应用侧 + 设备侧"运行时：应用的命令经
MessageProxy 进入调度，控制器把命令映射到物理（虚拟）设备并同步数字孪生。
它的**字符串状态协议**（`toString` 家族）是整个系统最重要的隐式契约——
学习器的节点、preCheck 的键、postCheck 的比对对象全部依赖它。本文沉淀
as-built 设计。

## 目标 / 非目标

- 目标：记录消息生命周期、控制器映射规则、孪生同步语义、虚拟设备语义、
  以及字符串协议的准确口径。
- 非目标：重新设计调度并发模型（缺陷修复走独立设计）。

## 方案（as-built）

### 消息生命周期

```
Main.main
  → initialize()：5 套 (Device, Twin, Controller) + TaskScheduler(5 线程)
  → loadBehaviorModelsAndOutApis()：Gson 加载 5 个模型 + buildOutEdgeApis 索引
  → MessageProxy(taskScheduler, 5 组索引, true)
  → MessageSender.sendMessage*()：脚本化发 N 条消息（每条 100ms 间隔）
      → Main.getCurrentStateBasedOnMsg(msg)：从对应 Twin 读系统态字符串
      → MessageProxy.addMessage → preCheck → TaskScheduler.addMessage
  → MessageQueue(LinkedBlockingQueue) FIFO
  → dispatchMessage：按 deviceType 反射调用 controller 方法
      （参数 String→Integer 解析，int 原型匹配）
      → invokeMethod：新线程 method.invoke(controller, args)
      → finally: ExecutionChecker.postCheck(...)  [挂钩点]
      → controller.printInternalState()：设备/孪生平行对比日志
  → shutdown()：5s 优雅关闭 + oshi 性能采样输出（CPU/磁盘差值）
```

### 控制器层（`AbstractDeviceController<D, T>`）

- 基类持有 `deviceId / device / twin` + 设备名与孪生名（用于日志前缀替换对比）。
- **映射规则（全部 5 个控制器一致）**：每个 API 方法先调物理设备同名方法，
  设备返回 `"success"` 才把变化镜像进孪生（按 API 涉及的字段逐个 set）；
  `"skip"` 表示设备拒绝（状态不满足前置），孪生不动——**这正是偏差的来源**：
  物理设备与孪生由同源事件更新，正常路径天然一致，不一致只在设备/孪生被
  外部单独改变时出现。
- `CMController.brewCoffee` 例外：无条件调设备并把 `updateAllFromString(设备toString)`
  全量灌入孪生（含体积字段）。
- 每控制器带 `main` 演示与设备名前缀 getter/setter（修复重置用）。

### 孪生层（`*Twin`）

- 5 个孪生均为设备字段的纯 Java 镜像 + fastjson 解析器。
- **三套字符串口径**（最容易踩坑的契约）：

| 方法 | 语义 | 例 |
| ---- | ---- | ---- |
| `toString()` | 完整状态（含体积等数值字段） | `WMTwin{...}` / `CMTwin{...}` |
| `toSystemDeviceString()` / `toSystemString()` | 抽象系统态（布尔化，对齐学习器节点） | `CoffeeMachine{...}`（设备名前缀）/ `Yeelight{...}` |
| `toDeviceString()`（仅 WM） | 设备视角抽象态 | `WashingMachine{...}` |

- `setTargetState`（修复用）：CM 版只更新 7 个布尔**忽略 3 个体积字段**，与
  `updateAllFromString` 不对称（CR-6 记录）；WM 版解析 `WashingMachine{...}` 串。
- `fromString` / `equals` / `hashCode`：CMTwin 完整实现（特征测试用）。

### 虚拟设备层（`VirtualDevice/*`）

- 10 个设备 = 确定性内存状态机：API 前置校验（返回 success/skip）+ 状态变更 +
  `toString`/`fromString`（fastjson）+ `toSystemString`（抽象布尔化）。
- 语义要点（部分）：
  - **Yeelight**：setBrightness/setRGB/setColorMode 要求 `powerOn`，亮度 0..100
    （**接受 0**，而 `toSystemString` 的 `brightness > 0` 判定不含 0——口径差记录）；
    RGB 范围 0..255；`toSystemString` 把 rgb 折叠为"总和 ≥ 0"布尔。
  - **CoffeeMachine**：addWater/addCoffeeBean/addMilk 注入 `Random` 体积
    （⇒ 学习器需 100 轮收敛）；brewCoffee 按 1/2/3 型号固定消耗（15g 豆 +
    240/40/40ml 水 + 0/200/100ml 奶）；派生标志（waterReady ≥250 等）分两步更新
    （⇒ 学习器定点追踪）；内部 `wait(seconds)` 会真实睡眠但当前无调用。
  - **Gateway**：复合语义（灯 + 闹钟 + 子设备列表），`addDevice` 上限 3（
    `deviceList < 3`），`removeDevice` 不校验成员身份只看计数；`toSystemString`
    把 lightBrightness 折叠为布尔。
  - **VideoCamera**：10 个布尔开关（2^10=1024 状态，模型基线 1024 节点/10240 边）。
- 真实设备等价物在 `RealDevice/`：Python（python-miio）脚本按 IP+token 直连设备；
  `deviceTwins4Real/*Status` 解析 status JSON。

### 并发现状

（与一致性引擎文档 [2026-11-17-as-built-consistency-engine.zh-CN.md](2026-11-17-as-built-consistency-engine.zh-CN.md) 并发一节互补）
线程池消费 + delayedQueue 搬运双常驻任务；每条消息独立线程执行；
`invokeMethod` 的 latch 未 await（同步未生效）；`MessageQueue.getMessage`
中断时返回 null（dispatch 空判断吞掉）；`TaskScheduler` 第一构造器残留
（null 控制器 NPE 风险）。

## 验收标准（as-built 校验）

- `IoTSystemTest`（10 测试）：Message 格式、Queue FIFO 与历史副本、
  DelayedMessage 时序、CMTwin/WMTwin 状态串与 setTargetState 语义。
- `VirtualDeviceTest` / `YeelightTest` / `CoffeeMachineTest`：设备语义特征测试
  （含 Yeelight 边界、CM 消耗量断言）。
- `mvn verify` 全绿（122 测试）。

## 风险与兼容性

- **字符串协议是行为契约**：任何字段增删、顺序调整、布尔化口径变化都会
  同时影响学习器节点内容、preCheck 键、postCheck 比对——三层联动，
  必须按行为变更流程走设计层并同步 golden。
- 主入口 `Main` 是演示脚本（oshi 采样、固定消息序列、消息循环 100 次），
  不是可配置的测试装置；性能数字仅供演示参考。

## 开放问题

- 调度并发模型收敛（latch 语义恢复 / 双构造器清理 / interrupted 语义）
  与孪生口径统一（`toSystemDeviceString` vs `toDeviceString`）：均属行为变更，
  待各自设计文档（部分已在 CODE_REVIEW.md 建议为独立 issue）。
