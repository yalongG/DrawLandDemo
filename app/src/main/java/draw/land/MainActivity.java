package draw.land;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;

import java.util.List;

import draw.land.util.DensityUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private LandMapView landMapView;
    private MirrorLandMapView mirrorLandMapView;
    private MirrorLandView mirrorLandView;

    @SuppressLint({"ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);
        landMapView = findViewById(R.id.landMapView);
        mirrorLandMapView = findViewById(R.id.mirror_land_map_view);
        mirrorLandView = findViewById(R.id.mirror_land_view);
        landMapView.onCreate(savedInstanceState);
        mirrorLandMapView.onCreate(savedInstanceState);

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
            public void showMessage(boolean isClose, boolean mIsIntersect, String message) {

            }
        });


        findViewById(R.id.btn_add).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    private void removeMirror() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mirrorLandMapView.getLayoutParams();
        params.rightMargin = DensityUtil.dp2px(this, 1000);
        params.topMargin = -DensityUtil.dp2px(this, 500);
        mirrorLandMapView.setLayoutParams(params);
    }

    private void showMirror() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mirrorLandMapView.getLayoutParams();
        params.rightMargin = DensityUtil.dp2px(this, 10);
        params.topMargin = DensityUtil.dp2px(this, 15);
        mirrorLandMapView.setLayoutParams(params);
    }

    @Override
    protected void onStart() {
        super.onStart();
        landMapView.onStart();
        mirrorLandMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        landMapView.onStop();
        mirrorLandMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        landMapView.onDestroy();
        mirrorLandMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        landMapView.onResume();
        mirrorLandMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        landMapView.onPause();
        mirrorLandMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        landMapView.onSaveInstanceState(outState);
        mirrorLandMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        landMapView.onLowMemory();
        mirrorLandMapView.onLowMemory();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                landMapView.addPoint();
                break;
            case R.id.btn_cancel:
                landMapView.cancelPoint();
                break;
        }
    }
}
