package com.huangruihao.canteen;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.huangruihao.canteen.strategy.CanteenSelector;
import com.huangruihao.canteen.strategy.DinnerRandomStrategy;
import com.huangruihao.canteen.strategy.DormitoryNearbyRandomStrategy;
import com.huangruihao.canteen.strategy.PreferedStrategy;
import com.huangruihao.canteen.strategy.RandomStrategy;
import com.huangruihao.canteen.update.UpdateManager;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final double MEAN_GRAVITY_CONSTANT = 9.81;
    private static final double SHAKE_THRESHOLD_G = 2.5;
    private static final int REQUEST_WRITE_STORAGE = 1;

    CanteenSelector mCanteenSelector;
    TextView mTextCanteen;
    Sensor mAccelerometer;
    SensorManager mSensorManager;
    long mSensorLastUpdated = 0;
    double mSensorEventLastX, mSensorEventLastY, mSensorEventLastZ;

    UpdateManager updateManager;

    private void chooseCanteen() {
        mTextCanteen.setText(mCanteenSelector.getCanteen());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int permission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("MainActivity", "Permission to record denied");

            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("自动更新需要写SD卡权限")
                        .setTitle("Permission required");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("MainActivity", "Clicked");
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_WRITE_STORAGE);
            }
        }

        updateManager = new UpdateManager();

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("MainActivity", "Permission has been denied by user");
                } else {
                    Log.i("MainActivity", "Permission has been granted by user");
                }
            }
        }
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
        else if (id == R.id.action_update) {
            updateManager.doUpdate(this);
        }
        else if (id == R.id.action_check_update) {
            updateManager.checkUpdate(new UpdateManager.CheckUpdateCallback() {
                @Override
                public void onCheckUpdateDone(boolean hasNewVersion, String latestVersion, String releaseDate) {
                    String titleStr = hasNewVersion ?
                        "发现新版本" :
                        "您已经是最新版";

                    showToastInOtherThreads(String.format("%s: '%s', '%s'", titleStr, latestVersion, releaseDate));
                }

                @Override
                public void onCheckUpdateFailed(String reason) {
                    showToastInOtherThreads(String.format("升级失败: 原因: %s", reason));
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    static final int MSG_TOAST = 1;

    /**
     * Handler for UI thread.
     * We use WeakReference to prevent from leaking issue.
     */
    static class MyHandler extends Handler {
        WeakReference<MainActivity> mMainActivity;
        public MyHandler(MainActivity mainActivity) {
            mMainActivity = new WeakReference<>(mainActivity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mMainActivity.get();
            if (mainActivity == null)
                return;
            switch (msg.what) {
                case MSG_TOAST:
                    Toast.makeText(mainActivity, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
    MyHandler mHandler = new MyHandler(this);

    /**
     * Send a TOAST message to the mHandler, and pop up a toast in the main thread(UI thread)
     * @param content the string to show on the toast
     */
    private void showToastInOtherThreads(String content) {
        Message message = new Message();
        message.what = MSG_TOAST;
        message.obj = content;
        mHandler.sendMessage(message);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 200ms.
            if ((curTime - mSensorLastUpdated) > 200) {

                double x, y, z;
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];

                double gForce = Math.sqrt(
                        (x - mSensorEventLastX) * (x- mSensorEventLastX) +
                        (y - mSensorEventLastY) * (y- mSensorEventLastY) +
                        (z - mSensorEventLastZ) * (z- mSensorEventLastZ)
                );

                if (gForce > MEAN_GRAVITY_CONSTANT * SHAKE_THRESHOLD_G) {
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
