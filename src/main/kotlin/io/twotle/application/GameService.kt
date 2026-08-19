package io.twotle.application

import io.twotle.domain.GameAction
import io.twotle.domain.GameAnnouncer
import io.twotle.domain.GameEvent
import io.twotle.domain.GameRepository
import io.twotle.domain.GameStatus
import io.twotle.domain.TeamRepository
import io.twotle.domain.TeamScore
import io.twotle.domain.TeamStanding
import java.util.UUID
import java.util.Locale

class GameService(
    private val games: GameRepository,
    private val teams: TeamRepository,
    private val announcer: GameAnnouncer,
    private val stateMachine: GameStateMachine = GameStateMachine(),
) {
    fun start() {
        if (games.status() == GameStatus.IDLE) {
            games.saveScores(
                teams.findAll().associate { it.name.scoreKey() to TeamScore() },
            )
        }
        transition(GameAction.START)
    }

    fun pause() = transition(GameAction.PAUSE)

    fun stop() {
        val status = games.status()
        if (status != GameStatus.RUNNING && status != GameStatus.PAUSED) {
            throw InvalidGameTransition(status, GameAction.STOP)
        }

        val standings = standings()
        val highestScore = standings.maxOfOrNull { it.score.points }
        val leaders = standings.filter { it.score.points == highestScore }
        val event =
            if (leaders.size == 1) {
                GameEvent.WonByScore(leaders.single().team, standings)
            } else {
                GameEvent.StoppedAsDraw(standings)
            }

        games.saveStatus(GameStatus.IDLE)
        games.clearScores()
        announcer.announce(event)
    }

    fun recordPlayerDeath(
        victimUuid: UUID,
        killerUuid: UUID?,
    ) {
        if (games.status() != GameStatus.RUNNING) return
        val victimTeam = teams.findByMember(victimUuid) ?: return
        val scores = games.scores().toMutableMap()
        val victimKey = victimTeam.name.scoreKey()
        val victimScore = scores[victimKey] ?: TeamScore()
        scores[victimKey] = victimScore.copy(deaths = victimScore.deaths + 1)

        val killerTeam = killerUuid?.let(teams::findByMember)
        if (killerTeam != null && !killerTeam.name.equals(victimTeam.name, ignoreCase = true)) {
            val killerKey = killerTeam.name.scoreKey()
            val killerScore = scores[killerKey] ?: TeamScore()
            scores[killerKey] = killerScore.copy(kills = killerScore.kills + 1)
        }
        games.saveScores(scores)
    }

    fun onDragonDefeated(killerUuid: UUID) {
        if (games.status() != GameStatus.RUNNING) return
        val winner = teams.findByMember(killerUuid) ?: return

        games.saveStatus(GameStatus.IDLE)
        games.clearScores()
        announcer.announce(GameEvent.Won(winner))
    }

    fun isFrozenParticipant(uuid: UUID): Boolean =
        games.status() == GameStatus.PAUSED && teams.findByMember(uuid) != null

    private fun transition(action: GameAction) {
        val transition = stateMachine.transition(games.status(), action)
        games.saveStatus(transition.nextStatus)
        announcer.announce(transition.event)
    }

    private fun standings(): List<TeamStanding> {
        val scores = games.scores()
        return teams
            .findAll()
            .map { team -> TeamStanding(team, scores[team.name.scoreKey()] ?: TeamScore()) }
            .sortedWith(
                compareByDescending<TeamStanding> { it.score.points }
                    .thenByDescending { it.score.kills }
                    .thenBy { it.team.name.lowercase(Locale.ROOT) },
            )
    }

    private fun String.scoreKey(): String = lowercase(Locale.ROOT)
}

class GameStateMachine {
    private val transitions = mapOf(
        TransitionKey(GameStatus.IDLE, GameAction.START) to
            Transition(GameStatus.RUNNING, GameEvent.Started),
        TransitionKey(GameStatus.PAUSED, GameAction.START) to
            Transition(GameStatus.RUNNING, GameEvent.Resumed),
        TransitionKey(GameStatus.RUNNING, GameAction.PAUSE) to
            Transition(GameStatus.PAUSED, GameEvent.Paused),
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
