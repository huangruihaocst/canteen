package com.huangruihao.canteen;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.huangruihao.canteen.strategy.CanteenSelector;
import com.huangruihao.canteen.strategy.DinnerRandomStrategy;
import com.huangruihao.canteen.strategy.DormitoryNearbyRandomStrategy;
import com.huangruihao.canteen.strategy.PreferedStrategy;
import com.huangruihao.canteen.strategy.RandomStrategy;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final double SHAKE_THRESHOLD_G = 2.5;

    CanteenSelector mCanteenSelector;
    TextView mTextCanteen;
    Sensor mAccelerometer;
    SensorManager mSensorManager;
    long mSensorLastUpdated = 0;
    double mSensorEventLastX, mSensorEventLastY, mSensorEventLastZ;

    private void chooseCanteen() {
        mTextCanteen.setText(mCanteenSelector.getCanteen());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCanteenSelector = new PreferedStrategy();

        mTextCanteen = (TextView) findViewById(R.id.canteen);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        // prevent fab from throwing NullPointerException
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chooseCanteen();
                    Snackbar.make(view, "选择成功", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }

        // initialize shaker sensor
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select_strategy, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_default) {
            mCanteenSelector = new PreferedStrategy();
            mTextCanteen.setText(String.format("%s\n%s",
                    getString(R.string.what_do_we_eat), getString(R.string.action_default)));
            return true;
        }
        else if (id == R.id.action_dormitory_nearby) {
            mCanteenSelector = new DormitoryNearbyRandomStrategy();
            mTextCanteen.setText(String.format("%s\n%s",
                    getString(R.string.what_do_we_eat), getString(R.string.action_dormitory_nearby)));
            return true;
        }
        else if (id == R.id.action_dinner) {
            mCanteenSelector = new DinnerRandomStrategy();
            mTextCanteen.setText(String.format("%s\n%s",
                    getString(R.string.what_do_we_eat), getString(R.string.action_dinner)));
            return true;
        }
        else if (id == R.id.action_completely_random) {
            mCanteenSelector = new RandomStrategy();
            mTextCanteen.setText(String.format("%s\n%s",
                    getString(R.string.what_do_we_eat), getString(R.string.action_completely_random)));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - mSensorLastUpdated) > 200) {
                // ignore last triggering time
                long diffTime = (curTime - mSensorLastUpdated);

                double x, y, z;
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];

                double gForce = Math.sqrt(
                        (x - mSensorEventLastX) * (x- mSensorEventLastX) +
                        (y - mSensorEventLastY) * (y- mSensorEventLastY) +
                        (z - mSensorEventLastZ) * (z- mSensorEventLastZ)
                );

                if (gForce > 9.8 * SHAKE_THRESHOLD_G) {
                    mSensorLastUpdated = curTime;
                    chooseCanteen();
                }

                mSensorEventLastX = x;
                mSensorEventLastY = y;
                mSensorEventLastZ = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
