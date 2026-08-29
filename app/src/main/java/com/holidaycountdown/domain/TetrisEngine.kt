package com.holidaycountdown.domain

data class TetrisCell(val x: Int, val y: Int, val color: Int)

class TetrisEngine(
    val columns: Int = 8,
    val rows: Int = 12
) {
    private data class Shape(val cells: List<Pair<Int, Int>>, val color: Int)
    private data class FallingPiece(val shape: Shape, val x: Int, val y: Int)

    private val board = Array(rows) { IntArray(columns) }
    private val shapes = listOf(
        Shape(listOf(0 to 0, 1 to 0, 0 to 1, 1 to 1), 1), // O
        Shape(listOf(0 to 0, 1 to 0, 2 to 0, 3 to 0), 2), // I
        Shape(listOf(0 to 0, 1 to 0, 2 to 0, 1 to 1), 3), // T
        Shape(listOf(0 to 0, 0 to 1, 0 to 2, 1 to 2), 4), // L
        Shape(listOf(1 to 0, 2 to 0, 0 to 1, 1 to 1), 5)  // S
    )
    private val targetColumns = intArrayOf(0, 2, 4, 6, 0, 4, 2, 5, 1, 3)
    private var sequenceIndex = 0
    private var active: FallingPiece? = null
    private var clearDelay = 0
    private var pendingRows = emptyList<Int>()

    var clearedLines: Int = 0
        private set
    var resets: Int = 0
        private set

    init { spawn() }

    fun tick() {
        if (clearDelay > 0) {
            clearDelay--
            if (clearDelay == 0) {
                clearRows()
                spawn()
            }
            return
        }
        val piece = active ?: run { spawn(); return }
        val moved = piece.copy(y = piece.y + 1)
        if (canPlace(moved)) active = moved else lock(piece)
    }

    fun settledCells(): List<TetrisCell> = buildList {
        board.forEachIndexed { y, row -> row.forEachIndexed { x, color -> if (color != 0) add(TetrisCell(x, y, color)) } }
    }

    fun activeCells(): List<TetrisCell> = active?.let { piece ->
        piece.shape.cells.map { (dx, dy) -> TetrisCell(piece.x + dx, piece.y + dy, piece.shape.color) }
            .filter { it.y >= 0 }
    }.orEmpty()

    fun clearingRows(): Set<Int> = pendingRows.toSet()

    private fun spawn() {
        val shape = if (sequenceIndex < 4) shapes[0] else shapes[sequenceIndex % shapes.size]
        val width = shape.cells.maxOf { it.first } + 1
        val target = targetColumns[sequenceIndex % targetColumns.size].coerceIn(0, columns - width)
        sequenceIndex++
        val piece = FallingPiece(shape, target, -shape.cells.maxOf { it.second } - 1)
        if (canPlace(piece)) active = piece else resetBoard()
    }

    private fun canPlace(piece: FallingPiece): Boolean = piece.shape.cells.all { (dx, dy) ->
        val x = piece.x + dx
        val y = piece.y + dy
        x in 0 until columns && y < rows && (y < 0 || board[y][x] == 0)
    }

    private fun lock(piece: FallingPiece) {
        if (piece.shape.cells.any { piece.y + it.second < 0 }) {
            resetBoard()
            return
        }
        piece.shape.cells.forEach { (dx, dy) -> board[piece.y + dy][piece.x + dx] = piece.shape.color }
        active = null
        pendingRows = board.indices.filter { row -> board[row].all { it != 0 } }
        if (pendingRows.isNotEmpty()) clearDelay = 3 else spawn()
    }

    private fun clearRows() {
        val survivors = board.filterIndexed { index, _ -> index !in pendingRows }.map { it.copyOf() }
        repeat(rows - survivors.size) { index -> board[index].fill(0) }
        survivors.forEachIndexed { index, row -> board[rows - survivors.size + index] = row }
        clearedLines += pendingRows.size
        pendingRows = emptyList()
    }

    private fun resetBoard() {
        board.forEach { it.fill(0) }
        pendingRows = emptyList()
        clearDelay = 0
        active = null
        sequenceIndex = 0
        resets++
        spawn()
    }
}
