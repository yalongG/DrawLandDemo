package draw.land.util;

import android.graphics.Point;
import android.graphics.PointF;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

import draw.land.entity.Line;


/**
 * Created by guoyalong on 2017/10/17.
 * 线的工具类。给一系列点，判断线是否闭合
 */

public class LineUtil {
    private static List<Line> lines = new ArrayList<>();

    /**
     * 先将点转换成线，然后判断先是否自相交
     *
     * @param mPolygonList 点的集合
     * @return true自相交，false没有自相交
     */
    public static boolean isLineIntersect(List<LatLng> mPolygonList, boolean isClose) {
        lines.clear();
        if (mPolygonList.size() > 3) {
            for (int i = 0; i < mPolygonList.size() - 1; i++) {
                Line line = new Line();
                line.setStartX(mPolygonList.get(i).getLongitude());
                line.setStartY(mPolygonList.get(i).getLatitude());
                line.setEndX(mPolygonList.get(i + 1).getLongitude());
                line.setEndY(mPolygonList.get(i + 1).getLatitude());
                lines.add(line);
            }
            return isLinesIntersect(lines, isClose);
        }
        return false;

    }

    /**
     * 先将点转换成线，然后判断先是否自相交
     *
     * @param mPoints 点的集合
     * @return true自相交，false没有自相交
     */
    public static boolean isLineIntersectPoints(List<Point> mPoints, boolean isClose) {
        lines.clear();
        if (mPoints.size() > 3) {
            for (int i = 0; i < mPoints.size() - 1; i++) {
                Line line = new Line();
                line.setStartX(mPoints.get(i).x);
                line.setStartY(mPoints.get(i).y);
                line.setEndX(mPoints.get(i + 1).x);
                line.setEndY(mPoints.get(i + 1).y);
                lines.add(line);
            }
            return isLinesIntersect(lines, isClose);
        }
        return false;

    }

    /**
     * 先将点转换成线，然后判断先是否自相交
     *
     * @param mPoints 点的集合
     * @return true自相交，false没有自相交
     */
    public static boolean isLineIntersectPointFs(List<PointF> mPoints, boolean isClose) {
        lines.clear();
        if (mPoints != null) {
            if (mPoints.size() > 3) {
                for (int i = 0; i < mPoints.size() - 1; i++) {
                    Line line = new Line();
                    line.setStartX(mPoints.get(i).x);
                    line.setStartY(mPoints.get(i).y);
                    line.setEndX(mPoints.get(i + 1).x);
                    line.setEndY(mPoints.get(i + 1).y);
                    lines.add(line);
                }
                return isLinesIntersect(lines, isClose);
            }
        }
        return false;

    }

    /**
     * 判断边型是否自相交
     *
     * @param lines   线的集合
     * @param isClose 是否是闭合图形
     * @return true相交，false没有相交
     */
    private static boolean isLinesIntersect(List<Line> lines, boolean isClose) {
        if (isClose) {
            for (int i = 0; i < lines.size() - 1; i++) {
                if (i == 0) {
                    for (int j = 2; j < lines.size() - 1; j++) {
                        if (intersectsLine(lines.get(i), lines.get(j))) {
                            return true;
                        }
                    }
                } else {
                    for (int j = i + 2; j < lines.size(); j++) {
                        if (intersectsLine(lines.get(i), lines.get(j))) {
                            return true;
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < lines.size() - 1; i++) {
                if (i == 0) {
                    for (int j = 2; j < lines.size(); j++) {
                        if (intersectsLine(lines.get(i), lines.get(j))) {
                            return true;
                        }
                    }
                } else {
                    for (int j = i + 2; j < lines.size(); j++) {
                        if (intersectsLine(lines.get(i), lines.get(j))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断2条线段是否相交
     *
     * @param line1 线段1
     * @param line2 线段2
     * @return 是否相交 相交返回true 不相交返回false
     */
    private static boolean intersectsLine(Line line1, Line line2) {
        return linesIntersect(line1.getStartX(), line1.getStartY(), line1.getEndX(), line1.getEndY(),
                line2.getStartX(), line2.getStartY(), line2.getEndX(), line2.getEndY());
    }


    private static boolean linesIntersect(double x1, double y1,
                                          double x2, double y2,
                                          double x3, double y3,
                                          double x4, double y4) {
        return ((relativeCCW(x1, y1, x2, y2, x3, y3) *
                relativeCCW(x1, y1, x2, y2, x4, y4) <= 0)
                && (relativeCCW(x3, y3, x4, y4, x1, y1) *
                relativeCCW(x3, y3, x4, y4, x2, y2) <= 0));
    }

    private static int relativeCCW(double x1, double y1,
                                   double x2, double y2,
                                   double px, double py) {
        x2 -= x1;
        y2 -= y1;
        px -= x1;
        py -= y1;
        double ccw = px * y2 - py * x2;
        if (ccw == 0.0) {
            ccw = px * x2 + py * y2;
            if (ccw > 0.0) {
                px -= x2;
                py -= y2;
                ccw = px * x2 + py * y2;
                if (ccw < 0.0) {
                    ccw = 0.0;
                }
            }
        }
        return (ccw < 0.0) ? -1 : ((ccw > 0.0) ? 1 : 0);
    }

}
