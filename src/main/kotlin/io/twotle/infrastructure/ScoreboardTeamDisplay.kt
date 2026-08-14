package io.twotle.infrastructure

import io.twotle.domain.Team
import io.twotle.domain.TeamDisplay
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.scoreboard.Scoreboard

class ScoreboardTeamDisplay : TeamDisplay {
    private val scoreboard: Scoreboard
        get() = Bukkit.getScoreboardManager().mainScoreboard

    override fun synchronize(teams: List<Team>) {
        reset()
        teams.forEach(::update)
    }

    override fun update(team: Team) {
        val scoreboardTeam =
            scoreboard.getTeam(team.name)
                ?: scoreboard.registerNewTeam(team.name)
        val color = BukkitTeamColor[team.color]

        scoreboardTeam.displayName(Component.text(team.name, color))
        scoreboardTeam.prefix(Component.text("[${team.name}] ", color))
        scoreboardTeam.color(color)
        scoreboardTeam.setOption(
            org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY,
            org.bukkit.scoreboard.Team.OptionStatus.ALWAYS,
        )
        scoreboardTeam.entries.toList().forEach(scoreboardTeam::removeEntry)
        team.members.forEach { scoreboardTeam.addEntry(it.username) }
    }

    override fun remove(team: Team) {
        scoreboard.getTeam(team.name)?.unregister()
    }

    override fun reset() {
        scoreboard.teams
            .filter { it.name.startsWith(SCOREBOARD_PREFIX) }
            .forEach { it.unregister() }
    }

    private companion object {
        const val SCOREBOARD_PREFIX = "etb_"
    }
}
