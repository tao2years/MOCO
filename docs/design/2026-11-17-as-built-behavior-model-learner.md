# Design (as-built): Behavior Model Learner

[English](2026-11-17-as-built-behavior-model-learner.md) | [简体中文](2026-11-17-as-built-behavior-model-learner.zh-CN.md)

- **Date:** 2026-11-17
- **Component:** `MOCO/Learner/BehaviorModelLearner` (skeleton) + 14 `*_Model` learners + `MOCO/API/Edge/Node` + `MOCO.Utils` (export/load)
- **Status:** Implemented (as-built record, reverse-engineered from the current `main` code)
- **Issue:** #5

## Problem & context

MOCA's first pillar is "learn device behavior models": active exploration of
virtual devices (in-memory deterministic state machines) or real devices
(Python-scripted Mi-Home devices) producing behavior graphs that the runtime
consumes directly (`modelFiles/*.json` + `*.dot`). Learner correctness
directly determines preCheck/postCheck correctness, so the exploration
semantics, numbering rules, and export formats are knowledge that must be
pinned. This document deposits the as-built design.

## Goals / non-goals

- Goals: exhaustively and deterministically enumerate a device's reachable
  state space, producing reproducible, verifiable model files.
- Non-goals: strict AAL theory (W-method / equivalence queries — the current
  approach is full-alphabet × state-space exploration plus a convergence
  heuristic); online learning (models are offline artifacts).

## Proposed solution (as-built)

### Two learner shapes

| Shape | Classes | Device interaction | Convergence |
| ----- | ------- | ------------------ | ----------- |
| Virtual-device learners | 12 (e.g. `Yeelight_Model`, extending `BehaviorModelLearner<D>`) | in-memory object; `executeApi` calls methods directly | repeated exploration, union of edges (2 rounds, or the `learnEdges` convergence loop) |
| Real-device learners | 4 (e.g. `Fan_real_Model`, standalone classes that do **not** extend the skeleton) | `Utils.executePythonScript` driving miio scripts | single exploration pass + per-step retry (MAX_RETRIES=5) |

> Virtual learners share one skeleton (the historically duplicated code of 11
> learners was consolidated into `BehaviorModelLearner`; see the fixed items in
> [../../CODE_REVIEW.md](../../CODE_REVIEW.md)). Real-device learners keep
> per-device standalone implementations because their I/O semantics
> (reset-replay-observe) differ.

### The virtual-device skeleton (`BehaviorModelLearner<D>`)

Five abstract contract methods (each subclass must provide):

```
getAllPossibleAPIs()  → List<API>: the exploration alphabet (API name + preemptive + humanIntervention flags)
executeApi(api, dev)  → String: run the API; "skip" means rejected, otherwise returns dev.toString()
fromString(state)     → D: rebuild a device from its toString state (the cloning mechanism)
toSystemState(dev)    → String: abstract system state (= graph node content, e.g. toSystemString())
toStringState(dev)    → String: full state (used for device cloning)
newInstance()         → BehaviorModelLearner<D>: a fresh, unexplored instance (for the convergence loop)
```

**Exploration algorithm** (`generateGraph`/`exploreState`, recursive DFS):

1. From the initial device, for every API in the alphabet: clone the current
   device via `fromString`, then `executeApi`.
2. A `"skip"` return produces no edge; otherwise the edge
   `Edge(systemState, nextSystemState, api.toString())` is recorded. Note
   that **`api.toString()` carries `|preemptive|humanIntervention` metadata**
   (e.g. `"turnOn|true|false"`) — this is the established edge-name
   convention, entangled with preCheck/postCheck matching semantics (see
   "Known defects").
3. **Fixed-point chasing (chaseFixedPoint)**: virtual devices update derived
   flags in two steps (the API changes the visible state first; the derived
   flags catch up on a later call). When the system state is unchanged but
   the full state changed, the same API is replayed until convergence, and
   the intermediate edges are recorded too. Bounded by `MAX_MODE=20` as a
   guard against non-converging devices.
4. New system states are explored recursively; seen states are not re-entered
   (deduplicated by `stateTransitions`).

