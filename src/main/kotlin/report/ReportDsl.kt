package report

@DslMarker
annotation class ReportDsl

sealed interface ReportItem {
    data class Metric(val title: String, val value: Any?) : ReportItem
    data class Section(val title: String, val items: List<ReportItem>) : ReportItem
}

class Report(val items: List<ReportItem>) {

    fun render(): String = buildString { items.forEach { render(it, 0) } }.trimEnd()

    private fun StringBuilder.render(item: ReportItem, level: Int) {
        val indent = "  ".repeat(level)
        when (item) {
            is ReportItem.Metric -> appendLine("$indent${item.title}: ${item.value}")
            is ReportItem.Section -> {
                appendLine("$indent== ${item.title} ==")
                item.items.forEach { render(it, level + 1) }
            }
        }
    }

    override fun toString(): String = render()
}

@ReportDsl
class ReportBuilder {
    private val items = mutableListOf<ReportItem>()

    fun metric(title: String, value: () -> Any?) {
        items += ReportItem.Metric(title, value())
    }

    fun section(title: String, block: ReportBuilder.() -> Unit) {
        items += ReportItem.Section(title, ReportBuilder().apply(block).items)
    }

    fun build(): Report = Report(items.toList())
}

fun report(block: ReportBuilder.() -> Unit): Report = ReportBuilder().apply(block).build()
