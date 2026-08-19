package io.twotle.application

import io.twotle.domain.ConfigurationRepository
import io.twotle.domain.LocatorBar
import io.twotle.domain.NoOpLocatorBar
import io.twotle.domain.NoOpPhantomSpawn
import io.twotle.domain.NoOpTeamSpawnDisplay
import io.twotle.domain.NoOpWorldBorderDisplay
import io.twotle.domain.PhantomSpawn
import io.twotle.domain.PlayerDirectory
import io.twotle.domain.Team
import io.twotle.domain.TeamColor
import io.twotle.domain.TeamDisplay
import io.twotle.domain.TeamMember
import io.twotle.domain.TeamRepository
import io.twotle.domain.TeamSpawnDisplay
import io.twotle.domain.TeamSpawnLayout
import io.twotle.domain.WorldBorderDisplay
import java.util.UUID

class TeamService(
    private val teams: TeamRepository,
    private val players: PlayerDirectory,
    private val configuration: ConfigurationRepository,
    private val display: TeamDisplay,
    private val locatorBar: LocatorBar = NoOpLocatorBar,
    private val phantomSpawn: PhantomSpawn = NoOpPhantomSpawn,
    private val worldBorder: WorldBorderDisplay = NoOpWorldBorderDisplay,
    private val spawns: TeamSpawnDisplay = NoOpTeamSpawnDisplay,
) {
    fun synchronizeDisplay() {
        val allTeams = teams.findAll()
        display.synchronize(allTeams)
        worldBorder.synchronize(configuration.worldBorderRadius())
        spawns.synchronize(allTeams)
        locatorBar.synchronize(configuration.locatorBarEnabled())
        phantomSpawn.synchronize(configuration.phantomSpawnAllowed())
    }

    fun reset() {
        teams.findAll().forEach(display::remove)
        configuration.reset()
        display.reset()
        spawns.reset()
        locatorBar.synchronize(configuration.locatorBarEnabled())
        phantomSpawn.synchronize(configuration.phantomSpawnAllowed())
        worldBorder.synchronize(configuration.worldBorderRadius())
    }

    fun create(
        teamName: String,
        colorName: String,
    ) {
        validateTeamName(teamName)
        val color = TeamColor.fromCommandName(colorName) ?: throw InvalidTeamColor(colorName)
        teams.findByName(teamName)?.let { throw TeamAlreadyExists(teamName) }
        val spawnIndex = (teams.findAll().maxOfOrNull { it.spawnIndex } ?: -1) + 1
        val requiredRadius = TeamSpawnLayout.requiredBorderRadius(spawnIndex)
        if (requiredRadius > configuration.worldBorderRadius()) {
            throw WorldBorderTooSmallForTeam(teamName, requiredRadius)
        }
        Team(teamName, color, spawnIndex = spawnIndex).also {
            teams.save(it)
            display.update(it)
            spawns.update(it)
        }
    }

    fun join(
        teamName: String,
        username: String,
    ) {
        validateTeamName(teamName)
        validateUsername(username)

        val team = getTeam(teamName)
        val member = players.findByUsername(username) ?: throw PlayerNotFound(username)
        teams.findByMember(member.uuid)?.let { currentTeam ->
            throw PlayerAlreadyAssigned(username, currentTeam.name)
        }
        team.add(member).also {
            teams.save(it)
            display.update(it)
            spawns.update(it)
        }
    }

    fun kick(
        teamName: String,
        username: String,
    ) {
        validateTeamName(teamName)
        validateUsername(username)

        val team = getTeam(teamName)
        val member = team.memberNamed(username) ?: throw PlayerNotInTeam(username, team.name)
        team.remove(member).also {
            teams.save(it)
            display.update(it)
            spawns.update(it)
        }
    }

    fun delete(teamName: String) {
        validateTeamName(teamName)
        getTeam(teamName).also {
            teams.delete(it)
            display.remove(it)
            spawns.remove(it)
        }
    }

    fun list(): List<TeamView> = teams.findAll().map { TeamView(it.name, it.color, it.members.size) }

    fun teamNames(): List<String> = teams.findAll().map { it.name }

    fun colorNames(): List<String> = TeamColor.commandNames()

    fun setTeamAttackAllowed(allowed: Boolean) = configuration.saveTeamAttackAllowed(allowed)

    fun setLocatorBarEnabled(enabled: Boolean) {
        configuration.saveLocatorBarEnabled(enabled)
        locatorBar.synchronize(enabled)
    }

    fun setPhantomSpawnAllowed(allowed: Boolean) {
        configuration.savePhantomSpawnAllowed(allowed)
        phantomSpawn.synchronize(allowed)
    }

    fun setWorldBorderRadius(radius: Int) {
        val requiredRadius =
            maxOf(
                MIN_WORLD_BORDER_RADIUS,
                teams.findAll().maxOfOrNull { TeamSpawnLayout.requiredBorderRadius(it.spawnIndex) } ?: 0,
            )
        if (radius !in requiredRadius..MAX_WORLD_BORDER_RADIUS) {
            throw InvalidWorldBorderRadius(radius, requiredRadius, MAX_WORLD_BORDER_RADIUS)
        }
        configuration.saveWorldBorderRadius(radius)
        worldBorder.synchronize(radius)
        spawns.synchronize(teams.findAll())
    }

    fun memberNames(teamName: String): List<String> =
        teams
            .findByName(teamName)
            ?.members
            ?.map { it.username }
            ?.sorted()
            .orEmpty()

    fun onlineUsernames(): List<String> = players.onlineUsernames()

    fun refreshMemberIdentity(
        uuid: UUID,
        username: String,
    ) {
        val team = teams.findByMember(uuid) ?: return
        val currentMember = team.members.first { it.uuid == uuid }
        if (currentMember.username == username) return

        team.remove(currentMember).add(TeamMember(uuid, username)).also {
            teams.save(it)
            display.update(it)
            spawns.update(it)
        }
    }

    private fun getTeam(teamName: String): Team = teams.findByName(teamName) ?: throw TeamNotFound(teamName)

    private fun validateTeamName(teamName: String) {
        if (!TEAM_NAME.matches(teamName)) throw InvalidTeamName()
    }

    private fun validateUsername(username: String) {
        if (!USERNAME.matches(username)) throw InvalidUsername()
    }

    data class TeamView(
        val name: String,
        val color: TeamColor,
        val memberCount: Int,
    )

    private companion object {
        const val MIN_WORLD_BORDER_RADIUS = 64
        const val MAX_WORLD_BORDER_RADIUS = 29_999_984
        val TEAM_NAME = Regex("^[A-Za-z0-9_-]{1,32}$")
        val USERNAME = Regex("^[A-Za-z0-9_]{3,16}$")
    }
}

