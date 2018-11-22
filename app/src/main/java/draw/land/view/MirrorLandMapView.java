package draw.land.view;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.List;

import draw.land.util.MapUtil;

/**
 * @author guoyalong
 * 镜子 mapView
 */
public class MirrorLandMapView extends MapView {
    private MapboxMap mMap;
    private List<PointF> movePointFList;
    private MirrorMoveDrawView moveDrawView;

    public MirrorLandMapView(@NonNull Context context) {
        this(context, null);
    }

    public MirrorLandMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MirrorLandMapView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 设置数据
     */
    public void setData(CameraPosition cameraPosition, PointF pointF1, int clickPosition,
                        boolean isClose, List<PointF> pointFList) {
        movePointFList.clear();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        float distanceX = pointF1.x - this.getWidth() / 2;
        float distanceY = pointF1.y - this.getHeight() / 2;
        for (int i = 0; i < pointFList.size(); i++) {
            PointF pointF = new PointF();
            if (isClose) {
                if (clickPosition == 0 || clickPosition == pointFList.size() - 1) {
                    if (i == 0 || i == pointFList.size() - 1) {
                        pointF.x = this.getWidth() / 2;
                        pointF.y = this.getHeight() / 2;
                    } else {
                        pointF.x = pointFList.get(i).x - distanceX;
                        pointF.y = pointFList.get(i).y - distanceY;
                    }
                } else {
                    if (clickPosition != i) {
                        pointF.x = pointFList.get(i).x - distanceX;
                        pointF.y = pointFList.get(i).y - distanceY;
                    } else {
                        pointF.x = this.getWidth() / 2;
                        pointF.y = this.getHeight() / 2;
                    }
                }
            } else {
                if (clickPosition != i) {
                    pointF.x = pointFList.get(i).x - distanceX;
                    pointF.y = pointFList.get(i).y - distanceY;
                } else {
                    pointF.x = this.getWidth() / 2;
                    pointF.y = this.getHeight() / 2;
                }
            }

            movePointFList.add(pointF);
        }
        if (moveDrawView != null) {
            moveDrawView.drawView(isClose, movePointFList);
        }
    }

    private void init() {
        movePointFList = new ArrayList<>();
        this.getMapAsync(mapboxMap -> {
                    this.mMap = mapboxMap;
                    MapUtil.hideLogo(mMap);
                }
        );
    }

    public interface MirrorMoveDrawView {
        void drawView(boolean isClose, List<PointF> pointFList);
    }

    public void setMoveDrawView(MirrorMoveDrawView moveDrawView) {
        this.moveDrawView = moveDrawView;
    }
}
