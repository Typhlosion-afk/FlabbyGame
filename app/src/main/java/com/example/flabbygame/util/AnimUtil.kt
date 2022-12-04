package com.example.flabbygame.util

import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import com.example.flabbygame.App
import com.example.flabbygame.R
import kotlin.math.cos
import kotlin.math.pow

object AnimUtil {
    fun View?.startAnimBounce(
        amplitude: Double = 0.2,
        frequency: Double = 20.0
    ) {
        val anim = AnimationUtils.loadAnimation(App.instances.applicationContext, R.anim.bounce)
        anim.interpolator = BounceInterpolator(amplitude, frequency)
        this?.startAnimation(anim)
    }

    /**
     * The first value 0.2 is the bounce amplitude. The higher value produces more pronounced bounces.
     * The second value 20 is the frequency of the bounces. The higher value creates more wobbles during the animation time period.
     */
    class BounceInterpolator(
        private var amplitude: Double = 1.0,
        private var frequency: Double = 10.0
    ) : Interpolator {

        override fun getInterpolation(time: Float): Float =
            (-1 * Math.E.pow(-time / amplitude) * cos(frequency * time) + 1).toFloat()
    }
}