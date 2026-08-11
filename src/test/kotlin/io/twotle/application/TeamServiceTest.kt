package io.twotle.application

import io.twotle.domain.ConfigurationRepository
import io.twotle.domain.PlayerDirectory
import io.twotle.domain.Team
import io.twotle.domain.TeamColor
import io.twotle.domain.TeamDisplay
import io.twotle.domain.TeamMember
import io.twotle.domain.TeamRepository
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TeamServiceTest {
    private val repository = InMemoryTeamRepository()
    private val players = InMemoryPlayerDirectory()
    private val configuration = InMemoryConfigurationRepository()
    private val display = InMemoryTeamDisplay()
    private val service = TeamService(repository, players, configuration, display)

    @Test
    fun `team creation rejects duplicate names regardless of case`() {
        service.create("Red", "red")

        assertFailsWith<TeamAlreadyExists> { service.create("red", "blue") }
        assertEquals(listOf("Red"), service.teamNames())
    }

    @Test
    fun `a player can belong to only one team`() {
        players.add("Steve")
        service.create("Red", "red")
        service.create("Blue", "blue")
        service.join("Red", "Steve")

        val exception = assertFailsWith<PlayerAlreadyAssigned> {
            service.join("Blue", "Steve")
        }
        assertEquals(listOf("Steve", "Red"), exception.arguments)
    }

    @Test
    fun `kicking a member and deleting a team remove membership`() {
        val member = players.add("Alex")
        service.create("Red", "red")
        service.join("Red", "Alex")
        service.kick("Red", "Alex")

        assertEquals(null, repository.findByMember(member.uuid))

        service.join("Red", "Alex")
        service.delete("Red")
        assertEquals(null, repository.findByMember(member.uuid))
        assertTrue(service.list().isEmpty())
    }

    @Test
    fun `reset delegates to the configuration repository`() {
        service.reset()

        assertTrue(configuration.wasReset)
        assertTrue(display.wasReset)
    }

    @Test
    fun `team color and member changes are reflected in the display`() {
        players.add("Steve")

        service.create("Red", "red")
        service.join("Red", "Steve")

        assertEquals(TeamColor.RED, display.updatedTeam?.color)
        assertEquals(listOf("Steve"), display.updatedTeam?.members?.map { it.username })
    }
}

private class InMemoryTeamRepository : TeamRepository {
    private val teams = mutableListOf<Team>()

    override fun findByName(name: String): Team? =
        teams.firstOrNull { it.name.equals(name, ignoreCase = true) }

    override fun findByMember(uuid: UUID): Team? = teams.firstOrNull { it.contains(uuid) }

    override fun findAll(): List<Team> = teams.toList()

    override fun save(team: Team) {
        teams.removeAll { it.name.equals(team.name, ignoreCase = true) }
        teams += team
    }

    override fun delete(team: Team) {
        teams.removeAll { it.name.equals(team.name, ignoreCase = true) }
    }
}

private class InMemoryPlayerDirectory : PlayerDirectory {
    private val players = mutableMapOf<String, TeamMember>()

    fun add(username: String): TeamMember =
        TeamMember(UUID.randomUUID(), username).also { players[username.lowercase()] = it }

    override fun findByUsername(username: String): TeamMember? = players[username.lowercase()]

    override fun onlineUsernames(): List<String> = players.values.map { it.username }
}

private class InMemoryTeamDisplay : TeamDisplay {
    var updatedTeam: Team? = null
        private set
    var wasReset = false
        private set

    override fun synchronize(teams: List<Team>) = Unit

    override fun update(team: Team) {
        updatedTeam = team
    }

    override fun remove(team: Team) = Unit

    override fun reset() {
        wasReset = true
    }
}

private class InMemoryConfigurationRepository : ConfigurationRepository {
    var wasReset = false
        private set

    override fun initialize() = Unit

    override fun reset() {
        wasReset = true
    }
}
