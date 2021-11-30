package ua.com.radiokot.feed.updater.di

import mu.KLogger
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE

class KLoggerKoinLogger(
    private val kLogger: KLogger
) : Logger(Level.ERROR) {
    override fun log(level: Level, msg: MESSAGE) {
        when (level) {
            Level.DEBUG -> kLogger.debug { msg }
            Level.INFO -> kLogger.info { msg }
            Level.ERROR -> kLogger.error { msg }
            Level.NONE -> {
            }
        }
    }
}