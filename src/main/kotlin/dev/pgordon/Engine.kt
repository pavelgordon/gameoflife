package dev.pgordon

import dev.pgordon.State.Alive
import dev.pgordon.State.Empty
import javafx.collections.ObservableList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tornadofx.asObservable
import kotlin.random.Random


enum class State { Empty, Alive }

data class Cell(val x: Int, val y: Int, var state: State) {
    fun isEmpty() = this.state == Empty
    fun isAlive() = this.state == Alive
}

class GameOfLife(
    val sizeOfField: Int,
    initialPopulation: Int = sizeOfField * 2
) {
    private var generation = 0
    private val gameController = GameController()
    private var field: ObservableList<Cell> = MutableList(sizeOfField * sizeOfField) {
        Cell(it / sizeOfField, it % sizeOfField, Empty)
    }.asObservable()


    init {
        for (i in 0..initialPopulation) {
            setAt(Random.nextInt(sizeOfField), Random.nextInt(sizeOfField), Alive)
        }
        start()
    }

    //todo sygar: set(Empty) at (1, 2)
    private fun List<Cell>.amountOfAliveCells(): Int = this.count { cell -> cell.state == Alive }

    private fun List<Cell>.amountOfAliveNeighbors(x: Int, y: Int): Int = with(this) {
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

    private fun setAt(x1: Int, y1: Int, state: State) {
        val (x, y) = normalizeCoordinates(x1 to y1)
        field[x * sizeOfField + y] = Cell(x, y, state)
    }

    private fun normalizeCoordinates(coords: Pair<Int, Int>): Pair<Int, Int> {
        var (x, y) = coords
        if (x < 0) x += sizeOfField
        if (y < 0) y += sizeOfField
        if (x >= sizeOfField) x -= sizeOfField
        if (y >= sizeOfField) y -= sizeOfField
        return x to y
    }

    private fun tick() {
        val oldState = mutableListOf<Cell>()
        field.forEach { state -> oldState.add(state) }

        for (i in 0 until sizeOfField) {
            for (j in 0 until sizeOfField) {
                when (val aliveNeighbors = oldState.amountOfAliveNeighbors(i, j)) {
                    2 -> {
                        // do nothing
                    }
                    3 -> if (getAt(i, j).isEmpty()) {
                        println("Birth at $i $j because 3 neighbors")
                        setAt(i, j, Alive)
                    }
                    else -> if (getAt(i, j).isAlive()) {
                        println("Death at $i $j because of $aliveNeighbors alive neighbors")
                        setAt(i, j, Empty)
                    }
                }

            }

        }
        gameController.updateGameState(generation++, field)
    }

    private fun start() {
        GlobalScope.launch {
            var generation = 1
            delay(1000) // delay for the ui to show up
            while (true) {
                println("Generation ${generation++}, alive cells ${field.amountOfAliveCells()}")
                tick()
                delay(1000)
            }
        }
    }
}
