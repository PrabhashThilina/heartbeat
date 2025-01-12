import paho.mqtt.client as mqtt

# MQTT Broker Configuration
BROKER = "127.0.0.1"  # Localhost since Mosquitto is running on the same Pi
PORT = 1883
TOPIC = "health_monitor/data"  # Topic that the Android app publishes to

# Callback function when connected to the broker
def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("Connected to MQTT Broker!")
        client.subscribe(TOPIC)
        print(f"Subscribed to topic: {TOPIC}")
    else:
        print(f"Failed to connect, return code {rc}")

# Callback function when a message is received
def on_message(client, userdata, msg):
    print(f"Message received on topic {msg.topic}: {msg.payload.decode()}")
    # Handle the data (JSON parsing, logging, or triggering an action)
    try:
        data = msg.payload.decode()
        print(f"Processed Data: {data}")
        # You can add code here to save data or perform an action
    except Exception as e:
        print(f"Error processing message: {e}")

# Initialize MQTT Client
client = mqtt.Client()

# Set the callback functions
client.on_connect = on_connect
client.on_message = on_message

# Connect to the MQTT broker
print(f"Connecting to broker at {BROKER}:{PORT}...")
client.connect(BROKER, PORT, 60)

# Start the loop to process callbacks and keep the connection alive
print("Waiting for messages...")
client.loop_forever()