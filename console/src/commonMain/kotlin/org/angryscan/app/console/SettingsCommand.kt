package org.angryscan.app.console

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.split
import org.angryscan.app.common.AppSettings
import org.angryscan.app.common.MatchersRegister
import org.angryscan.app.common.ScanSettings
import org.angryscan.app.scan.common.writer.ResultWriter
import org.angryscan.common.engine.IMatcher
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Command to manage settings
 */
class SettingsCommand : CliktCommand(
    name = "settings"
), KoinComponent {
    
    override fun run() {
        // Show help if no subcommand provided
        echo(context.formattedHelp())
    }
    
    init {
        subcommands(
            SettingsListCommand(),
            SettingsSetCommand(),
            SettingsSaveCommand()
        )
    }
}

/**
 * Command to list current settings
 */
class SettingsListCommand : CliktCommand(
    name = "list"
), KoinComponent {
    
    private val appSettings: AppSettings by inject()
    private val scanSettings: ScanSettings by inject()
    
    override fun run() {
        echo("=== Application Settings ===")
        echo("Report extension: ${appSettings.reportSaveExtension.value}")
        echo("Thread count: ${appSettings.threadCount.value}")
        echo("Debug mode: ${appSettings.debugMode.value}")
        
        echo("\n=== Scan Settings ===")
        echo("Matchers (${scanSettings.matchers.size}):")
        scanSettings.matchers.forEach { matcher ->
            echo("  - ${matcher.javaClass.simpleName}")
        }
        
        echo("\nFile extensions (${scanSettings.extensions.size}):")
        scanSettings.extensions.forEach { fileType ->
            echo("  - ${fileType.name}: ${fileType.extensions.joinToString(", ")}")
        }
        
        echo("\nFast scan: ${scanSettings.fastScan.value}")
    }
}

/**
 * Command to set settings
 */
class SettingsSetCommand : CliktCommand(
    name = "set"
), KoinComponent {
    
    private val matchers: List<String>? by option(
        "--matchers",
        help = "Comma-separated list of matcher names"
    ).split(",")
    
    private val extensions: List<String>? by option(
        "--extensions",
        help = "Comma-separated list of file extensions"
    ).split(",")
    
    private val outputPath: String? by option(
        "--output-path",
        help = "Default path for saving reports"
    )
    
    private val reportExtension: String? by option(
        "--report-extension",
        help = "Default report file extension (csv, xlsx, xml)"
    )
    
    private val scanSettings: ScanSettings by inject()
    private val appSettings: AppSettings by inject()
    
    override fun run() {
        var changed = false
        
        matchers?.let { matcherNames ->
            val parsedMatchers = parseMatchers(matcherNames)
            if (parsedMatchers.isNotEmpty()) {
                scanSettings.matchers.clear()
                scanSettings.matchers.addAll(parsedMatchers)
                echo("Matchers updated: ${parsedMatchers.size} matchers selected")
                changed = true
            }
        }
        
        extensions?.let { extNames ->
            val parsedExtensions = parseExtensions(extNames)
            if (parsedExtensions.isNotEmpty()) {
                scanSettings.extensions.clear()
                scanSettings.extensions.addAll(parsedExtensions)
                echo("Extensions updated: ${parsedExtensions.size} extensions selected")
                changed = true
            }
        }
        
        reportExtension?.let { ext ->
            val fileExt = try {
                ResultWriter.FileExtensions.valueOf(ext.uppercase())
            } catch (e: IllegalArgumentException) {
                echo("Error: Invalid report extension '$ext'. Valid values: csv, xlsx, xml", err = true)
                return
            }
            appSettings.reportSaveExtension.value = fileExt
            echo("Report extension updated: $ext")
            changed = true
        }
        
        if (!changed) {
            echo("No settings were changed. Use options to specify values to set.")
        }
    }
    
    private fun parseMatchers(matcherNames: List<String>): List<IMatcher> {
        val availableMatchers = MatchersRegister.toMap { it.javaClass.simpleName }
        val selected = mutableListOf<IMatcher>()
        
        for (name in matcherNames) {
            val trimmed = name.trim()
            val matcher = availableMatchers[trimmed] 
                ?: availableMatchers.values.find { 
                    it.javaClass.simpleName.equals(trimmed, ignoreCase = true) 
                }
            
            if (matcher != null) {
                selected.add(matcher)
            } else {
                echo("Warning: Matcher '$trimmed' not found, skipping", err = true)
            }
        }
        
        return selected
    }
    
    private fun parseExtensions(extNames: List<String>): List<org.angryscan.app.scan.common.files.types.IFileType> {
        val allFileTypes = org.angryscan.app.scan.common.files.types.IFileType.getAll()
        val selected = mutableSetOf<org.angryscan.app.scan.common.files.types.IFileType>()
        
        for (extName in extNames) {
            val trimmed = extName.trim().lowercase().removePrefix(".")
            val matchingFileTypes = allFileTypes.filter { fileType ->
                fileType.extensions.any { it.lowercase() == trimmed }
            }
            selected.addAll(matchingFileTypes)
        }
        
        return selected.toList()
    }
}

/**
 * Command to save settings
 */
class SettingsSaveCommand : CliktCommand(
    name = "save"
), KoinComponent {
    
    private val scanSettings: ScanSettings by inject()
    private val appSettings: AppSettings by inject()
    
    override fun run() {
        try {
            scanSettings.save()
            appSettings.save()
            echo("Settings saved successfully")
        } catch (e: Exception) {
            echo("Error: Failed to save settings: ${e.message}", err = true)
        }
    }
}

private fun <T> List<T>.toMap(keySelector: (T) -> String): Map<String, T> {
    return associateBy(keySelector)
}
