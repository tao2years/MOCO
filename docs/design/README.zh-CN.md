# 设计文档要求（Design Documentation Requirements）

[English](README.md) | [简体中文](README.zh-CN.md)

`docs/design/` 存放设计文档：描述**拟议的变更**、动机与验证方式。它是决策记录，
供后续变更作者与评审者追溯"当时为什么这样做"。

## 何时需要设计文档

按 [../workflow.md](../workflow.md) 第 1 节的变更分级矩阵判断。速查：

- **需要**：行为变更、跨模块重构、新虚拟设备/新学习器、检查逻辑语义调整、
  公共契约（`toString` 状态协议等）变化。
- **不需要**：局部 bug 修复（issue 写清根因即可）、纯文档、测试补充、
  不改变语义的小重构。

## 文件命名

- 命名：`YYYY-MM-DD-<短横线小写主题>.md`（日期 = 创建日期）。
- **中英成对**：同一目录 `<name>.md`（英文）与 `<name>.zh-CN.md`（中文），
  基名与日期前缀一致。
- 目录入口页：`README.md` / `README.zh-CN.md`。
- 文档标题下一行放双向语言互链：`[English](<name>.md) | [简体中文](<name>.zh-CN.md)`。

## 必含内容（模板见 [templates/design-template.zh-CN.md](templates/design-template.zh-CN.md)）

1. **问题与背景**：现状是什么（引用真实类/文件路径），为什么不满意。
2. **目标 / 非目标**：明确划界，防止范围蔓延。
3. **方案**：核心机制、涉及模块、关键接口/数据结构变化；**含被否决的备选方案**及否决理由。
4. **验收标准**：可执行的验证方式（测试名/命令/基线变化）。
5. **风险与兼容性**：对既有契约（见 [../architecture.md](../architecture.md) 第 4 节）的影响。
6. **状态**：`Draft` / `Approved` / `Implemented` / `Rejected`（拒绝的设计也保留，注明理由）。

## 双语对齐要求

- 两个版本覆盖**同一设计全貌**：章节顺序与层级一致；摘要或互链不能替代翻译。
- 技术标识符（类名、路径、命令、字段名、数字）在两版中保持一致，仅解释性文字翻译。
- 状态、日期、验收标准两版一致；翻译不得改变设计的状态。
- 同一 PR 内创建/更新两个版本；后续演进同步反映在两版。

## 模板

- [templates/design-template.zh-CN.md](templates/design-template.zh-CN.md)（中文）
- [templates/design-template.md](templates/design-template.md)（English）

## 后续变更要求

- 设计实现后：把 status 改为 `Implemented`（可附 PR 链接），**不删除文档**。
- 设计被推翻/替代：追加一节"替代方案"，标注新文档链接，status 置 `Replaced`。
