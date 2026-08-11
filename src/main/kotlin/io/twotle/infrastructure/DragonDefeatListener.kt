package io.twotle.infrastructure

import io.twotle.application.GameService
import org.bukkit.entity.EnderDragon
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class DragonDefeatListener(
    private val gameService: GameService,
) : Listener {
    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val dragon = event.entity as? EnderDragon ?: return
        val killer = dragon.killer ?: return
        gameService.onDragonDefeated(killer.uniqueId)
    }
}
