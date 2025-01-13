
#!/bin/bash
source /home/pi/myenv/bin/activate
cd /home/pi/heartbeat/iot-code/pythonFiles
python3 api_server.py & python3 sensorValuesMqtt.py & python3 patientDataMqtt.py
