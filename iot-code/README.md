# Health Monitoring System

This repository contains a Python-based health monitoring system leveraging APIs, MQTT, and machine learning to process and predict patient health status based on sensor inputs and stored data.

## Features

- API endpoints to fetch patient data from a CSV file.
- MQTT-based communication to receive and publish health data.
- Integration with a machine learning model to predict health status.
- Sensor data acquisition using the MCP3008 ADC.
- Real-time processing and storage of patient information.

## File Descriptions

### 1. `api_server.py`
A Flask-based API server that provides endpoints to access patient data stored in a CSV file.

- **Endpoints**:
  - `/api/data`: Returns all patient data.
  - `/api/data/<column_name>`: Returns data from a specific column.
- **Usage**: Run `python api_server.py` to start the server.

### 2. `patientDataMqtt.py`
An MQTT client to receive health data from a mobile app and store it in a CSV file.

- Connects to an MQTT broker and subscribes to the topic `health_monitor/data`.
- Processes JSON messages and writes data to `HealthMonitorPatientData.csv`.
- **Usage**: Run `python patientDataMqtt.py` to start the MQTT client.

### 3. `sensorValuesMqtt.py`
Acquires sensor data, predicts health status using a machine learning model, and publishes results via MQTT.

- Reads analog values from MCP3008 ADC.
- Fetches additional data from the API server.
- Predicts health status using a pre-trained ML model (`model.pkl`).
- Publishes results to the topic `sensor/heart_rate`.
- **Usage**: Run `python sensorValuesMqtt.py` to start the script.

### 4. Auto-Run Configuration Files

#### `on_reboot.sh`
This script ensures that the necessary Python scripts run automatically on Raspberry Pi boot.

- **Path**: Place `on_reboot.sh` in the following directory:
  ```
  /home/pi/on_reboot.sh
  ```
- Make the script executable:
  ```bash
  chmod +x /home/pi/on_reboot.sh
  ```

#### Autostart Configuration

1. Create an `autostart` file inside the `.config` directory:
   ```bash
   mkdir -p /home/pi/.config/autostart
   ```

2. Add `autostart.desktop` to the `autostart` directory with the following contents:
   ```
   [Desktop Entry]
   
   Exec=sudo bash /home/pi/on_reboot.sh

   ```

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/PrabhashThilina/heartbeat.git
   ```

2. Set up the Python environment:
   ```bash
   python3 -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

3. Ensure the following dependencies are installed:
   - Flask
   - pandas
   - paho-mqtt
   - joblib
   - numpy
   - Adafruit-GPIO

4. Place the necessary dataset files in the `datasets` directory:
   - `CardiacPatientData.csv`

5. Ensure the ML model file `model.pkl` is placed in the correct directory (`ml-algo/`).

6. Configure the auto-run files as described above.

## Usage

1. **Start the API Server:**
   ```bash
   python api_server.py
   ```

2. **Run the MQTT Client for Patient Data:**
   ```bash
   python patientDataMqtt.py
   ```

3. **Start the Sensor Data Processing Script:**
   ```bash
   python sensorValuesMqtt.py
   ```

