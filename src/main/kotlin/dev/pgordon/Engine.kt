package dev.pgordon

import dev.pgordon.State.ALIVE
import dev.pgordon.State.EMPTY
import javafx.collections.ObservableList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tornadofx.asObservable
import kotlin.random.Random


enum class State { EMPTY, ALIVE }

data class Cell(val x: Int, val y: Int, val state: State) {
    fun isEmpty() = this.state == EMPTY
    fun isAlive() = this.state == ALIVE
}

typealias Board = List<MutableList<Cell>>
typealias ObservableBoard = ObservableList<Cell>
typealias Coords = Pair<Int, Int>

class GameOfLife(
    val sizeOfField: Int,
    private val started: Boolean = true,
    initField: () -> Board = { InitBoard.random(sizeOfField) }
) {
    private var generation = 0
    private val gameController = GameController()
    private var field: ObservableBoard = initField().flatten().asObservable()


    init {
        if (started) {
            startGame()
        }
    }


    companion object InitBoard {
        fun empty(sizeOfField: Int): Board =
            List(sizeOfField) { i ->
                MutableList(sizeOfField) { j -> Cell(i, j, EMPTY) }
            }

        fun random(sizeOfField: Int, initPopulation: Int = sizeOfField * 2): Board =
            empty(sizeOfField).also { board ->
                for (i in 0..initPopulation) {
                    val (x, y) = Random.nextInt(sizeOfField) to Random.nextInt(sizeOfField)
                    board[x][y] = board[x][y].copy(state = ALIVE)
                }
            }
    }


    //todo sygar: set(Empty) at (1, 2)
    private fun ObservableBoard.amountOfAliveCells(): Int = this.count { cell -> cell.state == ALIVE }

    private fun ObservableBoard.amountOfAliveNeighbors(x: Int, y: Int): Int = with(this) {
        listOf(
            getAt(x - 1, y - 1), getAt(x - 1, y), getAt(x - 1, y + 1),
            getAt(x, y - 1), getAt(x, y + 1),
            getAt(x + 1, y - 1), getAt(x + 1, y), getAt(x + 1, y + 1)
        ).count { cell -> cell.isAlive() }
    }


    /**
    n=3
    0 1 2 3 0
    0 0 0 0 0
    0 0 0 0 1
    0 0 0 0 2
    0 0 0 0 3
    2;3

    0 0 0 0 0 0 0 0 0 0  0  0  0  0  0  0
    0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
     */
    private fun getAt(x1: Int, y1: Int): Cell {
        val (x, y) = normalizeCoordinates(x1 to y1)
        return field[x * sizeOfField + y]
    }

    private fun killAt(x1: Int, y1: Int) {
        if (getAt(x1, y1).isAlive()) setAt(x1, y1, EMPTY)
    }

    private fun birthAt(x1: Int, y1: Int) {
        if (getAt(x1, y1).isEmpty()) setAt(x1, y1, ALIVE)
    }

    private fun setAt(x1: Int, y1: Int, state: State) {
        val (x, y) = normalizeCoordinates(x1 to y1)
        field[x * sizeOfField + y] = Cell(x, y, state)
    }

    private fun normalizeCoordinates(coords: Coords): Coords {
        var (x, y) = coords
        if (x < 0) x += sizeOfField
        if (y < 0) y += sizeOfField
        if (x >= sizeOfField) x -= sizeOfField
        if (y >= sizeOfField) y -= sizeOfField
        return x to y
    }

    private fun tick() {
        val oldBoard: ObservableBoard = List<Cell>(sizeOfField) { field[it] }.asObservable()

        for (i in 0 until sizeOfField) {
            for (j in 0 until sizeOfField) when (oldBoard.amountOfAliveNeighbors(i, j)) {
                2 -> {
                    // do nothing
                }
                3 -> killAt(i, j)
                else -> birthAt(i, j)
            }

        }
        gameController.updateGameState(generation++, field)
    }

    public fun startGame() {
        GlobalScope.launch {
            var generation = 1
            delay(1000) // delay for the ui to show up
            while (true) {
                println("Generation ${generation++}, alive cells ${field.amountOfAliveCells()}")
                tick()
                delay(400)
            }
        }
    }
}
