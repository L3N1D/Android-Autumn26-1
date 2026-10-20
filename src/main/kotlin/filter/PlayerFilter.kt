package filter

import model.Player
import model.Position

sealed interface PlayerFilter {

    data class ByPosition(val position: Position) : PlayerFilter
    data class ByNationality(val nationality: String) : PlayerFilter
    data class CheaperThan(val cost: Long) : PlayerFilter
    data object HasAgency : PlayerFilter
    data class And(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter
    data class Or(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter
    data class Not(val filter: PlayerFilter) : PlayerFilter

    fun matches(player: Player): Boolean = when (this) {
        is ByPosition -> player.position == position
        is ByNationality -> player.nationality == nationality
        is CheaperThan -> player.transferCost < cost
        HasAgency -> player.agency != null
        is And -> left.matches(player) && right.matches(player)
        is Or -> left.matches(player) || right.matches(player)
        is Not -> !filter.matches(player)
    }
}

infix fun PlayerFilter.and(other: PlayerFilter): PlayerFilter = PlayerFilter.And(this, other)

infix fun PlayerFilter.or(other: PlayerFilter): PlayerFilter = PlayerFilter.Or(this, other)

operator fun PlayerFilter.not(): PlayerFilter = PlayerFilter.Not(this)

fun List<Player>.filterBy(filter: PlayerFilter): List<Player> = filter { filter.matches(it) }
