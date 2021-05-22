package com.example.oxygenscanner.util.math

import org.apache.commons.math3.complex.Complex



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
 * It's written on the tin.
 */
open class PoleZeroPair {
    var poles: ComplexPair
    var zeros: ComplexPair

    // single pole/zero
    constructor(p: Complex, z: Complex) {
        poles = ComplexPair(p)
        zeros = ComplexPair(z)
    }

    // pole/zero pair
    constructor(p1: Complex, z1: Complex, p2: Complex, z2: Complex) {
        poles = ComplexPair(p1, p2)
        zeros = ComplexPair(z1, z2)
    }

    val isSinglePole: Boolean
        get() = poles.second == Complex(0.0, 0.0) && zeros.second == Complex(0.0, 0.0)

    fun is_nan(): Boolean {
        return poles.is_nan() || zeros.is_nan()
    }
}