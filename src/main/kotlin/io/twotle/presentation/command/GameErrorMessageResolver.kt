package io.twotle.presentation.command

import io.twotle.application.InvalidGameTransition
import io.twotle.domain.GameAction
import io.twotle.domain.GameStatus

internal class GameErrorMessageResolver {
    private val messages = mapOf(
        TransitionKey(GameStatus.RUNNING, GameAction.START) to "게임이 이미 진행 중입니다.",
        TransitionKey(GameStatus.IDLE, GameAction.PAUSE) to "진행 중인 게임이 없어 일시정지할 수 없습니다.",
        TransitionKey(GameStatus.PAUSED, GameAction.PAUSE) to "게임이 이미 일시정지 상태입니다.",
        TransitionKey(GameStatus.IDLE, GameAction.STOP) to "강제 종료할 게임이 없습니다.",
    )

    fun resolve(exception: InvalidGameTransition): String =
        messages[TransitionKey(exception.status, exception.action)]
            ?: "현재 게임 상태에서는 해당 명령어를 실행할 수 없습니다."

    private data class TransitionKey(
        val status: GameStatus,
        val action: GameAction,
    )
}
