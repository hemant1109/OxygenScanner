package com.app.oxygenscanner.util.math


import org.apache.commons.math3.analysis.solvers.LaguerreSolver
import org.apache.commons.math3.complex.Complex
import kotlin.math.pow

/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*  Copyright (c) 2009 by Vinnie Falco
*  Copyright (c) 2016 by Bernd Porr
*/ /**
 * User facing class which contains all the methods the user uses to create
 * Bessel filters. This done in this way: Bessel bessel = new Bessel(); Then
 * call one of the methods below to create low-,high-,band-, or stopband
 * filters. For example: bessel.bandPass(2,250,50,5);
 */
class Bessel : Cascade() {
    // returns fact(n) = n!
    private fun fact(n: Int): Double {
        if (n == 0) return 1.0
        var y = n.toDouble()
        for (m in n - 1 downTo 1) y *= m
        return y
    }

    internal inner class AnalogLowPass(var degree: Int) : LayoutBase(degree) {
        var m_a: DoubleArray = DoubleArray(degree + 1)
        var m_root: Array<Complex?>

        // returns the k-th zero based coefficient of the reverse bessel
        // polynomial of degree n
        private fun reverseBessel(k: Int, n: Int): Double {
            return (fact(2 * n - k)
                    / (fact(n - k) * fact(k) * 2.0.pow((n - k).toDouble())))
        }

        fun design() {
            reset()
            for (i in 0 until degree + 1) {
                m_a[i] = reverseBessel(i, degree)
            }
            val laguerreSolver = LaguerreSolver()
            m_root = laguerreSolver.solveAllComplex(m_a, 0.0)
            val inf = Complex.INF
            val pairs = degree / 2
            for (i in 0 until pairs) {
                val c = m_root[i]
                addPoleZeroConjugatePairs(c, inf)
            }
            if (degree and 1 == 1) add(Complex(m_root[pairs]!!.real), inf)
        }

        // ------------------------------------------------------------------------------
        init {
            // input coefficients (degree+1 elements)
            m_root = arrayOfNulls(degree) // array of roots (degree elements)
            setNormal(0.0, 1.0)
        }
    }

    private fun setupLowPass(
        order: Int, sampleRate: Double,
        cutoffFrequency: Double, directFormType: Int
    ) {
        val m_analogProto = AnalogLowPass(order)
        m_analogProto.design()
        val m_digitalProto = LayoutBase(order)
        LowPassTransform(
            cutoffFrequency / sampleRate, m_digitalProto,
            m_analogProto
        )
        setLayout(m_digitalProto, directFormType)
    }

    /**
     * Bessel Lowpass filter with default topology
     *
     * @param order           The order of the filter
     * @param sampleRate      The sampling rate of the system
     * @param cutoffFrequency the cutoff frequency
     */
    fun lowPass(order: Int, sampleRate: Double, cutoffFrequency: Double) {
        setupLowPass(
            order, sampleRate, cutoffFrequency,
            DirectFormAbstract.Companion.DIRECT_FORM_II
        )
    }

    /**
     * Bessel Lowpass filter with custom topology
     *
     * @param order           The order of the filter
     * @param sampleRate      The sampling rate of the system
     * @param cutoffFrequency The cutoff frequency
     * @param directFormType  The filter topology. This is either
     * DirectFormAbstract.DIRECT_FORM_I or DIRECT_FORM_II
     */
    fun lowPass(
        order: Int, sampleRate: Double, cutoffFrequency: Double,
        directFormType: Int
    ) {
        setupLowPass(order, sampleRate, cutoffFrequency, directFormType)
    }

    private fun setupHighPass(
        order: Int, sampleRate: Double,
        cutoffFrequency: Double, directFormType: Int
    ) {
        val m_analogProto: AnalogLowPass = AnalogLowPass(order)
        m_analogProto.design()
        val m_digitalProto = LayoutBase(order)
        HighPassTransform(
            cutoffFrequency / sampleRate, m_digitalProto,
            m_analogProto
        )
        setLayout(m_digitalProto, directFormType)
    }

