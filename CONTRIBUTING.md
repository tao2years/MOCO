# 贡献指南（Contributing Guide）

欢迎为 MOCO/MOCA 贡献代码或文档。本指南帮你快速上手；完整开发规范在
[docs/](docs/README.md)。

## 环境准备

- **JDK 17（Temurin）** + **Maven 3.9+**（CI 验证过的组合；源码语法级别 Java 8，
  不要用 JDK 9+ 的语法写代码）。
- 克隆仓库后，在项目根目录验证环境：

```bash
mvn verify   # 约 10s：编译 + 122 个测试全绿即环境就绪
```

## 开发流程（摘要）

本仓库采用分层流程（Issue → 设计 → 计划 → 编码 → 验证 → 评审），
**先想清楚再动手**。完整流程与变更分级矩阵见 [docs/workflow.md](docs/workflow.md)。

速查：

1. **找/开 issue**：每个 PR 对应一个 issue；没有就先开（写清现象、场景、预期、验收标准）。
2. **判断该写哪几层**：行为变更/跨模块改动 → 先写设计文档
   `docs/design/YYYY-MM-DD-<slug>.md`（中英成对，模板在 `docs/design/templates/`）；
   多 PR 落地 → 再写 `docs/plans/` 计划。
3. **编码**：遵守 [docs/coding-standards.md](docs/coding-standards.md)。
4. **验证**：`mvn verify` 全绿（本地与 CI 同一条命令）；bug 修复须先有失败中的复现测试。
5. **自审**：通读自己将提交的完整 diff（含新文件），确认无调试残留与无关噪声。
6. **提 PR**：使用模板 [.github/pull_request_template.md](.github/pull_request_template.md)，
   链接 issue、写明验证证据；等 CI 绿 + 人工评审；**由人合并**（`main` 受分支保护）。

## 特别注意（本仓库特有）

- **golden 模型文件**（`src/main/java/MOCO/modelFiles/*.json`）是运行时输入：
  改虚拟设备或学习器导致基线漂移时，必须在**同一 commit** 里重新生成模型、更新
  `GoldenModelFilesTest.EXPECTED_COUNTS` 并在 commit message 写明原因。
  详见 [README.md](README.md#when-a-golden-model-changes-on-purpose) 与
  [docs/testing-standards.md](docs/testing-standards.md) 第 4 节。
- **测试必须从项目根目录运行**（模型加载路径相对根目录）。
- **已知遗留缺陷**（物理偏差检测硬编码、修复路径用错模型等）记录在
  [CODE_REVIEW.md](CODE_REVIEW.md)：修复它们属于行为变更，需 issue + 设计文档，
  不要在无关 PR 里顺手改。
- **包依赖方向**：`graph/` 保持零业务依赖；`MOCO` 不新增对 `IoTSystem` 的耦合。

## 文档贡献

- 修正/补充 `docs/` 下的规范文档同样是贡献：核对链接可达、命令可执行、与代码事实一致。
- 设计文档必须中英成对提交（要求：[docs/design/README.zh-CN.md](docs/design/README.zh-CN.md)）。

## 分支与提交

- 分支命名：`feat/<slug>`、`fix/<slug>`、`docs/<slug>`（或沿用你现有的 agent 前缀约定）。
- 提交信息用 Conventional Commits：`feat(learner): ...`、`fix(execution-checker): ...`、
  `docs: ...`；一个 commit 一件事。
- 不要直接推 `main`；所有变更走 PR。
