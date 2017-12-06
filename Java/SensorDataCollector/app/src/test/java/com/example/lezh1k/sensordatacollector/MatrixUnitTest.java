package com.example.lezh1k.sensordatacollector;

import org.junit.Test;

import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by lezh1k on 12/6/17.
 */

public class MatrixUnitTest {
    @Test
    public void matrixSetTest() throws Exception {
        Matrix m1, m2;
        m1 = new Matrix(3, 3);
        m1.Set(
                1.0, 2.0, 3.0,
                4.0, 5.0, 6.0,
                7.0, 8.0, 9.0);
        assertEquals(m1.data[0][0] , 1.0);
        assertEquals(m1.data[0][1] , 2.0);
        assertEquals(m1.data[0][2] , 3.0);
        assertEquals(m1.data[1][0] , 4.0);
        assertEquals(m1.data[1][1] , 5.0);
        assertEquals(m1.data[1][2] , 6.0);
        assertEquals(m1.data[2][0] , 7.0);
        assertEquals(m1.data[2][1] , 8.0);
        assertEquals(m1.data[2][2] , 9.0);
        m2 = new Matrix(2, 1);
        m2.Set( 4.2, 3.3);
        assertEquals(m2.data[0][0] , 4.2);
        assertEquals(m2.data[1][0] , 3.3);
    }

    @Test
    public void matrixAddTest() throws Exception {
        Matrix ma, mb, mc;
        ma = new Matrix(2, 3);
        mb = new Matrix(2, 3);
        mc = new Matrix(2, 3);

        ma.Set(1.0, 2.0, 3.0,
                4.0, 5.0, 6.0);
        mb.Set(
                7.0, 8.0, 9.0,
                10.0, 11.0, 12.0);
        Matrix.MatrixAdd(ma, mb, mc);
        assertEquals(mc.data[0][0] , 8.0);
        assertEquals(mc.data[0][1] , 10.0);
        assertEquals(mc.data[0][2] , 12.0);
        assertEquals(mc.data[1][0] , 14.0);
        assertEquals(mc.data[1][1] , 16.0);
        assertEquals(mc.data[1][2] , 18.0);

        Matrix.MatrixAdd(ma, mc, ma); //inplace test
        assertEquals(ma.data[0][0] , 9.0);
        assertEquals(ma.data[0][1] , 12.0);
        assertEquals(ma.data[0][2] , 15.0);
        assertEquals(ma.data[1][0] , 18.0);
        assertEquals(ma.data[1][1] , 21.0);
        assertEquals(ma.data[1][2] , 24.0);
    }

    @Test
    public void matrixSubstractTest() throws Exception {
        Matrix ma, mb, mc;
        ma = new Matrix(2, 3);
        mb = new Matrix(2, 3);
        mc = new Matrix(2, 3);

        ma.Set(
                12.0, 11.0, 10.0,
                9.0,  8.0,  7.0);
        mb.Set(
                1.0, 2.0, 3.0,
                4.0, 5.0, 6.0);

        Matrix.MatrixSubstract(ma, mb, mc);
        assertEquals(mc.data[0][0] , 11.0);
        assertEquals(mc.data[0][1] , 9.0);
        assertEquals(mc.data[0][2] , 7.0);
        assertEquals(mc.data[1][0] , 5.0);
        assertEquals(mc.data[1][1] , 3.0);
        assertEquals(mc.data[1][2] , 1.0);

        Matrix.MatrixSubstract(ma, mc, ma); //inplace test
        assertEquals(ma.data[0][0] , 1.0);
        assertEquals(ma.data[0][1] , 2.0);
        assertEquals(ma.data[0][2] , 3.0);
        assertEquals(ma.data[1][0] , 4.0);
        assertEquals(ma.data[1][1] , 5.0);
        assertEquals(ma.data[1][2] , 6.0);
    }

    @Test
    public void matrixSubstractFromIdentityTest() throws Exception {
        Matrix m;
        m = new Matrix(3, 3);
        m.Set(
                1.0, 2.0, 3.0,
                4.0, 5.0, 6.0,
                7.0, 8.0, 9.0);
        m.SubtractFromIdentity();

        assertEquals(m.data[0][0] , 0.0);
        assertEquals(m.data[0][1] , -2.0);
        assertEquals(m.data[0][2] , -3.0);
        assertEquals(m.data[1][0] , -4.0);
        assertEquals(m.data[1][1] , -4.0);
        assertEquals(m.data[1][2] , -6.0);
        assertEquals(m.data[2][0] , -7.0);
        assertEquals(m.data[2][1] , -8.0);
        assertEquals(m.data[2][2] , -8.0);
    }

