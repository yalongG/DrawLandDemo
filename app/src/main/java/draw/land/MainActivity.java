package draw.land;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.google.gson.JsonObject;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.geojson.Point;
import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import draw.land.util.DistanceUtil;
import draw.land.util.DoubleUtil;
import draw.land.util.LineUtil;

import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private LandMapView landView;
    private MapboxMap landMap;
    private CameraPosition cameraPosition;
    private List<LatLng> latLngList = new ArrayList<>();
    private boolean isClose = false; // 是否闭合
    private boolean isIntersect = false; // 是否相交
    private boolean isStartMove = false;// 是否开始移动

    // 点
    private final String POINT_SOURCE = "point_source_id";
    private final String POINT_LAYER = "point_layer_id";

    // 线
    private final String LINE_SOURCE = "line_source_id";
    private final String LINE_LAYER = "line_layer_id";

    // 面
    private final String TEXT_SOURCE = "text_source_id";
    private final String TEXT_LAYER = "text_layer_id";

    private Polygon polygon;
    private int touchIndex = -1; // 长按时的位置
    private int clickIndex = -1; // 点击时的位置

    @SuppressLint({"ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);
        landView = findViewById(R.id.landView);
        landView.onCreate(savedInstanceState);
        landView.getMapAsync(mapBoxMap -> {
                    landMap = mapBoxMap;
                    hideLog();
                    addBitmap();
                    landMap.setOnCameraChangeListener(position ->
                            cameraPosition = position // 获取中心点
                    );
                    landMap.addOnMapLongClickListener(point -> { // 长按
                        if (latLngList.size() <= 0) {
                            return;
                        }

                        touchIndex = judgeClickPosition(point);
                        if (touchIndex != -1) {
                            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                            vibrator.vibrate(200);
                            isStartMove = true;
                            removeLayer();
                        }
                    });

                    landMap.addOnMapClickListener(point -> {
                        if (latLngList.size() <= 0) {
                            return;
                        }

                        clickIndex = judgeClickPosition(point);
                        if (clickIndex != -1) {
                            drawLand();
                        }
                    });

                    landView.setOnTouchListener((v, event) -> {
                        if (isStartMove) {
                            LatLng latLng = landMap.getProjection()
                                    .fromScreenLocation(new PointF(event.getX(), event.getY()));
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_MOVE:
                                    break;
                                case MotionEvent.ACTION_UP:
                                    if (isClose) {
                                        if (touchIndex == 0 || touchIndex == latLngList.size() - 1) {
                                            latLngList.remove(latLngList.size() - 1);
                                            latLngList.remove(0);
                                            latLngList.add(0, latLng);
                                            latLngList.add(latLng);
                                        } else {
                                            latLngList.remove(touchIndex);
                                            latLngList.add(touchIndex, latLng);
                                        }
                                    } else {
                                        latLngList.remove(touchIndex);
                                        latLngList.add(touchIndex, latLng);
                                    }
                                    drawLand();
                                    isStartMove = false;
                                    touchIndex = -1;
                                    break;
                            }
                        }
                        return false;
                    });
                }
        );

        findViewById(R.id.btn_add).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    /**
     * 判断点击的位置
     *
     * @param point point
     * @return position
     */
    public int judgeClickPosition(LatLng point) {
        PointF pointF = landMap.getProjection().toScreenLocation(point);
        List<PointF> pointFList = new ArrayList<>();
        List<Float> differenceList = new ArrayList<>();
        List<Integer> differenceNumberList = new ArrayList<>();
        for (LatLng latLng : latLngList) {
            pointFList.add(landMap.getProjection().toScreenLocation(latLng));
        }

        for (int i = 0, j = pointFList.size(); i < j; i++) {
            if (Math.abs(pointFList.get(i).x - pointF.x) < 50 && Math.abs(pointFList.get(i).y - pointF.y) < 50) {
                differenceList.add(Math.abs(pointFList.get(i).x - pointF.x) + Math.abs(pointFList.get(i).y - pointF.y));
                differenceNumberList.add(i);
            }
        }

        if (differenceList.size() > 0) {
            float mix = Collections.min(differenceList);
            return differenceNumberList.get(differenceList.indexOf(mix));
        }
        return -1;
    }

    private void addBitmap() {
        if (landMap != null) {
            Bitmap normal = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_12_point_orange);
            landMap.addImage("normal", normal);
            Bitmap error = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_12_point_red);
            landMap.addImage("error", error);
            Bitmap click = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_20_point_orange);
            landMap.addImage("click", click);
        }
    }

    private void hideLog() {
        if (landMap != null) {
            MapUtil.hideLogo(landMap);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        landView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        landView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        landView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        landView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        landView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        landView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        landView.onLowMemory();
    }

    @Override
    public void onClick(View v) {
        if (landMap == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.btn_add:
                addPoint();
                break;
            case R.id.btn_cancel:
                cancelPoint();
                break;
        }
    }

    /**
     * 撤销上一步操作
     */
    private void cancelPoint() {

    }

    /**
     * 新增点
     */
    private void addPoint() {
        if (isClose) {
            return;
        }
        LatLng latLng = new LatLng();
        latLng.setLatitude(cameraPosition.target.getLatitude());
        latLng.setLongitude(cameraPosition.target.getLongitude());
        if (latLngList.size() >= 3) {
            PointF pointF = landMap.getProjection().toScreenLocation(latLng);
            List<Feature> features = landMap.queryRenderedFeatures(pointF, POINT_LAYER);
            if (!features.isEmpty()) {
                int position = Integer.parseInt(features.get(0).getStringProperty("position"));
                if (position == 0) {
                    latLng = latLngList.get(0);
                    isClose = true;
                }
            }
        }
        latLngList.add(latLng);
        drawLand();
    }

    /**
     * 绘制地块
     */
    private void drawLand() {
        isIntersect = LineUtil.isLineIntersect(latLngList, isClose);
        removeLayer();
        drawPolygon();
        drawLine();
        drawText();
        drawPoint();
    }


    private void drawText() {
        if (latLngList.size() <= 1) {
            return;
        }
        List<LatLng> centerLatLng = new ArrayList<>();
        for (int i = 0, j = latLngList.size() - 1; i < j; i++) {
            LatLng latLng = new LatLng();
            latLng.setLatitude((latLngList.get(i).getLatitude() + latLngList.get(i + 1).getLatitude()) / 2);
            latLng.setLongitude((latLngList.get(i).getLongitude() + latLngList.get(i + 1).getLongitude()) / 2);
            centerLatLng.add(latLng);
        }
        List<Feature> featureList = new ArrayList<>();
        List<Double> distanceList = DistanceUtil.getDistances(latLngList);
        for (int i = 0, j = centerLatLng.size(); i < j; i++) {
            JsonObject object = new JsonObject();
            object.addProperty("distance", DoubleUtil.pointTwo(distanceList.get(i)));
            featureList.add(Feature.fromGeometry(Point.fromCoordinates(Position.fromCoordinates(
                    centerLatLng.get(i).getLongitude(), centerLatLng.get(i).getLatitude())), object));
        }
        GeoJsonSource textSource = new GeoJsonSource(TEXT_SOURCE, FeatureCollection.fromFeatures(featureList));
        landMap.addSource(textSource);
        SymbolLayer textLayer = new SymbolLayer(TEXT_LAYER, TEXT_SOURCE);
        textLayer.setProperties(
                PropertyFactory.textField("{distance}" + "m"),
                PropertyFactory.textSize(12f),
                PropertyFactory.textColor(Color.parseColor("#ffffff"))
        );
        landMap.addLayer(textLayer);
    }

    /**
     * 移除所有的资源
     */
    private void removeLayer() {
        landMap.removeLayer(POINT_LAYER);
        landMap.removeSource(POINT_SOURCE);
        landMap.removeLayer(LINE_LAYER);
        landMap.removeSource(LINE_SOURCE);
        landMap.removeLayer(TEXT_LAYER);
        landMap.removeSource(TEXT_SOURCE);

        if (polygon != null) {
            landMap.removePolygon(polygon);
        }
    }

    /**
     * 画点
     */
    private void drawPoint() {
        List<Feature> features = new ArrayList<>();
        for (int i = 0, j = latLngList.size(); i < j; i++) {
            JsonObject object = new JsonObject();
            object.addProperty("position", String.valueOf(i));
            features.add(Feature.fromGeometry(Point.fromCoordinates(
                    Position.fromCoordinates(latLngList.get(i).getLongitude(),
                            latLngList.get(i).getLatitude())
            ), object));
        }
        GeoJsonSource pointSource = new GeoJsonSource(POINT_SOURCE, FeatureCollection.fromFeatures(features));
        landMap.addSource(pointSource);
        SymbolLayer pointLayer = new SymbolLayer(POINT_LAYER, POINT_SOURCE);
        if (isIntersect) {
            pointLayer.setProperties(
                    PropertyFactory.iconImage("error")
            );
        } else {
            pointLayer.setProperties(
                    PropertyFactory.iconImage("normal")
            );
        }
        landMap.addLayer(pointLayer);
    }

    /**
     * 画线
     */
    private void drawLine() {
        List<Position> positionList = new ArrayList<>();
        for (int i = 0, j = latLngList.size(); i < j; i++) {
            positionList.add(Position.fromCoordinates(latLngList.get(i).getLongitude(), latLngList.get(i).getLatitude()));
        }
        LineString lineString = LineString.fromCoordinates(positionList);
        GeoJsonSource lineSource = new GeoJsonSource(LINE_SOURCE,
                FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(lineString)}));
        landMap.addSource(lineSource);
        LineLayer lineLayer = new LineLayer(LINE_LAYER, LINE_SOURCE);
        if (!isClose) {
            if (isIntersect) { // 不闭合，相交
                lineLayer.setProperties(
                        PropertyFactory.lineDasharray(new Float[]{1f, 2f}),
                        PropertyFactory.lineCap(LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(LINE_JOIN_ROUND),
                        PropertyFactory.lineWidth(2f),
                        PropertyFactory.lineColor(Color.parseColor("#f44336"))
                );
            } else { // 不闭合，不相交
                lineLayer.setProperties(
                        PropertyFactory.lineDasharray(new Float[]{1f, 2f}),
                        PropertyFactory.lineCap(LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(LINE_JOIN_ROUND),
                        PropertyFactory.lineWidth(2f),
                        PropertyFactory.lineColor(Color.parseColor("#ff9900"))
                );
            }
        } else {
            if (isIntersect) { // 闭合相交
                lineLayer.setProperties(
                        PropertyFactory.lineCap(LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(LINE_JOIN_ROUND),
                        PropertyFactory.lineWidth(2f),
                        PropertyFactory.lineColor(Color.parseColor("#f44336"))
                );
            } else { // 闭合不相交
                lineLayer.setProperties(
                        PropertyFactory.lineCap(LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(LINE_JOIN_ROUND),
                        PropertyFactory.lineWidth(2f),
                        PropertyFactory.lineColor(Color.parseColor("#ff9900"))
                );
            }
        }
        landMap.addLayer(lineLayer);
    }

    /**
     * 画面
     */
    private void drawPolygon() {
        if (!isClose) {
            return;
        }
        polygon = landMap.addPolygon(new PolygonOptions().addAll(latLngList)
                .fillColor(Color.parseColor("#212121")).alpha(0.3f));
    }
}