    /**
     * Highpass filter with custom topology
     *
     * @param order           Filter order (ideally only even orders)
     * @param sampleRate      Sampling rate of the system
     * @param cutoffFrequency Cutoff of the system
     * @param directFormType  The filter topology. See DirectFormAbstract.
     */
    fun highPass(
        order: Int, sampleRate: Double, cutoffFrequency: Double,
        directFormType: Int
    ) {
        setupHighPass(order, sampleRate, cutoffFrequency, directFormType)
    }

    /**
     * Highpass filter with default filter topology
     *
     * @param order           Filter order (ideally only even orders)
     * @param sampleRate      Sampling rate of the system
     * @param cutoffFrequency Cutoff of the system
     */
    fun highPass(order: Int, sampleRate: Double, cutoffFrequency: Double) {
        setupHighPass(
            order, sampleRate, cutoffFrequency,
            DirectFormAbstract.Companion.DIRECT_FORM_II
        )
    }

    private fun setupBandStop(
        order: Int, sampleRate: Double,
        centerFrequency: Double, widthFrequency: Double, directFormType: Int
    ) {
        val m_analogProto: AnalogLowPass = AnalogLowPass(order)
        m_analogProto.design()
        val m_digitalProto = LayoutBase(order * 2)
        BandStopTransform(
            centerFrequency / sampleRate, widthFrequency
                    / sampleRate, m_digitalProto, m_analogProto
        )
        setLayout(m_digitalProto, directFormType)
    }

    /**
     * Bandstop filter with default topology
     *
     * @param order           Filter order (actual order is twice)
     * @param sampleRate      Samping rate of the system
     * @param centerFrequency Center frequency
     * @param widthFrequency  Width of the notch
     */
    fun bandStop(
        order: Int, sampleRate: Double, centerFrequency: Double,
        widthFrequency: Double
    ) {
        setupBandStop(
            order, sampleRate, centerFrequency, widthFrequency,
            DirectFormAbstract.DIRECT_FORM_II
        )
    }

    /**
     * Bandstop filter with custom topology
     *
     * @param order           Filter order (actual order is twice)
     * @param sampleRate      Samping rate of the system
     * @param centerFrequency Center frequency
     * @param widthFrequency  Width of the notch
     * @param directFormType  The filter topology
     */
    fun bandStop(
        order: Int, sampleRate: Double, centerFrequency: Double,
        widthFrequency: Double, directFormType: Int
    ) {
        setupBandStop(
            order, sampleRate, centerFrequency, widthFrequency,
            directFormType
        )
    }

    private fun setupBandPass(
        order: Int, sampleRate: Double,
        centerFrequency: Double, widthFrequency: Double, directFormType: Int
    ) {
        val m_analogProto: AnalogLowPass = AnalogLowPass(order)
        m_analogProto.design()
        val m_digitalProto = LayoutBase(order * 2)
        BandPassTransform(
            centerFrequency / sampleRate, widthFrequency
                    / sampleRate, m_digitalProto, m_analogProto
        )
        setLayout(m_digitalProto, directFormType)
    }

    /**
     * Bandpass filter with default topology
     *
     * @param order           Filter order
     * @param sampleRate      Sampling rate
     * @param centerFrequency Center frequency
     * @param widthFrequency  Width of the notch
     */
    fun bandPass(
        order: Int, sampleRate: Double, centerFrequency: Double,
        widthFrequency: Double
    ) {
        setupBandPass(
            order, sampleRate, centerFrequency, widthFrequency,
            DirectFormAbstract.Companion.DIRECT_FORM_II
        )
    }

    /**
     * Bandpass filter with custom topology
     *
     * @param order           Filter order
     * @param sampleRate      Sampling rate
     * @param centerFrequency Center frequency
     * @param widthFrequency  Width of the notch
     * @param directFormType  The filter topology (see DirectFormAbstract)
     */
    fun bandPass(
        order: Int, sampleRate: Double, centerFrequency: Double,
        widthFrequency: Double, directFormType: Int
    ) {
        setupBandPass(
            order, sampleRate, centerFrequency, widthFrequency,
            directFormType
        )
    }
}