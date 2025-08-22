#!/usr/bin/env python3
"""
Parkinson's Disease Detection - Single Image Analysis
Analyzes a single image for PD detection using traditional ML
"""

import os
import cv2
import numpy as np
from skimage import feature
import sys
import time
import joblib

# Import required libraries
try:
    from imutils import paths
    from sklearn.preprocessing import LabelEncoder
    from sklearn.ensemble import RandomForestClassifier
    from xgboost import XGBClassifier
    from sklearn.metrics import confusion_matrix
except ImportError as e:
    print(f"Error: Required library not installed. {e}")
    print("Please install required libraries:")
    print("pip install imutils scikit-learn xgboost opencv-python scikit-image joblib")
    sys.exit(1)

# Global Configuration Variables
TRAINING_DATA_PATH = "data"  # Path to training dataset
MODEL_SAVE_PATH = "plots"  # Where to save/load trained models


def quantify_image(image):
    """Extract HOG features from an image."""
    features = feature.hog(image, orientations=9,
                           pixels_per_cell=(10, 10), cells_per_block=(2, 2),
                           transform_sqrt=True, block_norm="L1")
    return features


def preprocess_image(image_path):
    """Load and preprocess a single image."""
    if not os.path.exists(image_path):
        raise FileNotFoundError(f"Image not found: {image_path}")
    
    # Load image
    image = cv2.imread(image_path)
    if image is None:
        raise ValueError(f"Could not load image: {image_path}")
    
    # Convert to grayscale and resize
    image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    image = cv2.resize(image, (200, 200))
    
    # Threshold to make drawing white on black background
    image = cv2.threshold(image, 0, 255,
                          cv2.THRESH_BINARY_INV | cv2.THRESH_OTSU)[1]
    
    return image


def load_split(path):
    """Load and preprocess training data."""
    if not os.path.exists(path):
        print(f"Warning: Path {path} does not exist")
        return np.array([]), np.array([])
    
    imagePaths = list(paths.list_images(path))
    if not imagePaths:
        print(f"Warning: No images found in {path}")
        return np.array([]), np.array([])
    
    data = []
    labels = []
    
    for imagePath in imagePaths:
        try:
            label = imagePath.split(os.path.sep)[-2]
            
            image = cv2.imread(imagePath)
            if image is None:
                continue
                
            image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
            image = cv2.resize(image, (200, 200))
            image = cv2.threshold(image, 0, 255,
                                  cv2.THRESH_BINARY_INV | cv2.THRESH_OTSU)[1]
            
            features = quantify_image(image)
            data.append(features)
            labels.append(label)
            
        except Exception as e:
            print(f"Error processing {imagePath}: {e}")
            continue
    
    return (np.array(data), np.array(labels))


