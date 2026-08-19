package io.twotle.domain

interface TeamSpawnDisplay {
    fun synchronize(teams: List<Team>)
    fun update(team: Team)
    fun remove(team: Team)
    fun reset()
}

object NoOpTeamSpawnDisplay : TeamSpawnDisplay {
    override fun synchronize(teams: List<Team>) = Unit
    override fun update(team: Team) = Unit
    override fun remove(team: Team) = Unit
    override fun reset() = Unit
}
