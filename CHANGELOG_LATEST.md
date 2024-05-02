The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

This is a copy of the changelog for the most recent version. For the full version history, go [here](https://github.com/TheIllusiveC4/Curios/blob/1.20.4/CHANGELOG.md).

## [8.0.0-beta+1.20.6] - 2024.05.02
### Added
- [API] Added `CuriosApi#withSlotModifier` to generate `ItemAttributeModifiers` with a slot modifier attached
### Changed
- Changed the default interface to the experimental menu and removed the legacy menu
- [API] Changed `Attribute` to `Holder<Attribute>`, affecting the following:
    - `ICurio#getAttributeModifiers`
    - `ICurioItem#getAttributeModifiers`
    - `SlotAttribute#getOrCreate`
    - `CuriosApi#getAttributeModifiers`
    - `CuriosApi#addSlotModifier`
    - `CuriosApi#addModifier`
    - All modifier methods in `CurioAttributeModifierEvent`
- [API] Changed `CurioEquipEvent` and `CurioUnequipEvent` to `CurioCanEquipEvent` and `CurioCanUnequipEvent`
- [API] Added `HolderLookup.Provider` to the signatures of `IDynamicStackHandler#serializeNbt` and `IDynamicStackHandler#deserializeNbt`
### Removed
- Removed `addModifier`, `addSlotModifier`, and `getAttributeModifiers` methods from `ICuriosHelper`, use the appropriate methods in `CuriosApi` instead
- [Forge] Forge removed stack capabilities so curios can now only be registered through `CuriosApi#registerCurio`or implementing `ICurioItem` on the item
- [Forge] Removed `CuriosApi#createCurioProvider`
