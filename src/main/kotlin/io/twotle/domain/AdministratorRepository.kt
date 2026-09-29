package io.twotle.domain

import java.util.UUID

interface AdministratorRepository {
    fun findAdministrator(uuid: UUID): Administrator?

    fun findAdministrator(username: String): Administrator?

    fun administrators(): List<Administrator>

    fun saveAdministrator(administrator: Administrator)

    fun deleteAdministrator(administrator: Administrator)
}

data class Administrator(
    val uuid: UUID,
    val username: String,
)
