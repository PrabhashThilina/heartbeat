import csv
import time  # Import time module for sleep

# Path to CSV file
csv_file_path = "../datasets/CardiacPatientData.csv"

# Function to read and print "Blood Temperature"
def read_blood_temperature(csv_path):
    print("Blood Temperature:")
    try:
        with open(csv_path, mode="r", newline="") as file:
            reader = csv.DictReader(file)
            for row in reader:
                print(f"Blood Temperature: {row.get('BT', 'N/A')}")
                time.sleep(0.3)  # Add 0.3 second delay
    except Exception as e:
        print(f"Error reading Blood Temperature: {e}")

# Function to read and print "Heart Rate"
def read_heart_rate(csv_path):
    print("Heart Rate:")
    try:
        with open(csv_path, mode="r", newline="") as file:
            reader = csv.DictReader(file)
            for row in reader:
                print(f"Heart Rate: {row.get('HR', 'N/A')}")
                time.sleep(0.3)  # Add 0.3 second delay
    except Exception as e:
        print(f"Error reading Heart Rate: {e}")

# Function to read and print "Blood Temperature" and "Heart Rate"
def read_health_data(csv_path):
    try:
        with open(csv_path, mode="r", newline="") as file:
            reader = csv.DictReader(file)
            for row in reader:
                # Print one Blood Temperature and one Heart Rate
                blood_temp = row.get("BT", "N/A")
                heart_rate = row.get("HR", "N/A")
                print(f"Blood Temperature: {blood_temp} | Heart Rate: {heart_rate}")
                time.sleep(0.3)  # Add 0.3 second delay
    except Exception as e:
        print(f"Error reading data: {e}")

# Main function to call both readers
def main():
   # print("Reading from CSV File...\n")
   # read_blood_temperature(csv_file_path)
   # read_heart_rate(csv_file_path)
    read_health_data(csv_file_path)

# Run the script
if __name__ == "__main__":
    main()