**Convergence loop** (`learnEdges(warmup, maxRuns)`): repeatedly run
`newInstance().generateGraph`, union the edge sets; stop when the edge count
is unchanged for `HISTORY_SIZE=20` consecutive runs. `CoffeeMachine_Model`
uses `learnEdges(100, 9999)` (because addWater-style APIs inject `Random`
volumes, so the state space needs many sampling rounds to saturate); the
other deterministic virtual devices use a fixed 2-round union (the
`Yeelight_Model.exploreEdges` pattern).

**Node numbering** (`numberNodes` / the inline versions in each
`*_Model.main`): all states go into a
`TreeSet(String.CASE_INSENSITIVE_ORDER)` and are numbered `S0..Sn`. The
**case-insensitive ordering** is a historical choice that keeps golden files
byte-comparable — changing it breaks every baseline.

### Export and load (`MOCO.Utils`)

- `export(edges, dotFile, jsonFile)`: loads a `Graph(true, true, false)`
  whose node content is the toString of `Node{nodeId, content}`, i.e.
  `Node{"nodeId":"S3", "content":"Yeelight{...}"}`; after deduplicating per
  (target, api) it writes the dot file (`generateDotFile`; HashSet iteration
  ⇒ **line order is meaningless**) and the JSON file (`saveEdgesToFile`,
  which serializes the `graph.Edge` collection with **Gson**).
- `loadGraphFromFile` (Gson read-back) → `loadOutEdgeApis` (indexes state
  node content → set of available APIs, consumed by preCheck).
- **Dual-write contract**: the JSON and dot files must describe the same edge
  set (verified by `GoldenModelFilesTest`).

### Real-device learners (template: `Fan_real_Model`)

- `XxxStatus.parse(script output)`: the miio device-status JSON → an abstract
  status object (e.g. FanStatus with power/speed_level/angle fields).
- Before exploring each step, **reset-replay**: run the reset script →
  replay turnOn/setSpeed/setAngle to reach the target state → run the API
  under test → observe the new state. Each (state, api) combination retries
  up to 5 times.
- Output is isomorphic to the virtual learners: `Xxx_real.json` + dot, with
  the same S0..Sn numbering.

### `API` metadata semantics

`API(name, preemptive, humanIntervention)`'s two booleans record whether the
command is preemptive and whether it needs human intervention; they are
encoded into the edge name (`name|p|h`). **The current runtime
(preCheck/postCheck) does not consume the semantics of these flags**; it is
only affected by them through string matching — a "half-implemented"
metadata design, recorded as-is.

## Known defects and couplings (not fixed here; see CODE_REVIEW.md)

- Edge names carry metadata while
  `getTargetStateBasedOnBehaviourModels` matches the bare API name with
  `equals(cmd)` ⇒ the runtime targetState is almost always `"null"` (CR-3).
- `preCheck` uses `contains` substring matching ⇒ `"turn"` matches
  `"turnOn|..."` (CR-3 related).
- `Utils.executePythonScript` closes the reader before `waitFor()`, which can
  deadlock on large output; stderr is not read (mitigated by
  `redirectErrorStream(true)` merging, not eliminated) (CR-6).

## Acceptance criteria (as-built checks)

- `GoldenModelFilesTest`: the structure and count baselines (nodes /
  unique transitions) of all 14 model files are pinned.
- `YeelightModelRegressionTest`: the Yeelight end-to-end re-learn equals the
  golden model (8 nodes, 16 edges), and two runs yield identical edge sets
  (determinism).
- `UtilsTest`: unit contracts for export/load/dot generation.
- `mvn verify` fully green (122 tests).

## Risks & compatibility

- **Any change** — virtual-device semantics, API alphabet, numbering order,
  edge-name format, export order — drifts the golden baselines: the model
  must be regenerated and the baselines updated in the same commit
  (procedure: [../testing-standards.md](../testing-standards.md)
  section 4).
- Real-device learners depend on LAN device IPs/tokens (hard-coded in the
  scripts) and cannot be reproduced in CI; therefore their golden files are
  only structure-checked, never re-learned.

## Open questions

- Should real-device learners be consolidated onto the shared skeleton
  (sharing numbering/export with virtual learners)? That is a structural
  refactor needing its own design doc; the current state is recorded as
  "deliberately not unified".
