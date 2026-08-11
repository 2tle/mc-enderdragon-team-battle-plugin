package io.twotle.presentation.command

import io.twotle.application.TeamErrorCode
import io.twotle.application.TeamServiceException

internal class TeamErrorMessageResolver {
    private val messages = mapOf<TeamErrorCode, (List<String>) -> String>(
        TeamErrorCode.INVALID_TEAM_NAME to {
            "팀 이름은 영문, 숫자, _ 또는 -만 사용해 1~32자로 입력해야 합니다."
        },
        TeamErrorCode.INVALID_USERNAME to {
            "올바른 마인크래프트 사용자 이름을 입력해 주세요."
        },
        TeamErrorCode.INVALID_TEAM_COLOR to {
            "'${it[0]}' 색상은 사용할 수 없습니다. 색상 자동완성을 이용해 주세요."
        },
        TeamErrorCode.TEAM_ALREADY_EXISTS to { "'${it[0]}' 팀은 이미 존재합니다." },
        TeamErrorCode.TEAM_NOT_FOUND to { "'${it[0]}' 팀이 존재하지 않습니다." },
        TeamErrorCode.PLAYER_NOT_FOUND to { "'${it[0]}' 님은 서버에서 찾을 수 없습니다." },
        TeamErrorCode.PLAYER_ALREADY_ASSIGNED to {
            "'${it[0]}' 님은 이미 '${it[1]}' 팀에 속해 있습니다."
        },
        TeamErrorCode.PLAYER_NOT_IN_TEAM to {
            "'${it[0]}' 님은 '${it[1]}' 팀에 속해 있지 않습니다."
        },
    )

    fun resolve(exception: TeamServiceException): String =
        requireNotNull(messages[exception.code]) {
            "처리되지 않은 팀 오류 코드입니다: ${exception.code}"
        }(exception.arguments)
}
