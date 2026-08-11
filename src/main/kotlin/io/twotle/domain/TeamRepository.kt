package io.twotle.domain

import java.util.UUID

interface TeamRepository {
    fun findByName(name: String): Team?
    fun findByMember(uuid: UUID): Team?
    fun findAll(): List<Team>
    fun save(team: Team)
    fun delete(team: Team)
}
