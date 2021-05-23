package com.example.oxygenscanner.util.math

import kotlin.math.abs
import kotlin.math.sqrt


object Maths {
    /**
     * sqrt(a^2 + b^2) without under/overflow.
     */
    fun hypot(a: Double, b: Double): Double {
        var r: Double
        if (abs(a) > abs(b)) {
            r = b / a
            r = abs(a) * sqrt(1 + r * r)
        } else if (b != 0.0) {
            r = a / b
            r = abs(b) * sqrt(1 + r * r)
        } else {
            r = 0.0
        }
        return r
    }
}