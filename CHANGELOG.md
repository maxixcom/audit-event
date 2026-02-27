# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.4] - 2026-02-27

### Changed
- `AuditEventSerializer` преобразован в интерфейс — теперь можно подключать собственные реализации сериализации
- Jackson-реализация вынесена в отдельный класс `AuditEventSerializerJson`
- Обновлены примеры и тесты для использования `AuditEventSerializerJson`

## [0.0.3] - 2026-02-27

### Changed
- **BREAKING**: Made `actor`, `action`, `category`, and `resource` fields optional (nullable) in `AuditEvent`
- Only 4 fields are now required: `eventId`, `version`, `timestamp`, and `source`
- Collections (`changes`, `metadata`, `tags`) remain non-null with empty default values
- Updated `getSummary()` method to handle nullable fields gracefully

### Added
- Support for creating minimal audit events with only required fields
- New tests for minimal event creation in DSL and Builder APIs
- Comprehensive documentation section "Обязательные и опциональные поля" in README
- Best practices guide for when to use minimal vs full events
- Examples of minimal, partial, and full event creation patterns

### Updated
- README.md with detailed information about required and optional fields
- CLAUDE.md with updated architecture notes
- All existing tests to work with nullable fields using safe navigation

### Fixed
- Test compilation issues after making fields nullable

## [0.0.2] - 2026-02-XX

### Added
- Initial release with full audit event model
- Kotlin DSL for creating events
- Builder API for Java compatibility
- Factory methods for common event patterns
- JSON serialization support via Jackson
- Support for nested resources with parent hierarchy
- Comprehensive test coverage

## [0.0.1] - 2026-02-XX

### Added
- Initial project setup
- Basic event model structure

[0.0.4]: https://github.com/maxixcom/audit-event/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/maxixcom/audit-event/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/maxixcom/audit-event/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/maxixcom/audit-event/releases/tag/v0.0.1
