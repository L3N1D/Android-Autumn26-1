package resolver

import filter.PlayerFilter
import filter.filterBy
import filter.not
import model.Player
import model.Position
import model.Team
import model.TeamSquad

class Resolver(private val players: List<Player>) : IResolver {

    val squads: List<TeamSquad> by lazy { TeamSquad.from(players) }

    override fun getCountWithoutAgency(): Int =
        players.filterBy(!PlayerFilter.HasAgency).size

    override fun getBestScorerDefender(): Pair<String, Int> =
        players.filterBy(PlayerFilter.ByPosition(Position.DEFENDER))
            .minWith(compareByDescending<Player> { it.goals }.thenBy { it.name })
            .let { it.name to it.goals }

    override fun getTheExpensiveGermanPlayerPosition(): String =
        players.filterBy(PlayerFilter.ByNationality(GERMANY))
            .maxBy { it.transferCost }
            .position.russianName

    override fun getTheRudestTeam(): Team =
        squads.maxBy { it.averageRedCards }.team

    override fun getAverageTransferCostByPosition(): Map<Position, Double> =
        players.groupBy { it.position }
            .mapValues { (_, group) -> group.map { it.transferCost }.average() }
            .entries
            .sortedByDescending { it.value }
            .associate { it.key to it.value }

    override fun getMostValuablePlayers(): List<Player> =
        players.sortedWith(compareByDescending<Player> { it.usefulness }.thenBy { it.name })
            .take(3)

    override fun getMostPopularAgencyByCountry(): Map<String, String> =
        players.groupBy { it.nationality }
            .mapNotNull { (country, group) ->
                group.mapNotNull { it.agency }
                    .groupingBy { it }
                    .eachCount()
                    .entries
                    .minWithOrNull(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                    ?.let { country to it.key }
            }
            .sortedBy { it.first }
            .toMap()

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        val median = medianTransferCost()
        val totalGoals = players.sumOf { it.goals }
        if (totalGoals == 0) return 0.0 to 0.0
        val (cheap, expensive) = players.partition { it.transferCost <= median }
        return cheap.sumOf { it.goals }.toDouble() / totalGoals to
            expensive.sumOf { it.goals }.toDouble() / totalGoals
    }

    fun medianTransferCost(): Double {
        val costs = players.map { it.transferCost }.sorted()
        require(costs.isNotEmpty()) { "Нет игроков для расчёта медианы" }
        val middle = costs.size / 2
        return if (costs.size % 2 == 1) {
            costs[middle].toDouble()
        } else {
            (costs[middle - 1] + costs[middle]) / 2.0
        }
    }

    private companion object {
        const val GERMANY = "Germany"
    }
}
