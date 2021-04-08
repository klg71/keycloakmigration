package de.klg71.keycloakmigration.keycloakapi.model

import java.util.UUID

data class Flow(val id: UUID,
    val alias: String,
    val description: String,
    val providerId: String,
    val topLevel: Boolean,
    val builtIn: Boolean,
    val authenticationExecutions: List<AuthenticationExecution>) {
    enum class Requirement {
        ALTERNATIVE, DISABLED, REQUIRED, CONDITIONAL
    }

    data class AuthenticationExecution(
        val authenticator: String?,
        val requirement: Requirement,
        val priority: Int,
        val userSetupAllowed: Boolean,
        // This looks very weird but they seem to have this in their codebase, i will assume it means authenticatorFlow
        @Suppress("SpellCheckingInspection")
        val autheticatorFlow: Boolean)
}

data class AuthenticationExecution(
    val id: UUID,
    val requirement: Flow.Requirement,
    val displayName: String,
    val requirementChoices: List<Flow.Requirement>,
    val configurable: Boolean,
    val providerId: String,
    val level: Int,
    val index: Int)

data class AddFlow(val alias: String,
    val builtIn: Boolean,
    val description: String,
    val providerId: String,
    val topLevel: Boolean)

data class UpdateFlow(
    val alias: String,
    val description: String,
)
data class AuthenticationExecutionImport(
    val requirement: Flow.Requirement,
    val providerId: String,
    val level: Int,
    val index: Int)

data class ImportFlow(
    val alias: String,
    val description: String,
    val providerId: String,
    val topLevel: Boolean,
    val buildIn: Boolean,
    val authenticationExecutions: List<AuthenticationExecutionImport>
)


data class AddFlowExecution(val provider: String)
data class UpdateFlowExecution(val id: UUID,
    val requirement: Flow.Requirement,
    val level: Int,
    val index: Int,
    val providerId: String)