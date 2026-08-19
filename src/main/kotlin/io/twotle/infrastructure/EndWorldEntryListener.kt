package io.twotle.infrastructure

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent

class EndWorldEntryListener : Listener {
    @EventHandler
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        if (event.from.environment == World.Environment.THE_END) return
        if (event.player.world.environment != World.Environment.THE_END) return

        Bukkit.broadcast(
            Component.text("[EnderTeamBattle] ", NamedTextColor.GOLD)
                .append(Component.text(event.player.name, NamedTextColor.YELLOW))
                .append(Component.text(" has entered the End!", NamedTextColor.GOLD)),
        )
    }
}
