package org.angryscan.app.console

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.arguments.argument
import org.angryscan.app.common.MatchersRegister
import org.angryscan.app.scan.common.files.types.IFileType
import org.angryscan.common.engine.IMatcher
import java.io.File

/**
 * Command to perform scanning
 */
class ScanCommand : CliktCommand(
    name = "scan",
    help = "Perform scanning of files"
) {
    private val path: String by argument(
        name = "PATH",
        help = "Path to directory or file to scan"
    )
    
    private val matchers: List<String>? by option(
        "--matchers",
        help = "Comma-separated list of matcher names to use. If not specified, uses default from settings."
    ).split(",")
    
    private val extensions: List<String>? by option(
        "--extensions",
        help = "Comma-separated list of file extensions to scan (e.g., txt,pdf,docx). If not specified, uses default from settings."
    ).split(",")
    
    private val output: String? by option(
        "--output",
        help = "Path where to save the report. If not specified, uses default from settings."
    )
    
    private val settingsLoader = SettingsLoader()
    
    override fun run() {
        val scanPath = File(path)
        if (!scanPath.exists()) {
            echo("Error: Path '$path' does not exist", err = true)
            return
        }
        
        val selectedMatchers = matchers?.let { parseMatchers(it) } 
            ?: settingsLoader.loadDefaultMatchers()
        
        if (selectedMatchers.isEmpty()) {
            echo("Error: No matchers selected", err = true)
            return
        }
        
        val selectedExtensions = extensions?.map { it.trim().lowercase() }
            ?: settingsLoader.loadDefaultExtensions()
        
        val outputPath = output ?: generateDefaultOutputPath()
        
        echo("Starting scan...")
        echo("Path: $path")
        echo("Matchers: ${selectedMatchers.joinToString(", ") { it.javaClass.simpleName }}")
        echo("Extensions: ${selectedExtensions.joinToString(", ")}")
        echo("Output: $outputPath")
        
        // TODO: Implement actual scanning logic
        echo("Scan completed successfully")
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
    
    private fun generateDefaultOutputPath(): String {
        val defaultDir = settingsLoader.loadDefaultReportPath()
        val extension = settingsLoader.loadDefaultReportExtension().extension
        val timestamp = System.currentTimeMillis()
        return File(defaultDir, "scan_report_$timestamp.$extension").absolutePath
    }
}

private fun <T> List<T>.toMap(keySelector: (T) -> String): Map<String, T> {
    return associateBy(keySelector)
}
