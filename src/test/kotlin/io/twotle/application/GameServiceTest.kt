package io.twotle.application

import io.twotle.domain.GameAction
import io.twotle.domain.GameAnnouncer
import io.twotle.domain.GameEvent
import io.twotle.domain.GameRepository
import io.twotle.domain.GameStatus
import io.twotle.domain.Team
import io.twotle.domain.TeamColor
import io.twotle.domain.TeamMember
import io.twotle.domain.TeamRepository
import io.twotle.domain.TeamScore
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GameServiceTest {
    private val games = InMemoryGameRepository()
    private val teams = GameTeamRepository()
    private val announcer = RecordingGameAnnouncer()
    private val service = GameService(games, teams, announcer)

    @Test
    fun `start pause and resume transition the game state`() {
        service.start()
        assertEquals(GameStatus.RUNNING, games.status())
        assertIs<GameEvent.Started>(announcer.lastEvent)

        service.pause()
        assertEquals(GameStatus.PAUSED, games.status())
        assertIs<GameEvent.Paused>(announcer.lastEvent)

        service.start()
        assertEquals(GameStatus.RUNNING, games.status())
        assertIs<GameEvent.Resumed>(announcer.lastEvent)
    }

    @Test
    fun `stop returns the game to idle with a draw when no team has a unique highest score`() {
        service.start()
        service.stop()

        assertEquals(GameStatus.IDLE, games.status())
        assertIs<GameEvent.StoppedAsDraw>(announcer.lastEvent)
    }

    @Test
    fun `stop awards the team with the highest opponent kills minus team deaths`() {
        val red = TeamMember(UUID.randomUUID(), "RedPlayer")
        val blue = TeamMember(UUID.randomUUID(), "BluePlayer")
        teams.save(Team("RED", TeamColor.RED, listOf(red)))
        teams.save(Team("BLUE", TeamColor.BLUE, listOf(blue)))
        service.start()

        service.recordPlayerDeath(victimUuid = blue.uuid, killerUuid = red.uuid)
        service.recordPlayerDeath(victimUuid = red.uuid, killerUuid = null)
        service.stop()

        val event = assertIs<GameEvent.WonByScore>(announcer.lastEvent)
        assertEquals("RED", event.team.name)
        assertEquals(0, event.standings.first { it.team.name == "RED" }.score.points)
        assertEquals(-1, event.standings.first { it.team.name == "BLUE" }.score.points)
        assertEquals(GameStatus.IDLE, games.status())
        assertTrue(games.scores().isEmpty())
    }

    @Test
    fun `team kills do not add a kill but every participating death is counted`() {
        val first = TeamMember(UUID.randomUUID(), "First")
        val second = TeamMember(UUID.randomUUID(), "Second")
        teams.save(Team("RED", TeamColor.RED, listOf(first, second)))
        service.start()

        service.recordPlayerDeath(victimUuid = second.uuid, killerUuid = first.uuid)

        assertEquals(TeamScore(kills = 0, deaths = 1), games.scores()["red"])
    }

    @Test
    fun `invalid state transitions are rejected`() {
        val exception = assertFailsWith<InvalidGameTransition> { service.pause() }

        assertEquals(GameStatus.IDLE, exception.status)
        assertEquals(GameAction.PAUSE, exception.action)
    }

    @Test
    fun `only the first team to defeat the dragon wins`() {
        val member = TeamMember(UUID.randomUUID(), "Steve")
        teams.save(Team("RED", TeamColor.RED, listOf(member)))
        service.start()

        service.onDragonDefeated(member.uuid)
        assertEquals(GameStatus.IDLE, games.status())
        assertEquals("RED", assertIs<GameEvent.Won>(announcer.lastEvent).team.name)

        service.onDragonDefeated(member.uuid)
        assertEquals(1, announcer.events.count { it is GameEvent.Won })
    }

    @Test
    fun `only team members are frozen while the game is paused`() {
        val member = TeamMember(UUID.randomUUID(), "Alex")
        teams.save(Team("BLUE", TeamColor.BLUE, listOf(member)))
        service.start()
        service.pause()

        assertTrue(service.isFrozenParticipant(member.uuid))
        assertFalse(service.isFrozenParticipant(UUID.randomUUID()))
    }
}

private class InMemoryGameRepository : GameRepository {
    private var currentStatus = GameStatus.IDLE
    private var currentScores = emptyMap<String, TeamScore>()

    override fun status(): GameStatus = currentStatus

    override fun saveStatus(status: GameStatus) {
        currentStatus = status
    }

    override fun scores(): Map<String, TeamScore> = currentScores

    override fun saveScores(scores: Map<String, TeamScore>) {
        currentScores = scores.toMap()
    }

    override fun clearScores() {
        currentScores = emptyMap()
    }
}

private class RecordingGameAnnouncer : GameAnnouncer {
    val events = mutableListOf<GameEvent>()
    val lastEvent: GameEvent?
        get() = events.lastOrNull()

    override fun announce(event: GameEvent) {
        events += event
    }
}

private class GameTeamRepository : TeamRepository {
    private val teams = mutableListOf<Team>()

    override fun findByName(name: String): Team? =
        teams.firstOrNull { it.name.equals(name, ignoreCase = true) }

    override fun findByMember(uuid: UUID): Team? = teams.firstOrNull { it.contains(uuid) }

    override fun findAll(): List<Team> = teams.toList()

    override fun save(team: Team) {
        teams.removeAll { it.name.equals(team.name, ignoreCase = true) }
        teams += team
    }

    override fun delete(team: Team) {
        teams.remove(team)
    }
}
