package com.hexcorp.futoshiki.ui.screens.game

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.hexcorp.futoshiki.game.Constraint
import com.hexcorp.futoshiki.game.Puzzle
import com.hexcorp.futoshiki.ui.components.shared.ArrowDirection
import com.hexcorp.futoshiki.ui.components.shared.ConstraintArrow

@Composable
fun PuzzleBoard(
    puzzle: Puzzle,
    grid: List<List<Int>>,
    size: Int,
    selected: Pair<Int, Int>?,
    errors: Set<String>,
    cellSizeDp: Dp,
    arrowSlotDp: Dp,
    gameKey: Int,
    isSolved: Boolean,
    onCellTap: (Int, Int) -> Unit,
    onCellClear: (Int, Int) -> Unit
) {
    val totalItems = (size * 2 - 1)

    val givenCount = remember(puzzle) {
        puzzle.initial.sumOf { row -> row.count { it != 0 } }
    }

    val constraintsMap = remember(puzzle) {
        val map = mutableMapOf<String, Constraint>()
        puzzle.constraints.forEach {
            map["${it.r1},${it.c1},${it.r2},${it.c2}"] = it
            map["${it.r2},${it.c2},${it.r1},${it.c1}"] = it
        }
        map
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (ri in 0 until totalItems) {
            key(ri) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (ci in 0 until totalItems) {
                        val isCell = ri % 2 == 0 && ci % 2 == 0
                        val isHCon = ri % 2 == 0 && ci % 2 == 1
                        val isVCon = ri % 2 == 1 && ci % 2 == 0

                        when {
                            isCell -> {
                                val r = ri / 2
                                val c = ci / 2
                                val val_ = grid[r][c]
                                val isGiven    = puzzle.initial[r][c] != 0
                                val isSelected = selected?.first == r && selected.second == c
                                val isRelated  = selected != null && !isSelected &&
                                                 (selected.first == r || selected.second == c)
                                val hasError   = errors.contains("$r,$c")
                                val cellIdx    = r * size + c
                                val delay      = cellIdx * 22

                                key(r, c) {
                                    PuzzleCell(
                                        value      = val_,
                                        isGiven    = isGiven,
                                        isSelected = isSelected,
                                        isRelated  = isRelated,
                                        hasError   = hasError,
                                        sizeDp     = cellSizeDp,
                                        animDelay  = delay,
                                        gameKey    = gameKey,
                                        givenCount = givenCount,
                                        r          = r,
                                        c          = c,
                                        isSolved   = isSolved,
                                        onTap      = onCellTap,
                                        onClear    = onCellClear
                                    )
                                }
                            }

                            isHCon -> {
                                val r  = ri / 2
                                val c1 = (ci - 1) / 2
                                val c2 = (ci + 1) / 2
                                val cn = constraintsMap["$r,$c1,$r,$c2"]
                                Box(
                                    modifier = Modifier
                                        .width(arrowSlotDp)
                                        .height(cellSizeDp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cn != null) {
                                        val dir = if (cn.gt) ArrowDirection.RIGHT else ArrowDirection.LEFT
                                        key(cn) {
                                            ConstraintArrow(
                                                direction = dir,
                                                sizeDp    = arrowSlotDp * 0.9f
                                            )
                                        }
                                    }
                                }
                            }

                            isVCon -> {
                                val r1 = (ri - 1) / 2
                                val r2 = (ri + 1) / 2
                                val c  = ci / 2
                                val cn = constraintsMap["$r1,$c,$r2,$c"]
                                Box(
                                    modifier = Modifier
                                        .width(cellSizeDp)
                                        .height(arrowSlotDp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cn != null) {
                                        val dir = if (cn.gt) ArrowDirection.DOWN else ArrowDirection.UP
                                        key(cn) {
                                            ConstraintArrow(
                                                direction = dir,
                                                sizeDp    = arrowSlotDp * 0.9f
                                            )
                                        }
                                    }
                                }
                            }

                            else -> {
                                Spacer(Modifier.size(arrowSlotDp))
                            }
                        }
                    }
                }
            }
        }
    }
}
