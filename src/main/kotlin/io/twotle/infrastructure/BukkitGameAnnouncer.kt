package io.twotle.infrastructure

import io.twotle.domain.GameAnnouncer
import io.twotle.domain.GameEvent
import io.twotle.domain.TeamStanding
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
        is GameEvent.StoppedAsDraw ->
            Component.text(
                "[EnderTeamBattle] The game has been stopped. The highest score is tied, so the result is a draw.",
                NamedTextColor.YELLOW,
            ).append(standings(event.standings))
        is GameEvent.WonByScore ->
            Component.text("[EnderTeamBattle] ", NamedTextColor.GOLD)
                .append(Component.text("[${event.team.name}]", BukkitTeamColor[event.team.color]))
                .append(Component.text(" wins with the highest score!", NamedTextColor.GOLD))
                .append(standings(event.standings))
        is GameEvent.Won -> Component.text("[EnderTeamBattle] ", NamedTextColor.GOLD)
            .append(Component.text("[${event.team.name}]", BukkitTeamColor[event.team.color]))
            .append(Component.text(" is the first team to defeat the Ender Dragon and wins!", NamedTextColor.GOLD))
    }

    private fun standings(standings: List<TeamStanding>): Component =
        standings.fold(Component.text("\nFinal scores:", NamedTextColor.GRAY)) { message, standing ->
            message
                .append(Component.text("\n- ", NamedTextColor.GRAY))
                .append(Component.text("[${standing.team.name}]", BukkitTeamColor[standing.team.color]))
                .append(
                    Component.text(
                        " ${standing.score.kills} kills - ${standing.score.deaths} deaths = ${standing.score.points}",
                        NamedTextColor.GRAY,
                    ),
                )
        }
}
