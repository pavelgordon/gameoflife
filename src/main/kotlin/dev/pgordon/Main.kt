package dev.pgordon

import dev.pgordon.State.ALIVE
import dev.pgordon.State.EMPTY
import javafx.scene.layout.Border
import javafx.scene.layout.BorderStroke
import javafx.scene.layout.BorderStrokeStyle.SOLID
import javafx.scene.layout.BorderWidths.DEFAULT
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Color.*
import tornadofx.*
import javafx.scene.layout.CornerRadii.EMPTY as RADII_EMPTY

const val sizeOfCell = 10.0

class MyApp : App(GameView::class)

class GameView : View() {
    private val game = GameOfLife(sizeOfField = 10)

    override val root = vbox {
        label("Generation 0, alive cells: -") {
            subscribe<GameStateEvent> { event ->
                text = "Gen ${event.generation}, alive cells ${event.cells.count { cell -> cell.isAlive() }}"
            }
        }

        datagrid<Cell>() {
            setPrefSize((game.sizeOfField + 2) * sizeOfCell * 3, (game.sizeOfField + 2) * sizeOfCell * 3)
            maxCellsInRow = game.sizeOfField
            cellHeight = sizeOfCell
            cellWidth = sizeOfCell
            verticalCellSpacing = 0.5
            horizontalCellSpacing = 0.5
            border = Border(
                BorderStroke(BLACK, SOLID, RADII_EMPTY, DEFAULT),
                BorderStroke(BLACK, SOLID, RADII_EMPTY, DEFAULT),
                BorderStroke(BLACK, SOLID, RADII_EMPTY, DEFAULT),
                BorderStroke(BLACK, SOLID, RADII_EMPTY, DEFAULT)
            )
            cellCache { cell ->
                rectangle(width = sizeOfCell, height = sizeOfCell) {
                    fill = when (cell.state) {
                        EMPTY -> WHITE
                        ALIVE -> FORESTGREEN
                    }
                }
            }

            subscribe<GameStateEvent> { event ->
                items.setAll(event.cells)
            }
        }
    }

}

data class GameStateEvent(val generation: Int, val cells: List<Cell>) : FXEvent()

class GameController : Controller() {
    fun updateGameState(generation: Int, cells: List<Cell>) {
        fire(GameStateEvent(generation, cells))
    }

}

fun main(args: Array<String>) {
    launch<MyApp>(args)
}