package io.twotle.presentation.command

import io.twotle.application.InvalidGameTransition
import io.twotle.domain.GameAction
import io.twotle.domain.GameStatus

internal class GameErrorMessageResolver {
    private val messages = mapOf(
        TransitionKey(GameStatus.RUNNING, GameAction.START) to "A game is already running.",
        TransitionKey(GameStatus.IDLE, GameAction.PAUSE) to "There is no running game to pause.",
        TransitionKey(GameStatus.PAUSED, GameAction.PAUSE) to "The game is already paused.",
        TransitionKey(GameStatus.IDLE, GameAction.STOP) to "There is no game to stop.",
    )

    fun resolve(exception: InvalidGameTransition): String =
        messages[TransitionKey(exception.status, exception.action)]
            ?: "This command cannot be used in the current game state."

    private data class TransitionKey(
        val status: GameStatus,
        val action: GameAction,
    )
}
