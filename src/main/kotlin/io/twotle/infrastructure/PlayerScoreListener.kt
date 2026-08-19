package io.twotle.infrastructure

import io.twotle.application.GameService
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerScoreListener(
    private val gameService: GameService,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        gameService.recordPlayerDeath(
            victimUuid = event.player.uniqueId,
            killerUuid = event.player.killer?.uniqueId,
        )
    }
}
