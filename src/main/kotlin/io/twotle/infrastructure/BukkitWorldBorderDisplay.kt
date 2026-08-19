package io.twotle.infrastructure

import io.twotle.domain.ConfigurationRepository
import io.twotle.domain.WorldBorderDisplay
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent

class BukkitWorldBorderDisplay(
    private val configuration: ConfigurationRepository,
) : WorldBorderDisplay, Listener {
    override fun synchronize(radius: Int) {
        Bukkit.getWorlds().forEach { apply(it, radius) }
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        apply(event.world, configuration.worldBorderRadius())
    }

    private fun apply(
        world: World,
        radius: Int,
    ) {
        world.worldBorder.apply {
            setCenter(0.0, 0.0)
            size = radius * 2.0
        }
    }
}
