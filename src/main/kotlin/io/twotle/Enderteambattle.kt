package io.twotle

import io.twotle.application.GameService
import io.twotle.application.TeamService
import io.twotle.infrastructure.BukkitGameAnnouncer
import io.twotle.infrastructure.BukkitPlayerDirectory
import io.twotle.infrastructure.DragonDefeatListener
import io.twotle.infrastructure.PausedPlayerListener
import io.twotle.infrastructure.PlayerIdentityListener
import io.twotle.infrastructure.ScoreboardTeamDisplay
import io.twotle.infrastructure.YamlDataStore
import io.twotle.presentation.command.EtbCommand
import io.twotle.presentation.command.createCommandTree
import org.bukkit.plugin.java.JavaPlugin

class Enderteambattle : JavaPlugin() {
    override fun onEnable() {
        val dataStore = YamlDataStore(this).also { it.initialize() }
        val teamService = TeamService(
            teams = dataStore,
            players = BukkitPlayerDirectory(),
            configuration = dataStore,
            display = ScoreboardTeamDisplay(),
        ).also { it.synchronizeDisplay() }
        val gameService = GameService(
            games = dataStore,
            teams = dataStore,
            announcer = BukkitGameAnnouncer(),
        )
        val etbCommand = EtbCommand(createCommandTree(teamService, gameService))

        listOf(
            PlayerIdentityListener(teamService),
            DragonDefeatListener(gameService),
            PausedPlayerListener(gameService),
        ).forEach { server.pluginManager.registerEvents(it, this) }

        requireNotNull(getCommand("etb")) {
            "The etb command is not registered in plugin.yml."
        }.apply {
            setExecutor(etbCommand)
            tabCompleter = etbCommand
        }
    }
}
