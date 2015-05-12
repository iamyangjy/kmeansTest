package com.ruijie.rbis.algorithm;

/**
 * Created by OA on 2014/12/29.
 */

import Jama.Matrix;
import Jama.QRDecomposition;

public class Mlr {
    private final int N;        // number of
    private final int p;        // number of dependent variables
    private final Matrix beta;  // regression coefficients
    private double SSE;         // sum of squared
    private double SST;         // sum of squared

    public Mlr(double[][] x, double[] y) {
        if (x.length != y.length) throw new RuntimeException("dimensions don't agree");
        N = y.length;
        p = x[0].length;

        //转化为矩阵
        Matrix X = new Matrix(x);

        // create matrix from vector
        Matrix Y = new Matrix(y, N);

        // find least squares solution
        QRDecomposition qr = new QRDecomposition(X);
        beta = qr.solve(Y);

        // mean of y[] values
        double sum = 0.0;
        for (int i = 0; i < N; i++)
            sum += y[i];
        double mean = sum / N;

        // total variation to be accounted for
        for (int i = 0; i < N; i++) {
            double dev = y[i] - mean;
            SST += dev*dev;
        }

        // variation not accounted for
        Matrix residuals = X.times(beta).minus(Y);

        SSE = residuals.norm2() * residuals.norm2();

    }

    //获取每个变量的系数
    public double beta(int j) {
        return beta.get(j, 0);
    }

    //获取R平方值，R平方值取值范围为0到1，越接近于1，拟合效果越好
    //越接近0，拟合效果最差。
    public double R2() {
        return 1.0 - SSE/SST;
    }

    public static  void main(String[] args){
       double[][] rawMatrix = new double[][]
               {
                       {1, 15.31, 57.3},
                       {1, 15.20, 63.8},
                       {1, 16.25, 65.4},
                       {1, 14.33, 57.0},
                       {1, 14.57, 63.8},
                       {1, 17.33, 63.2},
                       {1, 14.48, 60.2},
                       {1, 14.91, 57.7},
                       {1, 15.25, 56.4},
                       {1, 13.89, 55.6},
                       {1, 15.18, 62.6},
                       {1, 14.44, 63.4},
                       {1, 14.87, 60.2},
                       {1, 18.63, 67.2},
                       {1, 15.20, 57.1},
                       {1, 25.76, 89.6},
                       {1, 19.05, 68.6},
                       {1, 15.37, 60.1},
                       {1, 18.06, 66.3},
                       {1, 16.35, 65.8}};
/*                {
                       {15.31, 57.3},
                       {15.20, 63.8},
                       {16.25, 65.4},
                       {14.33, 57.0},
                       {14.57, 63.8},
                       {17.33, 63.2},
                       {14.48, 60.2},
                       {14.91, 57.7},
                       {15.25, 56.4},
                       {13.89, 55.6},
                       {15.18, 62.6},
                       {14.44, 63.4},
                       {14.87, 60.2},
                       {18.63, 67.2},
                       {15.20, 57.1},
                       {25.76, 89.6},
                       {19.05, 68.6},
                       {15.37, 60.1},
                       {18.06, 66.3},
                       {16.35, 65.8}};
 */
       double[] sale = new double[]{74.8, 74.0,72.9, 70.0, 74.9, 76.0, 72.0, 73.5, 74.5, 73.5, 71.5, 71.0, 78.9, 86.5, 68.0, 102.0, 84.0, 69.0, 88.0, 76.0};
        Mlr mlr = new Mlr(rawMatrix, sale);
        //例子答案:y = 30.967 + 2.637 z1 + 0.045 * z2, R2 = 0.834
        System.out.println("系数1:"+ mlr.beta(0) + "系数2:" + mlr.beta(1) + "系数3:" + mlr.beta(2) + "r平方" + mlr.R2());
    }


}
