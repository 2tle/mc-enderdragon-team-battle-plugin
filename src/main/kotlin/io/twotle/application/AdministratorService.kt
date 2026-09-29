package io.twotle.application

import io.twotle.domain.Administrator
import io.twotle.domain.AdministratorRepository
import io.twotle.domain.PlayerDirectory
import java.util.UUID

class AdministratorService(
    private val administrators: AdministratorRepository,
    private val players: PlayerDirectory,
) {
    fun hasAccess(uuid: UUID): Boolean = administrators.findAdministrator(uuid) != null

    fun add(username: String): Administrator {
        validateUsername(username)
        administrators.findAdministrator(username)?.let { throw AdministratorAlreadyExists(it.username) }
        val player = players.findByUsername(username) ?: throw PlayerNotFound(username)
        administrators.findAdministrator(player.uuid)?.let { throw AdministratorAlreadyExists(it.username) }
        return Administrator(player.uuid, player.username).also(administrators::saveAdministrator)
    }

    fun remove(username: String): Administrator {
        validateUsername(username)
        val administrator = administrators.findAdministrator(username) ?: throw AdministratorNotFound(username)
        administrators.deleteAdministrator(administrator)
        return administrator
    }

    fun list(): List<Administrator> = administrators.administrators()

    fun usernames(): List<String> = list().map { it.username }

    fun onlineUsernames(): List<String> = players.onlineUsernames()

    private fun validateUsername(username: String) {
        if (!USERNAME.matches(username)) throw InvalidUsername()
    }

    private companion object {
        val USERNAME = Regex("^[A-Za-z0-9_]{3,16}$")
    }
}

sealed class AdministratorServiceException(message: String) : RuntimeException(message)

class AdministratorAlreadyExists(val username: String) :
    AdministratorServiceException("Player '$username' is already an EnderTeamBattle administrator.")

class AdministratorNotFound(val username: String) :
    AdministratorServiceException("Player '$username' is not an EnderTeamBattle administrator.")
