import time
import requests
import joblib
import numpy as np

# Import SPI library (for hardware SPI) and MCP3008 library
import Adafruit_GPIO.SPI as SPI
import Adafruit_MCP3008

# Software SPI configuration:
# CLK  = 18
# MISO = 23
# MOSI = 24
# CS   = 25
# mcp = Adafruit_MCP3008.MCP3008(clk=CLK, cs=CS, miso=MISO, mosi=MOSI)

# Hardware SPI configuration:
SPI_PORT   = 0
SPI_DEVICE = 0
mcp = Adafruit_MCP3008.MCP3008(spi=SPI.SpiDev(SPI_PORT, SPI_DEVICE))

rawValue = 0 
adjustedValue = 0
previousValue = 0
ADC0 = 0

API_URL = "http://127.0.0.1:5000/api/data/BT"
APIIndex = 0


# Fetch Body Tempreature from API
def fetchAPIValue():
    try:
        response = requests.get(API_URL)
        response.raise_for_status()
        data = response.json()  # Assuming the API returns a JSON array of values
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
    while True:
        # Input data for prediction (raw values, no standardization)
        input_data = np.array([[HR, BT, Age, Smoke, FHCD]])  # HR, BT, Age, Smoke, FHCD

        # Make prediction
        outcome = model.predict(input_data)
        return outcome

        # Wait for 30 seconds before the next prediction
        # time.sleep(30)


# Reading the MCP3008 analog input in channel 0
print('Reading MCP3008 values, press Ctrl-C to quit...')
ADC0 = ('| {0:>4} |'.format(*range(1)))

# Main program loop
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

    # Health Status 
    healthStatus = predictHealthStatus(62, 92, 55, 1, 0)
    
    # Display results
    print(f"Value From Channel {0:>4}: ADC Raw = {abs(rawValue)} | ADC Adjusted = {abs(adjustedValue)} | API Value = {APIValues} | Health Status = {healthStatus}")
    
    # Increment the index
    APIIndex += 1

    # Pause for half a second.
    time.sleep(0.5)
