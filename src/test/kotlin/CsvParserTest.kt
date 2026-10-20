import model.Position
import model.Team
import parser.CsvParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CsvParserTest {

    private val header = "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards"
    private val validLine = "Iva Streich;Nevada whales;South Carolina;MIDFIELD;Colombia;D'Amore LLC;75012006;22;19;6;3;7"

    @Test
    fun `valid line is mapped to player`() {
        val player = assertNotNull(CsvParser.parseLineOrNull(validLine))
        assertEquals("Iva Streich", player.name)
        assertEquals(Team("Nevada whales", "South Carolina"), player.team)
        assertEquals(Position.MIDFIELD, player.position)
        assertEquals("Colombia", player.nationality)
        assertEquals("D'Amore LLC", player.agency)
        assertEquals(75_012_006L, player.transferCost)
        assertEquals(22, player.participations)
        assertEquals(19, player.goals)
        assertEquals(6, player.assists)
        assertEquals(3, player.yellowCards)
        assertEquals(7, player.redCards)
    }

    @Test
    fun `empty agency becomes null and player is kept`() {
        val player = CsvParser.parseLineOrNull("A;Team;City;FORWARD;Croatia;;52944545;27;10;5;5;8")
        assertNotNull(player)
        assertNull(player.agency)
    }

    @Test
    fun `blank agency becomes null`() {
        assertNull(CsvParser.parseLineOrNull("A;Team;City;FORWARD;Croatia;   ;1;1;1;1;1;1")?.agency)
    }

    @Test
    fun `position is recognized in any case and with spaces`() {
        listOf("defender", "  Defender ", "DEFENDER", " dEfEnDeR").forEach { token ->
            assertEquals(Position.DEFENDER, Position.fromCsvTokenOrNull(token), "token='$token'")
        }
        assertEquals(Position.GOALKEEPER, Position.fromCsvTokenOrNull("goalkeeper"))
        assertNull(Position.fromCsvTokenOrNull("striker"))
        assertNull(Position.fromCsvTokenOrNull(""))
    }

    @Test
    fun `lowercase position in csv line is parsed`() {
        val player = CsvParser.parseLineOrNull("A;Team;City; forward ;Croatia;X;1;1;1;1;1;1")
        assertEquals(Position.FORWARD, player?.position)
    }

    @Test
    fun `text fields are trimmed`() {
        val player = assertNotNull(
            CsvParser.parseLineOrNull("  Mrs. Adan Swift ; Florida tigers ; Washington ;FORWARD; Qatar ; Fadel LLC ; 37558077 ;25;16;18;8;4"),
        )
        assertEquals("Mrs. Adan Swift", player.name)
        assertEquals(Team("Florida tigers", "Washington"), player.team)
        assertEquals("Qatar", player.nationality)
        assertEquals("Fadel LLC", player.agency)
        assertEquals(37_558_077L, player.transferCost)
    }

    @Test
    fun `lines with empty or non numeric numbers are skipped`() {
        val broken = listOf(
            "A;T;C;FORWARD;Brazil;X;;1;1;1;1;1",
            "A;T;C;FORWARD;Brazil;X;abc;1;1;1;1;1",
            "A;T;C;FORWARD;Brazil;X;1;;1;1;1;1",
            "A;T;C;FORWARD;Brazil;X;1;1;;1;1;1",
            "A;T;C;FORWARD;Brazil;X;1;1;1;x;1;1",
            "A;T;C;FORWARD;Brazil;X;1;1;1;1;;1",
            "A;T;C;FORWARD;Brazil;X;1;1;1;1;1;",
        )
        broken.forEach { assertNull(CsvParser.parseLineOrNull(it), it) }
    }

    @Test
    fun `lines with empty required text fields are skipped`() {
        val broken = listOf(
            ";T;C;FORWARD;Brazil;X;1;1;1;1;1;1",
            "A;;C;FORWARD;Brazil;X;1;1;1;1;1;1",
            "A;T;;FORWARD;Brazil;X;1;1;1;1;1;1",
            "A;T;C;;Brazil;X;1;1;1;1;1;1",
            "A;T;C;FORWARD;;X;1;1;1;1;1;1",
            "A;T;C;FORWARD;Brazil;X;1;1;1",
        )
        broken.forEach { assertNull(CsvParser.parseLineOrNull(it), it) }
    }

    @Test
    fun `parse counts skipped lines and ignores header`() {
        val lines = sequenceOf(
            header,
            validLine,
            "A;;C;FORWARD;Brazil;X;1;1;1;1;1;1",
            "B;T;C;defender;Brazil;;1;1;1;1;1;1",
            "C;T;C;FORWARD;Brazil;X;;1;1;1;1;1",
            "",
        )
        val result = CsvParser.parse(lines)
        assertEquals(listOf("Iva Streich", "B"), result.players.map { it.name })
        assertEquals(2, result.skippedCount)
    }

    @Test
    fun `clean dataset is parsed without skips`() {
        val result = CsvParser.parseResource("fakePlayers.csv")
        assertEquals(250, result.players.size)
        assertEquals(0, result.skippedCount)
    }

    @Test
    fun `dirty dataset survives parsing`() {
        val result = CsvParser.parseResource("fakePlayersDirty.csv")
        assertEquals(250, result.players.size + result.skippedCount)
        assertEquals(30, result.skippedCount)
    }

    @Test
    fun `streaming search finds first matching player`() {
        val found = CsvParser.findFirstStreaming("fakePlayers.csv") { it.transferCost > 90_000_000 }
        assertEquals("Kenyetta Emard", found?.name)
        assertNull(CsvParser.findFirstStreaming("fakePlayers.csv") { it.goals > 1_000 })
    }
}
