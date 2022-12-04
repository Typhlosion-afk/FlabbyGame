package com.example.flabbygame.game_obj

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
class ColorSet(val bird: Int, val column: Int, val background: Int) : Parcelable
