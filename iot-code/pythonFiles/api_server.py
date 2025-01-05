from flask import Flask, jsonify, request
import pandas as pd

app = Flask(__name__)

# Load the CSV file
data = pd.read_csv('../datasets/CardiacPatientData.csv')

# Endpoint to get all data
@app.route('/api/data', methods=['GET'])
def get_all_data():
    return jsonify(data.to_dict(orient='records'))

# Endpoint to get data from a specific column
@app.route('/api/data/<column_name>', methods=['GET'])
def get_column_data(column_name):
    if column_name not in data.columns:
        return jsonify({"error": f"Column '{column_name}' not found"}), 404

    # Extract the specific column
    column_data = data[column_name].dropna().tolist()
    return jsonify({column_name: column_data})

if __name__ == '__main__':
    app.run(debug=True)
