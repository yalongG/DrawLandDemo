package draw.land;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

/**
 * @author guoyalong
 * landView
 */
public class LandView extends MirrorLandView {
    /**
     * 点击点的下标
     */
    private int position;
    private boolean isClose;
    private List<PointF> pointList;

    public LandView(Context context) {
        this(context, null);
    }

    public LandView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LandView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setData(boolean isClose, List<PointF> pointList, int position) {
        super.setData(isClose, pointList);
        this.isClose = isClose;
        this.pointList = pointList;
        this.position = position;
        setVisibility(VISIBLE);
        invalidate();
    }

    public void setEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                int endX = (int) event.getX();
                int endY = (int) event.getY();
                if (isClose) {
                    if (position == 0 || position == pointList.size() - 1) {
                        pointList.get(0).set(endX, endY);
                        pointList.get(pointList.size() - 1).set(endX, endY);
                    } else {
                        pointList.get(position).set(endX, endY);
                    }
                } else {
                    pointList.get(position).set(endX, endY);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
