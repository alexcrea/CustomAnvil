package xyz.alexcrea.cuanvil.util

import dev.faststats.bukkit.BukkitMetrics
import dev.faststats.core.ErrorTracker
import dev.faststats.core.data.Metric
import io.delilaheve.CustomAnvil
import io.delilaheve.util.ConfigOptions
import xyz.alexcrea.cuanvil.command.DiagnosticExecutor
import xyz.alexcrea.cuanvil.config.ConfigHolder

object MetricsUtil {

    private const val BSTATS_PLUGIN_ID = 20923
    private const val FASTSTATS_TOKEN = "fc282b048adcc71a77bc00ace49e8a81"

    private var ERROR_TRACKER: ErrorTracker? = null
    private var FAST_STATS_METRICS: BukkitMetrics? = null

    fun loadMetrics(plugin: CustomAnvil) {
        val metricString = ConfigHolder.DEFAULT_CONFIG.config.getString(ConfigOptions.METRIC_TYPE, MetricType.AUTO.value)!!
        val metricType = MetricType.from(metricString)

        val nmsType = DiagnosticExecutor.fetchNMSType()
        val isAlpha = CustomAnvil.instance.description.version.contains("dev")
        if(metricType.allowBStats) {
            try {
                val metric = Metrics(plugin, BSTATS_PLUGIN_ID)
                metric.addCustomChart(Metrics.SimplePie("nms_type") { nmsType })
                metric.addCustomChart(Metrics.SimplePie("using_alpha") { isAlpha.toString() })
            } catch (_: Exception) {}
        }

        if(metricType.allowFastStats) {
            ERROR_TRACKER = ErrorTracker.contextAware();
            FAST_STATS_METRICS = BukkitMetrics.factory()
                .addMetric(Metric.string("nms_type") { nmsType })
                .addMetric(Metric.bool("using_alpha") { isAlpha })
                .errorTracker(ERROR_TRACKER)
                .token(FASTSTATS_TOKEN)
                .create(plugin)

            FAST_STATS_METRICS!!.ready()
        }
    }

    fun shutdownMetrics() {
        FAST_STATS_METRICS?.shutdown()
    }

    fun trackError(e: Throwable) {
        ERROR_TRACKER?.trackError(e)
    }

    fun trackError(message: String) {
        ERROR_TRACKER?.trackError(message)
    }
}

enum class MetricType(
    val value: String,
    val allowBStats: Boolean,
    val allowFastStats: Boolean,
) {
    AUTO("auto", true, true),
    BSTATS("bstat", true, false),
    FAST_STATS("faststats", false, true),
    DISABLED("disabled", false, false),
    ;

    companion object {
        fun from(value: String): MetricType = entries.find { it.value == value } ?: AUTO
    }

}