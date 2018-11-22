package draw.land;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mapbox.mapboxsdk.Mapbox;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private DrawLandView drawLandView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);

        drawLandView = findViewById(R.id.drawLandView);
        drawLandView.onCreate(savedInstanceState);
        findViewById(R.id.btn_add).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        drawLandView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        drawLandView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        drawLandView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawLandView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        drawLandView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        drawLandView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        drawLandView.onLowMemory();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                drawLandView.addPoint();
                break;
            case R.id.btn_cancel:
                drawLandView.cancelPoint();
                break;
        }
    }
}
