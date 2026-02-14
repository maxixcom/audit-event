package io.github.maxixcom.audit.event.examples

import io.github.maxixcom.audit.event.builder.*
import io.github.maxixcom.audit.event.dsl.auditEvent
import io.github.maxixcom.audit.event.factory.AuditEventFactory
import io.github.maxixcom.audit.event.model.*
import io.github.maxixcom.audit.event.serialization.AuditEventSerializer

/**
 * Примеры использования библиотеки User Log
 */
object UsageExamples {

    private val serializer = AuditEventSerializer()

    /**
     * Пример 1: Использование Kotlin DSL
     */
    fun kotlinDslExample() {
        val event = auditEvent {
            source = "order-service"
            action = "sales.order.updated"
            category = ActionCategory.UPDATE
            correlationId = "req-abc-123"

            actor {
                actorType = ActorType.USER
                userId = "user-42"
                sessionId = "sess-xyz"
                role("manager", "sales")
                ipAddress = "192.168.1.10"
                userAgent = "Mozilla/5.0"
            }

            resource {
                type = "order"
                id = "ord-1001"
                displayName = "Заказ #1001"

                parent {
                    type = "customer"
                    id = "cust-500"
                }
            }

            change {
                field = "status"
                from("pending")
                to("confirmed")
            }

            change("deliveryAddress.city") {
                from("Москва")
                to("Казань")
            }

            outcome {
                success = true
                duration = 150
            }

            metadata("environment" to "production", "version" to "1.2.3")
            tags("important", "customer-facing")
        }

        println("Kotlin DSL Event:")
        println(serializer.serializePretty(event))
    }

    /**
     * Пример 2: Использование Java Builder API
     */
    fun javaBuilderExample() {
        val event = AuditEventBuilder.create()
            .source("order-service")
            .action("sales.order.updated")
            .category(ActionCategory.UPDATE)
            .actor(
                ActorBuilder.user("user-42")
                    .sessionId("sess-xyz")
                    .roles("manager", "sales")
                    .ipAddress("192.168.1.10")
                    .build()
            )
            .resource(
                ResourceBuilder.create("order", "ord-1001")
                    .displayName("Заказ #1001")
                    .build()
            )
            .addChange(
                ChangeBuilder.modified("status")
                    .from("pending")
                    .to("confirmed")
                    .build()
            )
            .outcome(OutcomeBuilder.success().duration(150L).build())
            .tags("important")
            .build()

        println("\nJava Builder Event:")
        println(serializer.serializePretty(event))
    }

    /**
     * Пример 3: Использование фабричных методов для типовых сценариев
     */
    fun factoryExample() {
        // Вход пользователя
        val loginEvent = AuditEventFactory.createLoginEvent(
            source = "auth-service",
            userId = "user-123",
            sessionId = "sess-xyz",
            ipAddress = "192.168.1.10",
            userAgent = "Mozilla/5.0",
            success = true
        )

        println("\nLogin Event:")
        println(serializer.serializePretty(loginEvent))

        // Создание ресурса
        val actor = Actor(
            actorType = ActorType.USER,
            userId = "user-123",
            sessionId = "sess-xyz",
            roles = listOf("manager")
        )

        val createEvent = AuditEventFactory.createResourceCreatedEvent(
            source = "order-service",
            actor = actor,
            resourceType = "order",
            resourceId = "ord-2001",
            displayName = "Новый заказ",
            initialValues = mapOf(
                "status" to "pending",
                "amount" to 2500.0,
                "customerId" to "cust-700"
            )
        )

        println("\nCreate Resource Event:")
        println(serializer.serializePretty(createEvent))

        // Изменение прав доступа
        val permissionEvent = AuditEventFactory.createPermissionChangedEvent(
            source = "iam-service",
            actor = actor,
            targetUserId = "user-456",
            resourceType = "project",
            resourceId = "proj-100",
            oldPermissions = listOf("read"),
            newPermissions = listOf("read", "write")
        )

        println("\nPermission Changed Event:")
        println(serializer.serializePretty(permissionEvent))
    }

