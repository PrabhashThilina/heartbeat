import time
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
        adjusted_value = raw_value - 935

        # Print the values to the console
        print(f"Value From Channel 0: {raw_value} | Adjusted Value: {abs(adjusted_value)}")

        # Publish the raw and adjusted values to MQTT
        client.publish(TOPIC, f"Raw: {raw_value}, Adjusted: {abs(adjusted_value)}")
        print(f"Published to MQTT: Raw: {raw_value}, Adjusted: {abs(adjusted_value)}")

        # Pause for half a second
        time.sleep(0.5)

except KeyboardInterrupt:
    print("\nProgram interrupted! Disconnecting MQTT...")
    client.disconnect()
    print("MQTT disconnected. Exiting...")
