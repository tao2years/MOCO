# Design (as-built): graph library

[English](2026-11-17-as-built-graph-library.md) | [简体中文](2026-11-17-as-built-graph-library.zh-CN.md)

- **Date:** 2026-11-17
- **Component:** `graph/` — `Graph`, `Edge`, `Utils`, `IllegalOperationException`, `graph/algorithms/` (Tarjan, Topsort, IsAcyclic, FindCycles, Components)
- **Status:** Implemented (as-built record, reverse-engineered from the current `main` code)
- **Issue:** #5

## Problem & context

MOCA's behavior models are directed graphs: nodes are abstract system-state
strings, edges are API transitions. The repository needs a standalone,
general-purpose graph library to carry them (and any future graph work)
without business semantics bleeding in. The `graph/` package exists for exactly
that; this document is its as-built design record — not a proposal for future
change, but a knowledge deposit of the implemented design decisions for later
contributors (humans and agents).

## Goals / non-goals

- Goals: node/edge CRUD with in/out-edge indexing, compound nodes, five graph
  algorithms, and **zero business dependencies** (no imports of `MOCO`,
  `IoTSystem`, `VirtualDevice`, `RealDevice`).
- Non-goals: performance tuning (HashMap indexes and Stream plumbing suffice
  here); weighted/hyper graphs; thread safety.

## Proposed solution (as-built)

### Data structures

`Graph<N, E>` maintains seven flat indexes (all keyed by `String` nodeId):

```
nodes:      nodeId → N (node data)
in:         nodeId → edgeId → Edge (incoming edges)
out:        nodeId → edgeId → Edge (outgoing edges)
pred:       nodeId → predecessor nodeId → edge count
sucs:       nodeId → successor nodeId → edge count
edgeObjs:   edgeId → Edge (all edge objects)
edgeLabels: edgeId → E (edge data label)
```

- **edgeId encoding**: `source + "\x01" + target + "\x01" +
  (name != null ? name : "\x00")`; for undirected graphs (`directed=false`)
  source/target are first ordered by `compareTo`, canonicalizing undirected
  edges.
- Three boolean switches (constructor args, setters exist but are rarely
  used): `directed` (default true), `multigraph` (default false), `compound`
  (default false).
- **Key constraint**: a named edge in a non-multigraph throws
  `IllegalOperationException` (checked inside `setEdge`). MOCA behavior-model
  graphs consistently use `new Graph<>(true, true, false)` — directed
  multigraph, because two states can have several distinct API edges.
- Compound mode provides `setParent/getParent/getChildren`; parent pointers
  initially reference the virtual root `\x00`; `setParent` walks the ancestor
  chain and throws on a would-be cycle.

### Node/edge lifecycle

- `setNode` is idempotent: for an existing id it replaces the data only when
  `replaceValue` is set, and never double-counts; for a new id it initializes
  the four index buckets and does `++nodeCount`.
- `setEdge`: computes the edgeId first; if it exists it only replaces the
  label (idempotent); otherwise it implicitly creates both endpoint nodes
  (`setNode(id, null, false)`), registers in/out/pred/sucs, and does
  `++edgeCount`.
- `removeNode`: cascades removal over all in/out edges (iterated over a
  snapshot to avoid ConcurrentModificationException); in compound mode it
  re-hangs orphaned children to the virtual root.
- `removeEdge`: cleans both index directions; `pred/sucs` counts are
  maintained decrement-to-zero-delete.
- **Contract (important)**: nodeCount/edgeCount are the single source of
  truth; all mutations must go through the methods above, never direct map
  edits (fields are private, but same-package or reflective tests must also
  respect this).

### Algorithms (`graph/algorithms/`)

| Class | Entry | Complexity | Notes |
| ------ | ----- | ---------- | ----- |
| `Tarjan` | `graph.tarjan()` | O(V+E) | Strongly connected components; instance fields reset at each entry (instance reusable) |
| `Topsort` | `graph.topsort()` | O(V+E) | Topological order; on a cycle it **throws** (does not return null) |
| `IsAcyclic` | `graph.isAcyclic()` | — | Cycle-freedom check |
| `FindCycles` | `graph.findCycles()` | — | Returns all node groups that lie on cycles |
| `Components` | `graph.components()` | O(V) | Connected components, returned as subgraphs (one new `Graph` each) |

Algorithm classes are mutually independent and stateless (or reset at entry),
all driven off `graph.getNodes()`.

### Known implementation details (decision record)

1. **`Edge.equals` compares via `toString`** (`graph.Edge`): literal
   comparison of the concatenated string instead of field-by-field equality.
   Simple but brittle — different field order renders unequal. Kept as-is;
   changing equals is a behavior change needing a design doc.
2. **`getEdgesBasedOnNodes` scans all edges with `key.contains(edgeId)`**
   (O(E) substring match): few callers, small graphs (model graphs ≤ a few
   thousand edges), acceptable; no prefix index added.
3. **Streams used liberally for filtering/collecting** (getSources/getSinks/
   inEdges filters): different from the explicit-loop style elsewhere in the
   repo, but this package is self-contained; original implementation kept.
4. `nodeEdges` returns an in+out concatenated List; `neighbors` mutates the
   Set returned by predecessors (that return is already a defensive copy —
   safe but subtle).

## Acceptance criteria (as-built checks)

The existing test suite is the acceptance evidence (all green under
`mvn verify`, 122 tests):

- `GraphTest` (31 tests): node/edge lifecycle, idempotency, cascading
  removal, compound mode, edgeId canonicalization.
- `GraphAlgorithmsTest` (10+ tests): the five algorithms pinned on known
  small graphs.
- `GoldenModelFilesTest`: real model files loaded through this library match
  the pinned node/edge counts (indirect contract).

## Risks & compatibility

- `Graph` implements `Serializable` without a serialVersionUID
  (deserialization compatibility unmanaged); no serialization use case exists
  today — documented, not addressed.
- The undirected + multigraph edgeId canonicalization path is unused by MOCA
  and thinly tested (behavior models are all directed multigraphs).

## Open questions

None. Future changes follow [../workflow.md](../workflow.md) with a new
design doc.
