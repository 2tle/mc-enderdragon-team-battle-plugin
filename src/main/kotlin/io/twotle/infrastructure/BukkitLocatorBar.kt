package io.twotle.infrastructure

import io.twotle.domain.ConfigurationRepository
import io.twotle.domain.LocatorBar
import org.bukkit.Bukkit
import org.bukkit.GameRules
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent

class BukkitLocatorBar(
    private val configuration: ConfigurationRepository,
) : LocatorBar, Listener {
    override fun synchronize(enabled: Boolean) {
        Bukkit.getWorlds().forEach { it.setGameRule(GameRules.LOCATOR_BAR, enabled) }
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        event.world.setGameRule(GameRules.LOCATOR_BAR, configuration.locatorBarEnabled())
    }
}
