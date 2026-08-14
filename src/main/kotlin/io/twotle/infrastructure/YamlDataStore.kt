package io.twotle.infrastructure

import io.twotle.Enderteambattle
import io.twotle.domain.ConfigurationRepository
import io.twotle.domain.GameRepository
import io.twotle.domain.GameStatus
import io.twotle.domain.Team
import io.twotle.domain.TeamColor
import io.twotle.domain.TeamMember
import io.twotle.domain.TeamRepository
import org.bukkit.configuration.ConfigurationSection
import java.util.Locale
import java.util.UUID

class YamlDataStore(
    private val plugin: Enderteambattle,
) : TeamRepository,
    ConfigurationRepository,
    GameRepository {
    private val config
        get() = plugin.config

    override fun initialize() {
        if (config.getInt(DATA_VERSION_PATH) == DATA_VERSION) return
        migrateTeamColors()
        migrateGameStatus()
        migrateTeamAttack()
        config.set(DATA_VERSION_PATH, DATA_VERSION)
        plugin.saveConfig()
    }

    override fun reset() {
        config.getKeys(false).toList().forEach { config.set(it, null) }
        config.set(DATA_VERSION_PATH, DATA_VERSION)
        plugin.saveConfig()
    }

    override fun status(): GameStatus =
        config
            .getString(GAME_STATUS_PATH)
            ?.let { runCatching { GameStatus.valueOf(it) }.getOrNull() }
            ?: GameStatus.IDLE

    override fun saveStatus(status: GameStatus) {
        config.set(GAME_STATUS_PATH, status.name)
        plugin.saveConfig()
    }

    override fun teamAttackAllowed(): Boolean =
        if (config.contains(TEAM_ATTACK_ALLOWED_PATH)) config.getBoolean(TEAM_ATTACK_ALLOWED_PATH) else true

    override fun saveTeamAttackAllowed(allowed: Boolean) {
        config.set(TEAM_ATTACK_ALLOWED_PATH, allowed)
        plugin.saveConfig()
    }

    override fun findByName(name: String): Team? =
        teamsSection()
            ?.getKeys(false)
            ?.asSequence()
            ?.mapNotNull(::readTeam)
            ?.firstOrNull { it.name.equals(name, ignoreCase = true) }

    override fun findByMember(uuid: UUID): Team? = findAll().firstOrNull { it.contains(uuid) }

    override fun findAll(): List<Team> =
        teamsSection()
            ?.getKeys(false)
            ?.mapNotNull(::readTeam)
            .orEmpty()
            .sortedBy { it.name.lowercase(Locale.ROOT) }

    override fun save(team: Team) {
        val path = teamPath(team.name)
        config.set(path, null)
        config.set("$path.name", team.name)
        config.set("$path.color", team.color.commandName)
        team.members.forEach { member ->
            config.set("$path.members.${member.uuid}", member.username)
        }
        plugin.saveConfig()
    }

    override fun delete(team: Team) {
        config.set(teamPath(team.name), null)
        plugin.saveConfig()
    }

    private fun readTeam(key: String): Team? {
        val path = "$TEAMS_PATH.$key"
        val name = config.getString("$path.name") ?: return null
        val color =
            config
                .getString("$path.color")
                ?.let(TeamColor::fromCommandName)
                ?: TeamColor.WHITE
        val members =
            config
                .getConfigurationSection("$path.members")
                ?.getKeys(false)
                ?.mapNotNull { memberKey -> readMember(path, memberKey) }
                .orEmpty()
        return Team(name, color, members)
    }

    private fun migrateGameStatus() {
        if (!config.contains(GAME_STATUS_PATH)) {
            config.set(GAME_STATUS_PATH, GameStatus.IDLE.name)
        }
    }

    private fun migrateTeamColors() {
        teamsSection()?.getKeys(false)?.forEach { teamKey ->
            val colorPath = "$TEAMS_PATH.$teamKey.color"
            if (!config.contains(colorPath)) config.set(colorPath, TeamColor.WHITE.commandName)
        }
    }

    private fun migrateTeamAttack() {
        if (!config.contains(TEAM_ATTACK_ALLOWED_PATH)) config.set(TEAM_ATTACK_ALLOWED_PATH, true)
    }

    private fun readMember(
        teamPath: String,
        memberKey: String,
    ): TeamMember? =
        runCatching {
            TeamMember(
                uuid = UUID.fromString(memberKey),
                username = requireNotNull(config.getString("$teamPath.members.$memberKey")),
            )
        }.getOrNull()

    private fun teamsSection(): ConfigurationSection? = config.getConfigurationSection(TEAMS_PATH)

    private fun teamPath(teamName: String): String = "$TEAMS_PATH.${teamName.lowercase(Locale.ROOT)}"

    private companion object {
        const val DATA_VERSION = 4
        const val DATA_VERSION_PATH = "data-version"
        const val GAME_STATUS_PATH = "game.status"
        const val TEAM_ATTACK_ALLOWED_PATH = "options.team-attack-allowed"
        const val TEAMS_PATH = "teams"
    }
}
