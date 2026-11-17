# Design (as-built): IoTSystem runtime (TaskScheduler / DeviceController / DeviceTwin / VirtualDevice)

[English](2026-11-17-as-built-iot-runtime.md) | [简体中文](2026-11-17-as-built-iot-runtime.zh-CN.md)

- **Date:** 2026-11-17
- **Component:** `IoTSystem` (Main / TaskScheduler / MessageQueue / MessageSender / Message / DelayedMessage), `IoTSystem.DeviceController.*` (5 controllers + `AbstractDeviceController`), `IoTSystem.DeviceTwin.*` (5 twins), `VirtualDevice.*` (10 virtual devices)
- **Status:** Implemented (as-built record, reverse-engineered from current `main` code)
- **Issue:** #5

## Problem & context

IoTSystem is the application-side and device-side runtime proxied by MOCA:
application commands enter scheduling through MessageProxy, and controllers
map commands onto the physical (virtual) devices while mirroring changes
into digital twins. Its **string state protocol** (the `toString` family) is
the single most important implicit contract of the whole system — learner
node contents, preCheck keys, and postCheck comparisons all depend on it.
This document deposits the as-built design.

## Goals / non-goals

- Goals: record the message lifecycle, controller mapping rules, twin
  synchronization semantics, virtual-device semantics, and the exact
  conventions of the string protocol.
- Non-goals: redesigning the scheduling/concurrency model (defect fixes go
  through their own design docs).

## Proposed solution (as-built)

### Message lifecycle

```
Main.main
  → initialize(): five (Device, Twin, Controller) sets + TaskScheduler (5 threads)
  → loadBehaviorModelsAndOutApis(): Gson-loads the 5 models + builds outEdgeApis indexes
  → MessageProxy(taskScheduler, 5 index groups, true)
  → MessageSender.sendMessage*(): scripts N messages (100 ms pacing each)
      → Main.getCurrentStateBasedOnMsg(msg): reads the system-state string from the twin
      → MessageProxy.addMessage → preCheck → TaskScheduler.addMessage
  → MessageQueue (LinkedBlockingQueue) FIFO
  → dispatchMessage: reflectively invokes the controller method by deviceType
      (String→Integer argument parsing, int primitive matching)
      → invokeMethod: a new thread runs method.invoke(controller, args)
      → finally: ExecutionChecker.postCheck(...)  [the hook point]
      → controller.printInternalState(): parallel device/twin comparison log
  → shutdown(): 5 s graceful shutdown + oshi performance sampling (CPU/disk deltas)
```

### Controller layer (`AbstractDeviceController<D, T>`)

- The base class holds `deviceId / device / twin` plus the device and twin
  class names (used for the log-prefix-replacement comparison).
- **Mapping rule (identical across all 5 controllers)**: each API method
  first calls the physical device's same-named method; only when the device
  returns `"success"` is the change mirrored into the twin (field-by-field
  sets for the fields the API touches); `"skip"` means the device rejected
  the call (a precondition failed) and the twin stays untouched — **which is
  precisely the origin of deviations**: device and twin are updated by the
  same event stream, so they are naturally consistent on the normal path;
  inconsistency appears only when one side is changed externally.
- `CMController.brewCoffee` is the exception: it unconditionally calls the
  device and pours `updateAllFromString(device toString)` wholesale into the
  twin (including volume fields).
- Each controller carries a `main` demo and device-name-prefixed
  getters/setters (used by repair resets).

### Twin layer (`*Twin`)

- The 5 twins are pure Java mirrors of device fields plus fastjson parsers.
- **Three string conventions** (the contract most easy to trip over):

| Method | Semantics | Example |
| ------ | --------- | ------- |
| `toString()` | full state (including numeric volume fields) | `WMTwin{...}` / `CMTwin{...}` |
| `toSystemDeviceString()` / `toSystemString()` | abstract system state (booleanized, aligned with learner nodes) | `CoffeeMachine{...}` (device-name prefix) / `Yeelight{...}` |
| `toDeviceString()` (WM only) | device-view abstract state | `WashingMachine{...}` |

