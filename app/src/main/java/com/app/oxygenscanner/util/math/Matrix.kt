package com.app.oxygenscanner.util.math

import kotlin.math.abs
import kotlin.math.sqrt


/**
 * The `Matrix` class provides some useful static functions to
 * compute matrices.
 * Here two-dimensional arrays represent matrices and are taken as
 * columns of rows.
 *
 * @author Michael Lambertz
 */
object Matrix {
    fun normalize(m: Array<DoubleArray>): Array<DoubleArray> {
        val newM = Array(m.size) { DoubleArray(m[0].size) }
        for (i in m.indices) {
            newM[i] = Vector.normalize(m[i])
        }
        return newM
    }

    /**
     * Square roots every element of the vector.
     *
     * @param inVector the vector
     * @return the resulting vector
     */
    fun sqrtVector(
        inVector: DoubleArray
    ): DoubleArray {
        val m = inVector.size
        val outVector = DoubleArray(m)
        for (i in 0 until m) {
            outVector[i] = sqrt(abs(inVector[i]))
        }
        return outVector
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
     * Converts a matrix object into a `String` object
     * representing its content.
     *
     * @param matrix the matrix to be converted to a string
     * @return the string representing the content of the matrix
     */
    fun toString(matrix: Array<DoubleArray>): String {
        var retVal = ""
        val m = matrix.size
        val n: Int = matrix[0].size
        for (i in 0 until m) {
            for (j in 0 until n) {
                retVal += fillString(
                    matrix[i][j].toString(), 24
                )
            }
            retVal += "\n"
        }
        return retVal
    }

    /**
     * Adds two matrices and returns the result in a new matrix object.
     *
     * @param mat1 the first matrix
     * @param mat2 the second matrix
     * @return the resulting matrix
     */
    fun add(mat1: Array<DoubleArray>, mat2: Array<DoubleArray>): Array<DoubleArray?> {
        val m = mat1.size
        val n: Int = mat1[0].size
        val matres = arrayOfNulls<DoubleArray>(m)
        for (i in 0 until m) {
            matres[i] = DoubleArray(n)
            for (j in 0 until n) {
                matres[i]!![j] = mat1[i][j] + mat2[i][j]
            }
        }
        return matres
    }

    /**
     * Subtracts two matrices and returns the result in a new matrix object.
     *
     * @param mat1 the first matrix
     * @param mat2 the second matrix
     * @return the resulting matrix
     */
    fun sub(mat1: Array<DoubleArray>, mat2: Array<DoubleArray>): Array<DoubleArray?> {
        val m = mat1.size
        val n: Int = mat1[0].size
        val matres = arrayOfNulls<DoubleArray>(m)
        for (i in 0 until m) {
            matres[i] = DoubleArray(n)
            for (j in 0 until n) {
                matres[i]!![j] = mat1[i][j] - mat2[i][j]
            }
        }
        return matres
    }

    /**
     * Multiplicates two matrices and returns the result in a new matrix object.
     *
     * @param mat1 the first matrix
     * @param mat2 the second matrix
     * @return the resulting matrix
     */
    fun mult(mat1: Array<DoubleArray>, mat2: Array<DoubleArray>): Array<DoubleArray?> {
        val m = mat1.size
        val n: Int = mat1[0].size
        val o: Int = mat2[0].size
        val matres = arrayOfNulls<DoubleArray>(m)
        for (i in 0 until m) {
            matres[i] = DoubleArray(o)
            for (j in 0 until o) {
                matres[i]!![j] = 0.0
                for (k in 0 until n) {
                    matres[i]!![j] += mat1[i][k] * mat2[k][j]
                }
            }
        }
        return matres
    }

    /**
     * Performs a matrix vector multiplication and returns the result
     * in a new vector object.
     *
     * @param mat the matrix
     * @param vec the vector
     * @return the resulting vector
     */
    fun mult(mat: Array<DoubleArray>, vec: DoubleArray): DoubleArray {
        val m = mat.size
        val n: Int = mat[0].size
        val vecres = DoubleArray(m)
        for (i in 0 until m) {
            vecres[i] = 0.0
            for (j in 0 until n) {
                vecres[i] += mat[i][j] * vec[j]
            }
        }
        return vecres
    }

    /**
     * Scales all elements of the matrix and returns the result
     * in a new matrix object.
     *
     * @param mat the matrix to scale
     * @param fac the factor to scale with
     * @return the scaled matrix
     */
    fun scale(mat: Array<DoubleArray>, fac: Double): Array<DoubleArray?> {
        val m = mat.size
        val n: Int = mat[0].size
        val res = arrayOfNulls<DoubleArray>(m)
        for (i in 0 until m) {
            res[i] = DoubleArray(n)
            for (j in 0 until n) {
                res[i]!![j] = mat[i][j] * fac
            }
        }
        return res
    }

    /**
     * Generates a random m*n matrix object.
     *
     * @param m number of desired rows
     * @param n number of desired columns
     * @return the random matrix
     */
    fun random(m: Int, n: Int): Array<DoubleArray?> {
        val matres = arrayOfNulls<DoubleArray>(m)
        for (i in 0 until m) {
            matres[i] = DoubleArray(n)
            for (j in 0 until n) {
                matres[i]!![j] = Math.random()
            }
        }
        return matres
    }

    /**
     * Builds a new m*n matrix object. Its content is undefined.
     *
     * @param m number of desired rows
     * @param n number of desired columns
     * @return the new matrix
     */
    fun newMatrix(m: Int, n: Int): Array<DoubleArray?> {
        val res = arrayOfNulls<DoubleArray>(m)
        for (i in 0 until m) {
            res[i] = DoubleArray(n)
        }
        return res
    }

    /**
     * Builds a new m*n matrix object, whose elements have a
     * predefined value.
     *
     * @param m   number of desired rows
     * @param n   number of desired columns
     * @param val the element's value
     * @return the new matrix
     */
    fun newMatrix(m: Int, n: Int, `val`: Double): Array<DoubleArray?> {
        val res = arrayOfNulls<DoubleArray>(m)
        for (i in 0 until m) {
            res[i] = DoubleArray(n)
            for (j in 0 until n) {
                res[i]!![j] = `val`
            }
        }
        return res
    }

    /**
     * Transposes a matrix and returns the result in a new
     * matrix object.
     *
     * @param mat the matrix to transpose
     * @return the transposed matrix
     */
    fun transpose(mat: Array<DoubleArray>): Array<DoubleArray?> {
        val m = mat.size
        val n: Int = mat[0].size
        val res = arrayOfNulls<DoubleArray>(n)
        for (i in 0 until n) {
            res[i] = DoubleArray(m)
            for (j in 0 until m) {
                res[i]!![j] = mat[j][i]
            }
        }
        return res
    }

    /**
     * Generates a copy of a given matrix.
     *
     * @param mat the matrix to copy
     * @return the copied matrix
     */
    fun clone(mat: Array<DoubleArray>): Array<DoubleArray?> {
        val m = mat.size
        val n: Int = mat[0].size
        val res = arrayOfNulls<DoubleArray>(m)
        for (i in 0 until m) {
            res[i] = DoubleArray(n)
            for (j in 0 until n) {
                res[i]!![j] = mat[i][j]
            }
        }
        return res
    }

    /**
     * Generates an identity n*n matrix.
     *
     * @param n the number of rows and columns
     * @return the identity matrix
     */
    fun identity(n: Int): Array<DoubleArray?> {
        val res = newMatrix(n, n, 0.0)
        for (i in 0 until n) {
            res[i]?.set(i, 1.0)
        }
        return res
    }

    /**
     * Generates a matrix, whose diagonal contains the content
     * of a given vector. The remaining elements of the matrix
     * contain zero.
     *
     * @param diag the diagonal vector
     * @return the resulting matrix
     */
    fun diag(diag: DoubleArray): Array<DoubleArray?> {
        val n = diag.size
        val res = newMatrix(n, n, 0.0)
        for (i in 0 until n) {
            res[i]?.set(i, diag[i])
        }
        return res
    }

    /**
     * Returns the `j`'th column of a matrix as a
     * new object.
     *
     * @param mat the matrix
     * @param j   the number of the column
     * @return a vector containing the column
     */
    fun getVecOfCol(mat: Array<DoubleArray>, j: Int): DoubleArray {
        val m = mat.size
        val res = DoubleArray(m)
        for (i in 0 until m) {
            res[i] = mat[i][j]
        }
        return res
    }

    /**
     * Returns the `i`'th row of a matrix as a
     * new object.
     *
     * @param mat the matrix
     * @param i   the number of the row
     * @return a vector containing the row
     */
    fun getVecOfRow(mat: Array<DoubleArray>, i: Int): DoubleArray {
        val n: Int = mat[0].size
        val res = DoubleArray(n)
        for (j in 0 until n) {
            res[j] = mat[i][j]
        }
        return res
    }

    /**
     * Returns the number of columns of a matrix.
     *
     * @param mat the matrix
     * @return the number of its columns
     */
    fun getNumOfColumns(mat: Array<DoubleArray>): Int {
        return mat[0].size
    }

    /**
     * Returns the number of rows of a matrix.
     *
     * @param mat the matrix
     * @return the number of its rows
     */
    fun getNumOfRows(mat: Array<DoubleArray>): Int {
        return mat.size
    }

    /**
     * Calculates the square matrix A * A'.
     *
     * @param mat the input matrix
     * @return the squared matrix
     */
    fun square(
        mat: Array<DoubleArray>
    ): Array<DoubleArray?> {
        val m = getNumOfRows(mat)
        val n = getNumOfColumns(mat)
        val res = newMatrix(m, m)
        for (i in 0 until m) {
            res[i]?.set(i, 0.0)
            for (k in 0 until n) {
                res[i]?.set(i, mat[i][k] * mat[i][k])
            }
            for (j in 0 until i) {
                res[i]?.set(j, 0.0)
                for (k in 0 until n) {
                    res[i]?.set(j, mat[i][k] * mat[j][k])
                }
                res[i]?.get(j)?.let { res[j]?.set(i, it) }
            }
        }
        return res
    }

    // only work for 3 x 3 matrix
    fun invMatrix(mat: DoubleArray): DoubleArray {
        val n = mat.size
        val res = DoubleArray(n)
        var det: Double
        var predet: Double
        predet = mat[0]
        for (x in 1..2) {
            predet = predet * mat[x]
        }
        res[0] = mat[1] * mat[2]
        res[1] = mat[0] * mat[2]
        res[2] = mat[1] * mat[0]
        if (predet != 0.0) {
            det = 1 / predet
            for (x in 0..2) {
                res[x] = det * res[x]
            }
        } else {
            for (x in 0..2) {
                res[x] = 0 * res[x]
            }
        }
        return res
    }
}