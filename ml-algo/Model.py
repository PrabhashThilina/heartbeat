import pandas as pd
from sklearn.discriminant_analysis import StandardScaler
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeClassifier
from sklearn.metrics import accuracy_score
import joblib

# Load and preprocess data
data = pd.read_csv('CardiacPatientData.csv')
X = data[["HR", "BT", "Age", "Smoke", "FHCD"]]
y = data["Outcome"]  # Replace "target" with your actual target column

# Standardize the features
scaler = StandardScaler()
X_standardized = scaler.fit_transform(X)

# Split data
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Train model using Decision Tree
model = DecisionTreeClassifier(random_state=42)
model.fit(X_train, y_train)

# Evaluate
predictions = model.predict(X_test)
print(X_standardized)
print("Accuracy:", accuracy_score(y_test, predictions))

joblib.dump(model, 'model.pkl')
print("Model saved as 'model.pkl'")
