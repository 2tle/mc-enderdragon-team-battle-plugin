package io.twotle.infrastructure

import io.twotle.domain.GameAnnouncer
import io.twotle.domain.GameEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit

class BukkitGameAnnouncer : GameAnnouncer {
    override fun announce(event: GameEvent) {
        Bukkit.broadcast(message(event))
    }

    private fun message(event: GameEvent): Component = when (event) {
        GameEvent.Started -> Component.text(
            "[EnderTeamBattle] The game has started! The first team to defeat the Ender Dragon wins.",
            NamedTextColor.GREEN,
        )
        GameEvent.Resumed -> Component.text(
            "[EnderTeamBattle] The game has resumed.",
            NamedTextColor.GREEN,
        )
        GameEvent.Paused -> Component.text(
            "[EnderTeamBattle] The game has been paused.",
            NamedTextColor.YELLOW,
        )
        GameEvent.StoppedAsDraw -> Component.text(
            "[EnderTeamBattle] The game has been stopped. The result is a draw.",
            NamedTextColor.RED,
        )
        is GameEvent.Won -> Component.text("[EnderTeamBattle] ", NamedTextColor.GOLD)
            .append(Component.text("[${event.team.name}]", BukkitTeamColor[event.team.color]))
            .append(Component.text(" is the first team to defeat the Ender Dragon and wins!", NamedTextColor.GOLD))
    }
}
