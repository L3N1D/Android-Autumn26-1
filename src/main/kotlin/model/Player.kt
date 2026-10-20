package model

data class Player(
    val name: String,
    val team: Team,
    val position: Position,
    val nationality: String,
    /** null — интересы игрока никто не представляет. */
    val agency: String?,
    val transferCost: Long,
    val participations: Int,
    val goals: Int,
    val assists: Int,
    val yellowCards: Int,
    val redCards: Int,
) {
    /** Полезность игрока: голы + 2 × голевые передачи. */
    val usefulness: Int get() = goals + 2 * assists
}
