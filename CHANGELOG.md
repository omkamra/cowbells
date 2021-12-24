# Change Log

## [Unreleased]

### Changed

- `defp` now compiles its pattern and binds the resulting pattern
  transformer to the var like `defp<` does (previously it bound the
  uncompiled pattern form - i.e. the Clojure data structure)
- common project bindings (`:dur`, `:scale`, etc.) must be supplied in
  the `:bindings` option of the project configuration now (previously
  they were accepted both in `:bindings` and at the top level of the
  configuration map)

### Fixed

- `defp` and `defp<` does not inject project-level bindings into the
  `:bind` map of compiled patterns as that makes it impossible to
  inherit these bindings from an enclosing scope.

## [0.1.0] - 2021-12-22

Initial release.

[Unreleased]: https://github.com/omkamra/cowbells/compare/0.1.0...HEAD
[0.1.0]: https://github.com/omkamra/cowbells/tree/0.1.0
