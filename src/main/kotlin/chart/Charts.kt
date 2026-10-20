package chart

import model.Player
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.ValueMarker
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer
import org.jfree.chart.renderer.xy.XYBarRenderer
import org.jfree.chart.ui.RectangleAnchor
import org.jfree.chart.ui.TextAnchor
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
import org.jfree.data.statistics.HistogramDataset
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.io.File

/** Вариант 3. Распределения трансферных стоимостей. */
object Charts {

    private const val MILLION = 1_000_000.0
    private const val BINS = 20
    private const val WIDTH = 1000
    private const val HEIGHT = 600

    /** Базовая: распределение трансферных стоимостей всех игроков (гистограмма + медиана). */
    fun transferCostHistogram(players: List<Player>, file: File): File {
        val costs = players.map { it.transferCost / MILLION }
        val dataset = HistogramDataset().apply {
            addSeries("Игроки", costs.toDoubleArray(), BINS)
        }
        val chart = ChartFactory.createHistogram(
            "Распределение трансферных стоимостей (${players.size} игроков)",
            "Трансферная стоимость, млн",
            "Количество игроков",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false,
        )
        chart.xyPlot.apply {
            (renderer as XYBarRenderer).apply {
                setSeriesPaint(0, Color(0x4C72B0))
                setDrawBarOutline(true)
                setSeriesOutlinePaint(0, Color.WHITE)
                setShadowVisible(false)
                barPainter = org.jfree.chart.renderer.xy.StandardXYBarPainter()
            }
            val median = costs.sorted().let { sorted ->
                val mid = sorted.size / 2
                if (sorted.size % 2 == 1) sorted[mid] else (sorted[mid - 1] + sorted[mid]) / 2
            }
            addDomainMarker(
                ValueMarker(median, Color(0xC44E52), BasicStroke(2.5f)).apply {
                    label = "медиана ≈ %.1f млн".format(median)
                    labelFont = Font(Font.SANS_SERIF, Font.BOLD, 14)
                    labelAnchor = RectangleAnchor.TOP_RIGHT
                    labelTextAnchor = TextAnchor.TOP_LEFT
                },
            )
        }
        return chart.saveTo(file)
    }

    /** Продвинутая: разброс стоимостей внутри каждой позиции (ящики с усами, по убыванию медианы). */
    fun transferCostByPositionBoxPlot(players: List<Player>, file: File): File {
        val dataset = DefaultBoxAndWhiskerCategoryDataset()
        players.groupBy { it.position }
            .mapValues { (_, group) -> group.map { it.transferCost / MILLION } }
            .entries
            .sortedByDescending { (_, costs) -> costs.sorted()[costs.size / 2] }
            .forEach { (position, costs) ->
                dataset.add(costs, "Стоимость", "${position.russianName} (${costs.size})")
            }
        val chart = ChartFactory.createBoxAndWhiskerChart(
            "Разброс трансферных стоимостей по позициям",
            "Позиция (число игроков)",
            "Трансферная стоимость, млн",
            dataset,
            false,
        )
        chart.categoryPlot.apply {
            (renderer as BoxAndWhiskerRenderer).apply {
                setSeriesPaint(0, Color(0x55A868))
                isMeanVisible = false
                isMedianVisible = true
                setSeriesStroke(0, BasicStroke(2f))
                maximumBarWidth = 0.15
                setUseOutlinePaintForWhiskers(false)
            }
            rangeAxis.lowerBound = 0.0
        }
        return chart.saveTo(file)
    }

    private fun JFreeChart.saveTo(file: File): File {
        file.parentFile?.mkdirs()
        backgroundPaint = Color.WHITE
        plot.backgroundPaint = Color(0xF5F5F5)
        ChartUtils.saveChartAsPNG(file, this, WIDTH, HEIGHT)
        return file
    }
}
