package com.tdtf.weili.information;

/**
 * Created by a on 2017/3/16.
 * 求解M个参数
 */

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
  * 解多元一次方程组
  * 只能解决n个变量n个方程的情况
   * 获得M1~Mn-1个参数
  */
public class Equation {
    private static final BigDecimal ZERO = new BigDecimal("0");
    private static final BigDecimal ONE = new BigDecimal("1");
    public static BigDecimal[] main(BigDecimal[] xray,BigDecimal[] yray,int lenth) {
        BigDecimal[] u=new BigDecimal[lenth-1];//系数μ
        BigDecimal[] r=new BigDecimal[lenth-1];//系数λ
        BigDecimal[] d=new BigDecimal[lenth-1];//系数d
        BigDecimal[] h=new BigDecimal[lenth-1];
        //'h' is 0 to 14
        for (int l=0;l<lenth-1;l++){
            h[l]=xray[l+1].subtract(xray[l]);
        }
        //'u' and 'r' and 'd' is 1 to 14
        for (int j=1;j<lenth-1;j++){
            u[j]=h[j-1].divide(h[j-1].add(h[j]),3,RoundingMode.HALF_UP);
            r[j]=new BigDecimal("1").subtract(u[j]);
            d[j]=(h[j-1].multiply(yray[j+1].subtract(yray[j])).subtract
                    (h[j].multiply(yray[j].subtract(yray[j-1])))).multiply(new BigDecimal("6")).
                    divide(h[j].multiply(h[j-1]).multiply(h[j-1].add(h[j])),3,RoundingMode.HALF_UP);
        }
        BigDecimal[][] matrix = new BigDecimal[lenth-2][lenth-1];
        matrix[0][0]=new BigDecimal("2");
        matrix[0][1]=r[1];
        matrix[lenth-3][lenth-4]=u[lenth-2];
        matrix[lenth-3][lenth-3]=new BigDecimal("2");
        for (int i=1;i<lenth-3;i++){
            matrix[i][i-1]=u[i+1];
            matrix[i][i]=new BigDecimal("2");
            matrix[i][i+1]=r[i+1];
        }
        for (int n=0;n<lenth-2;n++){
            matrix[n][lenth-2]=d[n+1];
        }
        for (int n=0;n<lenth-2;n++){
            for (int m=0;m<lenth-1;m++){
                if (matrix[n][m]==null){
                    matrix[n][m]=ZERO;
                }
            }
        }
        return new Equation().solveEquation(matrix, 3, RoundingMode.HALF_UP);
    }
    /**
     * 解多元一次方程组
     * 只能解决n个变量n个方程的情况,即矩阵是n*(n+1)的形式
     * @param matrix 矩阵
     * @param scale 精确小数位数
     * @param roundingMode 舍入模式
     * @return 返回
     */
    public BigDecimal[] solveEquation(BigDecimal[][] matrix, int scale, RoundingMode roundingMode){
        if(isNullOrEmptyMatrix(matrix)){
            return new BigDecimal[0];
        }
        BigDecimal[][] triangular = elimination(matrix, scale, roundingMode);
        return substitutionUpMethod(triangular, scale, roundingMode);
    }
    /**
     * 用高斯消元法将矩阵变为上三角形矩阵
     *
     * @param matrix 矩阵
     * @param scale 精确小数位数
     * @param roundingMode 舍入模式
     * @return 返回
     */
    private BigDecimal[][] elimination(BigDecimal[][] matrix, int scale, RoundingMode roundingMode) {
        if(isNullOrEmptyMatrix(matrix) || matrix.length != matrix[0].length - 1){
            return new BigDecimal[0][0];
        }
        int matrixLine = matrix.length;
        for (int i = 0; i < matrixLine - 1; ++i) {
            //第j行的数据 - (第i行的数据 / matrix[i][i])*matrix[j][i]
            for (int j = i + 1; j < matrixLine; ++j) {
                for (int k = i + 1; k <= matrixLine; ++k) {
                    //matrix[j][k] = matrix[j][k] - (matrix[i][k]/matrix[i][i])*matrix[j][i];
                    matrix[j][k] = matrix[j][k].subtract((matrix[i][k].divide(matrix[i][i], scale, roundingMode).multiply(matrix[j][i])));
                }
                matrix[j][i] = ZERO;
            }
        }
        return matrix;
    }
    /**
     * 回代求解(针对上三角形矩阵)
     *
     * @param matrix 上三角阵
     * @param scale 精确小数位数
     * @param roundingMode 舍入模式
     */
    private BigDecimal[] substitutionUpMethod(BigDecimal[][] matrix, int scale, RoundingMode roundingMode) {
        int row = matrix.length;
        for (int i = 0; i < row; ++i) {
            if (matrix[i][i].equals(ZERO.setScale(scale))) {//方程无解或者解不惟一
                return new BigDecimal[0];
            }
        }
        BigDecimal[] result = new BigDecimal[row];
        for (int i = 0; i < result.length; ++i) {
            result[i] = ONE;
        }
        BigDecimal tmp;
        for (int i = row - 1; i >= 0; --i) {
            tmp = ZERO;
            int j = row - 1;
            while (j > i) {
                tmp = tmp.add(matrix[i][j].multiply(result[j]));
                --j;
            }
            result[i] = matrix[i][row].subtract(tmp).divide(matrix[i][i], scale, roundingMode);
        }
        return result;
    }//
    /**
     * 判断系数矩阵是否是null或空数组
     * @param matrix 系数矩阵
     * @return null或空数组返回true,否则返回false
     */
    private static boolean isNullOrEmptyMatrix(BigDecimal[][] matrix){
        if(matrix == null || matrix.length == 0){
            return true;
        }
        int row = matrix.length;
        int col = matrix[0].length;
        for(int i = 0; i < row; ++i){
            for(int j = 0; j < col; ++j){
                if(matrix[i][j] == null){
                    return true;
                }
            }
        }
        return false;
        //
    }
}
