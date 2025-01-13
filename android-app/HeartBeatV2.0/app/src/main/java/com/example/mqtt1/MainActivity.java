package com.example.mqtt1;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    // Activity name
    private static final String TAG = "MainActivity";

    // MQTT client and broker details
    private MqttAndroidClient mqttClient;
    private static final String SERVER_URI = "tcp://192.168.1.9:1883";

    // UI components
    private LineGraphSeries<DataPoint> heartPulseGraphSeries;
    private LineGraphSeries<DataPoint> tempSeries;

    // Graph data
    private double graphLastX = 0;
    private double heartRate = 100;
    private double bodyTemp = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);// Edge to edge content rendering
        setContentView(R.layout.activity_splash);//Display splash screen

        // Switch to login page with a splash screen
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        new Handler().postDelayed(() -> {

            setContentView(R.layout.activity_login);

            // Bind login page UI components
            MaterialButton loginButton = findViewById(R.id.loginButton);
            EditText usernameField = findViewById(R.id.usernameField);
            EditText passwordField = findViewById(R.id.passwordField);

            // Handle login button click
            loginButton.setOnClickListener(v -> {
                String username = usernameField.getText().toString();
                String password = passwordField.getText().toString();

                // Validate user credentials
                if (username.equals("admin") && password.equals("1234")) {
                    loadData();
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            });
        }, 3000);//Delay of three seconds
    }

    // Load patient data page
    private void loadData(){
        setContentView(R.layout.activity_data);// Display patient data page

        // Bind patient data UI components
        MaterialButton submitButton = findViewById(R.id.submitButton);
        EditText nameField = findViewById(R.id.nameField);
        EditText ageField = findViewById(R.id.ageField);
        RadioGroup fhihdRadioGroup = findViewById(R.id.fhihdRadioGroup);
        RadioGroup smokingRadioGroup = findViewById(R.id.smokingRadioGroup);

        connect();//Connect to MQTT broker

        // Handle patient data form submission
        submitButton.setOnClickListener(v -> {
            // Get input patient input data
            String name = nameField.getText().toString();
            String age = ageField.getText().toString();

            int fhihdSelectedId = fhihdRadioGroup.getCheckedRadioButtonId();
            RadioButton fhihdSelectedRadio = findViewById(fhihdSelectedId);
            String fhihdValue = fhihdSelectedRadio.getText().toString();

// Get selected value from Smoking RadioGroup
            int smokingSelectedId = smokingRadioGroup.getCheckedRadioButtonId();
            RadioButton smokingSelectedRadio = findViewById(smokingSelectedId);
            String smokingValue = smokingSelectedRadio.getText().toString();

            // Check whether all the patient data are filled
            boolean isComplete = !name.isEmpty() && !age.isEmpty();

            if (isComplete) {
                // JSON payload for MQTT publishing
                String data = String.format(
                        "{\"name\":\"%s\",\"Age\":\"%s\",\"Smoke\":\"%s\",\"FHCD\":\"%s\"}",
                        name, age, smokingValue, fhihdValue
                );

                publishData(data); // Publish data to MQTT
                Toast.makeText(this, "Data submitted and published!", Toast.LENGTH_SHORT).show();
                loadDashboard(); // Navigate to the dashboard
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Publish data to MQTT data topic
    private void publishData(String data) {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                MqttMessage message = new MqttMessage();
                message.setPayload(data.getBytes());
                message.setQos(1); // Set Quality of Service level
                mqttClient.publish("health_monitor/data", message); // Topic to publish
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "MQTT client is not connected", Toast.LENGTH_SHORT).show();
        }
    }

    // Load dashboard screen
    private void loadDashboard(){
        setContentView(R.layout.activity_main);// Set dashboard layout

        // Bind dashboard UI components
        TextView heartRateTextView = (TextView) findViewById(R.id.txv_pulseValue);
        TextView bodyTempTextView = (TextView) findViewById(R.id.txv_tempValue);
        TextView healthStatusTextView = (TextView) findViewById(R.id.txv_statusValue);

        // Initialize GraphView
        GraphView graph_heartPulse = findViewById(R.id.graph_heartPulse);
        heartPulseGraphSeries = new LineGraphSeries<>();
        heartPulseGraphSeries.setThickness(2);
        graph_heartPulse.addSeries(heartPulseGraphSeries);

        // Enable scrolling and scaling
        graph_heartPulse.getViewport().setScalable(true);
        graph_heartPulse.getViewport().setScrollable(true);

        // Optional: Set X and Y axis bounds
        graph_heartPulse.getViewport().setXAxisBoundsManual(true);
        graph_heartPulse.getViewport().setMinX(0);
        graph_heartPulse.getViewport().setMaxX(10);

        graph_heartPulse.getViewport().setYAxisBoundsManual(true);
        graph_heartPulse.getViewport().setMinY(0);
        graph_heartPulse.getViewport().setMaxY(200);

        GraphView graph_bodyTemp = findViewById(R.id.graph_bodyTemp);
        tempSeries = new LineGraphSeries<>();
        graph_bodyTemp.addSeries(tempSeries);
        graph_bodyTemp.getViewport().setScalable(true);
        graph_bodyTemp.getViewport().setScrollable(true);

        // Optional: Set X and Y axis bounds
        graph_bodyTemp.getViewport().setXAxisBoundsManual(true);
        graph_bodyTemp.getViewport().setMinX(0);
        graph_bodyTemp.getViewport().setMaxX(10);

        graph_bodyTemp.getViewport().setYAxisBoundsManual(true);
        graph_bodyTemp.getViewport().setMinY(90);
        graph_bodyTemp.getViewport().setMaxY(110);

        connect();// Ensure connection to MQTT broker

        updateSensorData();// Update heart rate and temperature data series

        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    System.out.println("Reconnected to : " + serverURI);
                    // Re-subscribe as we lost it due to new session
                    subscribe("sensor/heart_rate");
                } else {
                    System.out.println("Connected to: " + serverURI);
                    subscribe("sensor/heart_rate");
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws
                    Exception {
                String newMessage = new String(message.getPayload());
                System.out.println("Incoming message: " + newMessage);
                String[] values = newMessage.split(",");
                heartRate = Double.parseDouble(values[1]);
                bodyTemp = Double.parseDouble(values[2]);
                heartRateTextView.setText("         " + values[1]);
                bodyTempTextView.setText("          " + values[2]);
                String healthStatus = values[3];
                if(healthStatus.equals("[]"))
                    healthStatusTextView.setText("Normal");
                else{
                    healthStatusTextView.setText("Attention");
                }
                updateSensorData();//Update heart rate and temperature data series
           }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    // Establish MQTT connection
    private void connect(){
        String clientId = MqttClient.generateClientId();
        mqttClient = new MqttAndroidClient(this.getApplicationContext(), SERVER_URI, clientId);
        try {
            System.out.println("Try connect");
            System.out.println(SERVER_URI);
            IMqttToken token = mqttClient.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "onSuccess");
                    System.out.println(TAG + " Success. Connected to " + SERVER_URI);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "onFailure");
                    System.out.println(TAG + " Oh no! Failed to connect to " + SERVER_URI);
                }
            });
        } catch (MqttException e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
    }

    //Subscribe to the MQTT topic
    private void subscribe(String topicToSubscribe) {
        final String topic = topicToSubscribe;
        int qos = 1;
        try {
            IMqttToken subToken = mqttClient.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Subscription successful to topic: " + topic);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Failed to subscribe to topic: " + topic);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // Update heart rate and temperature data series
    private void updateSensorData(){
        new Thread(() -> {
            while (true) {
                runOnUiThread(() -> {
                    graphLastX += 1;
                    heartPulseGraphSeries.appendData(new DataPoint(graphLastX, heartRate), true, 50);
                    tempSeries.appendData(new DataPoint(graphLastX, bodyTemp), true, 50);
                });

                try {
                    Thread.sleep(5000); // Update every second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

