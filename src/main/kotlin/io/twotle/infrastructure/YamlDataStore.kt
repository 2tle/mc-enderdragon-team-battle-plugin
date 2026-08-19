package io.twotle.infrastructure

import io.twotle.Enderteambattle
import io.twotle.domain.ConfigurationRepository
import io.twotle.domain.GameRepository
import io.twotle.domain.GameStatus
import io.twotle.domain.Team
import io.twotle.domain.TeamColor
import io.twotle.domain.TeamMember
import io.twotle.domain.TeamRepository
import io.twotle.domain.TeamScore
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
        migrateTeamSpawnIndices()
        migrateGameStatus()
        migrateTeamAttack()
        migrateLocatorBar()
        migratePhantomSpawn()
        migrateWorldBorder()
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

    override fun scores(): Map<String, TeamScore> =
        config
            .getConfigurationSection(GAME_SCORES_PATH)
            ?.getKeys(false)
            ?.associateWith { key ->
                TeamScore(
                    kills = config.getInt("$GAME_SCORES_PATH.$key.kills"),
                    deaths = config.getInt("$GAME_SCORES_PATH.$key.deaths"),
                )
            }
            .orEmpty()

    override fun saveScores(scores: Map<String, TeamScore>) {
        config.set(GAME_SCORES_PATH, null)
        scores.forEach { (teamName, score) ->
            val path = "$GAME_SCORES_PATH.${teamName.lowercase(Locale.ROOT)}"
            config.set("$path.kills", score.kills)
            config.set("$path.deaths", score.deaths)
        }
        plugin.saveConfig()
    }

    override fun clearScores() {
        config.set(GAME_SCORES_PATH, null)
        plugin.saveConfig()
    }

    override fun teamAttackAllowed(): Boolean =
        if (config.contains(TEAM_ATTACK_ALLOWED_PATH)) config.getBoolean(TEAM_ATTACK_ALLOWED_PATH) else true

    override fun saveTeamAttackAllowed(allowed: Boolean) {
        config.set(TEAM_ATTACK_ALLOWED_PATH, allowed)
        plugin.saveConfig()
    }

    override fun locatorBarEnabled(): Boolean =
        if (config.contains(LOCATOR_BAR_ENABLED_PATH)) config.getBoolean(LOCATOR_BAR_ENABLED_PATH) else true

    override fun saveLocatorBarEnabled(enabled: Boolean) {
        config.set(LOCATOR_BAR_ENABLED_PATH, enabled)
        plugin.saveConfig()
    }

    override fun phantomSpawnAllowed(): Boolean =
        if (config.contains(PHANTOM_SPAWN_ALLOWED_PATH)) config.getBoolean(PHANTOM_SPAWN_ALLOWED_PATH) else true

    override fun savePhantomSpawnAllowed(allowed: Boolean) {
        config.set(PHANTOM_SPAWN_ALLOWED_PATH, allowed)
        plugin.saveConfig()
    }

    override fun worldBorderRadius(): Int =
        if (config.contains(WORLD_BORDER_RADIUS_PATH)) {
            config.getInt(WORLD_BORDER_RADIUS_PATH)
        } else {
            DEFAULT_WORLD_BORDER_RADIUS
        }

    override fun saveWorldBorderRadius(radius: Int) {
        config.set(WORLD_BORDER_RADIUS_PATH, radius)
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
        config.set("$path.spawn-index", team.spawnIndex)
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
        return Team(name, color, members, config.getInt("$path.spawn-index"))
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

    private fun migrateTeamSpawnIndices() {
        var nextIndex =
            teamsSection()
                ?.getKeys(false)
                ?.mapNotNull { key ->
                    "$TEAMS_PATH.$key.spawn-index"
                        .takeIf(config::contains)
                        ?.let(config::getInt)
                }
                ?.maxOrNull()
                ?.plus(1)
                ?: 0
        teamsSection()
            ?.getKeys(false)
            ?.sorted()
            ?.forEach { teamKey ->
                val path = "$TEAMS_PATH.$teamKey.spawn-index"
                if (!config.contains(path)) config.set(path, nextIndex++)
            }
    }

    private fun migrateTeamAttack() {
        if (!config.contains(TEAM_ATTACK_ALLOWED_PATH)) config.set(TEAM_ATTACK_ALLOWED_PATH, true)
    }

    private fun migrateLocatorBar() {
        if (!config.contains(LOCATOR_BAR_ENABLED_PATH)) config.set(LOCATOR_BAR_ENABLED_PATH, true)
    }

    private fun migratePhantomSpawn() {
        if (!config.contains(PHANTOM_SPAWN_ALLOWED_PATH)) config.set(PHANTOM_SPAWN_ALLOWED_PATH, true)
    }

    private fun migrateWorldBorder() {
        if (!config.contains(WORLD_BORDER_RADIUS_PATH)) {
            config.set(WORLD_BORDER_RADIUS_PATH, DEFAULT_WORLD_BORDER_RADIUS)
        }
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
        const val DATA_VERSION = 6
        const val DEFAULT_WORLD_BORDER_RADIUS = 29_999_984
        const val DATA_VERSION_PATH = "data-version"
        const val GAME_STATUS_PATH = "game.status"
        const val GAME_SCORES_PATH = "game.scores"
        const val TEAM_ATTACK_ALLOWED_PATH = "options.team-attack-allowed"
        const val LOCATOR_BAR_ENABLED_PATH = "options.locator-bar-enabled"
        const val PHANTOM_SPAWN_ALLOWED_PATH = "options.phantom-spawn-allowed"
        const val WORLD_BORDER_RADIUS_PATH = "options.world-border-radius"
        const val TEAMS_PATH = "teams"
    }
}
