# Design (as-built): Runtime Consistency Engine (MessageProxy + ExecutionChecker + repair replay)

[English](2026-11-17-as-built-consistency-engine.md) | [简体中文](2026-11-17-as-built-consistency-engine.zh-CN.md)

- **Date:** 2026-11-17
- **Component:** `MOCO.MessageProxy`, `MOCO.ExecutionChecker` (preCheck / postCheck / repair-path computation / action generation), `IoTSystem.TaskScheduler.invokeMethod` (the postCheck hook), `IoTSystem.DelayedMessage` (the repair-replay vehicle)
- **Status:** Implemented (as-built record, reverse-engineered from current `main`; **including an honest account of known defects**)
- **Issue:** #5

## Problem & context

MOCA's second and third pillars: pre-execution legality checking (preCheck)
and post-execution three-way consistency verification (digital-twin state /
physical-device state / behavior-model predicted state), with automatic
repair on deviation. This is the core closed loop of the paper's method —
and also where the known defects concentrate (problems 1–3 in
[../../CODE_REVIEW.md](../../CODE_REVIEW.md)). This document deposits the
**actual** behavior as-built — including the paths short-circuited by
defects — as the baseline knowledge for future repair designs.

## Goals / non-goals

- Goals: precisely describe the current implementation's message flow,
  decision semantics, repair mechanism, and defect locations.
- Non-goals: proposing defect fixes (fixes are behavior changes that need
  their own design documents).

## Proposed solution (as-built)

### Message entry and the proxy switch (`MessageProxy`)

- `addMessage(message, currentState)`: with `proxyOn=true` it runs
  `preCheck` first and only then enqueues into `TaskScheduler` (returns
  true); otherwise it just logs and returns false. With `proxyOn=false` it
  passes straight through (MOCA-off mode).
- The constructor receives five outEdgeApis indexes (built by Main after
  loading the model files); **`ExecutionChecker` statically loads its own
  copy of the models** (double loading, recorded in CODE_REVIEW) — preCheck
  uses the indexes passed in by Main, postCheck uses its own static graphs;
  theoretically identical but from two sources.

### preCheck semantics (`ExecutionChecker.preCheck`)

```
currentState == "Invalid"             → false
deviceType outside the 5-device switch → false
apis = outEdgeApis.get(currentState)
apis != null && apis.toString().contains(message.getDeviceAPI()) → true (else false)
```

- **contains substring matching**: `"turn"` matches `"turnOn|true|false"` —
  an intentional preservation of the original semantics, pinned by the
  characterization test `preCheckUsesContainsSoPrefixApiNamesAlsoMatch`.
- The state key is the **full system-state string** (e.g.
  `Yeelight{'powerOn':false, ...}`) read from the corresponding twin by
  `Main.getCurrentStateBasedOnMsg` (note: WM uses `toDeviceString`, the
  others use `toSystemDeviceString`/`toSystemString` — inconsistent
  conventions that currently work).

### postCheck three-way verification (`postCheck` → `checkControllerDeviation`)

Triggered from `TaskScheduler.invokeMethod`'s `finally` after each reflective
controller invocation (passing the **pre-invocation** twin state as
preState). The skeleton:

```
currentState  = twin reading
targetState   = getTargetStateBasedOnBehaviourModels(preState, api, behaviorGraph)
if currentState != targetState && targetState != "null" → Digital Deviation
    → repairTwin (setTargetState the twin directly to the model's target state)
currentPhysicalState = physical device reading
if currentPhysicalState != targetState && targetState != "null" → Physical Deviation
    if currentPhysicalState == preState  → replay the original message after a 100ms delay (device didn't move, retry)
    else → calculateSolutions(behaviorGraph, currentPhysical, target, message)
           → generateXxxActions (translate into a concrete repair action sequence per device)
```

- The five devices' readTwin/readPhysical/repairTwin are injected via the
  `DeviationTarget<C>` interface (the unified skeleton after refactoring;
  behavior identical to the former per-device implementations).
