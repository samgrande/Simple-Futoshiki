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

fun generateSolution(size: Int, rng: Random = Random.Default): List<List<Int>> {
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
        val nums = (1..size).shuffled(rng)
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

fun generateConstraints(solution: List<List<Int>>, size: Int, count: Int, rng: Random = Random.Default): List<Constraint> {
    val pairs = mutableListOf<Pair<Pair<Int,Int>, Pair<Int,Int>>>()
    // Horizontal pairs
    for (r in 0 until size)
        for (c in 0 until size - 1)
            pairs.add(Pair(r, c) to Pair(r, c + 1))
    // Vertical pairs
    for (r in 0 until size - 1)
        for (c in 0 until size)
            pairs.add(Pair(r, c) to Pair(r + 1, c))

    return pairs.shuffled(rng).take(count).map { (a, b) ->
        val (r1, c1) = a
        val (r2, c2) = b
        Constraint(r1, c1, r2, c2, gt = solution[r1][c1] > solution[r2][c2])
    }
}

// ── Full puzzle builder ───────────────────────────────────────────────────────

// Each difficulty starts at its nominal fill budget and escalates through tiers
// if a unique solution can't be reached, so a playable puzzle is always returned.
// Boards 4x4 and 5x5 keep the original fill profile; 6x6+ uses denser easy/medium
// tiers so they read clearly easier than hard (which stays sparse and compensates
// with extra constraints).
private data class FillTier(val min: Double, val max: Double, val cap: Double)

private fun fillTiers(size: Int, difficulty: Difficulty): List<FillTier> {
    val isBig = size >= 6
    return when (difficulty) {
        Difficulty.EASY -> if (isBig) listOf(
            FillTier(0.60, 0.70, 0.70),
            FillTier(0.70, 0.75, 0.75)
        ) else listOf(
            FillTier(0.50, 0.60, 0.60),
            FillTier(0.60, 0.65, 0.65)
        )
        Difficulty.MEDIUM -> if (isBig) listOf(
            FillTier(0.45, 0.55, 0.55),
            FillTier(0.55, 0.60, 0.60)
        ) else listOf(
            FillTier(0.30, 0.40, 0.40),
            FillTier(0.40, 0.50, 0.50),
            FillTier(0.50, 0.55, 0.55)
        )
        Difficulty.HARD -> if (isBig) listOf(
            FillTier(0.00, 0.20, 0.20),
            FillTier(0.20, 0.30, 0.30),
            FillTier(0.30, 0.40, 0.40)
        ) else listOf(
            FillTier(0.00, 0.20, 0.20),
            FillTier(0.20, 0.35, 0.35),
            FillTier(0.35, 0.45, 0.45),
            FillTier(0.45, 0.55, 0.55)
        )
    }
}

private fun hasEmptyCellsInAllRowsAndCols(grid: Array<IntArray>, size: Int): Boolean {
    for (r in 0 until size) if (grid[r].all { it != 0 }) return false
    for (c in 0 until size) if ((0 until size).all { grid[it][c] != 0 }) return false
    return true
}

private fun makeRowColSatisfied(grid: Array<IntArray>, size: Int, rng: Random) {
    val maxAttempts = size * size * 2
    repeat(maxAttempts) {
        if (hasEmptyCellsInAllRowsAndCols(grid, size)) return
        val filledRows = (0 until size).filter { r -> grid[r].all { it != 0 } }
        val filledCols = (0 until size).filter { c -> (0 until size).all { grid[it][c] != 0 } }
        if (filledRows.isEmpty() && filledCols.isEmpty()) return
        if (filledRows.isNotEmpty()) {
            val r = filledRows.random(rng)
            val prefilled = (0 until size).filter { grid[r][it] != 0 }
            if (prefilled.isNotEmpty()) {
                grid[r][prefilled.random(rng)] = 0
            }
        } else {
            val c = filledCols.random(rng)
            val prefilled = (0 until size).filter { grid[it][c] != 0 }
            if (prefilled.isNotEmpty()) {
                grid[prefilled.random(rng)][c] = 0
            }
        }
    }
}

private fun tryFillLevel(
    size: Int,
    constraintCount: Int,
    targetFill: Int,
    maxFill: Int,
    totalCells: Int,
    rng: Random,
    maxAttempts: Int = 200
): Puzzle? {
    repeat(maxAttempts) {
        val solution = generateSolution(size, rng)
        val constraints = generateConstraints(solution, size, constraintCount, rng)
        val allCells = (0 until size).flatMap { r -> (0 until size).map { c -> r to c } }.shuffled(rng)

        val grid = Array(size) { IntArray(size) }
        val fillCount = targetFill.coerceAtMost(totalCells - size)
        allCells.take(fillCount).forEach { (r, c) -> grid[r][c] = solution[r][c] }

        makeRowColSatisfied(grid, size, rng)

        if (!hasEmptyCellsInAllRowsAndCols(grid, size)) {
            return@repeat
        }

        val initialGrid = grid.map { it.toList() }
        if (countSolutions(initialGrid, constraints, size) == 1) {
            return Puzzle(solution, constraints, initialGrid)
        }

        var exceeded = false
        for ((r, c) in allCells.drop(fillCount)) {
            if (grid[r][c] == 0) {
                grid[r][c] = solution[r][c]
                val currentFill = grid.sumOf { row -> row.count { it != 0 } }
                if (currentFill > maxFill) {
                    exceeded = true
                    break
                }
                if (countSolutions(grid.map { it.toList() }, constraints, size) == 1) {
                    return Puzzle(solution, constraints, grid.map { it.toList() })
                }
            }
        }
        if (exceeded) return@repeat
    }
    return null
}

fun generatePuzzle(size: Int, difficulty: Difficulty = Difficulty.EASY, rng: Random = Random.Default): Puzzle {
    val totalCells = size * size
    val baseConstraints = when (size) {
        3 -> 2; 4 -> 4; 5 -> 6; else -> 10
    }
    // HARD = more constraints (compensates for fewer prefilled cells)
    // EASY = fewer constraints (more ambiguity offset by more prefilled)
    // On 6x6+ hard needs extra arrows to stay unique while keeping the board sparse.
    val hardBonus = if (size >= 6) 4 else 2
    val constraintCount = when (difficulty) {
        Difficulty.EASY   -> (baseConstraints - 1).coerceAtLeast(1)
        Difficulty.MEDIUM -> baseConstraints
        Difficulty.HARD   -> baseConstraints + hardBonus
    }

    for (tier in fillTiers(size, difficulty)) {
        val targetFill = (totalCells * rng.nextDouble(tier.min, tier.max)).toInt().coerceAtLeast(1)
        val maxFill = (totalCells * tier.cap).toInt().coerceAtLeast(1)
        val puzzle = tryFillLevel(size, constraintCount, targetFill, maxFill, totalCells, rng)
        if (puzzle != null) return puzzle
    }

    // Absolute last resort: always return a playable, unique, winnable puzzle.
    return guaranteedPlayablePuzzle(size, constraintCount, rng)
}

// A fully solved grid with exactly one cell cleared is guaranteed to have a
// unique completion (the cleared value is forced by its row), so this is a safe
// final fallback that keeps constraints (arrows) and remains playable.
private fun guaranteedPlayablePuzzle(size: Int, constraintCount: Int, rng: Random): Puzzle {
    val solution = generateSolution(size, rng)
    val constraints = generateConstraints(solution, size, constraintCount, rng)
    val grid = solution.map { it.toMutableList() }
    val r = rng.nextInt(size)
    val c = rng.nextInt(size)
    grid[r][c] = 0
    return Puzzle(solution, constraints, grid.map { it.toList() })
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