    /**
     * Пример 4: Impersonation (действие от имени другого пользователя)
     */
    fun impersonationExample() {
        val event = auditEvent {
            source = "admin-service"
            action = "support.user.data_access"
            category = ActionCategory.VIEW

            actor {
                actorType = ActorType.USER
                userId = "admin-1"
                sessionId = "admin-sess-123"
                role("admin", "support")

                onBehalfOf(
                    userId = "user-42",
                    userName = "John Doe",
                    reason = "Customer support ticket #12345"
                )
            }

            resource {
                type = "user_profile"
                id = "user-42"
                displayName = "John Doe Profile"
            }

            metadata("ticketId" to "12345")
            tags("impersonation", "support", "critical")
        }

        println("\nImpersonation Event:")
        println(serializer.serializePretty(event))
    }

    /**
     * Пример 5: Системное событие (scheduled job)
     */
    fun systemEventExample() {
        val event = auditEvent {
            source = "billing-service"
            action = "billing.invoice.auto_generated"
            category = ActionCategory.CREATE

            actor {
                actorType = ActorType.SCHEDULER
                attribute("jobName", "monthly-invoice-generator")
                attribute("cronExpression", "0 0 1 * *")
            }

            resource {
                type = "invoice"
                id = "inv-3001"
                displayName = "Invoice for February 2026"
            }

            change {
                field = "amount"
                to(5000.0)
            }

            change {
                field = "period"
                to("2026-02")
            }

            outcome {
                success = true
                duration = 250
            }

            tags("automated", "billing")
        }

        println("\nSystem Event:")
        println(serializer.serializePretty(event))
    }

    /**
     * Пример 6: Вложенная структура ресурсов
     */
    fun nestedResourceExample() {
        val event = auditEvent {
            source = "project-service"
            action = "project.comment.created"
            category = ActionCategory.CREATE

            actor {
                actorType = ActorType.USER
                userId = "user-123"
            }

            resource {
                type = "comment"
                id = "comment-789"
                displayName = "New comment on task"

                parent {
                    type = "task"
                    id = "task-456"
                    displayName = "Implement feature X"

                    parent {
                        type = "project"
                        id = "project-123"
                        displayName = "Project Alpha"
                    }
                }
            }

            change {
                field = "text"
                to("This is a new comment")
            }
        }

        println("\nNested Resource Event:")
        println(serializer.serializePretty(event))
        println("Resource Full Path: ${event.resource.getFullPath()}")
    }

    /**
     * Пример 7: Событие с ошибкой
     */
    fun failureEventExample() {
        val event = auditEvent {
            source = "payment-service"
            action = "payment.transaction.process"
            category = ActionCategory.EXECUTE

            actor {
                actorType = ActorType.USER
                userId = "user-123"
            }

            resource {
                type = "payment"
                id = "pay-5001"
                displayName = "Payment for order #1234"
            }

            outcome {
                success = false
                errorCode = "INSUFFICIENT_FUNDS"
                errorMessage = "Payment declined: insufficient funds"
                duration = 1200
            }

            metadata(
                "paymentMethod" to "credit_card",
                "amount" to 15000.0,
                "currency" to "RUB"
            )

            tags("payment", "failure")
        }

        println("\nFailure Event:")
        println(serializer.serializePretty(event))
    }

    /**
     * Пример 8: Массовый экспорт данных
     */
    fun bulkExportExample() {
        val actor = Actor(
            actorType = ActorType.USER,
            userId = "user-123",
            roles = listOf("data_analyst")
        )

        val event = AuditEventFactory.createDataExportEvent(
            source = "export-service",
            actor = actor,
            resourceType = "customer",
            resourceIds = (1..1000).map { "cust-$it" },
            format = "CSV"
        )

        println("\nBulk Export Event:")
        println(serializer.serializePretty(event))
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println("=== User Log Library Usage Examples ===\n")

        kotlinDslExample()
        javaBuilderExample()
        factoryExample()
        impersonationExample()
        systemEventExample()
        nestedResourceExample()
        failureEventExample()
        bulkExportExample()

        println("\n=== Examples Complete ===")
    }
}
