# AGENTS.md

Working conventions for coding agents (and humans using agentic tools) in this
repository. Read this together with [docs/README.md](docs/README.md) — the
documentation map — before making any change.

## Project in one paragraph

MOCO/MOCA is a Java 8 (built on JDK 17 in CI) Maven single-module research tool
that maintains runtime consistency for IoT systems: it learns device behavior
models (AAL over virtual/real devices, exported as golden JSON+dot files),
checks commands pre-execution, verifies twin/physical states post-execution,
and schedules repair actions. The learned model files under
`src/main/java/MOCO/modelFiles/` are **runtime inputs**, so tests pin them.

## Working principles

1. **Spec first, code last.** Follow the layered workflow in
   [docs/workflow.md](docs/workflow.md): classify the change, produce the
   required design doc for behavior/cross-module changes before writing code.
2. **Behavior changes are never drive-by edits.** Known defects in
   `ExecutionChecker` (hardcoded state override, wrong model for repair path
   computation, API-name matching mismatch) are documented in
   [CODE_REVIEW.md](CODE_REVIEW.md) and need an issue + design first.
3. **Golden files move with the code that learns them.** If you change a
   virtual device or the Learner, regenerate the model files and update
   `GoldenModelFilesTest` baselines **in the same commit**, stating the reason.
4. **`mvn verify` is the only gate.** Local and CI run the identical command
   from the repository root. Never push with a red local run.
5. Do not merge PRs, do not bypass branch protection, do not rewrite `main`.
   A human merges every PR.

## Common commands

```bash
mvn verify                        # build + all 122 tests; identical to CI; run from repo root
mvn test -Dtest=GraphTest         # single test class
mvn test -Dtest=GraphTest#testX   # single test method
```

Run everything from the project root: model files are loaded via
root-relative paths. JDK 17 (Temurin) with Maven is the verified toolchain.

## Code conventions (summary)

- Java 8 language level: no `var`, no switch expressions, no records.
  Existing explicit-loop style; Stream/Optional allowed but not encouraged.
- Package dependency direction: `graph` has **zero** business imports;
  `IoTSystem → MOCO → VirtualDevice`; never widen `MOCO → IoTSystem` coupling.
- Logging: Log4j2 `LOGGER`; **no new `System.out`/`System.err`** (16 legacy
  call sites remain; do not add more, do not mass-clean them in feature PRs).
- JSON: fastjson for device-state serialization, Gson for model-file parsing —
  keep each in its established scope.
- Naming: new classes `UpperCamelCase` without underscores; constants
  `UPPER_SNAKE_CASE`; tests `<ClassUnderTest>Test` with behavior-statement
  method names.
- Comments: none by default; explain *why* when non-obvious; Javadoc for
  public helpers in `graph/` and `MOCO.Utils`.
- Full detail: [docs/coding-standards.md](docs/coding-standards.md).

## Testing conventions (summary)

- JUnit 4 only; tests live in the same package under `src/test/java`.
- Bug fixes need a test that was red before the fix and is green after.
- Golden-file comparisons are **set-based** (dot line order comes from HashSet
  iteration and is meaningless).
- Golden model change procedure: regenerate via the matching `*_Model.main()`,
  update `EXPECTED_COUNTS`, update the pinned 8/16 counts in
  `YeelightModelRegressionTest` when Yeelight is affected — one commit.
- Full detail: [docs/testing-standards.md](docs/testing-standards.md).

## Documentation requirements

- Design docs for behavior/cross-module changes go to `docs/design/` as
  **bilingual pairs** (`<name>.md` + `<name>.zh-CN.md`, same PR, aligned
  content). Template: `docs/design/templates/`.
- Multi-PR implementations get a plan in `docs/plans/`.
- User-visible behavior changes update `README.md`; contract/architecture
  changes update `docs/architecture.md` — in the same PR.

## Submitting PRs

Use [.github/pull_request_template.md](.github/pull_request_template.md).
Link the driving issue, state verification evidence (commands run, results,
golden baseline old→new), and list risks. Self-audit the full diff (including
new files) before opening the PR. A person reviews and merges.

## Reference repositories

The layered workflow and bilingual design-doc practice are modeled on
[qwen-code](https://github.com/QwenLM/qwen-code) (docs/design, docs/plans,
AGENTS.md conventions), adapted to this repository's scale.
