package org.angryscan.app.console

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SettingsLoaderTest {
    
    @Test
    fun testLoadDefaultMatchers() {
        // SettingsLoader should load matchers from ScanSettings
        val loader = SettingsLoader()
        val matchers = loader.loadDefaultMatchers()
        assertNotNull(matchers, "Default matchers should not be null")
        assertTrue(matchers.isNotEmpty(), "Default matchers should not be empty")
    }
    
    @Test
    fun testLoadDefaultExtensions() {
        // SettingsLoader should load extensions from ScanSettings
        val loader = SettingsLoader()
        val extensions = loader.loadDefaultExtensions()
        assertNotNull(extensions, "Default extensions should not be null")
        assertTrue(extensions.isNotEmpty(), "Default extensions should not be empty")
    }
    
    @Test
    fun testLoadDefaultReportPath() {
        // SettingsLoader should load report path from AppSettings
        val loader = SettingsLoader()
        val reportPath = loader.loadDefaultReportPath()
        assertNotNull(reportPath, "Default report path should not be null")
    }
}
