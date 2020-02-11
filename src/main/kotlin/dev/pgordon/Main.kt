package dev.pgordon

import dev.pgordon.State.ALIVE
import dev.pgordon.State.EMPTY
import javafx.geometry.Pos
import javafx.scene.layout.Border
import javafx.scene.layout.BorderStroke
import javafx.scene.layout.BorderStrokeStyle.SOLID
import javafx.scene.layout.BorderWidths.DEFAULT
import javafx.scene.paint.Color.*
import tornadofx.*
import javafx.scene.layout.CornerRadii.EMPTY as RADII_EMPTY

const val baseSizeOfCell = 15.0
const val fieldWidth = 600.0
const val fieldHeight = 600.0

//800
//15
class MyApp : App(GameView::class)

class GameView : View() {
    private val game = GameOfLife(sizeOfField = 40)

    private fun sizeOfCell(sizeOfField: Int): Double {
        val scale = 1
        return fieldWidth / sizeOfField
    }

    override val root = vbox {

        alignment = Pos.CENTER
        label("Generation 0, alive cells: -") {
            subscribe<GameStateEvent> { event ->
                text = "Gen ${event.generation}, alive cells ${event.cells.count { cell -> cell.isAlive() }}"
            }
        }

        datagrid<Cell>() {

            //            setPrefSize((game.sizeOfField + 2) * (game.sizeOfField) *2, 2 *(game.sizeOfField + 2) * sizeOfCell(game.sizeOfField))
            setPrefSize(
                (game.sizeOfField+5)* sizeOfCell(game.sizeOfField) ,
                (game.sizeOfField +5)* sizeOfCell(game.sizeOfField)
            )
            maxCellsInRow = game.sizeOfField
            cellHeight = sizeOfCell(game.sizeOfField)
            cellWidth = sizeOfCell(game.sizeOfField)
            verticalCellSpacing = 0.5
            horizontalCellSpacing = 0.5
            border = Border(
                BorderStroke(BLACK, SOLID, RADII_EMPTY, DEFAULT),
                BorderStroke(BLACK, SOLID, RADII_EMPTY, DEFAULT),
                BorderStroke(BLACK, SOLID, RADII_EMPTY, DEFAULT),
                BorderStroke(BLACK, SOLID, RADII_EMPTY, DEFAULT)
            )
            cellCache { cell ->
                rectangle(width = sizeOfCell(game.sizeOfField), height = sizeOfCell(game.sizeOfField)) {
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