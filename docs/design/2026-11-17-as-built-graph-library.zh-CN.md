# Design (as-built): graph library

[English](2026-11-17-as-built-graph-library.md) | [简体中文](2026-11-17-as-built-graph-library.zh-CN.md)

- **Date:** 2026-11-17
- **Component:** `graph/` — `Graph`, `Edge`, `Utils`, `IllegalOperationException`, `graph/algorithms/`（Tarjan、Topsort、IsAcyclic、FindCycles、Components）
- **Status:** Implemented（as-built 记录，逆向自 `main` 现有代码）
- **Issue:** #5

## 问题与背景

MOCA 的行为模型是一张有向图：节点是抽象系统状态（字符串），边是 API 转移。
仓库需要一张独立的通用图库来承载它（以及未来的其他图运算），且不得掺杂业务语义。
`graph/` 包即为此存在；本文是它的 as-built 设计记录——它**不是**为未来变更写的提案，
而是把已实现的设计决策沉淀成知识，供后续贡献者（人类/Agent）理解与遵循。

## 目标 / 非目标

- 目标：提供节点/边的增删查、入出边索引、复合节点（compound）、五种图算法，
  并保持**零业务依赖**（不 import `MOCO`/`IoTSystem`/`VirtualDevice`/`RealDevice` 任何类）。
- 非目标：性能优化（当前为 HashMap 索引、Stream 拼装，够用）；
  权重图、超图等扩展；并发安全。

## 方案（as-built）

### 数据结构

`Graph<N, E>` 维护 7 个平面索引（全部 `String` nodeId → …）：

```
nodes:     nodeId → N（节点数据）
in:        nodeId → edgeId → Edge（入边对象表）
out:       nodeId → edgeId → Edge（出边对象表）
pred:      nodeId → 前驱 nodeId → 边数（多重边计数）
sucs:      nodeId → 后继 nodeId → 边数
edgeObjs:  edgeId → Edge（全边对象表）
edgeLabels: edgeId → E（边数据标签）
```

- **edgeId 编码**：`source + "\x01" + target + "\x01" + (name != null ? name : "\x00")`；
  无向图（`directed=false`）时 source/target 先按 `compareTo` 排序，保证无向边规范化。
- 三个布尔开关（构造器参数，运行期 setter 可改但一般不改）：
  `directed`（默认 true）、`multigraph`（默认 false）、`compound`（默认 false）。
- **关键约束**：非 multigraph 图中带 name 的边会抛 `IllegalOperationException`
  （`setEdge` 内校验）。MOCA 行为模型图统一用 `new Graph<>(true, true, false)`
  ——有向 + 多重图，因为两个状态间可有多条不同 API 边。
- compound 模式提供 `setParent/getParent/getChildren`，父指针初始为虚拟根 `\x00`；
  `setParent` 做祖先环检查（沿 parent 链上溯，发现环抛异常）。

### 节点/边生命周期

- `setNode` 幂等：已存在时按 `replaceValue` 决定是否覆盖数据，**不重复计数**；
  新增时同时初始化四个空索引桶并 `++nodeCount`。
- `setEdge`：先算 edgeId，已存在则只替换标签（幂等）；否则隐式建两个端点节点
  （`setNode(id, null, false)`）、登记 in/out/pred/sucs 并 `++edgeCount`。
- `removeNode`：级联删除所有入/出边（快照后遍历，避免并发修改）；
  compound 模式还把孤儿子节点挂回虚拟根。
- `removeEdge`：双向清理索引，`pred/sucs` 用"减到 0 删键"的计数维护。
- **契约（重要）**：nodeCount/edgeCount 是唯一真相，所有修改必须走上述方法，
  不得直接改集合（字段是 private，但同包反射/测试也要守约）。

### 算法（`graph/algorithms/`）

| 类 | 入口 | 复杂度 | 说明 |
| ---- | ---- | ---- | ---- |
| `Tarjan` | `graph.tarjan()` | O(V+E) | 强连通分量；实例字段在每次入口重置（可复用实例） |
| `Topsort` | `graph.topsort()` | O(V+E) | 拓扑排序；有环时**抛出**（非返回 null） |
| `IsAcyclic` | `graph.isAcyclic()` | — | 无环判定 |
| `FindCycles` | `graph.findCycles()` | — | 返回所有处于环上的节点分组 |
| `Components` | `graph.components()` | O(V) | 连通分量，返回子图列表（每分量一个新 `Graph`） |

算法类互相独立、无状态（或入口时重置），全部 `graph.getNodes()` 驱动。

### 已知实现细节（决策记录）

1. **`Edge.equals` 用 `toString` 比较**（`graph.Edge`）：字面比较拼接串而非字段逐一比较。
   简单但脆弱——两字段拼接顺序不同即不等。保持现状；改 equals 属行为变更需设计文档。
2. **`getEdgesBasedOnNodes` 用 `key.contains(edgeId)`** 子串匹配扫描全边表（O(E)）：
   调用方少、图规模小（模型图 ≤ 数千边），可接受；不做前缀索引。
3. **Stream 大量用于过滤/收集**（getSources/getSinks/inEdges 过滤等）：
   与仓库其余部分的显式循环风格不同，但此包自成一体，保留原实现。
4. `nodeEdges` 返回 in+out 拼接 List；`neighbors` 直接改动 predecessors 返回的 Set
   （返回的是防御性拷贝，安全但隐晦）。

## 验收标准（as-built 校验）

现有验证即验收（已全部满足，`mvn verify` 122 绿）：

- `GraphTest`（31 测试）：节点/边生命周期、幂等、级联删除、compound、edgeId 规范化。
- `GraphAlgorithmsTest`（10+ 测试）：五算法在已知小图上的结果钉死。
- `GoldenModelFilesTest`：真实模型文件经本库加载后节点/边数与基线一致（间接契约）。

## 风险与兼容性

- `Graph` 实现了 `Serializable` 但无 serialVersionUID（反序列化兼容性未管理）；
  当前无序列化使用场景，仅标注不处理。
- 无向图 + multigraph 组合的 edgeId 规范化路径未被 MOCA 使用、测试覆盖较薄
  （行为模型全是有向多重图）。

## 开放问题

无。后续变更按 [../workflow.md](../workflow.md) 走新设计文档。
