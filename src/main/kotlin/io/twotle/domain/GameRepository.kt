package io.twotle.domain

interface GameRepository {
    fun status(): GameStatus
    fun saveStatus(status: GameStatus)
    fun scores(): Map<String, TeamScore>
    fun saveScores(scores: Map<String, TeamScore>)
    fun clearScores()
}
