package io.twotle.domain

interface GameRepository {
    fun status(): GameStatus
    fun saveStatus(status: GameStatus)
}
