package model

data class Team(
    val name: String,
    val city: String,
)

/**
 * Команда-агрегат: команда вместе со своим составом.
 * Агрегаты считаются лениво — при первом обращении — и затем переиспользуются.
 */
class TeamSquad(val team: Team, val players: List<Player>) {

    val totalTransferCost: Long by lazy { players.sumOf { it.transferCost } }

    val averageRedCards: Double by lazy { players.map { it.redCards }.average() }

    val averageYellowCards: Double by lazy { players.map { it.yellowCards }.average() }

    val averageCards: Double by lazy { averageRedCards + averageYellowCards }

    companion object {
        fun from(players: List<Player>): List<TeamSquad> =
            players.groupBy { it.team }.map { (team, squad) -> TeamSquad(team, squad) }
    }
}
