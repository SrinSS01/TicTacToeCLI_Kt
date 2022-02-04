import libncurses.*
import platform.posix.exit
import engine.TicTacToeEngine

fun main() {
    initscr()
    cbreak()
    noecho()
    start_color()
    // hide cursor
    curs_set(0)
    // check if color is supported by terminal if not then print error message and exit
    if (!has_colors()) {
        endwin()
        println("Terminal does not support color")
        exit(1)
    }
    init_pair(1, COLOR_BLUE.toShort(), COLOR_WHITE.toShort())
    init_pair(2, COLOR_WHITE.toShort(), COLOR_BLUE.toShort())
    init_pair(3, COLOR_BLACK.toShort(), COLOR_BLACK.toShort())
    init_pair(4, COLOR_RED.toShort(), COLOR_WHITE.toShort())
    init_pair(5, COLOR_BLACK.toShort(), COLOR_WHITE.toShort())
    bkgd(COLOR_PAIR(2).toUInt())
    // get terminal size
    val maxY = getmaxy(stdscr)
    val maxX = getmaxx(stdscr)
    // create a Window
    val startX = (maxX - 40) / 2
    val startY = (maxY - 19) / 2
    val board = Board(startX, startY, 40, 19)
    // create an array of coordinates
    val coords = arrayOf(
        Coordinate(0, 0),
        Coordinate(13, 0),
        Coordinate(26, 0),
        Coordinate(0, 6),
        Coordinate(13, 6),
        Coordinate(26, 6),
        Coordinate(0, 12),
        Coordinate(13, 12),
        Coordinate(26, 12),
    )
    // initialize the TicTacToeEngine
    val engine = TicTacToeEngine()
    displayPlayer(startX, startY, engine.getPlayer())
    // create a game loop and switch through the key, run until the user presses escape
    while (true) {
        when (val key = getch()) {
            48, 49, 50, 51, 52, 53, 54, 55, 56 -> {
                val pos = 8 - (key - 48)
                val (x, y) = coords[pos]
                val player = engine.getPlayer()
                if (engine.place(pos)) {
                    // check if player is x or o, if x then drawCross, if o then drawCircle
                    if (player == 'x') board.renderCross(x, y)
                    else board.renderCircle(x, y)
                    if (engine.isWin()) {
                        if (win(player)) {
                            board.clear()
                            engine.reset()
                        } else break
                    } else if (engine.isDraw()) {
                        if (draw()) {
                            board.clear()
                            engine.reset()
                        } else break
                    } else {
                        engine.swap()
                    }
                    displayPlayer(startX, startY, engine.getPlayer())
                }
            }
            27 -> break
        }
    }
    board.close()
    endwin()
}

// render current player
fun displayPlayer(x: Int, y: Int, player: Char) {
    // enable color red on white
    attron(COLOR_PAIR(4))
    // print player
    val text = "Current player: $player"
    mvprintw(y - 2, x + (20 - text.length) / 2, text)
    attroff(COLOR_PAIR(4))
}

fun win(player: Char): Boolean {
    // get terminal size
    val maxY = getmaxy(stdscr)
    val maxX = getmaxx(stdscr)
    val width = 20
    val height = 5
    // create a new Window instance and center it with title winner!!!
    val win = Window((maxX - width) / 2, (maxY - height) / 2, width, height, "Winner!!!")
    val message = "Player $player wins!"
    win.renderText((width - message.length) / 2, (height / 2) - 1, message)
    win.renderText((width - message.length) / 2, (height / 2)  + 1, "continue? (y/n)")
    // wait for user to press enter or escape
    while (true) {
        // check if user pressed y or n
        when (getch()) {
            121 -> {
                win.close()
                return true
            }
            110 -> {
                win.close()
                return false
            }
        }
    }
}
fun draw(): Boolean {
    // get terminal size
    val maxY = getmaxy(stdscr)
    val maxX = getmaxx(stdscr)
    val width = 20
    val height = 5
    // create a new Window instance and center it with title draw!!!
    val win = Window((maxX - width) / 2, (maxY - height) / 2, width, height, "Draw!!!")
    val message = "It's a draw :("
    win.renderText((width - message.length) / 2, (height / 2) - 1, message)
    win.renderText((width - message.length) / 2, (height / 2)  + 1, "continue? (y/n)")
    while (true) {
        // check if user pressed y or n
        when (getch()) {
            121 -> {
                win.close()
                return true
            }
            110 -> {
                win.close()
                return false
            }
        }
    }
}

