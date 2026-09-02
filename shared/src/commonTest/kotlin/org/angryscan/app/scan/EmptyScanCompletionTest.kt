package org.angryscan.app.scan

import org.angryscan.app.db.models.TaskState
import kotlin.test.Test
import kotlin.test.assertEquals

class EmptyScanCompletionTest {
    @Test
    fun `zero discovered files complete without entering SCANNING`() {
        assertEquals(TaskState.COMPLETED, TaskEntityViewModel.stateAfterFileDiscovery(0L))
    }

    @Test
    fun `discovered files proceed to SCANNING`() {
        assertEquals(TaskState.SCANNING, TaskEntityViewModel.stateAfterFileDiscovery(1L))
        assertEquals(TaskState.SCANNING, TaskEntityViewModel.stateAfterFileDiscovery(42L))
    }
}
