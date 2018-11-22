package draw.land;

import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.mapbox.mapboxsdk.camera.CameraPosition;

import java.util.List;

import draw.land.util.DensityUtil;
import draw.land.view.LandMapView;
import draw.land.view.MirrorLandMapView;
import draw.land.view.MirrorLandView;

public class DrawLandView extends RelativeLayout {
    private Context context;
    private LandMapView landMapView;
    private MirrorLandMapView mirrorLandMapView;
    private MirrorLandView mirrorLandView;

    public DrawLandView(Context context) {
        this(context, null);
    }

    public DrawLandView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawLandView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        View view = View.inflate(context, R.layout.layout_draw_land_view, this);
        landMapView = view.findViewById(R.id.landMapView);
        mirrorLandMapView = view.findViewById(R.id.mirror_land_map_view);
        mirrorLandView = view.findViewById(R.id.mirror_land_view);

        removeMirror();

        landMapView.setOnMoveLandListener(new LandMapView.MoveLandListener() {
            @Override
            public void startMove() {

                if (mirrorLandView.getVisibility() == View.GONE) {
                    mirrorLandView.setVisibility(View.VISIBLE);
                }

                showMirror();
            }

            @Override
            public void showMirrorLand(CameraPosition cameraPosition, PointF pointF1,
                                       int clickPosition, boolean isClose, List<PointF> pointFList) {
                mirrorLandMapView.setMoveDrawView((isClose1, pointFList1) -> mirrorLandView.setData(isClose1, pointFList1));
                mirrorLandMapView.setData(cameraPosition, pointF1, clickPosition, isClose, pointFList);
            }

            @Override
            public void dismissMirrorLand() {

                if (mirrorLandView.getVisibility() == View.VISIBLE) {
                    mirrorLandView.setVisibility(View.GONE);
                }
                removeMirror();
            }

            @Override
            public void showMessage(boolean isClose, boolean isIntersect, String message) {
                //TODO 做自己想做的事情
            }
        });
    }

    private void removeMirror() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mirrorLandMapView.getLayoutParams();
        params.rightMargin = DensityUtil.dp2px(context, 1000);
        params.topMargin = -DensityUtil.dp2px(context, 500);
        mirrorLandMapView.setLayoutParams(params);
    }

    private void showMirror() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mirrorLandMapView.getLayoutParams();
        params.rightMargin = DensityUtil.dp2px(context, 10);
        params.topMargin = DensityUtil.dp2px(context, 15);
        mirrorLandMapView.setLayoutParams(params);
    }

    public void onCreate(Bundle savedInstanceState) {
        landMapView.onCreate(savedInstanceState);
        mirrorLandMapView.onCreate(savedInstanceState);
    }

    public void onStart() {
        landMapView.onStart();
        mirrorLandMapView.onStart();
    }

    public void onStop() {
        landMapView.onStop();
        mirrorLandMapView.onStop();
    }

    public void onDestroy() {
        landMapView.onDestroy();
        mirrorLandMapView.onDestroy();
    }

    public void onResume() {
        landMapView.onResume();
        mirrorLandMapView.onResume();
    }

    public void onPause() {
        landMapView.onPause();
        mirrorLandMapView.onPause();
    }

    public void onSaveInstanceState(Bundle outState) {
        landMapView.onSaveInstanceState(outState);
        mirrorLandMapView.onSaveInstanceState(outState);
    }

    public void onLowMemory() {
        landMapView.onLowMemory();
        mirrorLandMapView.onLowMemory();
    }

    /**
     * 加点
     */
    public void addPoint() {
        landMapView.addPoint();
    }

    /**
     * 减点
     */
    public void cancelPoint() {
        landMapView.cancelPoint();
    }

    /**
     * 获取点集合
     */
    public void getPoints() {
        landMapView.getLatLngList();
    }
}
