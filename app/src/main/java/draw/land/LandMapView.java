package draw.land;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.mapbox.mapboxsdk.maps.MapView;

public class LandMapView extends MapView {
    public LandMapView(@NonNull Context context) {
        super(context);
    }

    public LandMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LandMapView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
