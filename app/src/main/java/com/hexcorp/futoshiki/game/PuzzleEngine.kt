package com.hexcorp.futoshiki.game

import kotlin.random.Random

// ── Data models ───────────────────────────────────────────────────────────────

data class Constraint(
    val r1: Int, val c1: Int,
    val r2: Int, val c2: Int,
    val gt: Boolean          // true  → cell(r1,c1) > cell(r2,c2)
)

data class Puzzle(
    val solution: List<List<Int>>,
    val constraints: List<Constraint>,
    val initial: List<List<Int>>
)

enum class Difficulty {
    EASY, MEDIUM, HARD
}

// ── Solver (backtracking Latin-square) ────────────────────────────────────────

fun generateSolution(size: Int): List<List<Int>> {
    val grid = Array(size) { IntArray(size) }

    fun isValid(row: Int, col: Int, num: Int): Boolean {
        for (i in 0 until size) {
            if (grid[row][i] == num || grid[i][col] == num) return false
        }
        return true
    }

    fun solve(pos: Int): Boolean {
        if (pos == size * size) return true
        val row = pos / size
        val col = pos % size
        val nums = (1..size).shuffled()
        for (n in nums) {
            if (isValid(row, col, n)) {
                grid[row][col] = n
                if (solve(pos + 1)) return true
                grid[row][col] = 0
            }
        }
        return false
    }

    solve(0)
    return grid.map { it.toList() }
}

fun countSolutions(initial: List<List<Int>>, constraints: List<Constraint>, size: Int, limit: Int = 2): Int {
    val grid = Array(size) { r -> IntArray(size) { c -> initial[r][c] } }
    var count = 0

    fun isValid(row: Int, col: Int, num: Int): Boolean {
        for (i in 0 until size) {
            if (grid[row][i] == num || grid[i][col] == num) return false
        }
        for (cn in constraints) {
            val v1 = if (cn.r1 == row && cn.c1 == col) num else grid[cn.r1][cn.c1]
            val v2 = if (cn.r2 == row && cn.c2 == col) num else grid[cn.r2][cn.c2]
            if (v1 != 0 && v2 != 0) {
                if (cn.gt && v1 <= v2) return false
                if (!cn.gt && v1 >= v2) return false
            }
        }
        return true
    }

    fun solve(pos: Int): Boolean {
        if (pos == size * size) {
            count++
            return count >= limit
        }
        val row = pos / size
        val col = pos % size
        if (grid[row][col] != 0) return solve(pos + 1)
        for (n in 1..size) {
            if (isValid(row, col, n)) {
                grid[row][col] = n
                if (solve(pos + 1)) return true
                grid[row][col] = 0
            }
        }
        return false
    }

    solve(0)
    return count
}

// ── Constraint generator ──────────────────────────────────────────────────────

fun generateConstraints(solution: List<List<Int>>, size: Int, count: Int): List<Constraint> {
    val pairs = mutableListOf<Pair<Pair<Int,Int>, Pair<Int,Int>>>()
    // Horizontal pairs
    for (r in 0 until size)
        for (c in 0 until size - 1)
            pairs.add(Pair(r, c) to Pair(r, c + 1))
    // Vertical pairs
    for (r in 0 until size - 1)
        for (c in 0 until size)
            pairs.add(Pair(r, c) to Pair(r + 1, c))

    return pairs.shuffled().take(count).map { (a, b) ->
        val (r1, c1) = a
        val (r2, c2) = b
        Constraint(r1, c1, r2, c2, gt = solution[r1][c1] > solution[r2][c2])
    }
}

// ── Full puzzle builder ───────────────────────────────────────────────────────

private fun fillPercent(difficulty: Difficulty): Double = when (difficulty) {
    Difficulty.EASY   -> 0.70
    Difficulty.MEDIUM -> 0.50
    Difficulty.HARD   -> 0.20
}

private fun hasEmptyCellsInAllRowsAndCols(grid: Array<IntArray>, size: Int): Boolean {
    for (r in 0 until size) if (grid[r].all { it != 0 }) return false
    for (c in 0 until size) if ((0 until size).all { grid[it][c] != 0 }) return false
    return true
}

private fun makeRowColSatisfied(grid: Array<IntArray>, size: Int, rng: Random) {
    val totalCells = size * size
    val maxAttempts = totalCells * 2
    repeat(maxAttempts) {
        if (hasEmptyCellsInAllRowsAndCols(grid, size)) return
        val filledRows = (0 until size).filter { r -> grid[r].all { it != 0 } }
        val filledCols = (0 until size).filter { c -> (0 until size).all { grid[it][c] != 0 } }
        if (filledRows.isEmpty() && filledCols.isEmpty()) return
        val r = filledRows.randomOrNull() ?: filledCols.randomOrNull()!!
        val prefilled = (0 until size).filter { grid[r][it] != 0 }
        if (prefilled.isNotEmpty()) {
            val c = prefilled.random()
            grid[r][c] = 0
        }
    }
}

