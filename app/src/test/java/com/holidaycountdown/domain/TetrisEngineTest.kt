package com.holidaycountdown.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TetrisEngineTest {
    @Test fun fullRowsFlashAndDisappear() {
        val engine = TetrisEngine()
        var sawClearing = false
        repeat(160) {
            engine.tick()
            if (engine.clearingRows().isNotEmpty()) sawClearing = true
        }
        assertTrue(sawClearing)
        assertTrue(engine.clearedLines >= 2)
        assertTrue(engine.settledCells().size < 8 * 12)
    }

    @Test fun everyCellStaysInsideBoard() {
        val engine = TetrisEngine()
        repeat(1_500) { engine.tick() }
        (engine.settledCells() + engine.activeCells()).forEach {
            assertTrue(it.x in 0 until engine.columns)
            assertTrue(it.y in 0 until engine.rows)
        }
    }
}
