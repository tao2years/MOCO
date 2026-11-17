# MOCO / MOCA 文档中心

本目录是仓库的**开发上下文**与**规范入口**：新贡献者（人类或编码 Agent）在这里理解项目、
找到开发规范，并按分层流程开展 work。参考了 [qwen-code](https://github.com/QwenLM/qwen-code)
等开源仓库的工程文档实践，并结合本仓库（Java/Maven、论文配套工具）的实际情况裁剪。

## 文档地图

| 文档 | 内容 | 读者 |
| ---- | ---- | ---- |
| [architecture.md](architecture.md) | 代码架构、模块地图、数据流与关键契约（开发上下文） | 所有贡献者，动手前必读 |
| [workflow.md](workflow.md) | 分层开发流程：Issue → 设计 → 计划 → 编码 → 验证 → 评审 | 所有贡献者，动手前必读 |
| [coding-standards.md](coding-standards.md) | Java 编码规范（语言级别、包结构、命名、日志、JSON、注释） | 写代码的人/Agent |
| [testing-standards.md](testing-standards.md) | 测试规范（单元、特征测试、golden 模型、CI） | 写代码的人/Agent |
| [documentation-standards.md](documentation-standards.md) | 文档规范（目录结构、语言政策、design 文档要求） | 写文档的人/Agent |
| [review-standards.md](review-standards.md) | 代码评审规范（PR 准入、评审重点、严重级别） | 提 PR 的人与评审者 |
| [design/](design/) | 设计文档（spec）目录：要求见 [design/README.zh-CN.md](design/README.zh-CN.md) | 非平凡变更的作者 |
| [plans/](plans/) | 实施计划目录：用途见 [plans/README.md](plans/README.md) | 多步/多 PR 变更的作者 |
| [../CODE_REVIEW.md](../CODE_REVIEW.md) | 代码审查发现记录（活文档，历史遗留问题清单） | 评审者、维护者 |
| [../AGENTS.md](../AGENTS.md) | 面向编码 Agent 的仓库工作约定（英文） | 编码 Agent |
| [../CONTRIBUTING.md](../CONTRIBUTING.md) | 贡献者上手指南（环境、流程、PR 规范） | 新贡献者 |

## 分层文档体系一览

```
仓库根
├── README.md            项目介绍、运行方式（面向使用者）
├── AGENTS.md            Agent 工作约定（英文，面向工具）
├── CONTRIBUTING.md      贡献上手指南
├── CODE_REVIEW.md       审查发现记录（活文档）
└── docs/                ← 本目录
    ├── README.md        文档中心（本文件）
    ├── architecture.md  开发上下文：模块地图与数据流
    ├── workflow.md      分层开发流程（spec-first）
    ├── coding-standards.md / testing-standards.md
    ├── documentation-standards.md / review-standards.md
    ├── design/          设计文档（中英成对，含模板）
    │   └── templates/
    └── plans/           实施计划
```

## 新人上手路径

1. 读根目录 [README.md](../README.md)，理解 MOCA 做什么（学习设备行为模型 → 运行时一致性监控）。
2. 读 [architecture.md](architecture.md)，建立模块地图与数据流心智模型。
3. 读 [workflow.md](workflow.md)，明确"先 spec、后 Coding"的分层流程与各层准入门槛。
4. 动手前按 [workflow.md](workflow.md) 的变更类型矩阵确认需要哪几层；写代码时对照
   [coding-standards.md](coding-standards.md) 与 [testing-standards.md](testing-standards.md)。
5. 提 PR 前对照 [review-standards.md](review-standards.md) 与
   [.github/pull_request_template.md](../.github/pull_request_template.md) 自查。

## 本地验证命令速查

```bash
mvn verify                 # 与 CI 完全等价的构建 + 全部测试（提交前必须绿）
mvn test -Dtest=GraphTest  # 运行单个测试类（从项目根目录运行）
```

详见 [testing-standards.md](testing-standards.md)。
