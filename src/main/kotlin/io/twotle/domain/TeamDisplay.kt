package io.twotle.domain

interface TeamDisplay {
    fun synchronize(teams: List<Team>)
    fun update(team: Team)
    fun remove(team: Team)
    fun reset()
}
