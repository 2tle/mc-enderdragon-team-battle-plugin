package io.twotle.presentation.command

import io.twotle.application.TeamErrorCode
import io.twotle.application.TeamServiceException

internal class TeamErrorMessageResolver {
    private val messages = mapOf<TeamErrorCode, (List<String>) -> String>(
        TeamErrorCode.INVALID_TEAM_NAME to {
            "Team names must be 1-32 characters and contain only letters, numbers, underscores, or hyphens."
        },
        TeamErrorCode.INVALID_USERNAME to {
            "Enter a valid Minecraft username."
        },
        TeamErrorCode.INVALID_TEAM_COLOR to {
            "'${it[0]}' is not a valid color. Use tab completion to select a color."
        },
        TeamErrorCode.TEAM_ALREADY_EXISTS to { "Team '${it[0]}' already exists." },
        TeamErrorCode.TEAM_NOT_FOUND to { "Team '${it[0]}' does not exist." },
        TeamErrorCode.PLAYER_NOT_FOUND to { "Player '${it[0]}' could not be found on the server." },
        TeamErrorCode.PLAYER_ALREADY_ASSIGNED to {
            "Player '${it[0]}' is already a member of team '${it[1]}'."
        },
        TeamErrorCode.PLAYER_NOT_IN_TEAM to {
            "Player '${it[0]}' is not a member of team '${it[1]}'."
        },
    )

    fun resolve(exception: TeamServiceException): String =
        requireNotNull(messages[exception.code]) {
            "Unhandled team error code: ${exception.code}"
        }(exception.arguments)
}
