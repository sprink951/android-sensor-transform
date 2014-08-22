package io.quadroid.UDPSensor.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import io.quadroid.UDPSensor.R;
import io.quadroid.UDPSensor.helper.IPAddressValidator;
import io.quadroid.UDPSensor.ndk.QuadroidLib;
import io.quadroid.UDPSensor.net.UDPClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
/**
 * UDPSensor
 * User: Matthias Nagel
 * Date: 26.11.13
 * http://www.quadroid.io
 */
public class MainActivity extends Activity implements SensorEventListener {

    private static TextView mAccelerometerX;
    private static TextView mAccelerometerY;
    private static TextView mAccelerometerZ;
    private static TextView mGyroscopeX;
    private static TextView mGyroscopeY;
    private static TextView mGyroscopeZ;
    private static TextView mMagnetometerX;
    private static TextView mMagnetometerY;
    private static TextView mMagnetometerZ;
    private static TextView mBarometer;
    private static TextView mTimestamp;
    private static TextView mSentPacketsTextView;
    private static MainActivity mMainActivity;
    private static int  move = 0;
    private static int movtion = 1;
    private static float mA0 = 0.0f;
    private static float mA1 = 0.0f;
    private static float mA2 = 0.0f;
    private static float mG0 = 0.0f;
    private static float mG1 = 0.0f;
    private static float mG2 = 0.0f;
    private static float mM0 = 0.0f;
    private static float mM1 = 0.0f;
    private static float mM2 = 0.0f;
    private static float mO0 = 0.0f;
    private static float mO1 = 0.0f;
    private static float mO2 = 0.0f;
    private static float mB = 0.0f;
    private static long sentPackets = 0;
    private static boolean sendActive = false;
    private EditText mIPAddressText;
    private EditText mPortText;
    private Thread ndk;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mOrientation;
    private Sensor mMAGNETIC;
    //private static UDPClient udphelper;
    UDPClient udphelper;
    float mAccelCurrent = SensorManager.GRAVITY_EARTH;;
    float mAccelLast = SensorManager.GRAVITY_EARTH;;
    private float mAccel;
    private long last_step_ts = 0;
    String url;
    Integer port;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event)
    {


        long now_ms = System.currentTimeMillis();
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:

                // just update the oldest z value
                mA0 = mA0 + 0.4f*(event.values[0]-mA0);
                mA1 = mA1 + 0.4f*(event.values[1]-mA1);
                mA2 = mA2 + 0.4f*(event.values[2]-mA2);
                //mA0 = event.values[0];
                //mA1 = event.values[1];
               // mA2 = event.values[2];
                mAccelLast = mAccelCurrent;
                mAccelCurrent = FloatMath.sqrt(mA0 * mA0 + mA1 * mA1 + mA2 * mA2);
                float delta = mAccelCurrent - mAccelLast;
                mAccel = mAccel * 0.5f + delta;
                mB = mAccel;
                if (Math.abs(mAccel )> 0.3)
                {
                    move = 1;
                }
                else
                {
                    move = 0;
                }
                break;
           /* case Sensor.TYPE_GYROSCOPE:
                if (timestamp != 0) {
                    final float dT = (event.timestamp - timestamp) * NS2S;
                    // 未规格化的旋转向量坐标值，。
                    float axisX = event.values[0];
                    float axisY = event.values[1];
                    float axisZ = event.values[2];

                    // 计算角速度
                    float omegaMagnitude = FloatMath.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

                    // 如果旋转向量偏移值足够大，可以获得坐标值，则规格化旋转向量
                    // (也就是说，EPSILON 为计算偏移量的起步值。小于该值的偏移视为误差，不予计算。)
                    if (omegaMagnitude > 0.05) {
                        axisX /= omegaMagnitude;
                        axisY /= omegaMagnitude;
                        axisZ /= omegaMagnitude;
                    }
                    float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                    float sinThetaOverTwo = FloatMath.sin(thetaOverTwo);
                    float cosThetaOverTwo = FloatMath.cos(thetaOverTwo);
                    deltaRotationVector[0] = sinThetaOverTwo * axisX;
                    deltaRotationVector[1] = sinThetaOverTwo * axisY;
                    deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                    deltaRotationVector[3] = cosThetaOverTwo;
                }
                timestamp = event.timestamp;
                float[] deltaRotationMatrix = new float[9];
                SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
               //.rotationCurrent = rotationCurrent * deltaRotationMatrix;
                break;*/
            case Sensor.TYPE_ORIENTATION:
                mO0 = mO0 + 0.5f*(event.values[0]-mO0);
                mO1 = mO1 + 0.5f*(event.values[1]-mO1);
                mO2 = mO2 + 0.5f*(event.values[2]-mO2);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mM0 = mM0 + 0.5f*(event.values[0]-mM0);
                mM1 = mM1 + 0.5f*(event.values[1]-mM1);
                mM2 = mM2 + 0.5f*(event.values[2]-mM2);
                break;
            default:
        };// switch (event.sensor.getType());
        if (move == 0) {
            if ((now_ms - last_step_ts) > 1000) {
                last_step_ts = now_ms;
                movtion = 0;

            }
        }
        else
        {
            movtion = 1;
            last_step_ts = now_ms;
        }
    }
    private void updateGUI(){
        mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAccelerometerX.setText(String.format("%.3f", mA0));
                mAccelerometerY.setText(String.format("%.3f", mA1));
                mAccelerometerZ.setText(String.format("%.3f", mA2));
                mGyroscopeX.setText(String.format("%.3f", mO0));
                mGyroscopeY.setText(String.format("%.3f", mO1));
                mGyroscopeZ.setText(String.format("%.3f", mO2));
                mMagnetometerX.setText(String.format("%.3f", mM0));
                mMagnetometerY.setText(String.format("%.3f", mM1));
                mMagnetometerZ.setText(String.format("%.3f", mM2));
                mBarometer.setText(String.format("%.3f", mB));
                mTimestamp.setText(String.valueOf(movtion));
                mSentPacketsTextView.setText(String.valueOf(sentPackets));
            }
        });
    }
    public  void onValuesEventChanged() {
        String macAddress = null;//, ip = null;
        WifiManager wifiMgr = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
        if (null != info) {
            macAddress = info.getMacAddress();
            //ip = int2ip(info.getIpAddress());
        }

        if (sendActive) {
            try {
                udphelper.Send(InetAddress.getByName(url),port,macAddress+";"+movtion+";"+mO0);


                sentPackets++;
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }


    }
    private boolean setUDPValues() {

        try {
            IPAddressValidator ipAddressValidator = new IPAddressValidator();
            url = mIPAddressText.getText().toString();

            if (ipAddressValidator.validate(url)) {


                port  = Integer.valueOf(mPortText.getText().toString());

                if (port >= 0 && port <= 65535) {

                    return true;
                } else {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mMainActivity, "No valid Port", 3000).show();
                        }
                    });
                }
            } else {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mMainActivity, "No valid IP address", 3000).show();
                    }
                });
            }
        }finally {
            //
        }
        return false;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mMainActivity = this;

        mAccelerometerX = (TextView) findViewById(R.id.accelerometerX);
        mAccelerometerY = (TextView) findViewById(R.id.accelerometerY);
        mAccelerometerZ = (TextView) findViewById(R.id.accelerometerZ);

        mGyroscopeX = (TextView) findViewById(R.id.gyroscopeX);
        mGyroscopeY = (TextView) findViewById(R.id.gyroscopeY);
        mGyroscopeZ = (TextView) findViewById(R.id.gyroscopeZ);

        mMagnetometerX = (TextView) findViewById(R.id.magnetometerX);
        mMagnetometerY = (TextView) findViewById(R.id.magnetometerY);
        mMagnetometerZ = (TextView) findViewById(R.id.magnetometerZ);

        mBarometer = (TextView) findViewById(R.id.barometer);

        mTimestamp = (TextView) findViewById(R.id.timestamp);

        mSentPacketsTextView = (TextView) findViewById(R.id.sentPacketsText);

        mIPAddressText = (EditText) findViewById(R.id.ipAddressText);
        mPortText = (EditText) findViewById(R.id.portText);

        //setUDPValues();
        udphelper = new UDPClient();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //get the accelerometer sensor
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mMAGNETIC = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        ndk = new Thread(new Runnable() {
            public void run() {
 //               QuadroidLib.init();
            }
        });
        ndk.start();

      /*  Timer updateTimer = new Timer("gForceUpdate");
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                updateGUI();
            }
        }, 0, 100);*/
        Timer eventTimer = new Timer("gEventUpdate");
        eventTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                onValuesEventChanged();
                updateGUI();
            }
        }, 0, 100);
    }

    public void onToggle(View view) {

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {

            if (setUDPValues()) {
                sendActive = ((ToggleButton) view).isChecked();


                if (sendActive) {
                    sentPackets = 0;
                    mIPAddressText.setEnabled(false);
                    mPortText.setEnabled(false);
                    ((ToggleButton) view).setChecked(true);
                } else {
                    mIPAddressText.setEnabled(true);
                    mPortText.setEnabled(true);
                    ((ToggleButton) view).setChecked(false);
                }

            } else {
                Toast.makeText(this, "Please connect to Wi-Fi", 3000).show();
                ((ToggleButton) view).setChecked(false);
            }
        }
    }

    public void onQuadroidButton(View view) {
        Uri uriUrl = Uri.parse("http://www.quadroid.io");
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMAGNETIC, SensorManager.SENSOR_DELAY_FASTEST);

    }
    @Override
    protected void onPause()
    {
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    /** Called when the activity is about to be destroyed. */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
}
