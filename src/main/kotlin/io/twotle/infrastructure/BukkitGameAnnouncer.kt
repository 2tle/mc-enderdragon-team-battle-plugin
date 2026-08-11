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
            "[EnderTeamBattle] 게임이 시작되었습니다! 엔더 드래곤을 최초로 처치한 팀이 승리합니다.",
            NamedTextColor.GREEN,
        )
        GameEvent.Resumed -> Component.text(
            "[EnderTeamBattle] 일시정지된 게임을 재개합니다.",
            NamedTextColor.GREEN,
        )
        GameEvent.Paused -> Component.text(
            "[EnderTeamBattle] 게임이 일시정지되었습니다.",
            NamedTextColor.YELLOW,
        )
        GameEvent.StoppedAsDraw -> Component.text(
            "[EnderTeamBattle] 게임이 강제 종료되었습니다. 결과는 무승부입니다.",
            NamedTextColor.RED,
        )
        is GameEvent.Won -> Component.text("[EnderTeamBattle] ", NamedTextColor.GOLD)
            .append(Component.text("[${event.team.name}]", BukkitTeamColor[event.team.color]))
            .append(Component.text(" 팀이 엔더 드래곤을 최초로 처치하여 승리했습니다!", NamedTextColor.GOLD))
    }
}
