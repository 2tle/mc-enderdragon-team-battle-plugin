package io.twotle.application

import io.twotle.domain.GameAction
import io.twotle.domain.GameAnnouncer
import io.twotle.domain.GameEvent
import io.twotle.domain.GameRepository
import io.twotle.domain.GameStatus
import io.twotle.domain.TeamRepository
import java.util.UUID

class GameService(
    private val games: GameRepository,
    private val teams: TeamRepository,
    private val announcer: GameAnnouncer,
    private val stateMachine: GameStateMachine = GameStateMachine(),
) {
    fun start() = transition(GameAction.START)

    fun pause() = transition(GameAction.PAUSE)

    fun stop() = transition(GameAction.STOP)

    fun onDragonDefeated(killerUuid: UUID) {
        if (games.status() != GameStatus.RUNNING) return
        val winner = teams.findByMember(killerUuid) ?: return

        games.saveStatus(GameStatus.IDLE)
        announcer.announce(GameEvent.Won(winner))
    }

    fun isFrozenParticipant(uuid: UUID): Boolean =
        games.status() == GameStatus.PAUSED && teams.findByMember(uuid) != null

    private fun transition(action: GameAction) {
        val transition = stateMachine.transition(games.status(), action)
        games.saveStatus(transition.nextStatus)
        announcer.announce(transition.event)
    }
}

class GameStateMachine {
    private val transitions = mapOf(
        TransitionKey(GameStatus.IDLE, GameAction.START) to
            Transition(GameStatus.RUNNING, GameEvent.Started),
        TransitionKey(GameStatus.PAUSED, GameAction.START) to
            Transition(GameStatus.RUNNING, GameEvent.Resumed),
        TransitionKey(GameStatus.RUNNING, GameAction.PAUSE) to
            Transition(GameStatus.PAUSED, GameEvent.Paused),
        TransitionKey(GameStatus.RUNNING, GameAction.STOP) to
            Transition(GameStatus.IDLE, GameEvent.StoppedAsDraw),
        TransitionKey(GameStatus.PAUSED, GameAction.STOP) to
            Transition(GameStatus.IDLE, GameEvent.StoppedAsDraw),
    )

    fun transition(status: GameStatus, action: GameAction): Transition =
        transitions[TransitionKey(status, action)]
            ?: throw InvalidGameTransition(status, action)

    private data class TransitionKey(
        val status: GameStatus,
        val action: GameAction,
    )
}

data class Transition(
    val nextStatus: GameStatus,
    val event: GameEvent,
)

class InvalidGameTransition(
    val status: GameStatus,
    val action: GameAction,
) : RuntimeException("The $action action cannot be performed while the game is $status.")
