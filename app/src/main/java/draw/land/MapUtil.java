package draw.land;

import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.UiSettings;

/**
 * @author guoyalong
 */
public class MapUtil {

    /**
     * 隐藏不需要显示的图标
     */
    public static void hideLogo(MapboxMap mapboxMap) {
        UiSettings uiSettings = mapboxMap.getUiSettings();
        uiSettings.setCompassEnabled(false); // 隐藏指南针
        uiSettings.setLogoEnabled(false); // 隐藏logo
        uiSettings.setRotateGesturesEnabled(false); // 设置是否可以旋转地图
        uiSettings.setAttributionEnabled(false); // 设置隐藏显示那个提示按钮
        uiSettings.setTiltGesturesEnabled(false); // 设置是否可以调整地图倾斜角
    }
}
