package com.example.flabbygame.game_obj

import com.example.flabbygame.util.Converter.toDp
import kotlin.random.Random

class Column(
    var spaceX: Float,
    var spaceY: Float,
    var width: Float = Util.width,
    var spaceHeight: Float = Util.spaceHeight,
    var moveSpeed: Float = Util.moveSpeed
){
    object Util {
        const val columnsSize = 5
        val columnMinHeight = 70f.toDp()
        val width= 55f.toDp()
        val spaceHeight = 150f.toDp()
        val moveSpeed = 130f.toDp()
        val distanceBetweenColumns: Float = 200f.toDp()

        fun randomSpaceY(screenHeight: Int, spaceHeight: Float, minHeight: Float) =
            Random.nextInt(
                (spaceHeight / 2).toInt() + minHeight.toInt(),
                screenHeight - (spaceHeight / 2).toInt() - minHeight.toInt()
            ).toFloat()
    }
}