fun generatePuzzle(size: Int, difficulty: Difficulty = Difficulty.EASY): Puzzle {
    val rng = Random.Default
    val totalCells = size * size
    val targetFill = (totalCells * fillPercent(difficulty)).toInt().coerceAtLeast(1)

    val baseConstraints = when (size) {
        3 -> 2; 4 -> 4; 5 -> 6; else -> 8
    }
    val constraintCount = when (difficulty) {
        Difficulty.EASY   -> baseConstraints + 2
        Difficulty.MEDIUM -> baseConstraints
        Difficulty.HARD   -> baseConstraints - 1
    }.coerceAtLeast(1)

    while (true) {
        val solution = generateSolution(size)
        val constraints = generateConstraints(solution, size, constraintCount)
        val allCells = (0 until size).flatMap { r -> (0 until size).map { c -> r to c } }.shuffled(rng)

        val grid = Array(size) { IntArray(size) }
        val fillCount = targetFill.coerceAtMost(totalCells - size)
        allCells.take(fillCount).forEach { (r, c) -> grid[r][c] = solution[r][c] }

        makeRowColSatisfied(grid, size, rng)

        if (!hasEmptyCellsInAllRowsAndCols(grid, size)) {
            continue
        }

        val initialGrid = grid.map { it.toList() }
        if (countSolutions(initialGrid, constraints, size) == 1) {
            return Puzzle(solution, constraints, initialGrid)
        }

        for ((r, c) in allCells.drop(fillCount)) {
            if (grid[r][c] == 0) {
                grid[r][c] = solution[r][c]
                if (countSolutions(grid.map { it.toList() }, constraints, size) == 1) {
                    return Puzzle(solution, constraints, grid.map { it.toList() })
                }
            }
        }
    }
}

// ── Validation ────────────────────────────────────────────────────────────────

fun checkConstraints(grid: List<List<Int>>, constraints: List<Constraint>): Set<String> {
    val violations = mutableSetOf<String>()
    for (cn in constraints) {
        val v1 = grid[cn.r1][cn.c1]
        val v2 = grid[cn.r2][cn.c2]
        if (v1 != 0 && v2 != 0) {
            val ok = if (cn.gt) v1 > v2 else v1 < v2
            if (!ok) {
                violations.add("${cn.r1},${cn.c1}")
                violations.add("${cn.r2},${cn.c2}")
            }
        }
    }
    return violations
}

fun checkDuplicates(grid: List<List<Int>>, size: Int): Set<String> {
    val dups = mutableSetOf<String>()
    // Rows
    for (r in 0 until size) {
        val seen = mutableMapOf<Int, Int>()
        for (c in 0 until size) {
            val v = grid[r][c]
            if (v != 0) {
                if (seen.containsKey(v)) { dups.add("$r,${seen[v]!!}"); dups.add("$r,$c") }
                else seen[v] = c
            }
        }
    }
    // Columns
    for (c in 0 until size) {
        val seen = mutableMapOf<Int, Int>()
        for (r in 0 until size) {
            val v = grid[r][c]
            if (v != 0) {
                if (seen.containsKey(v)) { dups.add("${seen[v]!!},$c"); dups.add("$r,$c") }
                else seen[v] = r
            }
        }
    }
    return dups
}

fun validateGrid(grid: List<List<Int>>, size: Int, puzzle: Puzzle): Set<String> {
    val cv = checkConstraints(grid, puzzle.constraints)
    val dv = checkDuplicates(grid, size)
    return cv + dv
}

fun isWon(grid: List<List<Int>>, errors: Set<String>): Boolean {
    val full = grid.all { row -> row.all { it != 0 } }
    return full && errors.isEmpty()
}

fun getCompletedRows(grid: List<List<Int>>, size: Int, errors: Set<String>): Set<Int> {
    if (errors.isNotEmpty()) return emptySet()
    val completed = mutableSetOf<Int>()
    for (r in 0 until size) {
        val rowValues = grid[r]
        if (rowValues.all { it != 0 } && rowValues.toSet().size == size) {
            completed.add(r)
        }
    }
    return completed
}

fun getCompletedRowsCount(grid: List<List<Int>>, size: Int, errors: Set<String>): Int {
    return getCompletedRows(grid, size, errors).size
}
