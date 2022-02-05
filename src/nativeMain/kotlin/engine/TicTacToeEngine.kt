package engine

class TicTacToeEngine(
    private var player: Char = 'x'
) {
    private val LAST_INDEX = 1 shl 8
    private val COLUMN_0 = 4 or (4 shl 3) or (4 shl 3 shl 3)
    private val COLUMN_1 = 2 or (2 shl 3) or (2 shl 3 shl 3)
    private val COLUMN_2 = 1 or (1 shl 3) or (1 shl 3 shl 3)
    private val ROW_0 = 7
    private val ROW_1 = 7 shl 3
    private val ROW_2 = 7 shl 3 shl 3
    private val DIAGONAL_RIGHT = 84
    private val DIAGONAL_LEFT = 273
    private val DRAW_BITMASK: Int = ROW_0 or ROW_1 or ROW_2
    private var xBoard = 0
    private var oBoard = 0
    private var won = false
    private var draw = false
    private val winCombinations = listOf(
        ROW_0,            ROW_1,        ROW_2,    // rows
        COLUMN_2,         COLUMN_1,     COLUMN_0, // columns
        DIAGONAL_RIGHT,   DIAGONAL_LEFT           // diagonals
    )
    fun place(pos: Int): Boolean {
        if (won or draw) return false
        val cell = 1 shl pos
        if (cell > LAST_INDEX || cell < 0 || xBoard or oBoard and cell == cell) return false
        if (player == 'x') {
            xBoard = xBoard or cell
        } else {
            oBoard = oBoard or cell
        }
        /*if (is_win()) {
            win(player);
            won = true;
            return true;
        } else if ((x_board | o_board) == DRAW_BITMASK) {
            draw();
            draw = true;
            return true;
        }
        player = player == 'x'? 'o': 'x';*/
        return true
    }
    private fun checkWin(): Boolean {
        return if (player == 'x') {
            winCombinations.any { xBoard and it == it }
        } else {
            winCombinations.any { oBoard and it == it }
        }
    }
    fun isWin(): Boolean {
        if (checkWin()) {
            won = true
            return true
        }
        return false
    }
    fun swap() {
        player = if (player == 'x') 'o' else 'x'
    }
    fun isDraw(): Boolean {
        if (xBoard or oBoard == DRAW_BITMASK) {
            draw = true
            return true
        }
        return false
    }
    // get player
    fun getPlayer(): Char {
        return player
    }
    // reset
    fun reset() {
        xBoard = 0
        oBoard = 0
        won = false
        draw = false
        player = 'x'
    }
}