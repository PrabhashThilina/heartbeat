import time
import requests
import Adafruit_GPIO.SPI as SPI
import Adafruit_MCP3008
import paho.mqtt.client as mqtt

# Hardware SPI configuration
SPI_PORT = 0
SPI_DEVICE = 0
mcp = Adafruit_MCP3008.MCP3008(spi=SPI.SpiDev(SPI_PORT, SPI_DEVICE))

# MQTT Configuration
BROKER = "127.0.0.1"   # Replace with your MQTT broker address (e.g., localhost or an IP address)
PORT = 1883
TOPIC = "sensor/heart_rate"

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


# Reading the MCP3008 analog input in channel 0
print('Reading MCP3008 values, press Ctrl-C to quit...')
ADC0 = ('| {0:>4} |'.format(*range(1)))

print('Reading MCP3008 values and publishing to MQTT, press Ctrl-C to quit...')

# Main program loop
try:
    while True:
        # Read the 0th ADC channel values
        values = [0] * 1
        for i in range(1):
            # Read the value of the specified channel (0)
            values[i] = mcp.read_adc(i)
        # Extract and process the raw value
        raw_value = int(values[0])
        adjusted_value = raw_value - 966

	# Fetch API value
        api_value = fetchAPIValue()

        # Print the values to the console
        print(f"Value From Channel {0:>4}: ADC Raw = {abs(raw_value)} | ADC Adjusted = {abs(adjusted_value)} | API Value = {api_value}")
 
        # Publish the raw and adjusted values to MQTT
        client.publish(TOPIC, f"{raw_value},{abs(adjusted_value)},{api_value} ")
        print(f"Published to MQTT: Raw: {raw_value}, Adjusted: {abs(adjusted_value)}")

	# Increment the index
        APIIndex += 1

        # Pause for half a second
        time.sleep(0.5)

except KeyboardInterrupt:
    print("\nProgram interrupted! Disconnecting MQTT...")
    client.disconnect()
    print("MQTT disconnected. Exiting...")