- `setTargetState` (used by repair): the CM version updates only 7 booleans
  **ignoring the 3 volume fields**, asymmetric with `updateAllFromString`
  (recorded as CR-6); the WM version parses the `WashingMachine{...}`
  string.
- `fromString` / `equals` / `hashCode`: fully implemented on CMTwin (used
  by characterization tests).

### Virtual-device layer (`VirtualDevice/*`)

- 10 devices = deterministic in-memory state machines: API precondition
  checks (returning success/skip) + state mutation + `toString`/
  `fromString` (fastjson) + `toSystemString` (abstract booleanization).
- Semantics highlights (selection):
  - **Yeelight**: setBrightness/setRGB/setColorMode require `powerOn`;
    brightness 0..100 (**accepts 0**, while `toSystemString`'s
    `brightness > 0` check excludes 0 — a convention mismatch, recorded);
    RGB range 0..255; `toSystemString` folds rgb into a "sum ≥ 0" boolean.
  - **CoffeeMachine**: addWater/addCoffeeBean/addMilk inject `Random`
    volumes (⇒ the learner needs 100 convergence rounds); brewCoffee has
    fixed consumption per type 1/2/3 (15 g beans + 240/40/40 ml water +
    0/200/100 ml milk); derived flags (waterReady ≥ 250, …) update in two
    steps (⇒ the learner's fixed-point chasing); the internal
    `wait(seconds)` really sleeps but is currently uncalled.
  - **Gateway**: composite semantics (light + alarm + sub-device list);
    `addDevice` caps at 3 (`deviceList < 3`); `removeDevice` doesn't check
    membership, only the counter; `toSystemString` folds lightBrightness
    into a boolean.
  - **VideoCamera**: 10 boolean switches (2^10 = 1024 states; the model
    baseline is 1024 nodes / 10240 edges).
- Real-device equivalents live under `RealDevice/`: Python (python-miio)
  scripts connecting devices directly by IP+token;
  `deviceTwins4Real/*Status` parse the status JSON.

### Concurrency (as-is)

(Complements the concurrency section of
[2026-11-17-as-built-consistency-engine.md](2026-11-17-as-built-consistency-engine.md))
Thread-pool consumption plus a resident delayedQueue mover; each message
executes on its own thread; `invokeMethod`'s latch is never awaited
(synchronization inert); `MessageQueue.getMessage` returns null on
interruption (swallowed by dispatch's null check); the first TaskScheduler
constructor is a leftover (null-controller NPE hazard).

## Acceptance criteria (as-built checks)

- `IoTSystemTest` (10 tests): Message format, Queue FIFO and history-copy
  semantics, DelayedMessage ordering, CMTwin/WMTwin state strings and
  setTargetState semantics.
- `VirtualDeviceTest` / `YeelightTest` / `CoffeeMachineTest`:
  characterization tests of device semantics (including Yeelight boundaries
  and CM consumption assertions).
- `mvn verify` fully green (122 tests).

## Risks & compatibility

- **The string protocol is a behavioral contract**: any field addition or
  removal, ordering change, or booleanization tweak simultaneously affects
  learner node contents, preCheck keys, and postCheck comparisons — three
  layers coupled; any change must go through the behavior-change flow
  (design layer) and synchronize the golden files.
- The `Main` entry is a demo script (oshi sampling, fixed message sequence,
  a 100-iteration message loop), not a configurable test harness;
  performance numbers are demo-grade only.

## Open questions

- Converging the scheduling concurrency model (restoring latch semantics /
  cleaning the dual constructors / interruption semantics) and unifying the
  twin conventions (`toSystemDeviceString` vs `toDeviceString`): both are
  behavior changes awaiting their own design docs (parts already suggested
  as separate issues in CODE_REVIEW.md).
