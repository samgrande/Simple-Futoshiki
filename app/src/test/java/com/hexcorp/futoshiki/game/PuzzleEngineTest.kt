package com.hexcorp.futoshiki.game

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class PuzzleEngineTest {

    @Test
    fun `generateSolution produces valid Latin square`() {
        for (size in 3..5) {
            val solution = generateSolution(size, Random(42))
            assertEquals(size, solution.size)
            solution.forEach { row ->
                assertEquals(size, row.size)
                assertEquals((1..size).toSet(), row.toSet())
            }
            for (c in 0 until size) {
                val col = (0 until size).map { solution[it][c] }
                assertEquals((1..size).toSet(), col.toSet())
            }
        }
    }

    @Test
    fun `generateSolution is deterministic with seeded RNG`() {
        val a = generateSolution(4, Random(123))
        val b = generateSolution(4, Random(123))
        assertEquals(a, b)
    }

    @Test
    fun `generateSolution produces different results with different seeds`() {
        val a = generateSolution(4, Random(123))
        val b = generateSolution(4, Random(456))
        assertNotEquals(a, b)
    }

    @Test
    fun `countSolutions returns 1 for a complete grid`() {
        val solution = generateSolution(4, Random(42))
        assertEquals(1, countSolutions(solution, emptyList(), 4))
    }

    @Test
    fun `countSolutions returns more than 1 for empty grid`() {
        val empty = List(3) { List(3) { 0 } }
        assertTrue(countSolutions(empty, emptyList(), 3, limit = 10) > 1)
    }

    @Test
    fun `generatePuzzle is deterministic with seeded RNG`() {
        val a = generatePuzzle(4, Difficulty.EASY, Random(99))
        val b = generatePuzzle(4, Difficulty.EASY, Random(99))
        assertEquals(a.solution, b.solution)
        assertEquals(a.initial, b.initial)
        assertEquals(a.constraints, b.constraints)
    }

    @Test
    fun `generatePuzzle produces puzzle with unique solution`() {
        val puzzle = generatePuzzle(4, Difficulty.EASY, Random(42))
        assertEquals(1, countSolutions(puzzle.initial, puzzle.constraints, 4))
    }

    @Test
    fun `generatePuzzle never returns a degenerate unplayable puzzle`() {
        for (size in 3..6) {
            for (diff in Difficulty.entries) {
                for (seed in 1L..3L) {
                    val puzzle = generatePuzzle(size, diff, Random(seed * 100L + size))
                    val tag = "size=$size diff=$diff seed=$seed"
                    val count = countSolutions(puzzle.initial, puzzle.constraints, size)
                    assertTrue("not unique: $tag (solutions=$count)", count == 1)
                    assertTrue("no arrows: $tag", puzzle.constraints.isNotEmpty())
                    assertTrue(
                        "fully prefilled (unwinnable): $tag",
                        puzzle.initial.any { row -> row.any { it == 0 } }
                    )
                    assertTrue("solution invalid: $tag", validateGrid(puzzle.solution, size, puzzle).isEmpty())
                }
            }
        }
    }

    @Test
    fun `size 6 easy is denser than medium which is denser than hard`() {
        fun avgFill(diff: Difficulty): Double {
            var total = 0
            var n = 0
            for (seed in 1L..3L) {
                val p = generatePuzzle(6, diff, Random(seed * 100L + 6))
                total += p.initial.sumOf { row -> row.count { it != 0 } }
                n++
            }
            return total.toDouble() / n
        }
        val easy = avgFill(Difficulty.EASY)
        val medium = avgFill(Difficulty.MEDIUM)
        val hard = avgFill(Difficulty.HARD)
        assertTrue("easy=$easy medium=$medium hard=$hard", easy > medium)
        assertTrue("easy=$easy medium=$medium hard=$hard", medium > hard)
    }

    @Test
    fun `validateGrid returns no errors for correct solution`() {
        val puzzle = generatePuzzle(4, Difficulty.EASY, Random(42))
        assertTrue(validateGrid(puzzle.solution, 4, puzzle).isEmpty())
    }

    @Test
    fun `checkConstraints detects violations`() {
        val constraints = listOf(Constraint(0, 0, 0, 1, gt = true))
        val grid = listOf(listOf(1, 2), listOf(3, 4))
        assertTrue(checkConstraints(grid, constraints).isNotEmpty())
    }

    @Test
    fun `checkConstraints passes satisfied constraints`() {
        val constraints = listOf(Constraint(0, 0, 0, 1, gt = true))
        val grid = listOf(listOf(2, 1), listOf(3, 4))
        assertTrue(checkConstraints(grid, constraints).isEmpty())
    }

    @Test
    fun `checkDuplicates detects row duplicates`() {
        val grid = listOf(listOf(1, 2, 1), listOf(3, 4, 5), listOf(6, 7, 8))
        assertTrue(checkDuplicates(grid, 3).isNotEmpty())
    }

    @Test
    fun `checkDuplicates detects column duplicates`() {
        val grid = listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(1, 7, 8))
        assertTrue(checkDuplicates(grid, 3).isNotEmpty())
    }

    @Test
    fun `checkDuplicates passes clean grid`() {
        val grid = listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9))
        assertTrue(checkDuplicates(grid, 3).isEmpty())
    }

    @Test
    fun `isWon returns true for full correct grid`() {
        val puzzle = generatePuzzle(3, Difficulty.EASY, Random(42))
        assertTrue(isWon(puzzle.solution, emptySet()))
    }

    @Test
    fun `isWon returns false for incomplete grid`() {
        val grid = listOf(listOf(1, 2), listOf(3, 0))
        assertFalse(isWon(grid, emptySet()))
    }

    @Test
    fun `isWon returns false when errors present`() {
        val grid = listOf(listOf(1, 2), listOf(3, 4))
        assertFalse(isWon(grid, setOf("0,0")))
    }
}
