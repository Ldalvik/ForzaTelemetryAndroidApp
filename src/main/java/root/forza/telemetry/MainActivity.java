package root.forza.telemetry;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    //TextViews for all API data
    TextView isRaceOn_TextView,
            timestamp_TextView;
    TextView engineMaxRPM_TextView,
            engineIdleRPM_TextView,
            engineCurrentRPM_TextView;
    TextView accelerationX_TextView,
            accelerationY_TextView,
            accelerationZ_TextView;
    TextView velocityX_TextView,
            velocityY_TextView,
            velocityZ_TextView,
            velocity_TextView;
    TextView angularVelocityX_TextView,
            angularVelocityY_TextView,
            angularVelocityZ_TextView;
    TextView yaw_TextView,
            pitch_TextView,
            roll_TextView;
    TextView normalizedSuspensionTravelFrontLeft_TextView,
            normalizedSuspensionTravelFrontRight_TextView,
            normalizedSuspensionTravelRearLeft_TextView,
            normalizedSuspensionTravelRearRight_TextView;
    TextView tireSlipRatioFrontLeft_TextView,
            tireSlipRatioFrontRight_TextView,
            tireSlipRatioRearLeft_TextView,
            tireSlipRatioRearRight_TextView;
    TextView wheelRotationSpeedFrontLeft_TextView,
            wheelRotationSpeedFrontRight_TextView,
            wheelRotationSpeedRearLeft_TextView,
            wheelRotationSpeedRearRight_TextView;
    TextView wheelOnRumbleStripFrontLeft_TextView,
            wheelOnRumbleStripFrontRight_TextView,
            wheelOnRumbleStripRearLeft_TextView,
            wheelOnRumbleStripRearRight_TextView;
    TextView wheelInPuddleDepthFrontLeft_TextView,
            wheelInPuddleDepthFrontRight_TextView,
            wheelInPuddleDepthRearLeft_TextView,
            wheelInPuddleDepthRearRight_TextView;
    TextView surfaceRumbleFrontLeft_TextView,
            surfaceRumbleFrontRight_TextView,
            surfaceRumbleRearLeft_TextView,
            surfaceRumbleRearRight_TextView;
    TextView tireSlipAngleFrontLeft_TextView,
            tireSlipAngleFrontRight_TextView,
            tireSlipAngleRearLeft_TextView,
            tireSlipAngleRearRight_TextView;
    TextView tireCombinedSlipFrontLeft_TextView,
            tireCombinedSlipFrontRight_TextView,
            tireCombinedSlipRearLeft_TextView,
            tireCombinedSlipRearRight_TextView;
    TextView suspensionTravelMetersFrontLeft_TextView,
            suspensionTravelMetersFrontRight_TextView,
            suspensionTravelMetersRearLeft_TextView,
            suspensionTravelMetersRearRight_TextView;
    TextView carOrdinal_TextView,
            carClass_TextView,
            carPerformanceIndex_TextView,
            driveTrain_TextView,
            numberOfCylinders_TextView,
            carCategory_TextView;
    TextView objectHit_TextView;
    TextView positionX_TextView,
            positionY_TextView,
            positionZ_TextView;
    TextView speedMps_TextView,
            speedMph_TextView,
            speedKph_TextView;
    TextView power_TextView,
            horsepower_TextView,
            torque_TextView;
    TextView tireTempFrontLeft_TextView,
            tireTempFrontRight_TextView,
            tireTempRearLeft_TextView,
            tireTempRearRight_TextView,
            tireTempAverageFront_TextView,
            tireTempAverageRear_TextView,
            tireTempAverageTotal_TextView,
            tireTempAverageLeft_TextView,
            tireTempAverageRight_TextView;
    TextView boost_TextView,
            fuel_TextView,
            distanceTraveled_TextView;
    TextView bestLap_TextView,
            lastLap_TextView,
            currentLap_TextView,
            currentRaceTime_TextView,
            lapNumber_TextView,
            racePosition_TextView;
    TextView throttle_TextView,
            brake_TextView,
            clutch_TextView,
            handbrake_TextView,
            gear_TextView,
            steer_TextView;
    TextView normalizedDrivingLine_TextView,
            normalizedAIBrakeDifference_TextView;
    TextView fpsCounter_TextView;

    //Main UI views
    TextView deviceIp_TextView;
    EditText portNumber_EditText;
    Button connect_Button;
    SeekBar engineRpm_SeekBar;

    //UDP Data classes
    DatagramSocket datagramSocket;
    DatagramPacket datagramPacket;
    ForzaApi forzaApi;

    //FPS count helpers;
    int fps = 0;
    int currentFPS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets theme to dark as default, can be changed in settings with toggle
        /*if (getPref("dark_theme", false)) {
            setTheme(R.style.Theme_ForzaTelemetryLight);
        } else {
            setTheme(R.style.Theme_ForzaTelemetryDark);
        }*/
        //Setting views
        setContentView(R.layout.activity_main);
        initTextViews();
        engineRpm_SeekBar.setEnabled(false);
        deviceIp_TextView = findViewById(R.id.device_ip);
        portNumber_EditText = findViewById(R.id.port);
        fpsCounter_TextView.setVisibility(getViewPref("show_fps", false));

        //Get device IP that is needed to connect to Forza
        try {
            deviceIp_TextView.setText(getDeviceIp());
        } catch (UnknownHostException e) {
           toast("IP not able to be retrieved: " + e);
        }

        //Setting toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Forza Telemetry");
        toolbar.setSubtitle((Html.fromHtml("<small>" + getResources().getString(R.string.app_version) + "</small>", Html.FROM_HTML_MODE_LEGACY)));
        toolbar.setNavigationIcon(R.mipmap.optn_logo_transparent);
        toolbar.setNavigationOnClickListener(
                v -> Toast.makeText(this, "Thank you for using OPTN.tools software. Come hang out :)", Toast.LENGTH_SHORT).show()
        );
        setSupportActionBar(toolbar);

        /* SharedPref debugging
        * if(getPref("is_metric", false)) toast("celsius");
        * else toast("fahrenheit");
        */

        /*
         * Option to hide data via checkboxes in settings. Too lazy to implement right now.
         * isRaceOn_TextView.setVisibility(getViewPref("isRaceOn"));
         * timestamp_TextView.setVisibility(getViewPref("timestamp"));
         */

        //Display welcome dialog if its the first time running the app
        if (getSharedPreferences("ForzaPrefs", 0).getBoolean("firstRun", true)) {
            new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setTitle("Version [" + getResources().getString(R.string.app_version) + "]")
                    .setMessage("Thank you for using OPTN.tools software! This app was created for the purpose of fine tuning your builds to achieve the perfect tune, " +
                            "or just for those who are interested in the data. You could use this to reverse engineer tunes as well.\n\nIf you'd like to hang out, " +
                            "discuss tunes, or just talk cars in general, join the discord or subreddit (Links are in the settings.) To report a bug, please message me on " +
                            "discord (root.#6923) or reddit (u/hey-im-root)\n\nFor a tutorial on how to use this app, click the Tutorial button in the settings. You can also " +
                            "find a more in-depth version in the Github that is linked as well. Enjoy :)")
                    .setPositiveButton("Got it!", (dialog, id) -> dialog.dismiss())
                    .create().show();
            getSharedPreferences("ForzaPrefs", 0).edit().putBoolean("firstRun", false).apply();
        }
        //Timer and counter to get frames received every 1000 milliseconds
        new Timer().scheduleAtFixedRate( new TimerTask() {
            public void run() {
                currentFPS = fps; fps = 0;
            }
        }, 1000, 1000);
    }

    //Start a datasocket/datapacket process that will wait for Forza to establish a connection
    public void connect(View v) {
        connect_Button.setEnabled(false);
        connect_Button.setText(R.string.awaiting_data);

        Thread udpSendThread = new Thread(() -> {
            try {
                datagramSocket = new DatagramSocket(Integer.parseInt(portNumber_EditText.getText().toString()));
            } catch (Exception e) {
                toast(e.toString());
            }

            //Only 323 bytes are received from the Forza UDP stream
            byte[] receive = new byte[323];
            boolean isConnected = false;
            while (true) {
                datagramPacket = new DatagramPacket(receive, receive.length);
                try {
                    assert datagramSocket != null;
                    datagramSocket.receive(datagramPacket);
                    //Set button and enable RPM bar once a connection is established
                    if (!isConnected) {
                        ForzaApi tempApi = new ForzaApi(datagramPacket.getData());
                        if (tempApi.getTimeStampMS() != 0) {
                            setButtonText("Connected.");
                            engineRpm_SeekBar.setEnabled(true);
                            isConnected = true;
                        }
                    }
                } catch (Exception e) {
                    toast(e.toString());
                }

                //Send data to the ForzaApi parsing class
                try {
                    forzaApi = new ForzaApi(datagramPacket.getData());
                } catch (Exception e) {
                    toast(e.toString());
                }

                //Update all TextViews in a UI thread, data can be received at 60fps depending on device,
                runOnUiThread(() -> {
                    fpsCounter_TextView.setVisibility(getViewPref("show_fps", false));
                    updateText(fpsCounter_TextView, "FPS: " + currentFPS);
                    setRpmData();
                    updateText(isRaceOn_TextView,            "isRaceOn: " + forzaApi.getIsRaceOn());
                    updateText(timestamp_TextView,           "timestamp: " + forzaApi.getTimeStampMS());
                    updateText(engineIdleRPM_TextView,       "engineIdleRPM: " + forzaApi.getEngineIdleRpm());
                    updateText(engineCurrentRPM_TextView,    "engineCurrentRPM: " + forzaApi.getCurrentEngineRpm());
                    updateText(engineMaxRPM_TextView,        "engineMaxRPM: " + forzaApi.getEngineMaxRpm());
                    updateText(accelerationX_TextView,       "accelerationX: " + forzaApi.getAccelerationX());
                    updateText(accelerationY_TextView,       "accelerationY: " + forzaApi.getAccelerationY());
                    updateText(accelerationZ_TextView,       "accelerationZ: " + forzaApi.getAccelerationZ());
                    updateText(velocityX_TextView,           "velocityX: " + forzaApi.getVelocityX());
                    updateText(velocityY_TextView,           "velocityY: " + forzaApi.getVelocityY());
                    updateText(velocityZ_TextView,           "velocityZ: " + forzaApi.getVelocityZ());
                    updateText(velocity_TextView,            "averageVelocity: " + forzaApi.getAverageVelocity());
                    updateText(angularVelocityX_TextView,    "angularVelocityX: " + forzaApi.getAngularVelocityX());
                    updateText(angularVelocityY_TextView,    "angularVelocityY: " + forzaApi.getAngularVelocityY());
                    updateText(angularVelocityZ_TextView,    "angularVelocityZ: " + forzaApi.getAngularVelocityZ());
                    updateText(yaw_TextView,                 "yaw: " + forzaApi.getYaw());
                    updateText(pitch_TextView,               "pitch: " + forzaApi.getPitch());
                    updateText(roll_TextView,                "roll: " + forzaApi.getRoll());
                    updateText(normalizedSuspensionTravelFrontLeft_TextView, "normalizedSuspensionTravelFrontLeft: " + forzaApi.getNormalizedSuspensionTravelFrontLeft()+"%");
                    updateText(normalizedSuspensionTravelFrontRight_TextView, "normalizedSuspensionTravelFrontRight: " + forzaApi.getNormalizedSuspensionTravelFrontRight()+"%");
                    updateText(normalizedSuspensionTravelRearLeft_TextView, "normalizedSuspensionTravelRearLeft: " + forzaApi.getNormalizedSuspensionTravelRearLeft()+"%");
                    updateText(normalizedSuspensionTravelRearRight_TextView, "normalizedSuspensionTravelRearRight: " + forzaApi.getNormalizedSuspensionTravelRearRight()+"%");
                    updateText(tireSlipRatioFrontLeft_TextView, "tireSlipRatioFrontLeft: " + forzaApi.getTireSlipRatioFrontLeft());
                    updateText(tireSlipRatioFrontRight_TextView, "tireSlipRatioFrontRight: " + forzaApi.getTireSlipRatioFrontRight());
                    updateText(tireSlipRatioRearLeft_TextView, "tireSlipRatioRearLeft: " + forzaApi.getTireSlipRatioRearLeft());
                    updateText(tireSlipRatioRearRight_TextView, "tireSlipRatioRearRight: " + forzaApi.getTireSlipRatioRearRight());
                    updateText(wheelRotationSpeedFrontLeft_TextView, "wheelRotationSpeedFrontLeft: " + forzaApi.getWheelRotationSpeedFrontLeft());
                    updateText(wheelRotationSpeedFrontRight_TextView, "wheelRotationSpeedFrontRight: " + forzaApi.getWheelRotationSpeedFrontRight());
                    updateText(wheelRotationSpeedRearLeft_TextView, "wheelRotationSpeedRearLeft: " + forzaApi.getWheelRotationSpeedRearLeft());
                    updateText(wheelRotationSpeedRearRight_TextView, "wheelRotationSpeedRearRight: " + forzaApi.getWheelRotationSpeedRearRight());
                    updateText(wheelOnRumbleStripFrontLeft_TextView, "wheelOnRumbleStripFrontLeft: " + forzaApi.getWheelOnRumbleStripFrontLeft());
                    updateText(wheelOnRumbleStripFrontRight_TextView, "wheelOnRumbleStripFrontRight: " + forzaApi.getWheelOnRumbleStripFrontRight());
                    updateText(wheelOnRumbleStripRearLeft_TextView, "wheelOnRumbleStripRearLeft: " + forzaApi.getWheelOnRumbleStripRearLeft());
                    updateText(wheelOnRumbleStripRearRight_TextView, "wheelOnRumbleStripRearRight: " + forzaApi.getWheelOnRumbleStripRearRight());
                    updateText(wheelInPuddleDepthFrontLeft_TextView, "wheelInPuddleDepthFrontLeft: " + forzaApi.getWheelInPuddleDepthFrontLeft());
                    updateText(wheelInPuddleDepthFrontRight_TextView, "wheelInPuddleDepthFrontRight: " + forzaApi.getWheelInPuddleDepthFrontRight());
                    updateText(wheelInPuddleDepthRearLeft_TextView, "wheelInPuddleDepthRearLeft: " + forzaApi.getWheelInPuddleDepthRearLeft());
                    updateText(wheelInPuddleDepthRearRight_TextView, "wheelInPuddleDepthRearRight: " + forzaApi.getWheelInPuddleDepthRearRight());
                    updateText(surfaceRumbleFrontLeft_TextView, "surfaceRumbleFrontLeft: " + forzaApi.getSurfaceRumbleFrontLeft());
                    updateText(surfaceRumbleFrontRight_TextView, "surfaceRumbleFrontRight: " + forzaApi.getSurfaceRumbleFrontRight());
                    updateText(surfaceRumbleRearLeft_TextView, "surfaceRumbleRearLeft: " + forzaApi.getSurfaceRumbleRearLeft());
                    updateText(surfaceRumbleRearRight_TextView, "surfaceRumbleRearRight: " + forzaApi.getSurfaceRumbleRearRight());
                    updateText(tireSlipAngleFrontLeft_TextView, "tireSlipAngleFrontLeft: " + forzaApi.getTireSlipAngleFrontLeft()+"°");
                    updateText(tireSlipAngleFrontRight_TextView, "tireSlipAngleFrontRight: " + forzaApi.getTireSlipAngleFrontRight()+"°");
                    updateText(tireSlipAngleRearLeft_TextView, "tireSlipAngleRearLeft: " + forzaApi.getTireSlipAngleRearLeft()+"°");
                    updateText(tireSlipAngleRearRight_TextView, "tireSlipAngleRearRight: " + forzaApi.getTireSlipAngleRearRight()+"°");
                    updateText(tireCombinedSlipFrontLeft_TextView, "tireCombinedSlipFrontLeft: " + forzaApi.getTireCombinedSlipFrontLeft());
                    updateText(tireCombinedSlipFrontRight_TextView, "tireCombinedSlipFrontRight: " + forzaApi.getTireCombinedSlipFrontRight());
                    updateText(tireCombinedSlipRearLeft_TextView, "tireCombinedSlipRearLeft: " + forzaApi.getTireCombinedSlipRearLeft());
                    updateText(tireCombinedSlipRearRight_TextView, "tireCombinedSlipRearRight: " + forzaApi.getTireCombinedSlipRearRight());
                    updateText(suspensionTravelMetersFrontLeft_TextView, "suspensionTravelMetersFrontLeft: " + forzaApi.getSuspensionTravelMetersFrontLeft() + "m");
                    updateText(suspensionTravelMetersFrontRight_TextView, "suspensionTravelMetersFrontRight: " + forzaApi.getSuspensionTravelMetersFrontRight() + "m");
                    updateText(suspensionTravelMetersRearLeft_TextView, "suspensionTravelMetersRearLeft: " + forzaApi.getSuspensionTravelMetersRearLeft() + "m");
                    updateText(suspensionTravelMetersRearRight_TextView, "suspensionTravelMetersRearRight: " + forzaApi.getSuspensionTravelMetersRearRight() + "m");
                    updateText(carOrdinal_TextView, "carOrdinal: " + forzaApi.getCarOrdinal());
                    updateText(carClass_TextView, "carClass: " + forzaApi.getCarClass());
                    updateText(carPerformanceIndex_TextView, "carPerformanceIndex: " + forzaApi.getCarPerformanceIndex());
                    updateText(driveTrain_TextView, "driveTrain: " + forzaApi.getDrivetrain());
                    updateText(numberOfCylinders_TextView, "numberOfCylinders: " + forzaApi.getNumCylinders());
                    updateText(carCategory_TextView, "carCategory: " + forzaApi.getCarType());
                    if(forzaApi.getObjectHit() != 0) updateText(objectHit_TextView, "objectHit: " + forzaApi.getObjectHit());
                    updateText(positionX_TextView, "positionX: " + forzaApi.getPositionX());
                    updateText(positionY_TextView, "positionY: " + forzaApi.getPositionY());
                    updateText(positionZ_TextView, "positionZ: " + forzaApi.getPositionZ());
                    updateText(speedMps_TextView, "speed (m/s): " + forzaApi.getSpeedMps() + " m/s");
                    updateText(speedMph_TextView, "speed (mph): " + forzaApi.getSpeedMph() + "MPH");
                    updateText(speedKph_TextView, "speed (kph): " + forzaApi.getSpeedKph() + "KPH");
                    updateText(power_TextView, "power (watts): " + forzaApi.getPower());
                    updateText(horsepower_TextView, "horsepower: " + forzaApi.getHorsePower());
                    updateText(torque_TextView, "torque: " + forzaApi.getTorque());
                    //Get temperature measurements for celsius or fahrenheit
                    if(getPref("is_metric", false)) {
                        updateText(tireTempFrontLeft_TextView, "tireTempFrontLeft: " + forzaApi.getTireTempFrontLeft_C() + "°C");
                        updateText(tireTempFrontRight_TextView, "tireTempFrontRight: " + forzaApi.getTireTempFrontRight_C() + "°C");
                        updateText(tireTempRearLeft_TextView, "tireTempRearLeft: " + forzaApi.getTireTempRearLeft_C() + "°C");
                        updateText(tireTempRearRight_TextView, "tireTempRearRight: " + forzaApi.getTireTempRearRight_C() + "°C");
                        updateText(tireTempAverageFront_TextView, "tireTempAverageFront: " + forzaApi.getTireTempAverageFront_C() + "°C");
                        updateText(tireTempAverageRear_TextView, "tireTempAverageRear: " + forzaApi.getTireTempAverageRear_C() + "°C");
                        updateText(tireTempAverageTotal_TextView, "tireTempAverageTotal: " + forzaApi.getTireTempAverageTotal_C() + "°C");
                        updateText(tireTempAverageLeft_TextView, "tireTempAverageLeft: " + forzaApi.getTireTempAverageLeft_C() + "°C");
                        updateText(tireTempAverageRight_TextView, "tireTempAverageRight: " + forzaApi.getTireTempAverageRight_C() + "°C");
                    } else {
                        updateText(tireTempFrontLeft_TextView, "tireTempFrontLeft: " + forzaApi.getTireTempFrontLeft() + "°F");
                        updateText(tireTempFrontRight_TextView, "tireTempFrontRight: " + forzaApi.getTireTempFrontRight() + "°F");
                        updateText(tireTempRearLeft_TextView, "tireTempRearLeft: " + forzaApi.getTireTempRearLeft() + "°F");
                        updateText(tireTempRearRight_TextView, "tireTempRearRight: " + forzaApi.getTireTempRearRight() + "°F");
                        updateText(tireTempAverageFront_TextView, "tireTempAverageFront: " + forzaApi.getTireTempAverageFront() + "°F");
                        updateText(tireTempAverageRear_TextView, "tireTempAverageRear: " + forzaApi.getTireTempAverageRear() + "°F");
                        updateText(tireTempAverageTotal_TextView, "tireTempAverageTotal: " + forzaApi.getTireTempAverageTotal() + "°F");
                        updateText(tireTempAverageLeft_TextView, "tireTempAverageLeft: " + forzaApi.getTireTempAverageLeft() + "°F");
                        updateText(tireTempAverageRight_TextView, "tireTempAverageRight: " + forzaApi.getTireTempAverageRight() + "°F");
                    }
                    updateText(boost_TextView, "boost: " + forzaApi.getBoost());
                    updateText(fuel_TextView, "fuel: " + forzaApi.getFuel() + "%");
                    updateText(distanceTraveled_TextView, "distanceTraveled: " + forzaApi.getDistanceTraveled());
                    updateText(bestLap_TextView, "bestLap: " + forzaApi.getBestLap());
                    updateText(lastLap_TextView, "lastLap: " + forzaApi.getLastLap());
                    updateText(currentLap_TextView, "currentLap: " + forzaApi.getCurrentLap());
                    updateText(currentRaceTime_TextView, "currentRaceTime: " + forzaApi.getCurrentRaceTime());
                    updateText(lapNumber_TextView, "lapNumber: " + forzaApi.getLapNumber());
                    updateText(racePosition_TextView, "racePosition: " + forzaApi.getRacePosition());
                    updateText(throttle_TextView, "throttle: " + forzaApi.getThrottle());
                    updateText(brake_TextView, "brake: " + forzaApi.getBrake());
                    updateText(clutch_TextView, "clutch: " + forzaApi.getClutch());
                    updateText(handbrake_TextView, "handbrake: " + forzaApi.getHandbrake());
                    updateText(gear_TextView, "gear: " + forzaApi.getGear());
                    updateText(steer_TextView, "steer: " + forzaApi.getSteer());
                    updateText(normalizedDrivingLine_TextView, "normalizedDrivingLine: " + forzaApi.getNormalizedDrivingLine());
                    updateText(normalizedAIBrakeDifference_TextView, "normalizedAiBrakeDifference: " + forzaApi.getNormalizedAIBrakeDifference());
                });
                receive = new byte[323];
                fps++;
            }
        });

        //Port form input logic
        int port = Integer.parseInt(portNumber_EditText.getText().toString());
        if (port == 0 || port > 65535) toast("Please type in a port from 1 to 65535");
        else udpSendThread.start();
    }

    public void initTextViews(){
        fpsCounter_TextView = findViewById(R.id.fpsCounter_id);
        isRaceOn_TextView =  findViewById(R.id.isRaceOn_id);
        timestamp_TextView =  findViewById(R.id.timestamp_id);
        engineMaxRPM_TextView =  findViewById(R.id.engineMaxRPM_id);
        engineIdleRPM_TextView =  findViewById(R.id.engineIdleRPM_id);
        engineCurrentRPM_TextView =  findViewById(R.id.engineCurrentRPM_id);
        accelerationX_TextView =  findViewById(R.id.accelerationX_id);
        accelerationY_TextView =  findViewById(R.id.accelerationY_id);
        accelerationZ_TextView =  findViewById(R.id.accelerationZ_id);
        velocityX_TextView =  findViewById(R.id.velocityX_id);
        velocityY_TextView =  findViewById(R.id.velocityY_id);
        velocityZ_TextView =  findViewById(R.id.velocityZ_id);
        velocity_TextView =  findViewById(R.id.averageVelocity_id);
        angularVelocityX_TextView =  findViewById(R.id.angularVelocityX_id);
        angularVelocityY_TextView =  findViewById(R.id.angularVelocityY_id);
        angularVelocityZ_TextView =  findViewById(R.id.angularVelocityZ_id);
        yaw_TextView =  findViewById(R.id.yaw_id);
        pitch_TextView =  findViewById(R.id.pitch_id);
        roll_TextView =  findViewById(R.id.roll_id);
        normalizedSuspensionTravelFrontLeft_TextView =  findViewById(R.id.normalizedSuspensionTravelFrontLeft_id);
        normalizedSuspensionTravelFrontRight_TextView =  findViewById(R.id.normalizedSuspensionTravelFrontRight_id);
        normalizedSuspensionTravelRearLeft_TextView =  findViewById(R.id.normalizedSuspensionTravelRearLeft_id);
        normalizedSuspensionTravelRearRight_TextView =  findViewById(R.id.normalizedSuspensionTravelRearRight_id);
        tireSlipRatioFrontLeft_TextView =  findViewById(R.id.tireSlipRatioFrontLeft_id);
        tireSlipRatioFrontRight_TextView =  findViewById(R.id.tireSlipRatioFrontRight_id);
        tireSlipRatioRearLeft_TextView =  findViewById(R.id.tireSlipRatioRearLeft_id);
        tireSlipRatioRearRight_TextView =  findViewById(R.id.tireSlipRatioRearRight_id);
        wheelRotationSpeedFrontLeft_TextView =  findViewById(R.id.wheelRotationSpeedFrontLeft_id);
        wheelRotationSpeedFrontRight_TextView =  findViewById(R.id.wheelRotationSpeedFrontRight_id);
        wheelRotationSpeedRearLeft_TextView =  findViewById(R.id.wheelRotationSpeedRearLeft_id);
        wheelRotationSpeedRearRight_TextView =  findViewById(R.id.wheelRotationSpeedRearRight_id);
        wheelOnRumbleStripFrontLeft_TextView =  findViewById(R.id.wheelOnRumbleStripFrontLeft_id);
        wheelOnRumbleStripFrontRight_TextView =  findViewById(R.id.wheelOnRumbleStripFrontRight_id);
        wheelOnRumbleStripRearLeft_TextView =  findViewById(R.id.wheelOnRumbleStripRearLeft_id);
        wheelOnRumbleStripRearRight_TextView =  findViewById(R.id.wheelOnRumbleStripRearRight_id);
        wheelInPuddleDepthFrontLeft_TextView =  findViewById(R.id.wheelInPuddleDepthFrontLeft_id);
        wheelInPuddleDepthFrontRight_TextView =  findViewById(R.id.wheelInPuddleDepthFrontRight_id);
        wheelInPuddleDepthRearLeft_TextView =  findViewById(R.id.wheelInPuddleDepthRearLeft_id);
        wheelInPuddleDepthRearRight_TextView =  findViewById(R.id.wheelInPuddleDepthRearRight_id);
        surfaceRumbleFrontLeft_TextView =  findViewById(R.id.surfaceRumbleFrontLeft_id);
        surfaceRumbleFrontRight_TextView =  findViewById(R.id.surfaceRumbleFrontRight_id);
        surfaceRumbleRearLeft_TextView =  findViewById(R.id.surfaceRumbleRearLeft_id);
        surfaceRumbleRearRight_TextView =  findViewById(R.id.surfaceRumbleRearRight_id);
        tireSlipAngleFrontLeft_TextView =  findViewById(R.id.tireSlipAngleFrontLeft_id);
        tireSlipAngleFrontRight_TextView =  findViewById(R.id.tireSlipAngleFrontRight_id);
        tireSlipAngleRearLeft_TextView =  findViewById(R.id.tireSlipAngleRearLeft_id);
        tireSlipAngleRearRight_TextView =  findViewById(R.id.tireSlipAngleRearRight_id);
        tireCombinedSlipFrontLeft_TextView =  findViewById(R.id.tireCombinedSlipFrontLeft_id);
        tireCombinedSlipFrontRight_TextView =  findViewById(R.id.tireCombinedSlipFrontRight_id);
        tireCombinedSlipRearLeft_TextView =  findViewById(R.id.tireCombinedSlipRearLeft_id);
        tireCombinedSlipRearRight_TextView =  findViewById(R.id.tireCombinedSlipRearRight_id);
        suspensionTravelMetersFrontLeft_TextView =  findViewById(R.id.suspensionTravelMetersFrontLeft_id);
        suspensionTravelMetersFrontRight_TextView =  findViewById(R.id.suspensionTravelMetersFrontRight_id);
        suspensionTravelMetersRearLeft_TextView =  findViewById(R.id.suspensionTravelMetersRearLeft_id);
        suspensionTravelMetersRearRight_TextView =  findViewById(R.id.suspensionTravelMetersRearRight_id);
        carOrdinal_TextView =  findViewById(R.id.carOrdinal_id);
        carClass_TextView =  findViewById(R.id.carClass_id);
        carPerformanceIndex_TextView =  findViewById(R.id.carPerformanceIndex_id);
        driveTrain_TextView =  findViewById(R.id.driveTrain_id);
        numberOfCylinders_TextView =  findViewById(R.id.numberOfCylinders_id);
        carCategory_TextView =  findViewById(R.id.carCategory_id);
        objectHit_TextView =  findViewById(R.id.objectHit_id);
        positionX_TextView =  findViewById(R.id.positionX_id);
        positionY_TextView =  findViewById(R.id.positionY_id);
        positionZ_TextView =  findViewById(R.id.positionZ_id);
        speedMps_TextView =  findViewById(R.id.speedMps_id);
        speedMph_TextView =  findViewById(R.id.speedMph_id);
        speedKph_TextView =  findViewById(R.id.speedKph_id);
        power_TextView =  findViewById(R.id.power_id);
        horsepower_TextView =  findViewById(R.id.horsepower_id);
        torque_TextView =  findViewById(R.id.torque_id);
        tireTempFrontLeft_TextView =  findViewById(R.id.tireTempFrontLeft_id);
        tireTempFrontRight_TextView =  findViewById(R.id.tireTempFrontRight_id);
        tireTempRearLeft_TextView =  findViewById(R.id.tireTempRearLeft_id);
        tireTempRearRight_TextView =  findViewById(R.id.tireTempRearRight_id);
        tireTempAverageFront_TextView =  findViewById(R.id.tireTempAverageFront_id);
        tireTempAverageRear_TextView =  findViewById(R.id.tireTempAverageRear_id);
        tireTempAverageTotal_TextView =  findViewById(R.id.tireTempAverageTotal_id);
        tireTempAverageLeft_TextView =  findViewById(R.id.tireTempAverageLeft_id);
        tireTempAverageRight_TextView =  findViewById(R.id.tireTempAverageRight_id);
        boost_TextView =  findViewById(R.id.boost_id);
        fuel_TextView =  findViewById(R.id.fuel_id);
        distanceTraveled_TextView =  findViewById(R.id.distanceTraveled_id);
        bestLap_TextView =  findViewById(R.id.bestLap_id);
        lastLap_TextView =  findViewById(R.id.lastLap_id);
        currentLap_TextView =  findViewById(R.id.currentLap_id);
        currentRaceTime_TextView =  findViewById(R.id.currentRaceTime_id);
        lapNumber_TextView =  findViewById(R.id.lapNumber_id);
        racePosition_TextView =  findViewById(R.id.racePosition_id);
        throttle_TextView =  findViewById(R.id.throttle_id);
        brake_TextView =  findViewById(R.id.brake_id);
        clutch_TextView =  findViewById(R.id.clutch_id);
        handbrake_TextView =  findViewById(R.id.handbrake_id);
        gear_TextView =  findViewById(R.id.gear_id);
        steer_TextView =  findViewById(R.id.steer_id);
        normalizedDrivingLine_TextView =  findViewById(R.id.normalizedDrivingLine_id);
        normalizedAIBrakeDifference_TextView =  findViewById(R.id.normalizedAIBrakeDifference_id);
        connect_Button =  findViewById(R.id.connect_button);
        engineRpm_SeekBar =  findViewById(R.id.engine_rpm_SeekBar_id);
    }

    //Update TextView method
    public void updateText(final TextView tv, final String text) {
        tv.setText(text);
    }

    //Changing view on UI thread workaround
    public void setButtonText(final String s) {
        runOnUiThread(() -> connect_Button.setText(s));
    }

    //Displaying toast on UI thread workaround
    public void toast(final String s) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show());
    }

    //Get device IP address
    public String getDeviceIp() throws UnknownHostException {
        WifiManager wm = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);
        return InetAddress.getByAddress(
                ByteBuffer
                        .allocate(Integer.BYTES)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putInt(wm.getConnectionInfo().getIpAddress())
                        .array()
        ).getHostAddress();
    }

    //Wrapped SharedPref function for changing TextView visibility (unimplemented)
    public int getViewPref(String prefName, boolean defValue) {
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(prefName, defValue)) return View.VISIBLE; else return View.GONE;
    }

    //Wrapped SharedPref function
    public boolean getPref(String prefName, boolean defValue) {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(prefName, defValue);
    }

    //Settings methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //RPM related functions, mainly for white-red fade when at high revs
    public void setRpmData() {
        engineRpm_SeekBar.setProgress(forzaApi.getCurrentEngineRpm());
        if (forzaApi.getCurrentEngineRpm() > forzaApi.getEngineMaxRpm() - 2500) {
            engineRpm_SeekBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
            engineRpm_SeekBar.setThumbTintList(ColorStateList.valueOf(Color.RED));
        } else {
            engineRpm_SeekBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#0085A3")));
            engineRpm_SeekBar.setThumbTintList(ColorStateList.valueOf(Color.parseColor("#034E60")));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings)
            startActivity(new Intent(this, SettingsActivity.class));
        return true;
    }

}
