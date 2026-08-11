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
    fun `게임 시작과 일시정지 및 재개 상태를 전이한다`() {
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
    fun `강제 종료는 게임을 대기 상태와 무승부로 변경한다`() {
        service.start()
        service.stop()

        assertEquals(GameStatus.IDLE, games.status())
        assertIs<GameEvent.StoppedAsDraw>(announcer.lastEvent)
    }

    @Test
    fun `유효하지 않은 상태 전이를 거부한다`() {
        val exception = assertFailsWith<InvalidGameTransition> { service.pause() }

        assertEquals(GameStatus.IDLE, exception.status)
        assertEquals(GameAction.PAUSE, exception.action)
    }

    @Test
    fun `진행 중 드래곤을 최초 처치한 팀만 승리한다`() {
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
    fun `일시정지 중에는 팀원만 동결 대상으로 판정한다`() {
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

    override fun status(): GameStatus = currentStatus

    override fun saveStatus(status: GameStatus) {
        currentStatus = status
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
