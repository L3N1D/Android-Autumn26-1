import filter.PlayerFilter
import filter.and
import filter.filterBy
import filter.not
import filter.or
import model.Position
import model.TeamSquad
import report.report
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExtrasTest {

    private val forward = player("F", position = Position.FORWARD, transferCost = 5, agency = null)
    private val defender = player("D", position = Position.DEFENDER, transferCost = 50)

    @Test
    fun `filters combine recursively`() {
        val cheapForward = PlayerFilter.ByPosition(Position.FORWARD) and PlayerFilter.CheaperThan(10)
        assertTrue(cheapForward.matches(forward))
        assertFalse(cheapForward.matches(defender))

        val anyOf = PlayerFilter.ByPosition(Position.GOALKEEPER) or PlayerFilter.ByPosition(Position.DEFENDER)
        assertEquals(listOf(defender), listOf(forward, defender).filterBy(anyOf))

        assertEquals(listOf(forward), listOf(forward, defender).filterBy(!PlayerFilter.HasAgency))
        assertTrue(PlayerFilter.Not(PlayerFilter.ByNationality("Germany")).matches(forward))
    }

    @Test
    fun `team squad aggregates are computed lazily from players`() {
        val squads = TeamSquad.from(
            listOf(
                player("A", team = DOLPHINS, transferCost = 10, yellowCards = 2, redCards = 1),
                player("B", team = DOLPHINS, transferCost = 30, yellowCards = 4, redCards = 3),
                player("C", team = TIGERS, transferCost = 5),
            ),
        ).associateBy { it.team }
        val dolphins = squads.getValue(DOLPHINS)
        assertEquals(40L, dolphins.totalTransferCost)
        assertEquals(2.0, dolphins.averageRedCards)
        assertEquals(3.0, dolphins.averageYellowCards)
        assertEquals(5.0, dolphins.averageCards)
        assertEquals(1, squads.getValue(TIGERS).players.size)
    }

    @Test
    fun `report dsl renders nested sections`() {
        val players = listOf(forward, defender.copy(goals = 3))
        val rendered = report {
            metric("Без агентства") { players.count { it.agency == null } }
            section("Атаки") {
                metric("Всего голов") { players.sumOf { it.goals } }
            }
        }.render()
        assertEquals(
            """
            Без агентства: 1
            == Атаки ==
              Всего голов: 3
            """.trimIndent(),
            rendered.replace("\r\n", "\n"),
        )
    }
}
