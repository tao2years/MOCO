# 分层开发流程（Layered Development Workflow）

本仓库采用 **spec-first**（先规格后编码）的分层流程：每个变更从 Issue 开始，
逐层产出规格/设计/计划/代码/验证/评审产物，每层有明确准入门槛（gate）。
参考 qwen-code 等开源仓库的实践，按本仓库体量（Java 单模块、论文配套工具）裁剪，
避免流程开销超过收益。

**核心原则：编码是流程的最后一步，不是第一步。**

```
Layer 0  Issue          问题/需求/决策记录（GitHub Issue，含验收标准）
   │
Layer 1  Spec/Design    非平凡变更写设计文档 docs/design/（中英成对）
   │
Layer 2  Plan           多步/多 PR 变化写实施计划 docs/plans/
   │
Layer 3  Coding         按 coding-standards.md 实现（含测试）
   │
Layer 4  Verify         mvn verify 全绿 + golden 基线同步 + 自审 diff
   │
Layer 5  Review         PR（模板）→ CI → 人工评审 → merge
   │
Layer 6  Record         行为变化更新 README/docs；遗留发现追加 CODE_REVIEW.md
```

## 1. 变更分级矩阵——我该写哪些层？

先判断变更类型，**只做该做的层，不要为小事写大文档**：

| 变更类型 | 设计文档(design) | 计划(plans) | 必须的验证 | 评审重点 |
| ---- | ---- | ---- | ---- | ---- |
| 微小修复（≤~50 行、单类、无行为变化） | 否 | 否 | `mvn verify` + 针对性测试 | 正确性 |
| 普通修复 / 局部增强 | 否（issue 里写清根因与方案即可） | 否 | `mvn verify` + 新增回归测试 | 根因是否真被修复 |
| **行为变更**（用户可见/检查逻辑/接口语义） | **是** | 视规模 | `mvn verify` + 特征测试 + 文档同步 | 设计决策、验收标准 |
| **跨模块重构**（动多个包或公共契约） | **是** | **是**（分 PR 序列） | 全量 verify + 特征测试基线 | 分步安全性 |
| 新虚拟设备 / 新学习器 | **是**（含状态空间预估） | 否 | verify + golden 基线新增条目 | 模型正确性 |
| golden 模型有意变更 | 简述于 commit message + PR | 否 | 同 commit 重新生成 + 基线更新 | 变更理由 |
| 文档 / 流程 | 否 | 否 | 链接检查、事实核对 | 准确性 |

> 判断不准时，倾向**多写一层**。判断标准不是行数而是：改完之后，别人理解"为什么这样做"
> 需要的上下文是否超过了 PR diff 本身。

## 2. Layer 0 —— Issue：一切从问题开始

- 每个 PR 应对应一个已存在的 issue（bug 或经维护者认可的功能）。
  没有就先开 issue 讨论方向，再动手。
- issue 应包含：**现象/需求 → 复现或场景 → 预期行为 → 验收标准**。
- 修复类 issue 的验收标准至少包含一条可执行的验证方式（测试名或复现命令）。

## 3. Layer 1 —— Spec/Design：把"怎么做"想清楚

适用：行为变更、跨模块改动、新设备/新学习器、以及所有"改完说不清为什么"的变更。
要求详见 [design/README.zh-CN.md](design/README.zh-CN.md)，要点：

- 文件名：`docs/design/YYYY-MM-DD-<slug>.md`（+ `.zh-CN.md` 中文版，**同一 PR 内成对提交**，
  内容对齐——本仓库作者中文母语，英文版供协作 Agent/国际化读者）。
- 必含章节：问题与背景、现状（引用真实代码路径）、目标/非目标、方案（含被否决的备选）、
  验收标准、风险。
- 模板：[design/templates/design-template.md](design/templates/design-template.md)（附中文版）。
- 设计文档是**决策记录**：写清"为什么选 A 不选 B"，比写"如何实现 A"更重要。

## 4. Layer 2 —— Plan：把大变更拆成可合并的小步

适用：一个 design 落地需要多个 PR、或需要按阶段推进的序列。
要求详见 [plans/README.md](plans/README.md)。要点：

- 每个 PR 步骤必须**独立可编译、可验证、可合并**，不得出现"中间态主干不可用"。
- 计划文件名：`docs/plans/YYYY-MM-DD-<slug>.md`。
- 模板：[plans/plan-template.md](plans/plan-template.md)。

## 5. Layer 3 —— Coding：实现规范

完整规范见 [coding-standards.md](coding-standards.md) 与 [testing-standards.md](testing-standards.md)。
此处只列流程性约束：

1. **从 issue/design 的验收标准推导测试**：先确认"哪些测试能证明做完了"，再写实现。
2. **行为变更必须带回归测试**：修 bug 必须先有失败中的测试（能复现），再修到绿。
3. **golden 模型同步规则**：改虚拟设备语义或 Learner 导致基线漂移时，
   同一 commit 内 ①重跑对应 `*_Model.main` 重新生成 ②更新 `GoldenModelFilesTest.EXPECTED_COUNTS`
   ③（Yeelight）更新 `YeelightModelRegressionTest` 钉住的数字 ④commit message 写明原因。
4. 不确定的小决策：选择合理默认值并在 PR 描述中说明，不阻塞等待人工裁决；
   真正的**产品/行为决策**回到 issue/design 层提出。

## 6. Layer 4 —— Verify：声明完成前必须做的事

按顺序，全部通过才允许提 PR：

1. `mvn verify`（根目录，与 CI 完全一致）。
2. 涉及行为的改动：新增/修改的测试确实运行且确实断言了新行为（绿≠对，
   一个断言错方向的测试照样绿）。
3. **自审（self-audit）**：通读自己将提交的完整 diff（含新增文件），
   不带目标地读——通常两遍：一遍看"有没有不该出现的东西"（调试残留、无关格式化、
   被注释掉的代码），一遍看"每个变更是否有测试/文档对应"。发现一处即修复并重新执行本层。
4. 文档改动：核对内部链接可达、命令可复制执行、与代码事实一致。

## 7. Layer 5 —— Review 与合并

- PR 使用 [.github/pull_request_template.md](../.github/pull_request_template.md)，
  逐项填写（见 [review-standards.md](review-standards.md)）。
- **所有 PR 必须过 CI（`mvn verify`）且经人工评审**，`main` 受分支保护。
- 评审者发现的问题：小问题直接修；设计层面的问题回到 Layer 1 补 design 或在 issue 讨论。
- 合并由人执行，不由 Agent/CI 自动合并。

## 8. Layer 6 —— Record：把变化留在文档里

- 用户可见行为变化 → 更新根 [README.md](../README.md) 对应段落。
- 架构/契约变化 → 更新 [architecture.md](architecture.md)（本文档体系的事实基线）。
- 评审中发现的**未修复**遗留问题 → 追加到 [CODE_REVIEW.md](../CODE_REVIEW.md)（按严重程度排序），
  并开 issue 跟踪。
- 已实现的设计文档 → 将 status 更新为 `Implemented`（不必删除，设计记录是资产）。

## 9. Agent / 人共用说明

本流程对人类贡献者和编码 Agent 一致适用。Agent 额外约束见根目录 [AGENTS.md](../AGENTS.md)：
先读 [architecture.md](architecture.md) 建立上下文，再按本流程选层执行；
Agent 不得自行合并 PR、不得绕过 CI 红灯提交、对"行为变更"必须先产出 design 草稿而非直接改代码。
