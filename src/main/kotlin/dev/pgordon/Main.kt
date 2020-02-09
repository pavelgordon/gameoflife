package dev.pgordon

import dev.pgordon.State.Alive
import dev.pgordon.State.Empty
import javafx.scene.Scene
import javafx.scene.layout.*
import javafx.scene.layout.BorderStrokeStyle.SOLID
import javafx.scene.layout.BorderWidths.DEFAULT
import javafx.scene.layout.CornerRadii.EMPTY
import javafx.scene.paint.Color
import javafx.scene.paint.Color.BLACK
import tornadofx.*
import kotlin.concurrent.thread
import kotlin.random.Random

const val n = 20
const val initialPopulation = n * 2
const val sizeOfCell = 30.0

const val width;
const val height;


enum class State { Empty, Alive }

data class Cell(val x: Int, val y: Int, var state: State)





var grid = MutableList<Cell>(n * n) {
    Cell(it / n, it % n, Empty)
}.asObservable()

public fun List<Cell>.amountOfAliveNeighbors(x: Int, y: Int): Int {
    return listOf(
        this.getAt(x - 1, y - 1), this.getAt(x - 1, y), this.getAt(x - 1, y + 1),
        this.getAt(x, y - 1), grid.getAt(x, y + 1),
        this.getAt(x + 1, y - 1), this.getAt(x + 1, y), this.getAt(x + 1, y + 1)
    ).count { cell -> cell.state == Alive }
}


/*
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
fun List<Cell>.getAt(x: Int, y: Int): Cell {
    var x_ = x
    var y_ = y
    if (x < 0) {
        x_ = n + x
    }
    if (y < 0) {
        y_ = n + y
    }
    if (x >= n) {
        x_ = x - n
    }
    if (y >= n) {
        y_ = y - n
    }
    return grid[x_ * n + y_]
}

fun List<Cell>.setAt(x: Int, y: Int, state: State) {
    var x_ = x
    var y_ = y
    if (x < 0) {
        x_ = n + x
    }
    if (y < 0) {
        y_ = n + y
    }
    if (x > n) {
        x_ = x - n
    }
    if (y > n) {
        y_ = y - n
    }
    grid[x_ * n + y_] = Cell(x, y, state)
}




class MyApp : App(MyView::class) {
    override fun createPrimaryScene(view: UIComponent) = Scene(view.root, (n + 2) * sizeOfCell, (n + 5) * sizeOfCell)
}

object GenerationRequest : FXEvent(EventBus.RunOn.BackgroundThread)
class GenerationEvent(val generation: Int, val aliveCells: Int) : FXEvent()

class CustomerController : Controller() {
    fun fireEvent(gen: Int, alive: Int) {
        fire(GenerationEvent(gen, alive))
    }
}

class MyView : View() {

    override val root = vbox {
//        button("Press me").action {
//            fire(GenerationRequest)
//        }
        label("Generation 0, alive cells: -") {
            subscribe<GenerationEvent> { event ->
                text = "Gen ${event.generation}, alive cells ${event.aliveCells}"
            }
        }

        datagrid(grid) {
            setPrefSize((n + 1) * sizeOfCell, (n + 1) * sizeOfCell)
            maxCellsInRow = n
            cellHeight = sizeOfCell
            cellWidth = sizeOfCell
            verticalCellSpacing = 0.5
            horizontalCellSpacing = 0.5
            border = Border(
                BorderStroke(BLACK, SOLID, EMPTY, DEFAULT),
                BorderStroke(BLACK, SOLID, EMPTY, DEFAULT),
                BorderStroke(BLACK, SOLID, EMPTY, DEFAULT),
                BorderStroke(BLACK, SOLID, EMPTY, DEFAULT)
            )

            cellCache { cell ->
                rectangle(
                    width = sizeOfCell,
                    height = sizeOfCell
                ) {
                    fill = when (cell.state) {
                        Empty -> Color.WHITE
                        Alive -> {
                            println("setting to green $cell")
                            Color.FORESTGREEN
                        }
                    }
                }
            }
        }
    }
}

fun main(args: Array<String>) {


    for (i in 0..initialPopulation) {
        grid.setAt(Random.nextInt(n), Random.nextInt(n), Alive)
    }
    println("currentAmountOfAliveCells: ${grid.count { cell -> cell.state == Alive }}")


    thread(start = true) {
        val controller = CustomerController()
        var generation = 1
        Thread.sleep(1000)

        while (true) {
            println("Generation ${generation++}")
            val currentGridState = mutableListOf<Cell>()
            grid.forEach { state -> currentGridState.add(state) }
            println("alive cells at the beginning of generation: ${grid.count { cell -> cell.state == Alive }} ")
            controller.fireEvent(generation, grid.count { cell -> cell.state == Alive })
            for (i in 0 until n) {
                for (j in 0 until n) {
                    when (currentGridState.amountOfAliveNeighbors(i, j)) {
                        2 -> {

                        }
                        3 -> if (grid.getAt(i, j).state == Empty) {
                            println("Birth in cell at $i $j because 3 neighbors")
                            grid.setAt(i, j, Alive)
                        }
                        else -> if (grid.getAt(i, j).state == Alive) {
                            println(
                                "Killing cell at $i $j because of ${currentGridState.amountOfAliveNeighbors(
                                    i,
                                    j
                                )} neighbors"
                            )
                            grid.setAt(i, j, Empty)
                        }
                    }

                }
            }
            println("alive cells at the end of generation: ${grid.count { cell -> cell.state == Alive }}  ")

            Thread.sleep(1_000)


        }
    }

    launch<MyApp>(args)
}


