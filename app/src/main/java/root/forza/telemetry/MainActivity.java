package root.forza.telemetry;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;
import androidx.annotation.StyleableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class MainActivity extends AppCompatActivity {
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
    TextView unknown1_TextView,
            unknown2_TextView;
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
            tireTempAverageTotal_TextView;
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

    TextView deviceIp;
    EditText portNumber;
    Button connect_Button;
    SeekBar engine_rpm_SeekBar;

    ForzaApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getBool("dark_mode")) {
            setTheme(R.style.Theme_ForzaTelemetryDark);
        } else {
            setTheme(R.style.Theme_ForzaTelemetryLight);
        }
        setContentView(R.layout.activity_main);

        init();

        engine_rpm_SeekBar.setMax(10000);

        deviceIp = (TextView) findViewById(R.id.device_ip);
        portNumber = (EditText) findViewById(R.id.port);
        deviceIp.setText(getDeviceIp());


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Forza Telemetry");
        toolbar.setSubtitle((Html.fromHtml("<small>beta.0.1.3</small>")));
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.mipmap.ic_launcher_adaptive_fore);
        toolbar.setNavigationOnClickListener(
                v -> Toast.makeText(MainActivity.this, "Thank you for using ForzaOpenTunes tools :) Come hang out at https://reddit.com/r/ForzaOpenTunes", Toast.LENGTH_SHORT).show()
        );

        //isRaceOn_TextView.setVisibility(getViewPref("isRaceOn"));


        if (!getSharedPreferences("ForzaPrefs", 0).getBoolean("firstRun", false)) {
            new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setTitle("Version [beta.0.1.3]")
                    .setMessage("Thank you for using ForzaOpenTunes software! This app was created for the purpose of fine tuning your builds to achieve the perfect tune. You could use this to reverse engineer tunes as well. If you'd like to hang out, discuss tunes, or just talk cars in general, join the discord or subreddit :) https://discord.gg/hhRPr2Gn\nhttps://reddit.com/r/ForzaOpenTunes\n\nTo report a bug, please message me on discord (root.#3543) or reddit (u/hey-im-root)")
                    .setPositiveButton("Ok", (dialog, id) -> dialog.dismiss())
                    .create().show();
            getSharedPreferences("ForzaPrefs", 0).edit().putBoolean("firstRun", true).apply();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void connect(View v) {
        connect_Button.setEnabled(false);
        connect_Button.setText("Awaiting data...");
        Thread udpSendThread = new Thread(() -> {
            DatagramSocket ds = null;
            try {
                ds = new DatagramSocket(Integer.parseInt(portNumber.getText().toString()));
            } catch (Exception e) {
                toast(e.toString());
            }

            byte[] receive = new byte[323];
            DatagramPacket dp;
            boolean connection = true;

            while (true) {
                dp = new DatagramPacket(receive, receive.length);
                try {
                    assert ds != null;
                    ds.receive(dp);
                    if (connection) {
                        if (new ForzaApi(dp.getData()).getTimeStampMS() != 0) {
                            setButtonText("Connected.");
                            connection = false;
                        }
                    }
                } catch (IOException e) {
                    toast(e.toString());
                }
                api = new ForzaApi(dp.getData());

                runOnUiThread(() -> {
                    engine_rpm_SeekBar.setMax(api.getEngineMaxRpm());
                    engine_rpm_SeekBar.setProgress(api.getCurrentEngineRpm());
                    updateText(isRaceOn_TextView,            "isRaceOn: " + api.getIsRaceOn());
                    updateText(timestamp_TextView,           "timestamp: " + api.getTimeStampMS());
                    updateText(engineMaxRPM_TextView,        "engineMaxRPM: " + api.getEngineMaxRpm());
                    updateText(engineIdleRPM_TextView,       "engineIdleRPM: " + api.getEngineIdleRpm());
                    updateText(engineCurrentRPM_TextView,    "engineCurrentRPM: " + api.getCurrentEngineRpm());
                    updateText(accelerationX_TextView,       "accelerationX: " + api.getAccelerationX());
                    updateText(accelerationY_TextView,       "accelerationY: " + api.getAccelerationY());
                    updateText(accelerationZ_TextView,       "accelerationZ: " + api.getAccelerationZ());
                    updateText(velocityX_TextView,           "velocityX: " + api.getVelocityX());
                    updateText(velocityY_TextView,           "velocityY: " + api.getVelocityY());
                    updateText(velocityZ_TextView,           "velocityZ: " + api.getVelocityZ());
                    updateText(velocity_TextView,            "averageVelocity: " + api.getAverageVelocity());
                    updateText(angularVelocityX_TextView,    "angularVelocityX: " + api.getAngularVelocityX());
                    updateText(angularVelocityY_TextView,    "angularVelocityY: " + api.getAngularVelocityY());
                    updateText(angularVelocityZ_TextView,    "angularVelocityZ: " + api.getAngularVelocityZ());
                    updateText(yaw_TextView,                 "yaw: " + api.getYaw());
                    updateText(pitch_TextView,               "pitch: " + api.getPitch());
                    updateText(roll_TextView,                "roll: " + api.getRoll());
                    updateText(normalizedSuspensionTravelFrontLeft_TextView, "normalizedSuspensionTravelFrontLeft: " + api.getNormalizedSuspensionTravelFrontLeft()+"%");
                    updateText(normalizedSuspensionTravelFrontRight_TextView, "normalizedSuspensionTravelFrontRight: " + api.getNormalizedSuspensionTravelFrontRight()+"%");
                    updateText(normalizedSuspensionTravelRearLeft_TextView, "normalizedSuspensionTravelRearLeft: " + api.getNormalizedSuspensionTravelRearLeft()+"%");
                    updateText(normalizedSuspensionTravelRearRight_TextView, "normalizedSuspensionTravelRearRight: " + api.getNormalizedSuspensionTravelRearRight()+"%");
                    updateText(tireSlipRatioFrontLeft_TextView, "tireSlipRatioFrontLeft: " + api.getTireSlipRatioFrontLeft());
                    updateText(tireSlipRatioFrontRight_TextView, "tireSlipRatioFrontRight: " + api.getTireSlipRatioFrontRight());
                    updateText(tireSlipRatioRearLeft_TextView, "tireSlipRatioRearLeft: " + api.getTireSlipRatioRearLeft());
                    updateText(tireSlipRatioRearRight_TextView, "tireSlipRatioRearRight: " + api.getTireSlipRatioRearRight());
                    updateText(wheelRotationSpeedFrontLeft_TextView, "wheelRotationSpeedFrontLeft: " + api.getWheelRotationSpeedFrontLeft());
                    updateText(wheelRotationSpeedFrontRight_TextView, "wheelRotationSpeedFrontRight: " + api.getWheelRotationSpeedFrontRight());
                    updateText(wheelRotationSpeedRearLeft_TextView, "wheelRotationSpeedRearLeft: " + api.getWheelRotationSpeedRearLeft());
                    updateText(wheelRotationSpeedRearRight_TextView, "wheelRotationSpeedRearRight: " + api.getWheelRotationSpeedRearRight());
                    updateText(wheelOnRumbleStripFrontLeft_TextView, "wheelOnRumbleStripFrontLeft: " + api.getWheelOnRumbleStripFrontLeft());
                    updateText(wheelOnRumbleStripFrontRight_TextView, "wheelOnRumbleStripFrontRight: " + api.getWheelOnRumbleStripFrontRight());
                    updateText(wheelOnRumbleStripRearLeft_TextView, "wheelOnRumbleStripRearLeft: " + api.getWheelOnRumbleStripRearLeft());
                    updateText(wheelOnRumbleStripRearRight_TextView, "wheelOnRumbleStripRearRight: " + api.getWheelOnRumbleStripRearRight());
                    updateText(wheelInPuddleDepthFrontLeft_TextView, "wheelInPuddleDepthFrontLeft: " + api.getWheelInPuddleDepthFrontLeft());
                    updateText(wheelInPuddleDepthFrontRight_TextView, "wheelInPuddleDepthFrontRight: " + api.getWheelInPuddleDepthFrontRight());
                    updateText(wheelInPuddleDepthRearLeft_TextView, "wheelInPuddleDepthRearLeft: " + api.getWheelInPuddleDepthRearLeft());
                    updateText(wheelInPuddleDepthRearRight_TextView, "wheelInPuddleDepthRearRight: " + api.getWheelInPuddleDepthRearRight());
                    updateText(surfaceRumbleFrontLeft_TextView, "surfaceRumbleFrontLeft: " + api.getSurfaceRumbleFrontLeft());
                    updateText(surfaceRumbleFrontRight_TextView, "surfaceRumbleFrontRight: " + api.getSurfaceRumbleFrontRight());
                    updateText(surfaceRumbleRearLeft_TextView, "surfaceRumbleRearLeft: " + api.getSurfaceRumbleRearLeft());
                    updateText(surfaceRumbleRearRight_TextView, "surfaceRumbleRearRight: " + api.getSurfaceRumbleRearRight());
                    updateText(tireSlipAngleFrontLeft_TextView, "tireSlipAngleFrontLeft: " + api.getTireSlipAngleFrontLeft()+"°");
                    updateText(tireSlipAngleFrontRight_TextView, "tireSlipAngleFrontRight: " + api.getTireSlipAngleFrontRight()+"°");
                    updateText(tireSlipAngleRearLeft_TextView, "tireSlipAngleRearLeft: " + api.getTireSlipAngleRearLeft()+"°");
                    updateText(tireSlipAngleRearRight_TextView, "tireSlipAngleRearRight: " + api.getTireSlipAngleRearRight()+"°");
                    updateText(tireCombinedSlipFrontLeft_TextView, "tireCombinedSlipFrontLeft: " + api.getTireCombinedSlipFrontLeft());
                    updateText(tireCombinedSlipFrontRight_TextView, "tireCombinedSlipFrontRight: " + api.getTireCombinedSlipFrontRight());
                    updateText(tireCombinedSlipRearLeft_TextView, "tireCombinedSlipRearLeft: " + api.getTireCombinedSlipRearLeft());
                    updateText(tireCombinedSlipRearRight_TextView, "tireCombinedSlipRearRight: " + api.getTireCombinedSlipRearRight());
                    updateText(suspensionTravelMetersFrontLeft_TextView, "suspensionTravelMetersFrontLeft: " + api.getSuspensionTravelMetersFrontLeft());
                    updateText(suspensionTravelMetersFrontRight_TextView, "suspensionTravelMetersFrontRight: " + api.getSuspensionTravelMetersFrontRight());
                    updateText(suspensionTravelMetersRearLeft_TextView, "suspensionTravelMetersRearLeft: " + api.getSuspensionTravelMetersRearLeft());
                    updateText(suspensionTravelMetersRearRight_TextView, "suspensionTravelMetersRearRight: " + api.getSuspensionTravelMetersRearRight());
                    updateText(carOrdinal_TextView, "carOrdinal: " + api.getCarOrdinal());
                    updateText(carClass_TextView, "carClass: " + api.getCarClass());
                    updateText(carPerformanceIndex_TextView, "carPerformanceIndex: " + api.getCarPerformanceIndex());
                    updateText(driveTrain_TextView, "driveTrain: " + api.getDrivetrain());
                    updateText(numberOfCylinders_TextView, "numberOfCylinders: " + api.getNumCylinders());
                    updateText(carCategory_TextView, "carCategory: " + api.getCarType());
                    updateText(unknown1_TextView, "unknown1: " + api.getUnknown1());
                    updateText(unknown2_TextView, "unknown2: " + api.getUnknown2());
                    updateText(positionX_TextView, "positionX: " + api.getPositionX());
                    updateText(positionY_TextView, "positionY: " + api.getPositionY());
                    updateText(positionZ_TextView, "positionZ: " + api.getPositionZ());
                    updateText(speedMps_TextView, "speed (m/s): " + api.getSpeedMps() + " m/s");
                    updateText(speedMph_TextView, "speed (mph): " + api.getSpeedMph() + "MPH");
                    updateText(speedKph_TextView, "speed (kph): " + api.getSpeedKph() + "KPH");
                    updateText(power_TextView, "power (watts): " + api.getPower());
                    updateText(horsepower_TextView, "horsepower: " + api.getHorsePower());
                    updateText(torque_TextView, "torque: " + api.getTorque());
                    updateText(tireTempFrontLeft_TextView, "tireTempFrontLeft: " + api.getTireTempFrontLeft()+"°F");
                    updateText(tireTempFrontRight_TextView, "tireTempFrontRight: " + api.getTireTempFrontRight()+"°F");
                    updateText(tireTempRearLeft_TextView, "tireTempRearLeft: " + api.getTireTempRearLeft()+"°F");
                    updateText(tireTempRearRight_TextView, "tireTempRearRight: " + api.getTireTempRearRight()+"°F");
                    updateText(tireTempAverageFront_TextView, "*tireTempAverageFront: " + api.getTireTempAverageFront()+"°F");
                    updateText(tireTempAverageRear_TextView, "*tireTempAverageRear: " + api.getTireTempAverageRear()+"°F");
                    updateText(tireTempAverageTotal_TextView, "*tireTempAverageTotal: " + api.getTireTempAverageTotal()+"°F");
                    updateText(boost_TextView, "boost: " + api.getBoost());
                    updateText(fuel_TextView, "fuel: " + api.getFuel()+"%");
                    updateText(distanceTraveled_TextView, "distanceTraveled: " + api.getDistanceTraveled());
                    updateText(bestLap_TextView, "bestLap: " + api.getBestLap());
                    updateText(lastLap_TextView, "lastLap: " + api.getLastLap());
                    updateText(currentLap_TextView, "currentLap: " + api.getCurrentLap());
                    updateText(currentRaceTime_TextView, "currentRaceTime: " + api.getCurrentRaceTime());
                    updateText(lapNumber_TextView, "lapNumber: " + api.getLapNumber());
                    updateText(racePosition_TextView, "racePosition: " + api.getRacePosition());
                    updateText(throttle_TextView, "throttle: " + api.getThrottle());
                    updateText(brake_TextView, "brake: " + api.getBrake());
                    updateText(clutch_TextView, "clutch: " + api.getClutch());
                    updateText(handbrake_TextView, "handbrake: " + api.getHandbrake());
                    updateText(gear_TextView, "gear: " + api.getGear());
                    updateText(steer_TextView, "steer: " + api.getSteer());
                    updateText(normalizedDrivingLine_TextView, "normalizedDrivingLine: " + api.getNormalizedDrivingLine());
                    updateText(normalizedAIBrakeDifference_TextView, "normalizedAiBrakeDifference: " + api.getNormalizedAIBrakeDifference());
                });
                receive = new byte[323];
            }
        });

        int port = Integer.parseInt(portNumber.getText().toString());
        if (port == 0 || port > 65535) {
            Toast.makeText(this, "Type in a port from 0 to 65535", Toast.LENGTH_SHORT).show();
        } else {
            udpSendThread.start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }
        return true;
    }

    public void init(){

        isRaceOn_TextView = (TextView) findViewById(R.id.isRaceOn_id);
        timestamp_TextView = (TextView) findViewById(R.id.timestamp_id);
        engineMaxRPM_TextView = (TextView) findViewById(R.id.engineMaxRPM_id);
        engineIdleRPM_TextView = (TextView) findViewById(R.id.engineIdleRPM_id);
        engineCurrentRPM_TextView = (TextView) findViewById(R.id.engineCurrentRPM_id);
        accelerationX_TextView = (TextView) findViewById(R.id.accelerationX_id);
        accelerationY_TextView = (TextView) findViewById(R.id.accelerationY_id);
        accelerationZ_TextView = (TextView) findViewById(R.id.accelerationZ_id);
        velocityX_TextView = (TextView) findViewById(R.id.velocityX_id);
        velocityY_TextView = (TextView) findViewById(R.id.velocityY_id);
        velocityZ_TextView = (TextView) findViewById(R.id.velocityZ_id);
        velocity_TextView = (TextView) findViewById(R.id.averageVelocity_id);
        angularVelocityX_TextView = (TextView) findViewById(R.id.angularVelocityX_id);
        angularVelocityY_TextView = (TextView) findViewById(R.id.angularVelocityY_id);
        angularVelocityZ_TextView = (TextView) findViewById(R.id.angularVelocityZ_id);
        yaw_TextView = (TextView) findViewById(R.id.yaw_id);
        pitch_TextView = (TextView) findViewById(R.id.pitch_id);
        roll_TextView = (TextView) findViewById(R.id.roll_id);
        normalizedSuspensionTravelFrontLeft_TextView = (TextView) findViewById(R.id.normalizedSuspensionTravelFrontLeft_id);
        normalizedSuspensionTravelFrontRight_TextView = (TextView) findViewById(R.id.normalizedSuspensionTravelFrontRight_id);
        normalizedSuspensionTravelRearLeft_TextView = (TextView) findViewById(R.id.normalizedSuspensionTravelRearLeft_id);
        normalizedSuspensionTravelRearRight_TextView = (TextView) findViewById(R.id.normalizedSuspensionTravelRearRight_id);
        tireSlipRatioFrontLeft_TextView = (TextView) findViewById(R.id.tireSlipRatioFrontLeft_id);
        tireSlipRatioFrontRight_TextView = (TextView) findViewById(R.id.tireSlipRatioFrontRight_id);
        tireSlipRatioRearLeft_TextView = (TextView) findViewById(R.id.tireSlipRatioRearLeft_id);
        tireSlipRatioRearRight_TextView = (TextView) findViewById(R.id.tireSlipRatioRearRight_id);
        wheelRotationSpeedFrontLeft_TextView = (TextView) findViewById(R.id.wheelRotationSpeedFrontLeft_id);
        wheelRotationSpeedFrontRight_TextView = (TextView) findViewById(R.id.wheelRotationSpeedFrontRight_id);
        wheelRotationSpeedRearLeft_TextView = (TextView) findViewById(R.id.wheelRotationSpeedRearLeft_id);
        wheelRotationSpeedRearRight_TextView = (TextView) findViewById(R.id.wheelRotationSpeedRearRight_id);
        wheelOnRumbleStripFrontLeft_TextView = (TextView) findViewById(R.id.wheelOnRumbleStripFrontLeft_id);
        wheelOnRumbleStripFrontRight_TextView = (TextView) findViewById(R.id.wheelOnRumbleStripFrontRight_id);
        wheelOnRumbleStripRearLeft_TextView = (TextView) findViewById(R.id.wheelOnRumbleStripRearLeft_id);
        wheelOnRumbleStripRearRight_TextView = (TextView) findViewById(R.id.wheelOnRumbleStripRearRight_id);
        wheelInPuddleDepthFrontLeft_TextView = (TextView) findViewById(R.id.wheelInPuddleDepthFrontLeft_id);
        wheelInPuddleDepthFrontRight_TextView = (TextView) findViewById(R.id.wheelInPuddleDepthFrontRight_id);
        wheelInPuddleDepthRearLeft_TextView = (TextView) findViewById(R.id.wheelInPuddleDepthRearLeft_id);
        wheelInPuddleDepthRearRight_TextView = (TextView) findViewById(R.id.wheelInPuddleDepthRearRight_id);
        surfaceRumbleFrontLeft_TextView = (TextView) findViewById(R.id.surfaceRumbleFrontLeft_id);
        surfaceRumbleFrontRight_TextView = (TextView) findViewById(R.id.surfaceRumbleFrontRight_id);
        surfaceRumbleRearLeft_TextView = (TextView) findViewById(R.id.surfaceRumbleRearLeft_id);
        surfaceRumbleRearRight_TextView = (TextView) findViewById(R.id.surfaceRumbleRearRight_id);
        tireSlipAngleFrontLeft_TextView = (TextView) findViewById(R.id.tireSlipAngleFrontLeft_id);
        tireSlipAngleFrontRight_TextView = (TextView) findViewById(R.id.tireSlipAngleFrontRight_id);
        tireSlipAngleRearLeft_TextView = (TextView) findViewById(R.id.tireSlipAngleRearLeft_id);
        tireSlipAngleRearRight_TextView = (TextView) findViewById(R.id.tireSlipAngleRearRight_id);
        tireCombinedSlipFrontLeft_TextView = (TextView) findViewById(R.id.tireCombinedSlipFrontLeft_id);
        tireCombinedSlipFrontRight_TextView = (TextView) findViewById(R.id.tireCombinedSlipFrontRight_id);
        tireCombinedSlipRearLeft_TextView = (TextView) findViewById(R.id.tireCombinedSlipRearLeft_id);
        tireCombinedSlipRearRight_TextView = (TextView) findViewById(R.id.tireCombinedSlipRearRight_id);
        suspensionTravelMetersFrontLeft_TextView = (TextView) findViewById(R.id.suspensionTravelMetersFrontLeft_id);
        suspensionTravelMetersFrontRight_TextView = (TextView) findViewById(R.id.suspensionTravelMetersFrontRight_id);
        suspensionTravelMetersRearLeft_TextView = (TextView) findViewById(R.id.suspensionTravelMetersRearLeft_id);
        suspensionTravelMetersRearRight_TextView = (TextView) findViewById(R.id.suspensionTravelMetersRearRight_id);
        carOrdinal_TextView = (TextView) findViewById(R.id.carOrdinal_id);
        carClass_TextView = (TextView) findViewById(R.id.carClass_id);
        carPerformanceIndex_TextView = (TextView) findViewById(R.id.carPerformanceIndex_id);
        driveTrain_TextView = (TextView) findViewById(R.id.driveTrain_id);
        numberOfCylinders_TextView = (TextView) findViewById(R.id.numberOfCylinders_id);
        carCategory_TextView = (TextView) findViewById(R.id.carCategory_id);
        unknown1_TextView = (TextView) findViewById(R.id.unknown1_id);
        unknown2_TextView = (TextView) findViewById(R.id.unknown2_id);
        positionX_TextView = (TextView) findViewById(R.id.positionX_id);
        positionY_TextView = (TextView) findViewById(R.id.positionY_id);
        positionZ_TextView = (TextView) findViewById(R.id.positionZ_id);
        speedMps_TextView = (TextView) findViewById(R.id.speedMps_id);
        speedMph_TextView = (TextView) findViewById(R.id.speedMph_id);
        speedKph_TextView = (TextView) findViewById(R.id.speedKph_id);
        power_TextView = (TextView) findViewById(R.id.power_id);
        horsepower_TextView = (TextView) findViewById(R.id.horsepower_id);
        torque_TextView = (TextView) findViewById(R.id.torque_id);
        tireTempFrontLeft_TextView = (TextView) findViewById(R.id.tireTempFrontLeft_id);
        tireTempFrontRight_TextView = (TextView) findViewById(R.id.tireTempFrontRight_id);
        tireTempRearLeft_TextView = (TextView) findViewById(R.id.tireTempRearLeft_id);
        tireTempRearRight_TextView = (TextView) findViewById(R.id.tireTempRearRight_id);
        tireTempAverageFront_TextView = (TextView) findViewById(R.id.tireTempAverageFront_id);
        tireTempAverageRear_TextView = (TextView) findViewById(R.id.tireTempAverageRear_id);
        tireTempAverageTotal_TextView = (TextView) findViewById(R.id.tireTempAverageTotal_id);
        boost_TextView = (TextView) findViewById(R.id.boost_id);
        fuel_TextView = (TextView) findViewById(R.id.fuel_id);
        distanceTraveled_TextView = (TextView) findViewById(R.id.distanceTraveled_id);
        bestLap_TextView = (TextView) findViewById(R.id.bestLap_id);
        lastLap_TextView = (TextView) findViewById(R.id.lastLap_id);
        currentLap_TextView = (TextView) findViewById(R.id.currentLap_id);
        currentRaceTime_TextView = (TextView) findViewById(R.id.currentRaceTime_id);
        lapNumber_TextView = (TextView) findViewById(R.id.lapNumber_id);
        racePosition_TextView = (TextView) findViewById(R.id.racePosition_id);
        throttle_TextView = (TextView) findViewById(R.id.throttle_id);
        brake_TextView = (TextView) findViewById(R.id.brake_id);
        clutch_TextView = (TextView) findViewById(R.id.clutch_id);
        handbrake_TextView = (TextView) findViewById(R.id.handbrake_id);
        gear_TextView = (TextView) findViewById(R.id.gear_id);
        steer_TextView = (TextView) findViewById(R.id.steer_id);
        normalizedDrivingLine_TextView = (TextView) findViewById(R.id.normalizedDrivingLine_id);
        normalizedAIBrakeDifference_TextView = (TextView) findViewById(R.id.normalizedAIBrakeDifference_id);
        connect_Button = (Button) findViewById(R.id.connect_button);
        engine_rpm_SeekBar = (SeekBar) findViewById(R.id.engine_rpm_SeekBar_id);
    }

    public void updateText(final TextView tv, final String text) {
        tv.setText(text);
    }

    public void setButtonText(final String s) {
        runOnUiThread(() -> connect_Button.setText(s));
    }

    public void toast(final String s) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show());
    }

    public String getDeviceIp() {
        WifiManager wm = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }

    public int getViewPref(String pref_name) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean(pref_name, true)) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    public boolean getBool(String pref_name) {
        return PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(pref_name, false);
    }
}
