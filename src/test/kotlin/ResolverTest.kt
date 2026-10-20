import model.Position
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {

    private fun assertClose(expected: Double, actual: Double) =
        assertEquals(expected, actual, absoluteTolerance = 1e-9)

    @Test
    fun `count players without agency`() {
        val resolver = Resolver(
            listOf(
                player("A", agency = null),
                player("B", agency = "X"),
                player("C", agency = null),
            ),
        )
        assertEquals(2, resolver.getCountWithoutAgency())
    }

    @Test
    fun `best scorer among defenders ignores other positions`() {
        val resolver = Resolver(
            listOf(
                player("Forward", position = Position.FORWARD, goals = 50),
                player("Def 1", position = Position.DEFENDER, goals = 7),
                player("Def 2", position = Position.DEFENDER, goals = 12),
            ),
        )
        assertEquals("Def 2" to 12, resolver.getBestScorerDefender())
    }

    @Test
    fun `best scorer defender tie is resolved alphabetically`() {
        val resolver = Resolver(
            listOf(
                player("Zed", position = Position.DEFENDER, goals = 5),
                player("Abe", position = Position.DEFENDER, goals = 5),
            ),
        )
        assertEquals("Abe" to 5, resolver.getBestScorerDefender())
    }

    @Test
    fun `position of most expensive german player in russian`() {
        val resolver = Resolver(
            listOf(
                player("Rich Brazilian", nationality = "Brazil", transferCost = 99_000_000, position = Position.FORWARD),
                player("German 1", nationality = "Germany", transferCost = 30_000_000, position = Position.DEFENDER),
                player("German 2", nationality = "Germany", transferCost = 50_000_000, position = Position.GOALKEEPER),
            ),
        )
        assertEquals("вратарь", resolver.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `rudest team has max average red cards per player`() {
        val resolver = Resolver(
            listOf(
                // Dolphins: в сумме больше, но в среднем 3.0
                player("A", team = DOLPHINS, redCards = 4),
                player("B", team = DOLPHINS, redCards = 2),
                player("C", team = DOLPHINS, redCards = 3),
                // Tigers: в среднем 5.0
                player("D", team = TIGERS, redCards = 5),
            ),
        )
        assertEquals(TIGERS, resolver.getTheRudestTeam())
    }

    @Test
    fun `average transfer cost by position sorted descending`() {
        val resolver = Resolver(
            listOf(
                player("A", position = Position.DEFENDER, transferCost = 10),
                player("B", position = Position.DEFENDER, transferCost = 20),
                player("C", position = Position.FORWARD, transferCost = 40),
                player("D", position = Position.GOALKEEPER, transferCost = 5),
            ),
        )
        val result = resolver.getAverageTransferCostByPosition()
        assertEquals(listOf(Position.FORWARD, Position.DEFENDER, Position.GOALKEEPER), result.keys.toList())
        assertClose(15.0, result.getValue(Position.DEFENDER))
    }

    @Test
    fun `top 3 most valuable players with alphabetical tie break`() {
        val resolver = Resolver(
            listOf(
                player("Low", goals = 1, assists = 0), // 1
                player("Bob", goals = 4, assists = 3), // 10
                player("Ann", goals = 0, assists = 5), // 10
                player("Top", goals = 10, assists = 5), // 20
            ),
        )
        assertEquals(listOf("Top", "Ann", "Bob"), resolver.getMostValuablePlayers().map { it.name })
    }

    @Test
    fun `most popular agency by country`() {
        val resolver = Resolver(
            listOf(
                player("A", nationality = "Brazil", agency = "Zeta"),
                player("B", nationality = "Brazil", agency = "Zeta"),
                player("C", nationality = "Brazil", agency = "Alpha"),
                player("D", nationality = "Brazil", agency = null),
                player("E", nationality = "Brazil", agency = null),
                player("F", nationality = "Brazil", agency = null),
                // ничья — по алфавиту
                player("G", nationality = "Ghana", agency = "Omega"),
                player("H", nationality = "Ghana", agency = "Beta"),
                // нет агентств вообще — страны нет в ответе
                player("I", nationality = "Palau", agency = null),
            ),
        )
        assertEquals(mapOf("Brazil" to "Zeta", "Ghana" to "Beta"), resolver.getMostPopularAgencyByCountry())
    }

    @Test
    fun `goals share split by median cost`() {
        val resolver = Resolver(
            listOf(
                player("A", transferCost = 10, goals = 1),
                player("B", transferCost = 20, goals = 3), // медиана = 20
                player("C", transferCost = 30, goals = 6),
            ),
        )
        assertClose(20.0, resolver.medianTransferCost())
        val (cheap, expensive) = resolver.getGoalsShareByMedianCost()
        assertClose(0.4, cheap)
        assertClose(0.6, expensive)
    }

    @Test
    fun `median for even number of players is average of middle values`() {
        val resolver = Resolver(
            listOf(
                player("A", transferCost = 10, goals = 2),
                player("B", transferCost = 20, goals = 2),
                player("C", transferCost = 40, goals = 3),
                player("D", transferCost = 100, goals = 3),
            ),
        )
        assertClose(30.0, resolver.medianTransferCost())
        val (cheap, expensive) = resolver.getGoalsShareByMedianCost()
        assertClose(0.4, cheap)
        assertClose(0.6, expensive)
    }
}
