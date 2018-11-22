package draw.land.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.gson.JsonObject;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.functions.Function;
import com.mapbox.mapboxsdk.style.functions.stops.CategoricalStops;
import com.mapbox.mapboxsdk.style.functions.stops.Stop;
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

import draw.land.R;
import draw.land.util.DistanceUtil;
import draw.land.util.DoubleUtil;
import draw.land.util.LineUtil;
import draw.land.util.MapUtil;

import static android.content.Context.VIBRATOR_SERVICE;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;

public class LandMapView extends MapView {
    private Context context;
    private LandView landView;
    private MapboxMap landMap;
    private CameraPosition cameraPosition;
    private List<LatLng> latLngList = new ArrayList<>();
    private boolean isClose = false; // 是否闭合
    private boolean isIntersect = false; // 是否相交
    private boolean isStartMove = false;// 是否开始移动
    private MoveLandListener moveLandListener;

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

    private List<Boolean> operState = new ArrayList<>(); // 操作的状态
    private List<LatLng> operLocation = new ArrayList<>(); // 操作的位置
    private List<Integer> clickLocation = new ArrayList<>(); // 点击的那个点
    private List<String> operAction = new ArrayList<>(); // 操作的行为  ADD MOVE DELETE

    private List<PointF> pointFList = new ArrayList<>();

