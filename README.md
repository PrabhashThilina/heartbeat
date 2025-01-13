# Heartbeat: Health Monitoring System

Heartbeat is an innovative health monitoring system that tracks heart rate and body temperature in real time. It leverages machine learning algorithms to detect potential heart issues by identifying irregularities and alerts healthcare providers for proactive intervention.

---

## Project Structure

This project is organized into three main components:

1. **android-app**: Contains the source code for the mobile application. The app serves as the user interface, displaying real-time health data and notifications.

2. **iot-code**: Includes the firmware for IoT devices that collect and transmit health data, such as heart rate and body temperature sensors.

3. **ml-algo**: Implements machine learning algorithms for detecting irregularities in heart rate and body temperature.

---

## Features

- **Real-time Monitoring**: Tracks heart rate and body temperature with high precision.
- **Machine Learning Integration**: Utilizes advanced algorithms to identify potential health risks.
- **Alerts & Notifications**: Sends timely alerts to healthcare providers for proactive intervention.
- **Cross-Platform Compatibility**: Seamless integration between IoT devices and mobile applications.

---

## Installation and Usage

### Steps to Setup

1. Clone the repository:
   ```bash
   git clone [Your GitHub Repository Link]
   ```

2. Navigate to the respective folders for setup:
   - `android-app`: Open the folder in Android Studio, build, and run the application.
   - `iot-code`: Deploy the firmware to the IoT hardware using Arduino IDE.
   - `ml-algo`: Install dependencies and run the machine learning models.

3. Connect the IoT devices to the mobile application following the app's pairing instructions.

---

## Dataset

The machine learning algorithms rely on a dataset for training and testing. The dataset includes samples of heart rate and body temperature data collected from real-world use cases.

### Dataset Access

[Dataset Link](https://zenodo.org/records/7603772)

(Replace `#` with the actual link to your dataset.)

### Usage

1. Download the dataset from the provided link.
2. Place the dataset in the `ml-algo` folder.
3. Update the script configuration to point to the dataset location.

---

## Repository Link

[GitHub Repository](https://github.com/PrabhashThilina/heartbeat.git)

