package de.klg71.keycloakmigration.rest

import de.klg71.keycloakmigration.changeControl.KeycloakException
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.ClientListItem
import de.klg71.keycloakmigration.model.GroupListItem
import de.klg71.keycloakmigration.model.Role
import feign.Response
import java.util.*

fun KeycloakClient.userByName(name: String, realm: String) =
        searchByUsername(name, realm)
                .run {
                    if (isEmpty()) {
                        throw MigrationException("User with name: $name does not exist in $realm")
                    }
                    first()
                }
                .run {
                    user(id, realm)
                }

fun KeycloakClient.clientById(clientId: String, realm: String): ClientListItem =
        clients(realm)
                .run {
                    if (isEmpty()) {
                        throw MigrationException("User with name: $clientId does not exist in $realm")
                    }
                    find { it.clientId == clientId }.let {
                        it ?: throw MigrationException("Client with name $clientId does not exist in realm: $realm!")
                    }
                }

fun KeycloakClient.groupByName(name: String, realm: String) =
        searchGroup(name, realm)
                .run {
                    if (isEmpty()) {
                        throw MigrationException("Group with name: $name does not exist in realm: $realm!")
                    }
                    searchByName(name) ?: throw MigrationException("Group with name: $name does not exist in $realm")
                }

fun KeycloakClient.existsGroup(name: String, realm: String): Boolean =
        searchGroup(name, realm)
                .run {
                    if (isEmpty()) {
                        return false
                    }
                    if (searchByName(name) == null) {
                        return false
                    }
                    return true
                }

private fun List<GroupListItem>.searchByName(name: String): GroupListItem? {
    return firstOrNull { it.name == name } ?: map { it.subGroups.searchByName(name) }.first()
}

fun KeycloakClient.clientRoleByName(name: String, clientId: String, realm: String): Role =
        clientById(clientId, realm)
                .run {
                    clientRoles(realm, id)
                }.run {
                    find { it.name == name }.let {
                        if (it == null) {
                            throw MigrationException("Role with name: $name does not exist on client $clientId on realm $realm!")
                        }
                        clientRole(it.id, realm, UUID.fromString(it.containerId))
                    }
                }

fun KeycloakClient.userUUID(user: String, realm: String) = userByName(user, realm).id

fun KeycloakClient.clientUUID(clientId: String, realm: String) = clientById(clientId, realm).id

fun Response.isSuccessful() = when (status()) {
    in 200..299 -> true
    else -> false
}

fun Response.extractLocationUUID(): UUID {
    if (!isSuccessful()) {
        throw KeycloakException(this.body().asReader().readText())
    }
    return headers()["location"]!!.first()
            .run {
                split("/").last()
            }.let {
                UUID.fromString(it)
            }
}

fun KeycloakClient.realmById(id: String) =
        realms().firstOrNull() { it.id == id } ?: throw MigrationException("Realm with id: $id does not exist!")

fun KeycloakClient.realmExistsById(id: String) = realms().any() { it.id == id }