def train_and_save_models():
    """Train models and save them for later use."""
    print("Training models...")
    
    # Create model save directory
    os.makedirs(MODEL_SAVE_PATH, exist_ok=True)
    
    for dataset_type in ["spiral", "wave"]:
        print(f"Training {dataset_type} models...")
        
        # Load training data
        training_path = os.path.join(TRAINING_DATA_PATH, dataset_type, "training")
        testing_path = os.path.join(TRAINING_DATA_PATH, dataset_type, "testing")
        
        (trainX, trainY) = load_split(training_path)
        (testX, testY) = load_split(testing_path)
        
        if len(trainX) == 0:
            print(f"No training data found for {dataset_type}")
            continue
        
        # Encode labels
        le = LabelEncoder()
        trainY_encoded = le.fit_transform(trainY)
        testY_encoded = le.transform(testY) if len(testY) > 0 else []
        
        # Train models
        models = {
            "rf": RandomForestClassifier(random_state=1, n_estimators=100),
            "xgb": XGBClassifier(random_state=1)
        }
        
        model_metrics = {}
        
        for model_name, model in models.items():
            model.fit(trainX, trainY_encoded)
            
            # Calculate metrics if test data exists
            if len(testX) > 0:
                predictions = model.predict(testX)
                cm = confusion_matrix(testY_encoded, predictions).ravel()
                
                if len(cm) == 4:
                    tn, fp, fn, tp = cm
                    accuracy = (tp + tn) / float(cm.sum())
                    sensitivity = tp / float(tp + fn) if (tp + fn) > 0 else 0
                    specificity = tn / float(tn + fp) if (tn + fp) > 0 else 0
                    
                    model_metrics[model_name] = {
                        "accuracy": accuracy,
                        "sensitivity": sensitivity,
                        "specificity": specificity
                    }
            
            # Save model and label encoder
            model_file = os.path.join(MODEL_SAVE_PATH, f"{dataset_type}_{model_name}_model.pkl")
            le_file = os.path.join(MODEL_SAVE_PATH, f"{dataset_type}_label_encoder.pkl")
            
            joblib.dump(model, model_file)
            joblib.dump(le, le_file)
        
        # Save metrics
        metrics_file = os.path.join(MODEL_SAVE_PATH, f"{dataset_type}_metrics.pkl")
        joblib.dump(model_metrics, metrics_file)
        
        print(f"{dataset_type.capitalize()} models trained and saved")


def load_trained_model(dataset_type, model_type="rf"):
    """Load a pre-trained model."""
    model_file = os.path.join(MODEL_SAVE_PATH, f"{dataset_type}_{model_type}_model.pkl")
    le_file = os.path.join(MODEL_SAVE_PATH, f"{dataset_type}_label_encoder.pkl")
    metrics_file = os.path.join(MODEL_SAVE_PATH, f"{dataset_type}_metrics.pkl")
    
    if not all(os.path.exists(f) for f in [model_file, le_file, metrics_file]):
        raise FileNotFoundError(f"Trained models not found. Run train_and_save_models() first.")
    
    model = joblib.load(model_file)
    le = joblib.load(le_file)
    metrics = joblib.load(metrics_file)
    
    return model, le, metrics


