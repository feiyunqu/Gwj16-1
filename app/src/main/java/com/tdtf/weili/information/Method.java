package com.tdtf.weili.information;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

/**
 * Created by a on 2017/3/16.
 * 四大系数
 * 修正值
 * 均值
 * 自定义均值
 */

public class Method {

    public static BigDecimal[][] main(BigDecimal[] horizon, BigDecimal[] vertical, BigDecimal[] margs) {
        BigDecimal[] a = new BigDecimal[horizon.length - 1];
        BigDecimal[] b = new BigDecimal[horizon.length - 1];
        BigDecimal[] c = new BigDecimal[horizon.length - 1];
        BigDecimal[] d = new BigDecimal[horizon.length - 1];
        BigDecimal[] h = new BigDecimal[horizon.length - 1];
        for (int i = 0; i < horizon.length - 1; i++) {
            h[i] = horizon[i + 1].subtract(horizon[i]);
        }
        for (int i = 0; i < horizon.length - 1; i++) {
            a[i] = margs[i + 1].subtract(margs[i]).divide
                    (h[i].multiply(new BigDecimal("6")), 3, RoundingMode.HALF_UP);
        }
        for (int i = 0; i < horizon.length - 1; i++) {
            //b[i]=(horizon[i+1]*margs[i]-horizon[i]*margs[i+1])/(2*h[i]);
            b[i] = horizon[i + 1].multiply(margs[i]).subtract(horizon[i].multiply(margs[i + 1])).divide
                    (h[i].multiply(new BigDecimal("2")), 3, RoundingMode.HALF_UP);
        }
        for (int i = 0; i < horizon.length - 1; i++) {
//            c[i]=(vertical[i+1]-vertical[i])/h[i]+h[i]*(margs[i]-margs[i+1])/6
//                    +(horizon[i]*horizon[i]*margs[i+1]-horizon[i+1]*horizon[i+1]*margs[i])/(2*h[i]);
            c[i] = vertical[i + 1].subtract(vertical[i]).divide(h[i], 3, RoundingMode.HALF_UP).add
                    (h[i].multiply(margs[i].subtract(margs[i + 1])).divide
                            (new BigDecimal("6"), 3, RoundingMode.HALF_UP)).add
                    (horizon[i].multiply(horizon[i]).multiply(margs[i + 1]).subtract(horizon[i + 1].multiply(horizon[i + 1]).multiply(margs[i])).divide
                            (h[i].multiply(new BigDecimal("2")), 3, RoundingMode.HALF_UP));
//            c[i] = (vertical[i + 1].multiply(new BigDecimal("6"))
//                    .subtract(vertical[i].multiply(new BigDecimal("6")))
//                    .add(h[i].multiply(h[i]).multiply(margs[i]))
//                    .subtract(h[i].multiply(h[i]).multiply(margs[i + 1]))
//                    .add(horizon[i].multiply(horizon[i]).multiply(margs[i + 1]).multiply(new BigDecimal("3")))
//                    .subtract(horizon[i + 1].multiply(horizon[i + 1]).multiply(margs[i]).multiply(new BigDecimal("3"))))
//                    .divide(h[i].multiply(new BigDecimal("6")), 3, RoundingMode.HALF_UP);
        }
        for (int i = 0; i < horizon.length - 1; i++) {
//            d[i]=(vertical[i]*horizon[i+1]-vertical[i+1]*horizon[i])/h[i]
//                    +h[i]*(margs[i+1]*horizon[i]-margs[i]*horizon[i+1])/6
//                    +(horizon[i+1]*horizon[i+1]*horizon[i+1]*margs[i]-horizon[i]*horizon[i]*horizon[i]*margs[i+1])/(6*h[i]);
            d[i] = vertical[i].multiply(horizon[i + 1]).subtract(vertical[i + 1].multiply(horizon[i])).divide(h[i], 3, RoundingMode.HALF_UP).add
                    (margs[i + 1].multiply(horizon[i]).subtract(margs[i].multiply(horizon[i + 1])).multiply(h[i]).divide(new BigDecimal("6"), 3, RoundingMode.HALF_UP)).add
                    (horizon[i + 1].multiply(horizon[i + 1]).multiply(horizon[i + 1]).multiply(margs[i]).subtract
                            (horizon[i].multiply(horizon[i]).multiply(horizon[i]).multiply(margs[i + 1])).divide(h[i].multiply(new BigDecimal("6")), 3, RoundingMode.HALF_UP));
//            d[i] = (vertical[i].multiply(horizon[i + 1]).multiply(new BigDecimal("6"))
//                    .subtract(vertical[i + 1].multiply(horizon[i]).multiply(new BigDecimal("6")))
//                    .add(h[i].multiply(h[i]).multiply(margs[i + 1]).multiply(horizon[i]))
//                    .subtract(h[i].multiply(h[i]).multiply(margs[i]).multiply(horizon[i + 1]))
//                    .add(horizon[i + 1].multiply(horizon[i + 1]).multiply(horizon[i + 1]).multiply(margs[i]))
//                    .subtract(horizon[i].multiply(horizon[i]).multiply(horizon[i]).multiply(margs[i + 1])))
//                    .divide(h[i].multiply(new BigDecimal("6")), 3, RoundingMode.HALF_UP);

        }
        return new BigDecimal[][]{a, b, c, d};
    }

    public static BigDecimal[] correct(BigDecimal[] vertical, BigDecimal[] modify) {
        BigDecimal[] x = new BigDecimal[16];
        for (int i = 0; i < 16; i++) {
            try {
                x[i] = vertical[i].add(modify[i].divide(new BigDecimal("10"),1,RoundingMode.HALF_UP)).multiply(new BigDecimal("6553.5")).divide(modify[16], 1, RoundingMode.HALF_UP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return x;
    }

    public static String average(ArrayList<String> arrayList) {
        BigDecimal mAverage = new BigDecimal("0.0");
        for (int i = 0; i < arrayList.size(); i++) {
            mAverage = mAverage.add(new BigDecimal(arrayList.get(i)));
        }
        return String.valueOf(mAverage.divide(new BigDecimal("" + arrayList.size()), 1, RoundingMode.HALF_UP));
    }

    public static ArrayList<String> mathaverage(ArrayList<String> arrayList) {
        BigDecimal mAverage = new BigDecimal("0.0");
        int cishu = arrayList.size() / 16;
        ArrayList<String> arrayList1 = new ArrayList<>();
        for (int k = 0; k < 16; k++) {
            try {
                for (int i = 0; i < cishu; i++) {
                    mAverage = mAverage.add(new BigDecimal(arrayList.get(k + i * 16)));
                }
                arrayList1.add(String.valueOf(mAverage.divide(new BigDecimal("" + cishu), 1, RoundingMode.HALF_UP)));
                mAverage = new BigDecimal("0.0");
            } catch (Exception e) {
                e.printStackTrace();
                arrayList1.add("");
                mAverage = new BigDecimal("0.0");
            }
        }
        return arrayList1;
    }
}//