    public LandMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        landView = new LandView(context, attrs);
        this.addView(landView);
        if (landView.getVisibility() == View.VISIBLE) {
            landView.setVisibility(GONE);
        }

        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        this.getMapAsync(mapBoxMap -> {
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
                            Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
                            vibrator.vibrate(200);
                            if (moveLandListener != null) {
                                moveLandListener.startMove();
                            }
                            isStartMove = true;
                            removeLayer();
                            landView.setData(isClose, pointFList, touchIndex);
                        }
                    });

                    landMap.addOnMapClickListener(point -> {
                        if (latLngList.size() <= 0) {
                            return;
                        }

                        clickIndex = judgeClickPosition(point);
                        if (clickIndex != -1) {
                            removePoint();
                            drawPoint();
                            deletePoint();
                        }
                    });

                    this.setOnTouchListener((v, event) -> {
                        if (isStartMove) {
                            PointF pointF = new PointF(event.getX(), event.getY());
                            LatLng latLng = landMap.getProjection().fromScreenLocation(pointF);
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_MOVE:
                                    landView.setEvent(event);
                                    if (moveLandListener != null) {
                                        CameraPosition position = new CameraPosition.Builder()
                                                .tilt(cameraPosition.tilt).zoom(cameraPosition.zoom)
                                                .target(latLng).bearing(cameraPosition.bearing).build();
                                        moveLandListener.showMirrorLand(position, pointF,
                                                touchIndex, isClose, pointFList);
                                    }
                                    break;
                                case MotionEvent.ACTION_UP:
                                    if (moveLandListener != null) {
                                        moveLandListener.dismissMirrorLand();
                                    }
                                    landView.setEvent(event);
                                    operState.add(isClose);
                                    operAction.add("MOVE");
                                    if (isClose) {
                                        if (touchIndex == 0 || touchIndex == latLngList.size() - 1) {
                                            operLocation.add(latLngList.get(0));
                                            latLngList.remove(latLngList.size() - 1);
                                            latLngList.remove(0);
                                            latLngList.add(0, latLng);
                                            latLngList.add(latLng);
                                            clickLocation.add(0);
                                        } else {
                                            operLocation.add(latLngList.get(touchIndex));
                                            latLngList.remove(touchIndex);
                                            latLngList.add(touchIndex, latLng);
                                            clickLocation.add(touchIndex);
                                        }
                                    } else {
                                        operLocation.add(latLngList.get(touchIndex));
                                        latLngList.remove(touchIndex);
                                        latLngList.add(touchIndex, latLng);
                                        clickLocation.add(touchIndex);
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
    }

    /**
     * 删除点
     */
    private void deletePoint() {
        new AlertDialog.Builder(context).setMessage("是否要删除这个点")
                .setPositiveButton("确定", (dialog, which) -> {
                    operState.add(isClose);
                    operAction.add("DELETE");
                    if (isClose) {
                        if (latLngList.size() == 4) {
                            isClose = false;
                            if (clickIndex == 0 || clickIndex == latLngList.size() - 1) {
                                operLocation.add(latLngList.get(0));
                                clickLocation.add(0);
                                latLngList.remove(latLngList.size() - 1);
                                latLngList.remove(0);
                            } else {
                                operLocation.add(latLngList.get(clickIndex));
                                clickLocation.add(clickIndex);
                                latLngList.remove(latLngList.size() - 1);
                                latLngList.remove(clickIndex);
                            }
                        } else {
                            if (clickIndex == 0 || clickIndex == latLngList.size() - 1) {
                                operLocation.add(latLngList.get(0));
                                clickLocation.add(0);
                                latLngList.remove(latLngList.size() - 1);
                                latLngList.remove(0);
                                latLngList.add(latLngList.get(0));
                            } else {
                                operLocation.add(latLngList.get(clickIndex));
                                clickLocation.add(clickIndex);
                                latLngList.remove(clickIndex);
                            }
                        }
                    } else {
                        operLocation.add(latLngList.get(clickIndex));
                        clickLocation.add(clickIndex);
                        latLngList.remove(clickIndex);
                    }
                    clickIndex = -1;
                    drawLand();
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    clickIndex = -1;
                    removePoint();
                    drawPoint();
                }).show();
    }

    /**
     * 判断点击的位置
     *
     * @param point point
     * @return position
     */
    public int judgeClickPosition(LatLng point) {
        pointFList.clear();
        PointF pointF = landMap.getProjection().toScreenLocation(point);
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

    /**
     * 撤销上一步操作
     */
    public void cancelPoint() {
        if (landMap == null) {
            return;
        }
        if (operState.size() > 0) {
            String action = operAction.get(operAction.size() - 1);
            isClose = operState.get(operState.size() - 1);
            int index = clickLocation.get(clickLocation.size() - 1);
            LatLng latLng = operLocation.get(operLocation.size() - 1);
            switch (action) {
                case "ADD":
                    latLngList.remove(latLngList.size() - 1);
                    break;
                case "MOVE":
                    if (isClose) {
                        if (index == 0) {
                            latLngList.remove(latLngList.size() - 1);
                            latLngList.remove(0);
                            latLngList.add(0, latLng);
                            latLngList.add(latLng);
                        } else {
                            latLngList.remove(index);
                            latLngList.add(index, latLng);
                        }
                    } else {
                        latLngList.remove(index);
                        latLngList.add(index, latLng);
                    }
                    break;
                case "DELETE":
                    if (isClose) {
                        if (latLngList.size() == 2) {
                            if (index == 0) {
                                latLngList.add(0, latLng);
                                latLngList.add(latLng);
                            } else {
                                latLngList.add(index, latLng);
                                latLngList.add(latLngList.get(0));
                            }
                        } else {
                            if (index == 0) {
                                latLngList.remove(latLngList.size() - 1);
                                latLngList.add(0, latLng);
                                latLngList.add(latLng);
                            } else {
                                latLngList.add(index, latLng);
                            }
                        }
                    } else {
                        latLngList.add(index, latLng);
                    }
                    break;
            }
            drawLand();
            operState.remove(operState.size() - 1);
            operLocation.remove(operLocation.size() - 1);
            clickLocation.remove(clickLocation.size() - 1);
            operAction.remove(operAction.size() - 1);
        }
    }

    /**
     * 新增点
     */
    public void addPoint() {
        if (landMap == null) {
            return;
        }
        if (isClose) {
            return;
        }
        LatLng latLng = new LatLng();
        latLng.setLatitude(cameraPosition.target.getLatitude());
        latLng.setLongitude(cameraPosition.target.getLongitude());
        PointF pointF = landMap.getProjection().toScreenLocation(latLng);
        List<Feature> features = landMap.queryRenderedFeatures(pointF, POINT_LAYER);
        if (latLngList.size() < 3) {
            if (!features.isEmpty()) {
                if (moveLandListener != null) {
                    moveLandListener.showMessage(isClose, isIntersect, "不可重复打点，请移动地图");
                    return;
                }
            }
            operState.add(isClose);
        } else {
            if (!features.isEmpty()) {
                int position = Integer.parseInt(features.get(0).getStringProperty("position"));
                if (position == 0) {
                    operState.add(isClose);
                    latLng = latLngList.get(0);
                    isClose = true;
                } else {
                    if (moveLandListener != null) {
                        moveLandListener.showMessage(isClose, isIntersect, "不可重复打点，请移动地图");
                        return;
                    }
                }
            } else {
                operState.add(isClose);
            }
        }
        latLngList.add(latLng);
        operLocation.add(latLng);
        clickLocation.add(latLngList.size() - 1);
        operAction.add("ADD");
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

        showMessage(isIntersect);
    }

    private void showMessage(boolean mIsIntersect) {
        String message;
        if (latLngList.size() <= 0) {
            message = "移动地图进行打点操作";
        } else if (latLngList.size() < 3) {
            message = "移动地图进行下一个拐点";
        } else {
            if (isClose) {
                if (mIsIntersect) {
                    message = "请调整图形避免相交";
                } else {
                    message = "拖动点位可修改地块形状";
                }
            } else {
                if (mIsIntersect) {
                    message = "请调整图形避免相交";
                } else {
                    message = "继续打点或移至起始点闭合路径";
                }
            }
        }
        if (moveLandListener != null) {
            moveLandListener.showMessage(isClose, mIsIntersect, message);
        }
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
        removePoint();
        landMap.removeLayer(LINE_LAYER);
        landMap.removeSource(LINE_SOURCE);
        landMap.removeLayer(TEXT_LAYER);
        landMap.removeSource(TEXT_SOURCE);

        if (polygon != null) {
            landMap.removePolygon(polygon);
        }
    }

    private void removePoint() {
        landMap.removeLayer(POINT_LAYER);
        landMap.removeSource(POINT_SOURCE);
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
            if (clickIndex != -1) {
                pointLayer.setProperties(
                        PropertyFactory.iconImage(Function.property("position",
                                CategoricalStops.categorical(
                                        Stop.stop(String.valueOf(clickIndex),
                                                PropertyFactory.iconImage("click"))
                                )).withDefaultValue(PropertyFactory.iconImage("error")))
                );
            } else {
                pointLayer.setProperties(
                        PropertyFactory.iconImage("error")
                );
            }
        } else {
            if (clickIndex != -1) {
                pointLayer.setProperties(
                        PropertyFactory.iconImage(Function.property("position",
                                CategoricalStops.categorical(
                                        Stop.stop(String.valueOf(clickIndex),
                                                PropertyFactory.iconImage("click"))
                                )).withDefaultValue(PropertyFactory.iconImage("normal")))
                );
            } else {
                pointLayer.setProperties(
                        PropertyFactory.iconImage("normal")
                );
            }
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


    public List<LatLng> getLatLngList() {
        return latLngList;
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


    public interface MoveLandListener {
        void startMove();

        void showMirrorLand(CameraPosition cameraPosition, PointF pointF1, int clickPosition,
                            boolean isClose, List<PointF> pointFList);

        void dismissMirrorLand();

        void showMessage(boolean isClose, boolean isIntersect, String message);
    }

    public void setOnMoveLandListener(MoveLandListener moveLandListener) {
        this.moveLandListener = moveLandListener;
    }

}
