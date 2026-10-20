import chart.Charts
import filter.PlayerFilter
import filter.and
import filter.filterBy
import filter.not
import filter.or
import model.Position
import parser.CsvParser
import report.report
import resolver.Resolver
import java.io.File

private const val DATASET = "fakePlayersDirty.csv"

fun main() {
    System.setProperty("java.awt.headless", "true")

    val (players, skipped) = CsvParser.parseResource(DATASET)
    val resolver = Resolver(players)

    val firstCleanStar = CsvParser.findFirstStreaming(DATASET) {
        it.transferCost > 50_000_000 && it.participations >= 25 && it.yellowCards == 0 && it.redCards == 0
    }

    val consoleReport = report {
        section("Парсинг $DATASET") {
            metric("Загружено игроков") { players.size }
            metric("Пропущено строк") { skipped }
        }
        section("Задание 2") {
            metric("1. Без агентства") { resolver.getCountWithoutAgency() }
            metric("2. Лучший бомбардир среди защитников") {
                resolver.getBestScorerDefender().let { (name, goals) -> "$name — $goals" }
            }
            metric("3. Позиция самого дорогого немца") { resolver.getTheExpensiveGermanPlayerPosition() }
            metric("4. Самая грубая команда") {
                resolver.getTheRudestTeam().let { "${it.name} (${it.city})" }
            }
            section("5. Средняя стоимость по позициям") {
                resolver.getAverageTransferCostByPosition().forEach { (position, cost) ->
                    metric(position.russianName) { cost.formatMoney() }
                }
            }
            section("6. Топ-3 полезных игрока (голы + 2 × передачи)") {
                resolver.getMostValuablePlayers().forEachIndexed { index, player ->
                    metric("${index + 1}. ${player.name}") {
                        "${player.usefulness} (${player.goals} + 2 × ${player.assists})"
                    }
                }
            }
            section("7. Популярное агентство по странам") {
                resolver.getMostPopularAgencyByCountry().forEach { (country, agency) ->
                    metric(country) { agency }
                }
            }
            section("8. Доля голов относительно медианы") {
                val (cheap, expensive) = resolver.getGoalsShareByMedianCost()
                metric("Медианная стоимость") { resolver.medianTransferCost().formatMoney() }
                metric("Не дороже медианы") { cheap.formatPercent() }
                metric("Дороже медианы") { expensive.formatPercent() }
            }
        }
        section("Задание 3") {
            section("Команды-агрегаты (by lazy): топ-5 по стоимости состава") {
                resolver.squads.sortedByDescending { it.totalTransferCost }.take(5).forEach { squad ->
                    metric(squad.team.name) {
                        "${squad.totalTransferCost.toDouble().formatMoney()}, " +
                            "карточек на игрока: %.2f".format(squad.averageCards)
                    }
                }
            }
            metric("Потоковый поиск (>50 млн, ≥25 матчей, без карточек)") {
                firstCleanStar?.let { "${it.name}, ${it.team.name}, ${it.transferCost.toDouble().formatMoney()}" }
                    ?: "не найден"
            }
            section("Фильтры") {
                val cheapAttackers = PlayerFilter.CheaperThan(10_000_000) and
                    (PlayerFilter.ByPosition(Position.FORWARD) or PlayerFilter.ByPosition(Position.MIDFIELD))
                metric("Атакующие дешевле 10 млн") { players.filterBy(cheapAttackers).size }
                metric("Защитники без агентства") {
                    players.filterBy(PlayerFilter.ByPosition(Position.DEFENDER) and !PlayerFilter.HasAgency).size
                }
            }
        }
        section("Атаки") {
            metric("Всего голов") { players.sumOf { it.goals } }
            metric("Всего голевых передач") { players.sumOf { it.assists } }
        }
    }
    println(consoleReport.render())

    val chartsDir = File("charts")
    val histogram = Charts.transferCostHistogram(players, File(chartsDir, "variant3_base_cost_histogram.png"))
    val boxPlot = Charts.transferCostByPositionBoxPlot(players, File(chartsDir, "variant3_advanced_cost_by_position.png"))
    println()
    println("Графики сохранены:")
    println("  ${histogram.absolutePath}")
    println("  ${boxPlot.absolutePath}")
}

private fun Double.formatMoney(): String = "%,.0f".format(this)

private fun Double.formatPercent(): String = "%.1f%%".format(this * 100)
