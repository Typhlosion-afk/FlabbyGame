package com.example.flabbygame.game_obj

import com.example.flabbygame.util.Converter.toDp

class Bird(
    var cx: Float = Util.defaultCx,
    var cy: Float = 0f,
    var radius: Float = Util.radius,
    var jumpPower:Float = Util.jumpPower
)
{
    object Util {
        val defaultCx = 50f.toDp()
        val radius: Float = 10.5f.toDp()
        val jumpPower: Float = 580f.toDp()
        val viewRadius: Float = 12f.toDp()
    }
}