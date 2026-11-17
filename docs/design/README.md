# Design Documentation Requirements

[English](README.md) | [简体中文](README.zh-CN.md)

`docs/design/` holds design documents: descriptions of **proposed changes**,
their rationale, and how to verify them. A design doc is a decision record that
later authors and reviewers can trace back to "why it was done this way".

## When a design document is required

Use the change-classification matrix in [../workflow.md](../workflow.md)
section 1. Quick reference:

- **Required**: behavior changes, cross-module refactors, new virtual devices /
  learners, checker-semantics adjustments, changes to shared contracts (the
  `toString` state protocol, etc.).
- **Not required**: localized bug fixes (the issue captures root cause and fix
  approach), documentation-only work, test additions, semantics-preserving
  small refactors.

## File naming

- Name: `YYYY-MM-DD-<kebab-case-topic>.md` (date = creation date).
- **Bilingual pairs**: `<name>.md` (English) and `<name>.zh-CN.md` (Simplified
  Chinese) in the same directory, sharing the base name and date prefix.
- Directory entry pages: `README.md` / `README.zh-CN.md`.
- Put reciprocal language links directly below the document title:
  `[English](<name>.md) | [简体中文](<name>.zh-CN.md)`.

## Required content (template: [templates/design-template.md](templates/design-template.md))

1. **Problem & context**: what exists today (cite real classes/file paths) and
   why it is unsatisfactory.
2. **Goals / non-goals**: draw the boundary explicitly to prevent scope creep.
3. **Proposed solution**: core mechanism, modules touched, key interface/data
   structure changes; **include rejected alternatives** with reasons.
4. **Acceptance criteria**: executable verification (test names, commands,
   baseline changes).
5. **Risks & compatibility**: impact on existing contracts (see
   [../architecture.md](../architecture.md) section 4).
6. **Status**: `Draft` / `Approved` / `Implemented` / `Rejected` (rejected
   designs are kept, with the reason stated).

## Bilingual alignment requirements

- Both versions cover the **full design**: matching section order and levels.
  A summary or a link to the other language does not replace a translation.
- Technical identifiers (class names, paths, commands, field names, numbers)
  stay identical across versions; only explanatory prose is translated.
- Status, dates, and acceptance criteria match; translation must not change a
  design's status.
- Create or update both versions in the same PR; later evolution is reflected
  in both.

## Template

- [templates/design-template.md](templates/design-template.md) (English)
- [templates/design-template.zh-CN.md](templates/design-template.zh-CN.md) (Chinese)

## Subsequent changes

- After implementation: set status to `Implemented` (optionally with the PR
  link). **Do not delete the document.**
- If a design is superseded: append a "Superseded by" note linking the new
  document and set status to `Replaced`.
