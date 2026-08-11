package io.twotle.domain

interface GameAnnouncer {
    fun announce(event: GameEvent)
}
