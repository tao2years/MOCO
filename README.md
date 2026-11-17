# MOCA
Maintain Runtime Consistency for IoT Systems Based on Device Behavior Models

This repository hosts the tool MOCA and associated data for the paper "Maintain Runtime Consistency for IoT Systems Based on Device Behavior Models."

## Development

Read [CONTRIBUTING.md](CONTRIBUTING.md) to get started, and [docs/](docs/README.md) for the full development standards:

- [docs/architecture.md](docs/architecture.md) — architecture & development context (module map, data flow, key contracts)
- [docs/workflow.md](docs/workflow.md) — layered workflow: issue → spec/design → plan → coding → verification → review
- [docs/coding-standards.md](docs/coding-standards.md), [docs/testing-standards.md](docs/testing-standards.md), [docs/review-standards.md](docs/review-standards.md), [docs/documentation-standards.md](docs/documentation-standards.md)
- [CODE_REVIEW.md](CODE_REVIEW.md) — recorded code-review findings and known legacy issues

## Methodology

MOCA is a platform-independent tool designed to maintain runtime consistency in IoT systems.

1. **Learning Device Behavior Models**: MOCA uses Active Automata Learning (AAL) to construct behavior models of IoT devices.
2. **Runtime Monitoring**: MOCA monitors system operations, checking the feasibility of executing commands based on the learned behavior models.
3. **Post-execution Verification**: After executing a command, MOCA verifies the post-execution states of both the physical device and its digital representation against the theoretical states specified by the behavior model.
4. **Inconsistency Resolution**: Upon detecting inconsistencies, MOCA employs appropriate actions to resolve the issues and restore consistency.

## Implementation

The implementation of MOCA involves several key components:

## System Architecture

MOCA operates between the application and the IoT system, providing a runtime consistency assurance framework. The key components of MOCA are:

### Learner

The **Learner** interacts with IoT devices to generate precise behavior models, which are essential for understanding device functionality under various conditions. These models form the basis for detecting and resolving inconsistencies.

### Concurrent Handler

The **Concurrent Handler** manages concurrent requests from applications, sequencing them and forwarding them to the **Pre-execution Checker**.

### Pre-execution Checker

The **Pre-execution Checker** verifies the legality of the current instruction before execution.

### Execution Monitor

The **Execution Monitor** ensures the execution of requests and handles exceptions.

### Post-execution Checker

The **Post-execution Checker** identifies any inconsistencies after command execution. If an inconsistency is detected, it generates a resolution and communicates it to the **IoT System** to maintain system consistency.

## IoT System Components

The **IoT System** comprises several modules, including the **Task Scheduler**, **Device Controller**, and **Device Twin**.

### Task Scheduler

The **Task Scheduler** receives commands from MOCA and forwards them to the corresponding device controllers.

### Device Controller

The **Device Controller** interacts directly with physical IoT devices, such as Yeelight and Gateway, updating their digital states accordingly.

### Device Twin

The **Device Twin** maintains a digital representation of each device, enabling virtual interaction and monitoring.

## Getting MOCA to Run

Ensure you have the necessary dependencies configured as per the `pom.xml` file. You can use Maven to manage dependencies.

## Continuous Integration

GitHub Actions runs the regression pipeline (`.github/workflows/ci.yml`) on every push and pull request targeting `main`. The pipeline executes `mvn verify`, which covers:

- **Build**: all 60 source files compile.
- **Regression tests**: the graph library (node/edge bookkeeping, Tarjan, topsort, cycle detection, connected components) and the Yeelight virtual device semantics are pinned by unit tests.
- **Golden-file validation**: every learned behavior model in `src/main/java/MOCO/modelFiles` is checked for well-formedness, unique transitions, contiguous `S0..Sn` node ids, JSON↔dot consistency, and the recorded node/transition baseline. The Yeelight model is additionally re-learned from scratch and compared against the checked-in golden file.

The `main` branch is protected: changes arrive via pull requests whose CI must pass before merging.

Run the same pipeline locally:

```
mvn verify
```

### When a golden model changes on purpose

If you intentionally change a virtual device or the Learner, the golden baseline will fail. In the same commit:

1. Re-run the corresponding `*_Model` main (e.g. `MOCO.Learner.Yeelight_Model`) to regenerate `modelFiles/yeelight.json` and `yeelight_output.dot`.
2. Update the expected counts in `src/test/java/MOCO/GoldenModelFilesTest.java` (and the pinned counts in `MOCO.YeelightModelRegressionTest` for the Yeelight).
3. State the reason for the model change in the commit message.

## Running MOCA

To run MOCA, execute the `Main.java` file. Configuration for messaging can be set in the `sendMessage`.

To enable or disable MOCA, set the proxy on or off in the Main function:

```
messageProxy.setProxyOn(true); // Enable MOCA
messageProxy.setProxyOn(false); // Disable MOCA
```

In the TaskScheduler class, you can comment or uncomment the invoke methods to enable or disable the post-check in the ExecutionChecker:
```
// ExecutionChecker.postCheck(...); 
ExecutionChecker.postCheck(...); 
```