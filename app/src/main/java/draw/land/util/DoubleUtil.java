package draw.land.util;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class DoubleUtil {

    /**
     * 对double数据进行取精度.
     *
     * @param value        double数据.
     * @param scale        精度位数(保留的小数位数).
     * @param roundingMode 精度取值方式.
     * @return 精度计算后的数据.
     */
    public static double round(double value, int scale, int roundingMode) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(scale, roundingMode);
        double d = bd.doubleValue();
        bd = null;
        return d;
    }

    /**
     * 保留两位小数
     *
     * @param number
     * @return
     */
    public static String pointTwo(String number) {
        if (TextUtils.isEmpty(number)) {
            return "0.00";
        }
        double d = (new Double(number)).doubleValue();
        DecimalFormat df = new DecimalFormat("######.00");
        String string = df.format(d);
        if (".".equals(string.substring(0, 1))) {
            return "0" + df.format(d);
        }
        return df.format(d);
    }

    /**
     * 保留4位小数
     *
     * @param number
     * @return
     */
    public static String pointFour(double number) {
        DecimalFormat df = new DecimalFormat("######.0000");
        return df.format(number);
    }

    /**
     * 保留1位小数,千位分隔符
     *
     * @param number
     * @return
     */
    public static String pointOne(double number) {
        double d = (new Double(number)).doubleValue();
        DecimalFormat df = new DecimalFormat("#,###.0");
        String string = df.format(d);
        if (".".equals(string.substring(0, 1))) {
            return "0" + df.format(d);
        }
        return df.format(number);
    }

    /**
     * 保留4位小数
     *
     * @param number
     * @return
     */
    public static String pointTwo(double number) {
        DecimalFormat df = new DecimalFormat("######.00");
        String string = df.format(number);
        if (".".equals(string.substring(0, 1))) {
            return "0" + string;
        }
        return string;
    }
}
