package ua.com.radiokot.feed.updater.util

import mu.KotlinLogging
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

object Running {
    private val logger = KotlinLogging.logger("Running")

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

        logger.info {
            "start: " +
                    "runnable=$runnableName, " +
                    "normalInterval=$normalInterval, " +
                    "minAbnormalInterval=$minAbnormalInterval, " +
                    "maxAbnormalInterval=$maxAbnormalInterval, " +
                    "abnormalIntervalMultiplier=$abnormalIntervalMultiplier"
        }

        scheduleRunWithReschedule(
            runnable = object : Runnable {
                override fun run() =
                    runnable.run()

                override fun toString(): String =
                    runnableName ?: runnable.toString()
            },
            delayMillis = 0,
            normalIntervalMillis = normalInterval.toMillis(),
            minAbnormalIntervalMillis = minAbnormalInterval.toMillis(),
            maxAbnormalIntervalMillis = maxAbnormalInterval.toMillis(),
            abnormalIntervalMultiplier = abnormalIntervalMultiplier,
        )
    }

    private fun scheduleRunWithReschedule(
        runnable: Runnable,
        delayMillis: Long,
        normalIntervalMillis: Long,
        minAbnormalIntervalMillis: Long,
        maxAbnormalIntervalMillis: Long,
        abnormalIntervalMultiplier: Double,
    ) {
        logger.info {
            "schedule: " +
                    "runnable=$runnable, " +
                    "delayMs=$delayMillis"
        }

        runExecutor.schedule(
            {
                try {
                    logger.info {
                        "run: " +
                                "runnable=$runnable"
                    }

                    runnable.run()

                    logger.info {
                        "run_successful: " +
                                "runnable=$runnable"
                    }

                    if (delayMillis > normalIntervalMillis) {
                        logger.info {
                            "error_is_gone: " +
                                    "runnable=$runnable"
                        }
                    }

                    scheduleRunWithReschedule(
                        runnable = runnable,
                        delayMillis = normalIntervalMillis,
                        normalIntervalMillis = normalIntervalMillis,
                        minAbnormalIntervalMillis = minAbnormalIntervalMillis,
                        maxAbnormalIntervalMillis = maxAbnormalIntervalMillis,
                        abnormalIntervalMultiplier = abnormalIntervalMultiplier,
                    )
                } catch (e: Exception) {
                    val nextDelayMillis =
                        (delayMillis * abnormalIntervalMultiplier)
                            .toLong()
                            .coerceAtLeast(minAbnormalIntervalMillis)
                            .coerceAtMost(maxAbnormalIntervalMillis)

                    logger.error(e) {
                        "${
                            if (delayMillis <= normalIntervalMillis)
                                "error_occurred"
                            else
                                "error_occurred_again"
                        }: " +
                                "runnable=$runnable, " +
                                "nextDelayMillis=$nextDelayMillis"
                    }

                    scheduleRunWithReschedule(
                        runnable = runnable,
                        delayMillis = nextDelayMillis,
                        normalIntervalMillis = normalIntervalMillis,
                        minAbnormalIntervalMillis = minAbnormalIntervalMillis,
                        maxAbnormalIntervalMillis = maxAbnormalIntervalMillis,
                        abnormalIntervalMultiplier = abnormalIntervalMultiplier,
                    )
                }
            },
            delayMillis,
            TimeUnit.MILLISECONDS
        )
    }

    fun shutdownNow() {
        val skippedRunnables = runExecutor.shutdownNow()
        logger.info("shutdown_now: " +
                "skipped_runnables=${skippedRunnables.size}")
    }
}