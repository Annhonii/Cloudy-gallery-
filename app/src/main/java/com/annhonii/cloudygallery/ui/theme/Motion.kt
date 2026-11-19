package com.annhonii.cloudygallery.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween

// Nothing OS inspired motion — snappy, precise
object CloudyMotion {
    // Durations
    const val DurationShort = 150
    const val DurationMedium = 250
    const val DurationLong = 400

    // Easing curves - Nothing OS style: sharp entry, smooth exit
    val EasingEmphasized = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EasingEmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val EasingEmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EasingStandard = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    fun <T> tweenShort() = tween<T>(DurationShort, easing = EasingStandard)
    fun <T> tweenMedium() = tween<T>(DurationMedium, easing = EasingEmphasized)
    fun <T> tweenLong() = tween<T>(DurationLong, easing = EasingEmphasizedDecelerate)
}
