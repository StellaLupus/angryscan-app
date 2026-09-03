package org.angryscan.app.ui

import org.angryscan.app.ui.components.SelectionTypes
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileShareScanPathsTest {

    @Test
    fun `File mode scans csv content path itself not as path list`() {
        val dir = createTempDirectory(prefix = "csv-file-mode-").toFile()
        try {
            val csv = File(dir, "data.csv")
            // Data rows look like paths / free text — must NOT be expanded in File mode.
            csv.writeText(
                """
                id,name,note
                1,Alice,/tmp/does-not-exist-a.pdf
                2,Bob,/tmp/does-not-exist-b.pdf
                3,Carol,plain text without slash
                """.trimIndent()
            )

            val resolved = FileShareScanPaths.resolve(csv.absolutePath, SelectionTypes.File)

            assertEquals(SelectionTypes.File, resolved.selectionType)
            assertEquals(csv.absolutePath, resolved.scanPath)
            assertEquals(null, resolved.listFilePath)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `FileWithPaths expands only existing paths from list file`() {
        val dir = createTempDirectory(prefix = "csv-paths-mode-").toFile()
        try {
            val real1 = File(dir, "a.txt").apply { writeText("a") }
            val real2 = File(dir, "b.txt").apply { writeText("b") }
            val list = File(dir, "paths.csv")
            list.writeText(
                """
                ${real1.absolutePath}
                ${dir.resolve("missing.txt").absolutePath}
                ${real2.absolutePath}
                
                """.trimIndent()
            )

            val resolved = FileShareScanPaths.resolve(list.absolutePath, SelectionTypes.FileWithPaths)

            assertEquals(SelectionTypes.FileWithPaths, resolved.selectionType)
            assertEquals(list.absolutePath, resolved.listFilePath)
            val paths = resolved.scanPath.split(";").filter { it.isNotEmpty() }
            assertEquals(listOf(real1.absolutePath, real2.absolutePath), paths)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `guessUiSelectionType does not force FileWithPaths for csv unless explicit`() {
        val dir = createTempDirectory(prefix = "csv-guess-").toFile()
        try {
            val csv = File(dir, "report.csv").apply { writeText("a,b\n1,2\n") }

            assertEquals(
                SelectionTypes.File,
                FileShareScanPaths.guessUiSelectionType(csv.absolutePath, SelectionTypes.File)
            )
            assertEquals(
                SelectionTypes.FileWithPaths,
                FileShareScanPaths.guessUiSelectionType(csv.absolutePath, SelectionTypes.FileWithPaths)
            )
            assertEquals(
                SelectionTypes.Folder,
                FileShareScanPaths.guessUiSelectionType(dir.absolutePath, SelectionTypes.File)
            )
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `legacy auto FileWithPaths for any csv is wrong behavior`() {
        // Documents the regression: treating every csv as a path list.
        val dir = createTempDirectory(prefix = "csv-legacy-").toFile()
        try {
            val csv = File(dir, "customers.csv")
            csv.writeText("name,city\nAda,London\nGrace,Berlin\n")
            val legacyForced = csv.isFile && csv.extension.lowercase() in setOf("txt", "csv")
            assertTrue(legacyForced)

            val resolvedAsFile = FileShareScanPaths.resolve(csv.absolutePath, SelectionTypes.File)
            assertEquals(csv.absolutePath, resolvedAsFile.scanPath)
            assertTrue(resolvedAsFile.scanPath.split(";").size == 1)
        } finally {
            dir.deleteRecursively()
        }
    }
}
