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
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    RouletteRandom mRandom;
    TextView mTextCanteen;
    Sensor mAccelerometer;
    SensorManager mSensorManager;
    long mSensorLastUpdated = 0;
    double mSensorEventLastX, mSensorEventLastY, mSensorEventLastZ;
    double SHAKE_THRESHOLD = 800;

    private void nextCanteen() {
        String canteenName = (String) mRandom.choose();
        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 5) // Thursday
            canteenName = "桃李地下1层";
        mTextCanteen.setText(canteenName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Object[] canteensWithProbability = {
                "桃李1层",     10,
                "桃李2层",     1,
                "桃李3层",     1,
                "紫荆1层",     10,
                "紫荆2层",     1,
                "紫荆3层",     1,
                "紫荆4层",     10,
                "桃李地下1层", 1,
                "紫荆地下1层", 1,
                "芝兰1层",     1,
                "芝兰2层",     1,
                "玉树1层",     1,
                "玉树2层",     1,
                "观畴",        1,
                "清芬2层",     1,
                "清芬3层",     1,
                "轻轻快餐",    1,
                "听涛",        0,
                "澜园",        1,
                "荷园",        1
        };
        mRandom = RouletteRandom.fromAssociatedArray(canteensWithProbability);

        mTextCanteen = (TextView) findViewById(R.id.canteen);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        // prevent fab from raising NullPointerException
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nextCanteen();
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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
                long diffTime = (curTime - mSensorLastUpdated);
                mSensorLastUpdated = curTime;

                double x, y, z;
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];

                double gForce = Math.sqrt(
                        (x - mSensorEventLastX) * (x- mSensorEventLastX) +
                        (y - mSensorEventLastY) * (y- mSensorEventLastY) +
                        (z - mSensorEventLastZ) * (z- mSensorEventLastZ)
                );

                if (gForce > 9.8 * 2.5) {
                    //Log.d("sensor", "shake detected w/ speed: " + accRate);
                    Toast.makeText(this, "shake detected acc: " + gForce, Toast.LENGTH_SHORT).show();
                    nextCanteen();
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
