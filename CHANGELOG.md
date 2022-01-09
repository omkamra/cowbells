# Change Log

## [0.3.1] - 2022-01-09

### Fixed

- a regression in the sequencer which made the timing of ticks less
  precise

## [0.3.0] - 2022-01-06

### Added

- new pattern expressions: `[:bank N]` to change MIDI bank, `[:cc C
  V]` to change value of MIDI controller, `[:pitch-bend V]` for
  setting pitch bend - see the new "MIDI CONTROLLERS" section of the
  tutorial for more

### Fixed

- a bug in the sequencer which occasionally resulted in 100% CPU usage

## [0.2.0] - 2021-12-24

### Added

- live coding enhancement: `:var` patterns can now invoke Clojure
  functions at build time (even with arguments) and compile the
  returned value into a pattern transformer (see tutorial for an
  example)

### Changed

- common project bindings (`:dur`, `:scale`, etc.) must be supplied in
  the `:bindings` option of the project configuration now (previously
  they were accepted both in `:bindings` and at the top level of the
  configuration map)
- bumped `omkamra/sequencer` and `omkamra/sequencer-fluidsynth`
  dependencies to `0.3.0`

### Fixed

- `defp` now compiles its pattern and binds the resulting pattern
  transformer to the var like `defp<` does (previously it bound the
  uncompiled pattern form - i.e. the Clojure data structure)
- `defp` and `defp<` does not inject project-level bindings into the
  `:bind` map of compiled patterns as that makes it impossible to
  inherit these bindings from an enclosing scope.

## [0.1.0] - 2021-12-22

Initial release.

[0.3.1]: https://github.com/omkamra/cowbells/compare/0.3.0...0.3.1
[0.3.0]: https://github.com/omkamra/cowbells/compare/0.2.0...0.3.0
[0.2.0]: https://github.com/omkamra/cowbells/compare/0.1.0...0.2.0
[0.1.0]: https://github.com/omkamra/cowbells/tree/0.1.0
