package com.hex.futoshiki.game

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

fun generatePuzzle(size: Int): Puzzle {
    val solution = generateSolution(size)
    val constraintCount = when (size) { 4 -> 4; 5 -> 6; else -> 8 }
    val constraints = generateConstraints(solution, size, constraintCount)

    val revealCount = when (size) { 4 -> 4; 5 -> 5; else -> 6 }
    val allCells = (0 until size).flatMap { r -> (0 until size).map { c -> r to c } }
    val revealed = allCells.shuffled().take(revealCount)

    val initial = Array(size) { IntArray(size) }
    for ((r, c) in revealed) initial[r][c] = solution[r][c]

    return Puzzle(solution, constraints, initial.map { it.toList() })
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
