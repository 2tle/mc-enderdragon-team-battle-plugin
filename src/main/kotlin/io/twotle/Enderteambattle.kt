package io.twotle

import io.twotle.application.GameService
import io.twotle.application.TeamService
import io.twotle.infrastructure.BukkitGameAnnouncer
import io.twotle.infrastructure.BukkitLocatorBar
import io.twotle.infrastructure.BukkitPhantomSpawn
import io.twotle.infrastructure.BukkitPlayerDirectory
import io.twotle.infrastructure.BukkitTeamSpawnDisplay
import io.twotle.infrastructure.BukkitWorldBorderDisplay
import io.twotle.infrastructure.DragonDefeatListener
import io.twotle.infrastructure.EndWorldEntryListener
import io.twotle.infrastructure.PausedPlayerListener
import io.twotle.infrastructure.PlayerIdentityListener
import io.twotle.infrastructure.PlayerScoreListener
import io.twotle.infrastructure.ScoreboardTeamDisplay
import io.twotle.infrastructure.TeamAttackListener
import io.twotle.infrastructure.YamlDataStore
import io.twotle.presentation.command.EtbCommand
import io.twotle.presentation.command.createCommandTree
import org.bukkit.plugin.java.JavaPlugin

class Enderteambattle : JavaPlugin() {
    override fun onEnable() {
        val dataStore = YamlDataStore(this).also { it.initialize() }
        val locatorBar = BukkitLocatorBar(dataStore)
        val phantomSpawn = BukkitPhantomSpawn(dataStore)
        val worldBorder = BukkitWorldBorderDisplay(dataStore)
        val teamSpawns = BukkitTeamSpawnDisplay()
        val teamService =
            TeamService(
                teams = dataStore,
                players = BukkitPlayerDirectory(),
                configuration = dataStore,
                display = ScoreboardTeamDisplay(),
                locatorBar = locatorBar,
                phantomSpawn = phantomSpawn,
                worldBorder = worldBorder,
                spawns = teamSpawns,
            ).also { it.synchronizeDisplay() }
        val gameService =
            GameService(
                games = dataStore,
                teams = dataStore,
                announcer = BukkitGameAnnouncer(),
            )
        val etbCommand = EtbCommand(createCommandTree(teamService, gameService))

        listOf(
            PlayerIdentityListener(teamService),
            PlayerScoreListener(gameService),
            locatorBar,
            phantomSpawn,
            worldBorder,
            teamSpawns,
            EndWorldEntryListener(),
            DragonDefeatListener(gameService),
            PausedPlayerListener(gameService),
            TeamAttackListener(dataStore, dataStore),
        ).forEach { server.pluginManager.registerEvents(it, this) }

        requireNotNull(getCommand("etb")) {
            "The etb command is not registered in plugin.yml."
        }.apply {
            setExecutor(etbCommand)
            tabCompleter = etbCommand
        }
    }
}
