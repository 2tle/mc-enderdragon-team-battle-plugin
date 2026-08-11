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
    data object StoppedAsDraw : GameEvent
    data class Won(val team: Team) : GameEvent
}
