# 文档规范（Documentation Standards）

管理范围：`docs/**`、根目录 `README.md`、`AGENTS.md`、`CONTRIBUTING.md`、
`CODE_REVIEW.md`、`.github/pull_request_template.md`。

## 1. 目录职责（不得混放）

| 位置 | 职责 | 语气/读者 |
| ---- | ---- | ---- |
| `README.md`（根） | 项目是什么、怎么跑 | 使用者；中文 |
| `docs/README.md` | 文档地图与上手路径 | 贡献者；中文 |
| `docs/architecture.md` | 开发上下文：模块、数据流、契约（**事实基线**） | 贡献者；中文 |
| `docs/workflow.md` | 分层流程与准入门槛 | 贡献者；中文 |
| `docs/*-standards.md` | 各领域规范 | 贡献者；中文 |
| `docs/design/` | 设计文档（决策记录） | 变更作者/评审者；**中英成对** |
| `docs/plans/` | 实施计划（PR 序列） | 变更作者；中文（如需国际化可成对） |
| `CODE_REVIEW.md`（根） | 遗留问题活文档 | 维护者；中英混合（现状） |
| `AGENTS.md`（根） | 编码 Agent 工作约定 | Agent 工具；**英文**（工具兼容性优先） |
| `CONTRIBUTING.md`（根） | 贡献上手 | 新贡献者；中文 |

## 2. 语言政策

- 基础设施文档（workflow/standards/architecture/CODE_REVIEW）：**中文为主**，
  保留技术标识符（类名、路径、命令）为原文。
- `AGENTS.md` 用英文（编码 Agent 的指令遵循对英文最稳）。
- **设计文档中英成对**：`docs/design/<name>.md`（英）+ `<name>.zh-CN.md`（中），
  同一 PR 提交、内容对齐。这是本仓库唯一强制双语的位置，详见
  [design/README.zh-CN.md](design/README.zh-CN.md)。
- 文档顶部语言互链格式（示例，写作时用代码片段呈现、替换为真实文件名）：
  `[English](README.md) | [简体中文](README.zh-CN.md)`。

## 3. 写作要求

1. **事实基线可验证**：文档中引用的命令必须可复制执行；引用的类/文件/行为必须与
   `main` 一致。写不确定的事实前先跑一遍验证（例：写 `mvn verify` 耗时前先跑一次）。
2. **链接用相对路径**：站内引用一律 `./coding-standards.md`、`../README.md` 形式，
   不写绝对 URL（仓库可迁移）。
3. **表格优先于长段落**（映射、矩阵、契约类信息）；流程用 ASCII 图。
4. 每份规范开头一句"适用范围"；正文可执行的规则用祈使句（"必须/禁止/默认"），
   避免模棱两可的"建议考虑"。
5. **规范变更本身走流程**：修改本文档体系中的准入门槛/规则 = 流程变更，
   PR 描述需说明动机与影响面。

## 4. 维护与时效

- **行为变化 ⇒ 文档同步**（[workflow.md](workflow.md) Layer 6）：
  README（用户可见）、architecture.md（契约/模块）、对应 standards（约定变化）。
- 文档不再成立的内容**删除或修正**，不保留"历史包袱"段落；git 历史即存档。
- `CODE_REVIEW.md` 是活文档：修复一个记录项时整条移除或标注 `已修复（PR #N）`，
  新发现按严重程度插入。
- 每次发布/里程碑时对 `docs/` 做一次链接核对（可用脚本或手工点击主要入口）。

## 5. Markdown 格式约定

- 标题层级：一级标题每文件一个；正文从二级开始。
- 代码块标注语言（```java / ```bash / ```text）。
- 行宽不强制硬换行；段落自然书写（与 qwen-code 的 PR 行宽建议一致，避免窄列渲染）。
- 列表符号统一 `-`；强调用 `**粗体**` 而不是斜体（中文斜体渲染差）。
- 文件名：`kebab-case.md`；design/plans 带日期前缀（见各自 README）。
