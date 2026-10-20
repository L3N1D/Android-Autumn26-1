package parser

import model.Player
import model.Position
import model.Team
import java.io.Reader

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int,
)

object CsvParser {

    private const val DELIMITER = ';'
    private const val COLUMNS_COUNT = 12

    fun parseResource(resourceName: String): ParseResult =
        openResource(resourceName).useLines { parse(it) }

    /** Разбирает строки CSV вместе с заголовком; пустые строки не считаются пропущенными. */
    fun parse(lines: Sequence<String>): ParseResult {
        val players = mutableListOf<Player>()
        var skipped = 0
        lines.drop(1)
            .filter { it.isNotBlank() }
            .forEach { line ->
                val player = parseLineOrNull(line)
                if (player != null) players += player else skipped++
            }
        return ParseResult(players, skipped)
    }

    /** Строка CSV → игрок, либо null, если строку нужно пропустить. */
    fun parseLineOrNull(line: String): Player? {
        val cells = line.split(DELIMITER).map { it.trim() }
        if (cells.size != COLUMNS_COUNT) return null

        val name = cells[0].ifEmpty { return null }
        val teamName = cells[1].ifEmpty { return null }
        val city = cells[2].ifEmpty { return null }
        val position = Position.fromCsvTokenOrNull(cells[3]) ?: return null
        val nationality = cells[4].ifEmpty { return null }
        val agency = cells[5].ifEmpty { null }

        return Player(
            name = name,
            team = Team(teamName, city),
            position = position,
            nationality = nationality,
            agency = agency,
            transferCost = cells[6].toLongOrNull() ?: return null,
            participations = cells[7].toIntOrNull() ?: return null,
            goals = cells[8].toIntOrNull() ?: return null,
            assists = cells[9].toIntOrNull() ?: return null,
            yellowCards = cells[10].toIntOrNull() ?: return null,
            redCards = cells[11].toIntOrNull() ?: return null,
        )
    }

    /**
     * Потоковый поиск: файл читается построчно и обрывается на первом подходящем игроке,
     * целиком в память не загружается.
     */
    fun findFirstStreaming(resourceName: String, predicate: (Player) -> Boolean): Player? =
        openResource(resourceName).useLines { lines ->
            lines.drop(1)
                .mapNotNull { parseLineOrNull(it) }
                .firstOrNull(predicate)
        }

    private fun openResource(resourceName: String): Reader {
        val stream = CsvParser::class.java.classLoader.getResourceAsStream(resourceName)
            ?: error("Ресурс $resourceName не найден")
        return stream.bufferedReader(Charsets.UTF_8)
    }
}
