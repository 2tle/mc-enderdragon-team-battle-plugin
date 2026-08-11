package io.twotle.infrastructure

import io.twotle.domain.TeamColor
import net.kyori.adventure.text.format.NamedTextColor

internal object BukkitTeamColor {
    private val colors = mapOf(
        TeamColor.BLACK to NamedTextColor.BLACK,
        TeamColor.DARK_BLUE to NamedTextColor.DARK_BLUE,
        TeamColor.DARK_GREEN to NamedTextColor.DARK_GREEN,
        TeamColor.DARK_AQUA to NamedTextColor.DARK_AQUA,
        TeamColor.DARK_RED to NamedTextColor.DARK_RED,
        TeamColor.DARK_PURPLE to NamedTextColor.DARK_PURPLE,
        TeamColor.GOLD to NamedTextColor.GOLD,
        TeamColor.GRAY to NamedTextColor.GRAY,
        TeamColor.DARK_GRAY to NamedTextColor.DARK_GRAY,
        TeamColor.BLUE to NamedTextColor.BLUE,
        TeamColor.GREEN to NamedTextColor.GREEN,
        TeamColor.AQUA to NamedTextColor.AQUA,
        TeamColor.RED to NamedTextColor.RED,
        TeamColor.LIGHT_PURPLE to NamedTextColor.LIGHT_PURPLE,
        TeamColor.YELLOW to NamedTextColor.YELLOW,
        TeamColor.WHITE to NamedTextColor.WHITE,
    )

    operator fun get(color: TeamColor): NamedTextColor = requireNotNull(colors[color])
}
