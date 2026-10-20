import model.Player
import model.Position
import model.Team

val DOLPHINS = Team("Dolphins", "Miami")
val TIGERS = Team("Tigers", "Tampa")

fun player(
    name: String,
    team: Team = DOLPHINS,
    position: Position = Position.FORWARD,
    nationality: String = "Brazil",
    agency: String? = "Agency A",
    transferCost: Long = 10_000_000,
    participations: Int = 10,
    goals: Int = 0,
    assists: Int = 0,
    yellowCards: Int = 0,
    redCards: Int = 0,
) = Player(
    name = name,
    team = team,
    position = position,
    nationality = nationality,
    agency = agency,
    transferCost = transferCost,
    participations = participations,
    goals = goals,
    assists = assists,
    yellowCards = yellowCards,
    redCards = redCards,
)
