The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

This is a copy of the changelog for the most recent version. For the full version history, go [here](https://github.com/TheIllusiveC4/Curios/blob/1.20.4/CHANGELOG.md).

## [7.4.0+1.20.4] - 2024.04.29
### Added
- [API] Added `CuriosApi#getCurioPredicates`
### Changed
- New interface no longer shifts the screen to the right
- Scrolling through pages in the new interface is twice as fast
- Lowered the maximum value of `maxSlotsPerPage` configuration option from 64 to 48
### Fixed
- Fixed generic curio slots from failing validation checks when only those slots exist on an entity [#402](https://github.com/TheIllusiveC4/Curios/issues/402)
