import paho.mqtt.client as mqtt
import json
import csv

# MQTT Broker Configuration
BROKER = "127.0.0.1"  # Localhost since Mosquitto is running on the same Pi
PORT = 1883
TOPIC = "health_monitor/data"  # Topic that the Android app publishes to

# CSV file path
CSV_FILE = "../datasets/HealthMonitorPatientData.csv"

# Function to write data to a CSV file
def write_to_csv(data):
    # Ensure data is a dictionary
    if isinstance(data, dict):
        # Get the keys (columns) and values
        keys = data.keys()
        values = data.values()

        # Write to CSV file (overwrite the row)
        with open(CSV_FILE, mode='w', newline='') as csv_file:
            writer = csv.DictWriter(csv_file, fieldnames=keys)
            writer.writeheader()  # Write the header
            writer.writerow(data)  # Write the single row
    else:
        print("Error: Data is not a valid dictionary.")

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
    try:
        # Parse the JSON data
        data = json.loads(msg.payload.decode())
        print(f"Processed Data: {data}")

        # Write the data to the CSV file
        write_to_csv(data)
        print(f"Data written to {CSV_FILE}")
    except json.JSONDecodeError:
        print("Error: Received message is not valid JSON.")
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