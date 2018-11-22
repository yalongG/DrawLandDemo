package draw.land.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

import draw.land.util.DensityUtil;
import draw.land.util.LineUtil;

/**
 * @author guoyalong
 * 镜子
 */
public class MirrorLandView extends View {
    private Context context;
    /**
     * 是否闭合
     */
    private boolean isClose;
    /**
     * 是否相交
     */
    private boolean isIntersect;
    /**
     * 点位置集合
     */
    private List<PointF> pointList;

    /**
     * 正常拐点画笔
     */
    private Paint normalPointPaint;
    /**
     * 相交拐点画笔
     */
    private Paint errorPointPaint;
    /**
     * 外圈白色圆的画笔
     */
    private Paint outsidePointPaint;
    /**
     * 正常线画笔
     */
    private Paint normalLinePaint;
    /**
     * 相交线画笔
     */
    private Paint errorLinePaint;
    /**
     * 不闭合正常线画笔
     */
    private Paint noCloseNormalLinePaint;
    /**
     * 不闭合相交线画笔
     */
    private Paint noCloseErrorLinePaint;
    /**
     * 填充色画笔
     */
    private Paint fillPaint;

    public MirrorLandView(Context context) {
        this(context, null);
    }

    public MirrorLandView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MirrorLandView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        DashPathEffect dashPathEffect =
                new DashPathEffect(new float[]{8f, 8f}, 0);
        /* 点画笔 */
        outsidePointPaint = new Paint();
        outsidePointPaint.setColor(Color.parseColor("#ffffff"));
        outsidePointPaint.setStrokeCap(Paint.Cap.ROUND);
        outsidePointPaint.setStyle(Paint.Style.FILL);
        outsidePointPaint.setAntiAlias(true);

        normalPointPaint = new Paint();
        normalPointPaint.setColor(Color.parseColor("#ff9900"));
        normalPointPaint.setStrokeCap(Paint.Cap.ROUND);
        normalPointPaint.setStyle(Paint.Style.FILL);
        normalPointPaint.setAntiAlias(true);

        errorPointPaint = new Paint();
        errorPointPaint.setColor(Color.parseColor("#f44336"));
        errorPointPaint.setStrokeCap(Paint.Cap.ROUND);
        errorPointPaint.setStyle(Paint.Style.FILL);
        errorPointPaint.setAntiAlias(true);

        /* 线的部分 */
        normalLinePaint = new Paint();
        normalLinePaint.setColor(Color.parseColor("#ff9900"));
        normalLinePaint.setStrokeCap(Paint.Cap.ROUND);
        normalLinePaint.setStrokeWidth(DensityUtil.dp2px(context, 2));
        normalLinePaint.setStyle(Paint.Style.STROKE);
        normalLinePaint.setAntiAlias(true);

        errorLinePaint = new Paint();
        errorLinePaint.setColor(Color.parseColor("#f44336"));
        errorLinePaint.setStrokeCap(Paint.Cap.ROUND);
        errorLinePaint.setStrokeWidth(DensityUtil.dp2px(context, 2));
        errorLinePaint.setStyle(Paint.Style.STROKE);
        errorLinePaint.setAntiAlias(true);

        noCloseNormalLinePaint = new Paint();
        noCloseNormalLinePaint.setColor(Color.parseColor("#ff9900"));
        noCloseNormalLinePaint.setStrokeCap(Paint.Cap.ROUND);
        noCloseNormalLinePaint.setStrokeWidth(DensityUtil.dp2px(context, 2));
        noCloseNormalLinePaint.setStyle(Paint.Style.STROKE);
        noCloseNormalLinePaint.setAntiAlias(true);
        noCloseNormalLinePaint.setPathEffect(dashPathEffect);

        noCloseErrorLinePaint = new Paint();
        noCloseErrorLinePaint.setColor(Color.parseColor("#f44336"));
        noCloseErrorLinePaint.setStrokeCap(Paint.Cap.ROUND);
        noCloseErrorLinePaint.setStrokeWidth(DensityUtil.dp2px(context, 2));
        noCloseErrorLinePaint.setStyle(Paint.Style.STROKE);
        noCloseErrorLinePaint.setAntiAlias(true);
        noCloseErrorLinePaint.setPathEffect(dashPathEffect);

