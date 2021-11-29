package ua.com.radiokot.feed.updater.util

import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

object Running {
    private val runExecutor: ScheduledExecutorService by lazy {
        Executors.newScheduledThreadPool(5) {
            Thread(it).apply {
                name = "RunningScheduledThread#${it.hashCode()}"
            }
        }
    }

    fun withBackoff(
        runnable: Runnable,
        normalInterval: Duration,
        minAbnormalInterval: Duration,
        maxAbnormalInterval: Duration,
        abnormalIntervalMultiplier: Double = 1.5,
        runnableName: String? = null
    ) {
        require(abnormalIntervalMultiplier > 1.0) {
            "Abnormal interval multiplier $abnormalIntervalMultiplier is lower than 1.0, " +
                    "hence the backoff is useless"
        }

        Logger.getGlobal().log(
            Level.INFO,
            "start: " +
                    "name=$runnableName, " +
                    "normalInterval=$normalInterval, " +
                    "minAbnormalInterval=$minAbnormalInterval, " +
                    "maxAbnormalInterval=$maxAbnormalInterval, " +
                    "abnormalIntervalMultiplier=$abnormalIntervalMultiplier"
        )

        scheduleRunWithReschedule(
            runnable = runnable,
            delayMillis = 0,
            normalIntervalMillis = normalInterval.toMillis(),
            minAbnormalIntervalMillis = minAbnormalInterval.toMillis(),
            maxAbnormalIntervalMillis = maxAbnormalInterval.toMillis(),
            abnormalIntervalMultiplier = abnormalIntervalMultiplier,
            name = runnableName
        )
    }

    private fun scheduleRunWithReschedule(
        runnable: Runnable,
        delayMillis: Long,
        normalIntervalMillis: Long,
        minAbnormalIntervalMillis: Long,
        maxAbnormalIntervalMillis: Long,
        abnormalIntervalMultiplier: Double,
        name: String?
    ) {
        runExecutor.schedule(
            {
                try {
                    runnable.run()

                    if (delayMillis > normalIntervalMillis && name != null) {
                        Logger.getGlobal().log(
                            Level.INFO,
                            "error_gone: " +
                                    "name=$name"
                        )
                    }

                    scheduleRunWithReschedule(
                        runnable = runnable,
                        delayMillis = normalIntervalMillis,
                        normalIntervalMillis = normalIntervalMillis,
                        minAbnormalIntervalMillis = minAbnormalIntervalMillis,
                        maxAbnormalIntervalMillis = maxAbnormalIntervalMillis,
                        abnormalIntervalMultiplier = abnormalIntervalMultiplier,
                        name = name
                    )
                } catch (e: Exception) {
                    val nextDelayMillis =
                        (delayMillis * abnormalIntervalMultiplier)
                            .toLong()
                            .coerceAtLeast(minAbnormalIntervalMillis)
                            .coerceAtMost(maxAbnormalIntervalMillis)

                    Logger.getGlobal().log(
                        Level.SEVERE,
                        "${
                            if (delayMillis == normalIntervalMillis)
                                "error_occurred"
                            else
                                "error_occurred_again"
                        }: " +
                                "name=$name, " +
                                "nextDelayMillis=$nextDelayMillis",
                        e
                    )

                    scheduleRunWithReschedule(
                        runnable = runnable,
                        delayMillis = nextDelayMillis,
                        normalIntervalMillis = normalIntervalMillis,
                        minAbnormalIntervalMillis = minAbnormalIntervalMillis,
                        maxAbnormalIntervalMillis = maxAbnormalIntervalMillis,
                        abnormalIntervalMultiplier = abnormalIntervalMultiplier,
                        name = name
                    )
                }
            },
            delayMillis,
            TimeUnit.MILLISECONDS
        )
    }
}