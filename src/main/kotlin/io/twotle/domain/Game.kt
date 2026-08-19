package io.twotle.domain

enum class GameStatus {
    IDLE,
    RUNNING,
    PAUSED,
}

enum class GameAction {
    START,
    PAUSE,
    STOP,
}

sealed interface GameEvent {
    data object Started : GameEvent
    data object Resumed : GameEvent
    data object Paused : GameEvent
    data class StoppedAsDraw(val standings: List<TeamStanding>) : GameEvent
    data class WonByScore(
        val team: Team,
        val standings: List<TeamStanding>,
    ) : GameEvent
    data class Won(val team: Team) : GameEvent
}

data class TeamScore(
    val kills: Int = 0,
    val deaths: Int = 0,
) {
    val points: Int
        get() = kills - deaths
}

data class TeamStanding(
    val team: Team,
    val score: TeamScore,
)
