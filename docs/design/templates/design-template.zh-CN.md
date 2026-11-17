# Design: <主题标题>

[English](design-template.md) | [简体中文](design-template.zh-CN.md)

- **日期**：YYYY-MM-DD
- **组件**：涉及的模块（如 `MOCO.ExecutionChecker` / `VirtualDevice.Yeelight`）
- **状态**：Draft | Approved | Implemented | Rejected
- **关联 Issue**：#N

## 问题与背景

现状是什么（引用真实类/文件路径与方法），为什么不满意。
如果是为了修复已知缺陷，链接 [../../CODE_REVIEW.md](../../../CODE_REVIEW.md) 对应条目。

## 目标 / 非目标

- 目标：本次要达成的、可验证的结果。
- 非目标：明确不做的部分（防止范围蔓延）。

## 方案

核心机制与数据流（可画 ASCII 图）；涉及的模块/接口/数据结构变化；
对既有契约（[../architecture.md](../../architecture.md) 第 4 节）的影响。

### 备选方案与否决理由

| 方案 | 优点 | 否决理由 |
| ---- | ---- | ---- |
| A（本方案） | … | — |
| B | … | … |

## 验收标准

可执行的验证方式，逐条列出（测试名 / 命令 / golden 基线变化）：

1. `mvn verify` 全绿。
2. <新增测试类/方法> 断言 <新行为>。
3. golden 基线从 <旧数字> 变为 <新数字>，原因为 <…>。

## 风险与兼容性

- 对字符串状态协议的影响：…
- 对 golden 模型基线的影响：…
- 需要同步更新的文档：…

## 开放问题

（实现前需要拍板的问题，逐条列出；拍板后补结论与日期。）
