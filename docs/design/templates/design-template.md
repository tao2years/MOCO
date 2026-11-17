# Design: <topic title>

[English](design-template.md) | [简体中文](design-template.zh-CN.md)

- **Date:** YYYY-MM-DD
- **Component:** modules involved (e.g. `MOCO.ExecutionChecker` / `VirtualDevice.Yeelight`)
- **Status:** Draft | Approved | Implemented | Rejected
- **Issue:** #N

## Problem & context

What exists today (cite real classes/file paths and methods) and why it is
unsatisfactory. If this fixes a known defect, link the matching entry in
[CODE_REVIEW.md](../../../CODE_REVIEW.md).

## Goals / non-goals

- Goals: the verifiable outcomes this change delivers.
- Non-goals: what is explicitly out of scope (to prevent scope creep).

## Proposed solution

Core mechanism and data flow (an ASCII diagram is fine); modules, interfaces,
and data structures touched; impact on existing contracts
([architecture.md](../../architecture.md) section 4).

### Alternatives considered

| Option | Pros | Rejected because |
| ------ | ---- | ---------------- |
| A (this design) | ... | — |
| B | ... | ... |

## Acceptance criteria

Executable verification, listed one by one (test names / commands / golden
baseline changes):

1. `mvn verify` is green.
2. `<new test class/method>` asserts `<new behavior>`.
3. Golden baseline moves from `<old numbers>` to `<new numbers>` because `<...>`.

## Risks & compatibility

- Impact on the string state protocol: ...
- Impact on golden model baselines: ...
- Documentation that must be updated together: ...

## Open questions

(Questions that must be answered before implementation; record each decision
with its date.)