// class Coordinate
class Coordinate(private val x: Int, private val y: Int) {
    operator fun component1(): Int {
        return x
    }
    operator fun component2(): Int {
        return y
    }
}
open class Window(
    x: Int,
    y: Int,
    protected val width: Int,
    protected val height: Int,
    private val title: String
) {
    protected val win = newwin(height, width, y, x)
    init {
        wbkgd(win, COLOR_PAIR(1).toUInt())
        wrefresh(win)
        refresh()
        renderBorder()
        attron(COLOR_PAIR(3))
        mvhline(y + height, x + 1, 0, width)
        mvvline(y + 1, x + width, 0, height)
        attroff(COLOR_PAIR(3))
        refresh()
    }
    // render border
    private fun renderBorder() {
        box(win, 0, 0)
        wattron(win, COLOR_PAIR(4))
        mvwprintw(win, 0, 1 + (width - 2 - title.length) / 2, " %s ", title)
        wattroff(win, COLOR_PAIR(4))
        wrefresh(win)
    }
    // destructor
    fun close() {
        delwin(win)
    }
    fun renderText(x: Int, y: Int, format: String) {
        // enable color black on white
        wattron(win, COLOR_PAIR(5))
        mvwprintw(win, y, x, format)
        wattroff(win, COLOR_PAIR(5))
        wrefresh(win)
    }
    // clear the window
    open fun clear() {
        wclear(win)
        renderBorder()
        wrefresh(win)
    }
}
class Board(
    x: Int,
    y: Int,
    width: Int,
    height: Int
): Window(x, y, width, height, "TicTacToe") {
    init {
        renderBoard()
    }
    // private renderBoard
    private fun renderBoard() {
        mvwhline(win, height / 3, 1, 0, width - 2)
        mvwhline(win, (height * 2) / 3, 1, 0, width - 2)
        mvwvline(win, 1, width / 3, 0, height - 2)
        mvwvline(win, 1, (width * 2) / 3, 0, height - 2)
        // loop row from 0 to 12 and column from 0 to 26
        var k = 8u
        // enable color red on white
        wattron(win, COLOR_PAIR(4))
        for (i in 0..12 step 6) {
            for (j in 0..26 step 13) {
                mvwaddch(win, i + 5, j + 12, 48u + k--)
            }
        }
        wattroff(win, COLOR_PAIR(4))
        wrefresh(win)
    }
    // render cross
    fun renderCross(x: Int, y: Int) {
        wattron(win, COLOR_PAIR(5))
        mvwprintw(win, y + 1, x + 2, "XX")
        mvwprintw(win, y + 1, x + 2 + 8, "XX")
        mvwprintw(win, y + 2, x + 4, "XX")
        mvwprintw(win, y + 2, x + 4 + 6 - 2, "XX")
        mvwprintw(win, y + 3, x + 6, "XX")
        mvwprintw(win, y + 4, x + 4, "XX")
        mvwprintw(win, y + 4, x + 4 + 6 - 2, "XX")
        mvwprintw(win, y + 5, x + 2, "XX")
        mvwprintw(win, y + 5, x + 2 + 8, "XX")
        wattroff(win, COLOR_PAIR(5))
        wrefresh(win)
    }
    // render circle
    fun renderCircle(x: Int, y: Int) {
        wattron(win, COLOR_PAIR(5))
        mvwprintw(win, y + 1, x + 6, "00")
        mvwprintw(win, y + 2, x + 4, "0")
        mvwprintw(win, y + 2, x + 9, "0")
        mvwprintw(win, y + 3, x + 3, "0")
        mvwprintw(win, y + 3, x + 10, "0")
        mvwprintw(win, y + 4, x + 4, "0")
        mvwprintw(win, y + 4, x + 9, "0")
        mvwprintw(win, y + 5, x + 6, "00")
        wattroff(win, COLOR_PAIR(5))
        wrefresh(win)
    }
    // override clear
    override fun clear() {
        super.clear()
        renderBoard()
    }
}