    @Test
    public void matrixMultiplyTest() throws Exception {
        Matrix ma, mb, mc, md;
        ma = new Matrix(3, 3);
        mb = new Matrix(3, 3);
        mc = new Matrix(3, 3);
        md = new Matrix(3, 3);

        ma.Set(
                1.0, 2.0, 3.0,
                4.0, 5.0, 6.0,
                7.0, 8.0, 9.0);

        mb.Set(
                10.0, 11.0, 12.0,
                13.0, 14.0, 15.0,
                16.0, 17.0, 18.0);

        mc.Set(
                84.0, 90.0, 96.0,
                201.0, 216.0, 231.0,
                318.0, 342.0, 366.0);
        Matrix.MatrixMultiply(ma, mb, md);
        assertTrue(Matrix.MatrixEq(md, mc, 1e-06));
    }

    @Test
    public void matrixMultiplyByTransposeTest() throws Exception {
        Matrix ma, mb, mc, md;
        ma = new Matrix(3, 3);
        mb = new Matrix(3, 3);
        mc = new Matrix(3, 3);
        md = new Matrix(3, 3);

        ma.Set(
                1.0, 2.0, 3.0,
                4.0, 5.0, 6.0,
                7.0, 8.0, 9.0);

        mb.Set(
                10.0, 11.0, 12.0,
                13.0, 14.0, 15.0,
                16.0, 17.0, 18.0);

        mc.Set(
                68.0, 86.0, 104.0,
                167.0, 212.0, 257.0,
                266.0, 338.0, 410.0);

        Matrix.MatrixMultiplyByTranspose(ma, mb, md);
        assertTrue(Matrix.MatrixEq(mc, md, 1e-06));
    }

    @Test
    public void matrixTransposeTest() throws Exception{
        Matrix ma, mb, mc;ma = new Matrix(3, 1);
        mb = new Matrix(1, 3);
        mc = new Matrix(1, 3);
        ma.Set( 1, 2, 3);
        mb.Set( 1, 2, 3);
        Matrix.MatrixTranspose(ma, mc);
        assert(Matrix.MatrixEq(mb, mc, 1e-06));
    }

    @Test
    public void matrixEqTest() throws Exception {
        Matrix m1, m2;
        int r, c;
        m1 = new Matrix(3, 3);
        m2 = new Matrix(3, 3);
        Random rnd = new Random();
        for (r = 0; r < 3; ++r) {
            for (c = 0; c < 3; ++c) {
                m1.data[r][c] = m2.data[r][c] = rnd.nextDouble();
            }
        }
        assertTrue(Matrix.MatrixEq(m1, m2, 1e-06));
    }

    @Test
    public void matrixScaleTest() throws Exception{
        Matrix ma, mb;
        double scalar = 2.5;
        ma = new Matrix(2, 2);
        mb = new Matrix(2, 2);

        ma.Set(
                1.0, 2.0,
                3.0, 4.0);
        mb.Set(
                2.5, 5.0,
                7.5, 10.0);
        ma.Scale(scalar);
        assertTrue(Matrix.MatrixEq(ma, mb, 1e-06));
    }

    @Test
    public void matrixDestructiveInvertTest() throws Exception {
        Matrix ma, mb, mc;
        ma = new Matrix(3, 3);
        mb = new Matrix(3, 3);
        mc = new Matrix(3, 3);

        ma.Set(
                5.0, 2.0, 3.0,
                8.0, 12.0, 22.0,
                39.0, 3.0, 11.0);

        mc.Set(
                33.0 / 269.0, -13.0 / 538.0, 4.0 / 269.0,
                385.0 / 269.0, -31.0 / 269.0, -43.0 / 269.0,
                -222.0 / 269.0, 63.0 / 538.0, 22.0 / 269.0);

        Matrix.MatrixDestructiveInvert(ma, mb);
        assertTrue(Matrix.MatrixEq(mb, mc, 1e-06));
    }
}
