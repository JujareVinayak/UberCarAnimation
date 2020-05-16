package com.jujare.maps.ubercaranimation.utils

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator

object AnimationUtils {
    //Animator to animate the car
    fun carAnimator(): ValueAnimator {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 3000
        valueAnimator.interpolator = LinearInterpolator()
        return valueAnimator
    }

    //Animator to draw the path.
    fun polylineAnimator(): ValueAnimator {
        val valueAnimator = ValueAnimator.ofInt(0, 100)
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 4000
        return valueAnimator
    }
}