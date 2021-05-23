package com.example.oxygenscanner.util.math

import com.example.oxygenscanner.util.math.Butterworth
import com.example.oxygenscanner.util.math.DoubleFft1d
import kotlin.math.abs


object Fft2 {
    fun fFT(`in`: Array<Double>, size: Int, samplingFrequency: Double): Double {
        var temp = 0.0
        var POMP = 0.0
        val output = DoubleArray(2 * size)
        val butterworth = Butterworth()
        butterworth.bandPass(2, samplingFrequency, 0.2, 0.3)
        for (i in output.indices) output[i] = 0.0
        for (x in 0 until size) {
            output[x] = `in`[x]
        }
        val fft = DoubleFft1d(size)
        fft.realForward(output)
        for (x in 0 until 2 * size) {
            output[x] = butterworth.filter(output[x])
        }
        for (x in 0 until 2 * size) {
            output[x] = abs(output[x])
        }
        for (p in 12 until size) {
            if (temp < output[p]) {
                temp = output[p]
                POMP = p.toDouble()
            }
        }
        val frequency: Double = POMP * samplingFrequency / (2 * size)
        return frequency
    }
}