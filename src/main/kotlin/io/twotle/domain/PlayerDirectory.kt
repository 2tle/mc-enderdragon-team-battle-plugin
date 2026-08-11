package io.twotle.domain

interface PlayerDirectory {
    fun findByUsername(username: String): TeamMember?
    fun onlineUsernames(): List<String>
}
