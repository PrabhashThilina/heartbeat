package com.example.mqtt1;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

import java.time.LocalTime;

public class MainActivity extends AppCompatActivity {
    private MqttAndroidClient client;
    private static final String SERVER_URI = "tcp://192.168.1.9:1883";
    private static final String TAG = "MainActivity";
    private TextView txv_rgb;
    private TextView txt_bodyTemp;

    private LineGraphSeries<DataPoint> pulseSeries;
    private LineGraphSeries<DataPoint> tempSeries;
    private double lastXValue = 0;
    private double heartRate = 100;
    private double bodyTemp = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        // Delay and switch to the main content layout
        new Handler().postDelayed(() -> {

            setContentView(R.layout.activity_login);

            MaterialButton loginButton = findViewById(R.id.loginButton);
            EditText usernameField = findViewById(R.id.usernameField);
            EditText passwordField = findViewById(R.id.passwordField);

            loginButton.setOnClickListener(v -> {
                String username = usernameField.getText().toString();
                String password = passwordField.getText().toString();

                if (username.equals("admin") && password.equals("1234")) {
                    // Step 3: Transition to Main App Layout
                    loadData();
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            });
        }, 3000);
    }

    private void loadData(){

        setContentView(R.layout.activity_data);

        MaterialButton submitButton = findViewById(R.id.submitButton);
        EditText nameField = findViewById(R.id.nameField);
        EditText ageField = findViewById(R.id.ageField);
        EditText sysBPField = findViewById(R.id.sysBPField);
        EditText sodiumField = findViewById(R.id.sodiumField);
        EditText creatinineField = findViewById(R.id.creatinineField);
        EditText potasiumField = findViewById(R.id.potassiumField);
        EditText chlorideField = findViewById(R.id.chlorideField);

        submitButton.setOnClickListener(v -> {
            String name = nameField.getText().toString();
            String age = ageField.getText().toString();
            String sysBP = sysBPField.getText().toString();
            String sodium = sodiumField.getText().toString();
            String creatinine = creatinineField.getText().toString();
            String potasium = potasiumField.getText().toString();
            String chloride = chlorideField.getText().toString();

            boolean isComplete = !name.isEmpty() && !age.isEmpty() && !sysBP.isEmpty() && !sodium.isEmpty()
                    && !creatinine.isEmpty() && !potasium.isEmpty() && !chloride.isEmpty();

            if (isComplete) {
                // Proceed to the dashboard
                loadDashboard();
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void loadDashboard(){

        setContentView(R.layout.activity_main);

        txv_rgb = (TextView) findViewById(R.id.txv_rgbValue);
        txt_bodyTemp = (TextView) findViewById(R.id.txv_tempValue);

        //////////////////////
        // Initialize GraphView
        GraphView graph_heartPulse = findViewById(R.id.graph_heartPulse);
        pulseSeries = new LineGraphSeries<>();
        pulseSeries.setThickness(2);
        //pulseSeries.setColor(4);
        graph_heartPulse.addSeries(pulseSeries);

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
        //.........................
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

        System.out.println("Before connect");

        connect();

        updateHearBeta();

        client.setCallback(new MqttCallbackExtended() {
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
                txv_rgb.setText("         " + values[1]);
                txt_bodyTemp.setText("          " + values[2]);
                updateHearBeta();

        /* add code here to interact with elements
        (text views, buttons)
        using data from newMessage
         */

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
    private void connect(){
        System.out.println("In connect");

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), SERVER_URI, clientId);
        try {
            System.out.println("Try connect");
            System.out.println(SERVER_URI);
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
// We are connected
                    Log.d(TAG, "onSuccess");
                    System.out.println(TAG + " Success. Connected to " + SERVER_URI);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
// Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                    System.out.println(TAG + " Oh no! Failed to connect to " + SERVER_URI);
                }
            });
        } catch (MqttException e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
    }

    private void subscribe(String topicToSubscribe) {
        final String topic = topicToSubscribe;
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Subscription successful to topic: " + topic);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Failed to subscribe to topic: " + topic);
// The subscription could not be performed, maybe the user was not
// authorized to subscribe on the specified topic e.g. using wildcards
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void updateHearBeta(){
        new Thread(() -> {
            while (true) {
                runOnUiThread(() -> {
                    lastXValue += 1;
                    pulseSeries.appendData(new DataPoint(lastXValue, heartRate), true, 50);
                    tempSeries.appendData(new DataPoint(lastXValue, bodyTemp), true, 50);
                });

                try {
                    Thread.sleep(1000000); // Update every second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}

