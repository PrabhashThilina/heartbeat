# Reading the MCP3008 analog input in channel 0

import time

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
    adjustedValue = rawValue - 935
    print('Value From Channel ', ADC0, '= ', abs(rawValue), ' | ', abs(adjustedValue))
    
    # Pause for half a second.
    time.sleep(0.5)