sealed class TeamServiceException(
    val code: TeamErrorCode,
    val arguments: List<String> = emptyList(),
) : RuntimeException(code.name)

enum class TeamErrorCode {
    INVALID_TEAM_NAME,
    INVALID_USERNAME,
    INVALID_TEAM_COLOR,
    TEAM_ALREADY_EXISTS,
    TEAM_NOT_FOUND,
    PLAYER_NOT_FOUND,
    PLAYER_ALREADY_ASSIGNED,
    PLAYER_NOT_IN_TEAM,
    INVALID_WORLD_BORDER_RADIUS,
    WORLD_BORDER_TOO_SMALL_FOR_TEAM,
}

class InvalidTeamName : TeamServiceException(TeamErrorCode.INVALID_TEAM_NAME)

class InvalidUsername : TeamServiceException(TeamErrorCode.INVALID_USERNAME)

class InvalidTeamColor(
    colorName: String,
) : TeamServiceException(
        TeamErrorCode.INVALID_TEAM_COLOR,
        listOf(colorName),
    )

class TeamAlreadyExists(
    teamName: String,
) : TeamServiceException(
        TeamErrorCode.TEAM_ALREADY_EXISTS,
        listOf(teamName),
    )

class TeamNotFound(
    teamName: String,
) : TeamServiceException(
        TeamErrorCode.TEAM_NOT_FOUND,
        listOf(teamName),
    )

class PlayerNotFound(
    username: String,
) : TeamServiceException(
        TeamErrorCode.PLAYER_NOT_FOUND,
        listOf(username),
    )

class PlayerAlreadyAssigned(
    username: String,
    teamName: String,
) : TeamServiceException(
        TeamErrorCode.PLAYER_ALREADY_ASSIGNED,
        listOf(username, teamName),
    )

class PlayerNotInTeam(
    username: String,
    teamName: String,
) : TeamServiceException(
        TeamErrorCode.PLAYER_NOT_IN_TEAM,
        listOf(username, teamName),
    )

class InvalidWorldBorderRadius(
    radius: Int,
    minimum: Int,
    maximum: Int,
) : TeamServiceException(
        TeamErrorCode.INVALID_WORLD_BORDER_RADIUS,
        listOf(radius.toString(), minimum.toString(), maximum.toString()),
    )

class WorldBorderTooSmallForTeam(
    teamName: String,
    requiredRadius: Int,
) : TeamServiceException(
        TeamErrorCode.WORLD_BORDER_TOO_SMALL_FOR_TEAM,
        listOf(teamName, requiredRadius.toString()),
    )
