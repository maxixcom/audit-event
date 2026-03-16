# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin/Java library for creating structured audit events. It provides three API styles:
1. **Kotlin DSL** - expressive syntax for Kotlin (primary approach)
2. **Builder API** - fluent builders for Java
3. **Factory methods** - ready-made templates for common scenarios

The library produces audit events in a structured JSON format, commonly sent to Kafka for storage/analytics.

## Development Commands

```bash
# Build the library
./gradlew build

# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "*.AuditEventDslTest"

# Run usage examples
./gradlew test --tests "*.UsageExamples"

# Publish to staging (local)
./gradlew publish

# Full release to Maven Central
./gradlew jreleaserFullRelease
```

## Architecture

The codebase follows a layered structure:

**model/** - Core data classes that form the audit event schema
- `AuditEvent` - The main event container with eventId, timestamp, actor, resource, changes, outcome
  - Required fields: `eventId`, `version`, `timestamp`, `source`
  - Optional fields: `actor`, `action`, `category`, `resource`, `correlationId`, `outcome`
- `Actor` fields: `actorType`, `userId`, `groupId`, `sessionId`, `roles`, `ipAddress`, `userAgent`, `onBehalfOf`, `attributes`
  - Collections default to empty: `changes`, `metadata`, `tags`
- `Actor` - Who performed the action (user, system, scheduler, service account). Includes optional `groupId` for group membership
- `Resource` - What was affected (supports parent hierarchy for nested resources)
- `Change` - Field-level changes with old/new values
- Enums: `ActorType`, `ActionCategory` for standardized values

**dsl/** - Kotlin DSL implementation
- Uses `@DslMarker` to prevent improper nesting
- Main entry point: `auditEvent { }` block
- Validates only `source` as required at build time (all other fields are optional)

**builder/** - Java-friendly Builder API
- Fluent builders for each model type
- Factory methods like `ActorBuilder.user()`, `OutcomeBuilder.success()`
- Equivalent to DSL but with explicit method chaining

**factory/** - Pre-built event templates
- `createLoginEvent()`, `createResourceCreatedEvent()`, etc.
- Standardizes common patterns (login, CRUD, permissions, data export)
- Automatically sets correct category and tags

**serialization/** - Jackson-based JSON serialization
- Configured for Kotlin data classes, Java Time types
- Utility methods ignore during serialization (`@JsonIgnore` on getters)
- `serializeToBytes()` for Kafka integration

## Key Conventions

### Action Naming
Always use format: `domain.entity.verb`
```kotlin
✓ "sales.order.created"
✓ "iam.user.role_changed"
✓ "billing.invoice.updated"
✗ "createOrder" or "update_user"
```

### Nested Field Changes
Use dot-notation for nested fields:
```kotlin
change {
    field = "address.city"          // not just "city"
    field = "deliveryInfo.street"   // can go multiple levels
}
```

### Resource Hierarchies
Build parent chains for nested resources:
```kotlin
resource {
    type = "comment"
    id = "comment-789"
    parent {
        type = "task"
        id = "task-456"
        parent {
            type = "project"
            id = "project-123"
        }
    }
}
// Result: "project:123/task:456/comment:789" via getFullPath()
```

### Correlation IDs
Use the same `correlationId` for all events in a business transaction chain (e.g., order creation → inventory reservation → payment).

## Design Decisions

1. **Three API styles** - DSL for Kotlin ergonomics, Builders for Java compatibility, Factories for standardization
2. **Immutable models** - All models are Kotlin data classes (immutable by convention)
3. **Minimal required fields** - Only `eventId`, `version`, `timestamp`, and `source` are required. All other fields (`actor`, `action`, `category`, `resource`) are optional to support flexible event creation patterns
4. **Version field** - Events include a `version` field for schema evolution
5. **Changes as list** - Multiple field changes tracked in a single event
6. **Null semantics** - `oldValue: null` = creation, `newValue: null` = deletion
7. **Tags array** - For filtering (e.g., "security", "gdpr", "critical", "automated")
8. **SecurityCritical check** - `isSecurityCritical()` method examines tags and action patterns

## Testing Patterns

Tests are organized by component:
- `AuditEventDslTest` - DSL syntax and validation
- `AuditEventBuilderTest` - Builder API and Java compatibility
- `AuditEventFactoryTest` - Factory method outputs
- `AuditEventSerializerTest` - JSON serialization round-trips
- `UsageExamples` - Runnable examples demonstrating real usage

When adding new functionality, maintain this test structure.

## Kafka Integration Pattern

The library is designed for Kafka workflows:
```kotlin
val key = event.resource.type          // partition by resource type
val value = serializer.serializeToBytes(event)
val record = ProducerRecord(topic, key, value)

// Optionally add headers for filtering
record.headers().add("category", event.category.name.toByteArray())
record.headers().add("securityCritical", event.isSecurityCritical().toString().toByteArray())
```

## Requirements

- **Java 17+** - language level and runtime requirement
- **Kotlin 1.7.21** - library compiled with this version (but compatible with 1.7+)
- Jackson dependencies for JSON (see build.gradle.kts)

## Publication

The library publishes to Maven Central and GitHub Packages via jReleaser. See `doc/PUBLISHING.md` for the full release process. Key points:
- Tags matching `v*` trigger GitHub Actions release workflow
- Version in `build.gradle.kts` must NOT have `-SNAPSHOT` for releases
- GPG signing required for Maven Central
- Namespace `io.github.maxixcom.audit` registered with Sonatype
