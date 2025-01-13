import time
import requests
import joblib
import numpy as np
import pandas as pd

# Import SPI library (for hardware SPI) and MCP3008 library
import Adafruit_GPIO.SPI as SPI
import Adafruit_MCP3008

import paho.mqtt.client as mqtt

# Hardware SPI configuration
SPI_PORT = 0
SPI_DEVICE = 0
mcp = Adafruit_MCP3008.MCP3008(spi=SPI.SpiDev(SPI_PORT, SPI_DEVICE))

# MQTT Configuration
BROKER = "127.0.0.1"   # Replace with your MQTT broker address 
PORT = 1883
TOPIC = "sensor/heart_rate"  # Topic that the RaspberryPi publishes to

# Initialize MQTT Client
client = mqtt.Client()
client.connect(BROKER, PORT, 60)

print('Reading temerature  values from API')
API_URL = "http://127.0.0.1:5000/api/data/BT"
APIIndex = 0


# Fetch Body Tempreature from API
def fetchAPIValue():
    try:
        response = requests.get(API_URL)
        response.raise_for_status()
        data = response.json()  # Assuming the API returns a JSON array of v>
        if 'BT' in data:
            return data['BT'][APIIndex] if len(data['BT']) > APIIndex else 'N/A'
        return 'N/A'
    except requests.exceptions.RequestException as e:
        print(f"Error fetching data from API: {e}")
        return 'N/A'


# Predict Health Status 
def predictHealthStatus(HR, BT, Age, Smoke, FHCD):
    # Load the trained model
    model = joblib.load('../../ml-algo/model.pkl')

    # Input data for prediction (raw values, no standardization)
    input_data = np.array([[HR, BT, Age, Smoke, FHCD]])  # HR, BT, Age, Smoke, FHCD

    # Make prediction
    outcome = model.predict(input_data)
    return outcome


# Reading the MCP3008 analog input in channel 0
print('Reading MCP3008 values, press Ctrl-C to quit...')
ADC0 = ('| {0:>4} |'.format(*range(1)))

print('Reading MCP3008 values and publishing to MQTT, press Ctrl-C to quit...')

# Main program loop
try:
    while True:
        # Read the 0th ADC channel values
        values = [0]*1
        for i in range(1):
            # The read_adc function will get the value of the specified channel (0)
            values[i] = mcp.read_adc(i)
        # Print the ADC values.
        rawValue = ('{0:>4}'.format(*values))
        rawValue = int(rawValue)
        adjustedValue = rawValue - 966 # Raw Value
        
        # Fetch API value
        APIValues = fetchAPIValue()

        # Load CSV data (e.g., patient data with Age, Smoke, FHCD columns)
        CSV_FILE = "../datasets/HealthMonitorPatientData.csv"
        csv_data = pd.read_csv(CSV_FILE)

        # Get Age, Smoke, FHCD values from CSV (assuming single-row file for simplicity)
        # Extract and process Age, Smoke, and FHCD values from the CSV
        age = int(csv_data.iloc[0]['Age']) if 'Age' in csv_data.columns else 0
        smoke = 1 if csv_data.iloc[0]['Smoke'].strip().lower() == "yes" else 0 if csv_data.iloc[0]['Smoke'].strip().lower() == "no" else None
        fhcd = 1 if csv_data.iloc[0]['FHCD'].strip().lower() == "yes" else 0 if csv_data.iloc[0]['FHCD'].strip().lower() == "no" else None

        # Health Status 
        healthStatus = predictHealthStatus(adjustedValue, APIValues, age, smoke, fhcd)

        # Display results
        print(f"Value From Channel {0:>4}: ADC Raw = {abs(rawValue)} | ADC Adjusted = {abs(adjustedValue)} | Body Temperature = {APIValues} | Age = {age} | Smoke = {smoke} | FHCD = {fhcd} | Health Status = {healthStatus}")
    
        # Publish the raw and adjusted values to MQTT
        client.publish(TOPIC, f"{rawValue},{abs(adjustedValue)},{APIValues},{healthStatus} ")
        print(f"Published to MQTT: Raw: {rawValue}, Adjusted: {abs(adjustedValue)}, Health Status: {healthStatus}")

	    # Increment the index
        APIIndex += 1

        # Pause for half a second.
        time.sleep(0.5)

except KeyboardInterrupt:
    print("\nProgram interrupted! Disconnecting MQTT...")
    client.disconnect()
    print("MQTT disconnected. Exiting...")
