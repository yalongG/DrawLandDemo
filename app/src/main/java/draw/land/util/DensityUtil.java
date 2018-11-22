package draw.land.util;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by guoyalong on 2017/8/25.
 * Density 工具类
 */

public class DensityUtil {
    /**
     * dp转px
     *
     * @param context *
     * @return
     */
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    /**
     * sp转px
     *
     * @param context *
     * @return
     */
    public static int sp2px(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());
    }

}
