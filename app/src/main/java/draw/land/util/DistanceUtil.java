package draw.land.util;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guoyalong on 2018/1/30.
 * 根据两个点坐标点，计算距离
 */

public class DistanceUtil {
    private static double EARTH_RADIUS = 6378.137;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    public static List<Double> getDistances(List<LatLng> latLngs) {
        List<Double> distances = new ArrayList<>();
        for (int i = 0; i < latLngs.size() - 1; i++) {
            distances.add(getDistance(latLngs.get(i), latLngs.get(i + 1)));
        }
        return distances;
    }

    /**
     * 通过经纬度获取距离(单位：米)
     */
    private static double getDistance(LatLng latLng1, LatLng latLng2) {
        double radLat1 = rad(latLng1.getLatitude());
        double radLat2 = rad(latLng2.getLatitude());
        double a = radLat1 - radLat2;
        double b = rad(latLng1.getLongitude()) - rad(latLng2.getLongitude());
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000d) / 10000d;
        s = s * 1000;
        return s;
    }
}
