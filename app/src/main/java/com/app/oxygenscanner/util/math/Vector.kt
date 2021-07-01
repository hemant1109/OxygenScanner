package com.app.oxygenscanner.util.math

import kotlin.math.pow
import kotlin.math.sqrt


/**
 * The `Vector` class provides some useful static functions to
 * compute vectors.
 *
 * @author Michael Lambertz
 */
object Vector {
    fun normalize(input: DoubleArray): DoubleArray {
        var sumSquares = 0.0
        // First calculate the length
        for (i in input.indices) {
            sumSquares += input[i].pow(2.0)
        }
        // The actual length of the vector
        val len = sqrt(sumSquares)
        return scale(1 / len, input)
    }

    /**
     * Inverts every element of the vector.
     *
     * @param inVector the vector
     * @return the resulting vctor
     */
    fun invVector(
        inVector: DoubleArray
    ): DoubleArray {
        val m = inVector.size
        val outVector = DoubleArray(m)
        for (i in 0 until m) {
            if (inVector[i] != 0.0) {
                outVector[i] = 1 / inVector[i]
            } else {
                outVector[i] = 0.0
            }
        }
        return outVector
    }

    /**
     * Compares the content of two string objects.
     *
     * @param vec1 the first vector
     * @param vec2 the second vector
     * @return true, if the vectors are equal
     */
    fun equals(vec1: DoubleArray, vec2: DoubleArray): Boolean {
        if (vec1.size != vec2.size) {
            return false
        }
        for (i in vec1.indices) {
            if (vec1[i] != vec2[i]) {
                return false
            }
        }
        return true
    }

    /**
     * Fills a string with blanks until it reaches a desired length.
     *
     * @param in  string to fill
     * @param len desired length
     * @return the input string eventually suffixed with blanks
     */
    private fun fillString(`in`: String, len: Int): String {
        var out = `in`
        while (out.length < len) {
            out = " $out"
        }
        return out
    }

    /**
     * Converts a vector object into a `String` object
     * representing its content.
     *
     * @param vector the vector to be converted to a string
     * @return the string representing the content of the vector
     */
    fun toString(vector: DoubleArray): String {
        var result = ""
        for (i in vector.indices) {
            result += """
                ${Vector.fillString(java.lang.Double.toString(vector[i]), 24)}
                
                """.trimIndent()
        }
        return result
    }

    /**
     * Builds a new m-dimensional vector object. Its content is undefined.
     *
     * @param m number of elements
     * @return the new vector
     */
    fun newVector(m: Int): DoubleArray {
        return DoubleArray(m)
    }

    /**
     * Builds a new m-dimensional vector object, whose elements
     * have a predefined value.
     *
     * @param m   number of elements
     * @param val the element's value
     * @return the new vector
     */
    fun newVector(m: Int, `val`: Double): DoubleArray {
        val res = DoubleArray(m)
        for (i in 0 until m) {
            res[i] = `val`
        }
        return res
    }

    /**
     * Scales a vector and returns the result in a new
     * vector object.
     *
     * @param vector the vector to scale
     * @param fac    the factor to scale with
     * @return the scaled vector
     */
    fun scale(fac: Double, vector: DoubleArray): DoubleArray {
        val n = vector.size
        val res = DoubleArray(n)
        for (i in 0 until n) {
            res[i] = fac * vector[i]
        }
        return res
    }

    /**
     * Calculates the scalar product of two vectors.
     *
     * @param vec1 the first vector
     * @param vec2 the second vector
     * @return the scalar product of the vectors
     */
    fun dot(vec1: DoubleArray, vec2: DoubleArray): Double {
        val n = vec1.size
        var res = 0.0
        for (i in 0 until n) {
            res += vec1[i] * vec2[i]
        }
        return res
    }

    /**
     * Adds two vectors and returns the result in a new vector object.
     *
     * @param vec1 the first vector
     * @param vec2 the second vector
     * @return the resulting vector
     */
    fun add(vec1: DoubleArray, vec2: DoubleArray): DoubleArray {
        val m = vec1.size
        val res = DoubleArray(m)
        for (i in 0 until m) {
            res[i] = vec1[i] + vec2[i]
        }
        return res
    }

    /**
     * Subtracts two vectors and returns the result in a new vector object.
     *
     * @param vec1 the first vector
     * @param vec2 the second vector
     * @return the resulting vector
     */
    fun sub(vec1: DoubleArray, vec2: DoubleArray): DoubleArray {
        val m = vec1.size
        val res = DoubleArray(m)
        for (i in 0 until m) {
            res[i] = vec1[i] - vec2[i]
        }
        return res
    }

    /**
     * Generates a copy of a given vector.
     *
     * @param vector the vector to copy
     * @return the copied vector
     */
    fun clone(vector: DoubleArray): DoubleArray {
        val m = vector.size
        val res = DoubleArray(m)
        for (i in 0 until m) {
            res[i] = vector[i]
        }
        return res
    }

    /**
     * Generates a random m-dimensional vector object.
     *
     * @param m the number of elements
     * @return the random vector
     */
    fun random(m: Int): DoubleArray {
        val res = DoubleArray(m)
        for (i in 0 until m) {
            res[i] = Math.random()
        }
        return res
    }

    /**
     * Adds a vector to every vector in a set.
     *
     * @param vecSet the set of vectors
     * @param addVec the vector to subtract
     * @return the resulting set
     */
    fun addVecToSet(
        vecSet: Array<DoubleArray>,
        addVec: DoubleArray
    ): Array<DoubleArray?> {
        val m = Matrix.getNumOfRows(vecSet)
        val n = Matrix.getNumOfColumns(vecSet)
        val res = Matrix.newMatrix(m, n)
        for (i in 0 until m) {
            val add = addVec[i]
            for (j in 0 until n) {
                res[i]?.set(j, vecSet[i][j] + add)
            }
        }
        return res
    }

    fun center(vec: DoubleArray): DoubleArray {
        val n = vec.size
        var mValue = 0.0
        for (i in 0 until n) {
            mValue += vec[i]
        }
        mValue /= n.toDouble()
        val cVec = DoubleArray(n)
        for (i in 0 until n) {
            cVec[i] = vec[i] - mValue
        }
        return cVec
    }
}