def analyze_single_image(image_path, drawing_type):
    """
    Analyze a single image for Parkinson's detection.
    
    Args:
        image_path: Path to the image file
        drawing_type: 's' for spiral, 'w' for wave
    """
    # Validate inputs
    if drawing_type not in ['s', 'w']:
        raise ValueError("drawing_type must be 's' (spiral) or 'w' (wave)")
    
    dataset_type = "spiral" if drawing_type == 's' else "wave"
    
    print(f"PARKINSON'S DISEASE ANALYSIS - {dataset_type.upper()} DRAWING")
    print("=" * 60)
    
    # Load models
    try:
        rf_model, le, metrics = load_trained_model(dataset_type, "rf")
        xgb_model, _, _ = load_trained_model(dataset_type, "xgb")
    except FileNotFoundError:
        print("Trained models not found. Training new models...")
        train_and_save_models()
        rf_model, le, metrics = load_trained_model(dataset_type, "rf")
        xgb_model, _, _ = load_trained_model(dataset_type, "xgb")
    
    # Preprocess image
    start_time = time.time()
    try:
        processed_image = preprocess_image(image_path)
        features = quantify_image(processed_image)
        preprocessing_time = time.time() - start_time
    except Exception as e:
        print(f"Error preprocessing image: {e}")
        return
    
    # Make predictions
    inference_start = time.time()
    
    # Random Forest prediction
    rf_prediction = rf_model.predict([features])[0]
    rf_probability = rf_model.predict_proba([features])[0]
    
    # XGBoost prediction
    xgb_prediction = xgb_model.predict([features])[0]
    xgb_probability = xgb_model.predict_proba([features])[0]
    
    inference_time = time.time() - inference_start
    total_time = preprocessing_time + inference_time
    
    # Convert predictions back to labels
    rf_label = le.inverse_transform([rf_prediction])[0]
    xgb_label = le.inverse_transform([xgb_prediction])[0]
    
    # Print detailed analysis
    print(f"Image: {os.path.basename(image_path)}")
    print(f"Drawing Type: {dataset_type.capitalize()}")
    print(f"Image Size: {processed_image.shape}")
    print(f"Feature Vector Size: {len(features)}")
    print()
    
    print("TIMING ANALYSIS:")
    print(f"Preprocessing Time: {preprocessing_time*1000:.2f} ms")
    print(f"Inference Time: {inference_time*1000:.2f} ms")
    print(f"Total Analysis Time: {total_time*1000:.2f} ms")
    print()
    
    print("RANDOM FOREST ANALYSIS:")
    print(f"Prediction: {rf_label}")
    print(f"Confidence - Healthy: {rf_probability[0]*100:.2f}%")
    print(f"Confidence - Parkinson's: {rf_probability[1]*100:.2f}%")
    if "rf" in metrics:
        print(f"Model Accuracy: {metrics['rf']['accuracy']*100:.2f}%")
        print(f"Model Sensitivity: {metrics['rf']['sensitivity']*100:.2f}%")
        print(f"Model Specificity: {metrics['rf']['specificity']*100:.2f}%")
    print()
    
    print("XGBOOST ANALYSIS:")
    print(f"Prediction: {xgb_label}")
    print(f"Confidence - Healthy: {xgb_probability[0]*100:.2f}%")
    print(f"Confidence - Parkinson's: {xgb_probability[1]*100:.2f}%")
    if "xgb" in metrics:
        print(f"Model Accuracy: {metrics['xgb']['accuracy']*100:.2f}%")
        print(f"Model Sensitivity: {metrics['xgb']['sensitivity']*100:.2f}%")
        print(f"Model Specificity: {metrics['xgb']['specificity']*100:.2f}%")
    print()
    
    # Consensus analysis
    parkinson_votes = sum([1 for pred in [rf_prediction, xgb_prediction] if pred == 1])
    consensus_confidence = (rf_probability[1] + xgb_probability[1]) / 2
    
    print("CONSENSUS ANALYSIS:")
    print(f"Models agreeing on Parkinson's: {parkinson_votes}/2")
    print(f"Average Parkinson's probability: {consensus_confidence*100:.2f}%")
    
    if consensus_confidence > 0.7:
        risk_level = "HIGH"
    elif consensus_confidence > 0.4:
        risk_level = "MODERATE"
    else:
        risk_level = "LOW"
    
    print(f"Parkinson's Risk Level: {risk_level}")
    print()
    
    print("CLINICAL INTERPRETATION:")
    if consensus_confidence > 0.6:
        print("⚠️  RECOMMENDATION: Consider clinical evaluation for Parkinson's disease")
        print("   Drawing patterns show characteristics consistent with motor impairment")
    elif consensus_confidence > 0.4:
        print("⚡ RECOMMENDATION: Monitor for progression, consider follow-up testing")
        print("   Some patterns suggest possible early motor changes")
    else:
        print("✅ RECOMMENDATION: Drawing patterns appear within normal range")
        print("   No significant indicators of Parkinson's-related motor impairment")
    
    print()
    print("NOTE: This is a screening tool only. Clinical diagnosis requires")
    print("comprehensive medical evaluation by qualified healthcare professionals.")


def main():
    """Main function - requires command line arguments."""
    if len(sys.argv) != 3:
        print("Usage: python parkinsons_detection.py <image_path> <drawing_type>")
        print("  image_path: Path to the image file")
        print("  drawing_type: 's' for spiral, 'w' for wave")
        print()
        print("Example: python parkinsons_detection.py patient_spiral.jpg s")
        sys.exit(1)
    
    image_path = sys.argv[1]
    drawing_type = sys.argv[2].lower()
    
    try:
        analyze_single_image(image_path, drawing_type)
    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
