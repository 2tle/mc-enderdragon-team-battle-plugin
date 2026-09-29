package io.twotle.application

import io.twotle.domain.Administrator
import io.twotle.domain.AdministratorRepository
import io.twotle.domain.PlayerDirectory
import io.twotle.domain.TeamMember
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdministratorServiceTest {
    private val repository = InMemoryAdministratorRepository()
    private val players = AdministratorPlayerDirectory()
    private val service = AdministratorService(repository, players)

    @Test
    fun `adding a player grants access by uuid`() {
        val player = players.add("Steve")

        service.add("Steve")

        assertTrue(service.hasAccess(player.uuid))
        assertEquals(listOf("Steve"), service.usernames())
    }

    @Test
    fun `adding the same administrator twice is rejected`() {
        players.add("Steve")
        service.add("Steve")

        assertFailsWith<AdministratorAlreadyExists> { service.add("steve") }
    }

    @Test
    fun `removing an administrator revokes access`() {
        val player = players.add("Alex")
        service.add("Alex")

        service.remove("alex")

        assertFalse(service.hasAccess(player.uuid))
    }

    @Test
    fun `removing an unknown administrator is rejected`() {
        assertFailsWith<AdministratorNotFound> { service.remove("Steve") }
    }
}

private class InMemoryAdministratorRepository : AdministratorRepository {
    private val values = mutableListOf<Administrator>()

    override fun findAdministrator(uuid: UUID): Administrator? = values.firstOrNull { it.uuid == uuid }

    override fun findAdministrator(username: String): Administrator? =
        values.firstOrNull { it.username.equals(username, ignoreCase = true) }

    override fun administrators(): List<Administrator> = values.toList()

    override fun saveAdministrator(administrator: Administrator) {
        values += administrator
    }

    override fun deleteAdministrator(administrator: Administrator) {
        values.remove(administrator)
    }
}

private class AdministratorPlayerDirectory : PlayerDirectory {
    private val players = mutableMapOf<String, TeamMember>()

    fun add(username: String): TeamMember =
        TeamMember(UUID.randomUUID(), username).also { players[username.lowercase()] = it }

    override fun findByUsername(username: String): TeamMember? = players[username.lowercase()]

    override fun onlineUsernames(): List<String> = players.values.map { it.username }
}
