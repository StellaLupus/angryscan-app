package org.angryscan.app.console

import org.angryscan.app.common.AppSettings
import org.angryscan.app.common.ScanSettings
import org.angryscan.app.scan.common.writer.ResultWriter
import org.angryscan.common.engine.IMatcher
import org.angryscan.common.extensions.Matchers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

/**
 * Loader for default settings from AppSettings and ScanSettings
 */
class SettingsLoader : KoinComponent {
    private val appSettings: AppSettings by inject()
    private val scanSettings: ScanSettings by inject()
    
    /**
     * Load default matchers from ScanSettings
     */
    fun loadDefaultMatchers(): List<IMatcher> {
        return scanSettings.matchers.toList()
    }
    
    /**
     * Load default file extensions from ScanSettings
     */
    fun loadDefaultExtensions(): List<String> {
        return scanSettings.extensions.flatMap { it.extensions }
    }
    
    /**
     * Load default report save path from AppSettings
     * Returns directory path where reports should be saved
     */
    fun loadDefaultReportPath(): String {
        // Default to user home directory/reports
        return File(System.getProperty("user.home"), "reports").absolutePath
    }
    
    /**
     * Get default report file extension from AppSettings
     */
    fun loadDefaultReportExtension(): ResultWriter.FileExtensions {
        return appSettings.reportSaveExtension.value
    }
}
