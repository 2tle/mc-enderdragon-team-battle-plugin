package io.twotle.infrastructure

import io.twotle.domain.ConfigurationRepository
import io.twotle.domain.PhantomSpawn
import org.bukkit.Bukkit
import org.bukkit.GameRules
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent

class BukkitPhantomSpawn(
    private val configuration: ConfigurationRepository,
) : PhantomSpawn, Listener {
    override fun synchronize(allowed: Boolean) {
        Bukkit.getWorlds().forEach { it.setGameRule(GameRules.SPAWN_PHANTOMS, allowed) }
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        event.world.setGameRule(GameRules.SPAWN_PHANTOMS, configuration.phantomSpawnAllowed())
    }
}
