package org.angryscan.app.console

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import org.angryscan.app.di.SettingsModule
import org.koin.core.context.GlobalContext.startKoin

/**
 * Main console application
 */
class ConsoleApp : CliktCommand(
    name = "angry-scanner",
    help = "Angry Data Scanner - Console Application"
) {
    
    override fun run() {
        // Initialize Koin DI if not already initialized
        if (org.koin.core.context.GlobalContext.getOrNull() == null) {
            startKoin {
                modules(SettingsModule.settingsModule)
            }
        }
    }
    
    init {
        subcommands(
            ScanCommand(),
            SettingsCommand()
        )
    }
}

fun main(args: Array<String>) {
    ConsoleApp().main(args)
}
