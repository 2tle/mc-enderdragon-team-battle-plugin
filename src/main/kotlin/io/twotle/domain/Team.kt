package io.twotle.domain

import java.util.UUID

data class Team(
    val name: String,
    val color: TeamColor,
    val members: List<TeamMember> = emptyList(),
) {
    fun contains(uuid: UUID): Boolean = members.any { it.uuid == uuid }

    fun memberNamed(username: String): TeamMember? =
        members.firstOrNull { it.username.equals(username, ignoreCase = true) }

    fun add(member: TeamMember): Team = copy(members = members + member)

    fun remove(member: TeamMember): Team = copy(members = members - member)
}

data class TeamMember(
    val uuid: UUID,
    val username: String,
)

enum class TeamColor {
    BLACK,
    DARK_BLUE,
    DARK_GREEN,
    DARK_AQUA,
    DARK_RED,
    DARK_PURPLE,
    GOLD,
    GRAY,
    DARK_GRAY,
    BLUE,
    GREEN,
    AQUA,
    RED,
    LIGHT_PURPLE,
    YELLOW,
    WHITE;

    val commandName: String
        get() = name.lowercase()

    companion object {
        fun fromCommandName(value: String): TeamColor? =
            entries.firstOrNull { it.commandName.equals(value, ignoreCase = true) }

        fun commandNames(): List<String> = entries.map { it.commandName }
    }
}