- **targetState matching defect (CR-3)**:
  `getTargetStateBasedOnBehaviourModels` uses `edge.getName().equals(cmd)`
  while callers pass the bare API name (`"turnOn"`), but the edge name is
  actually `"turnOn|true|false"` ⇒ **targetState is almost always `"null"`**
  ⇒ both deviation checks are short-circuited in most runs. Pinned by the
  characterization test
  `getTargetStateWithBareApiNameMissesBecauseOfNameMetadata`.
- **CM physical-state hardcoding (CR-1)**: in the CM branch, readPhysical
  fetches the real state and then it is overwritten by
  `currentPhysicalState = "CoffeeMachine{'waterReady':'true', ...}"`
  (a debug leftover, preserved verbatim by the refactor to keep behavior).

### Repair-path computation (`calculateSolutions` + `Utils.findShortestPaths`)

- `findNodeContaining`: finds the edge whose source contains the state
  fragment and returns that source node (the `"S0"` fragment finds the
  origin). Returns `""` when nothing matches.
- `findShortestPaths`: BFS for source→target paths (collecting all shortest
  ones, capped at ≤3); when no direct path exists it degrades to an
  origin→source path with a `"START_FROM_ORIGIN"` marker prepended.
- Path-selection precedence (single-pass implementation, semantics equal to
  the original three-stage version): last API == requested API and not
  FROM_ORIGIN ＞ last API == requested API ＞ not FROM_ORIGIN ＞ the first
  path.
- **Wrong-model defect (CR-2)**: historically the non-CM branches passed the
  CM model into `calculateSolutions`; after the refactor unified the
  skeleton, each branch passes its own model (`gateway_behaviorGraph` etc.)
  — for 4/5 devices the behavior went from "wrong model" to "right model".
  This was a deliberate behavioral correction during refactoring, covered
  by characterization tests and honestly recorded here as an evolution
  difference.

### Repair action generation and replay

- `generateXxxActions` translates the abstract API sequence into concrete
  messages: parameterized APIs get fixed demo parameters
  (setBrightness→50, setRGB→120/130/111, …); `START_FROM_ORIGIN` sends no
  message but instead **resets wholesale** the device and twin (fresh
  initial objects).
- Repair messages are wrapped as `DelayedMessage` (100ms) into
  `TaskScheduler.delayedQueue`; a dedicated consumer thread moves them back
  to messageQueue to re-run the full dispatch flow (including postCheck).

### Concurrency model (as-is)

- `TaskScheduler`: a fixed thread pool (5 in the demo) plus two resident
  tasks (messageQueue consumer, delayedQueue mover). `invokeMethod` starts
  a fresh thread per message for the reflective call; a `CountDownLatch` is
  created and counted down but its **await is commented out** (the
  synchronization is inert, CR-4).
- `delayedQueue` is a static field assigned in instance constructors; the
  first constructor (CM-only) is a leftover whose use leaves 4 null
  controllers (Main uses only the second constructor).
- `START_FROM_ORIGIN` wholesale-replaces the controller's device/twin
  references with no synchronization against in-flight threads (acceptable
  for the demo scenario, recorded as CR-5).

## Acceptance criteria (as-built checks)

- `ExecutionCheckerTest` (7 tests): all preCheck branch semantics, both
  faces of targetState matching (full-name match succeeds / bare name
  always misses), and the `"null"` fallback.
- `IoTSystemTest`: DelayedMessage ordering, Message/Queue behavior.
- `mvn verify` fully green (122 tests) — every "pinned current behavior"
  has a characterization test backing it.

## Risks & compatibility

- This area hosts CODE_REVIEW.md problems 1–5: any "drive-by fix" changes
  behavior pinned by characterization tests ⇒ the issue → design → explicit
  characterization-test-update flow must be followed.
- Repair replay can self-excite (repairing a deviation triggers another
  deviation): currently mitigated only by the 100ms delay and message
  deduplication semantics, with no explicit cap — fine for the demo,
  needs design for anything larger.

## Open questions

- The API-name unification scheme (bare name vs full name with metadata vs
  stripping metadata at load time) — the core design decision for fixing
  CR-3, awaiting a dedicated design document.
- Consolidating MessageProxy's five Map parameters and ExecutionChecker's
  duplicate static model loading.