        /* 面的填充色 */
        fillPaint = new Paint();
        fillPaint.setColor(Color.parseColor("#4c212121"));
        fillPaint.setStrokeCap(Paint.Cap.ROUND);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);
    }

    public void setData(boolean isClose, List<PointF> pointList) {
        this.isClose = isClose;
        this.pointList = pointList;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pointList != null) {
            drawLayer(canvas);
            drawLine(canvas);
            drawPoint(canvas);
        }
    }

    private void drawLayer(Canvas canvas) {
        if (isClose) {
            Path path = new Path();
            path.moveTo(pointList.get(0).x, pointList.get(0).y);
            for (int i = 1; i < pointList.size() - 1; i++) {
                path.lineTo(pointList.get(i).x, pointList.get(i).y);
            }
            path.close();
            canvas.drawPath(path, fillPaint);
        }
    }

    private void drawPoint(Canvas canvas) {
        if (isIntersect) {
            for (int i = 0; i < pointList.size(); i++) {
                canvas.drawCircle(pointList.get(i).x, pointList.get(i).y, DensityUtil.dp2px(context, 6), outsidePointPaint);
                canvas.drawCircle(pointList.get(i).x, pointList.get(i).y, DensityUtil.dp2px(context, 5), errorPointPaint);
            }
        } else {
            if (isClose) {
                for (int i = 0; i < pointList.size(); i++) {
                    canvas.drawCircle(pointList.get(i).x, pointList.get(i).y, DensityUtil.dp2px(context, 6), outsidePointPaint);
                    canvas.drawCircle(pointList.get(i).x, pointList.get(i).y, DensityUtil.dp2px(context, 5), normalPointPaint);
                }
            } else {
                for (int i = 0; i < pointList.size(); i++) {
                    canvas.drawCircle(pointList.get(i).x, pointList.get(i).y, DensityUtil.dp2px(context, 6), outsidePointPaint);
                    canvas.drawCircle(pointList.get(i).x, pointList.get(i).y, DensityUtil.dp2px(context, 5), normalPointPaint);
                }
            }

        }
    }

    private void drawLine(Canvas canvas) {
        isIntersect = LineUtil.isLineIntersectPointFs(pointList, isClose);
        Path path = new Path();
        if (isClose) {
            if (isIntersect) { //闭合,相交
                path.reset();
                path.moveTo(pointList.get(0).x, pointList.get(0).y);
                for (int i = 1; i < pointList.size() - 1; i++) {
                    path.lineTo(pointList.get(i).x, pointList.get(i).y);
                }
                path.lineTo(pointList.get(0).x, pointList.get(0).y);
                canvas.drawPath(path, errorLinePaint);

            } else { //闭合,不相交
                path.reset();
                path.moveTo(pointList.get(0).x, pointList.get(0).y);
                for (int i = 1; i < pointList.size() - 1; i++) {
                    path.lineTo(pointList.get(i).x, pointList.get(i).y);
                }
                path.lineTo(pointList.get(0).x, pointList.get(0).y);
                canvas.drawPath(path, normalLinePaint);
            }
        } else {
            if (isIntersect) { //不闭合,相交
                path.reset();
                path.moveTo(pointList.get(0).x, pointList.get(0).y);
                for (int i = 1; i < pointList.size(); i++) {
                    path.lineTo(pointList.get(i).x, pointList.get(i).y);
                }
                canvas.drawPath(path, noCloseErrorLinePaint);
            } else {//不闭合,不相交
                path.reset();
                path.moveTo(pointList.get(0).x, pointList.get(0).y);
                for (int i = 1; i < pointList.size(); i++) {
                    path.lineTo(pointList.get(i).x, pointList.get(i).y);
                }
                canvas.drawPath(path, noCloseNormalLinePaint);
            }
        }
    }
}
