import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'
os.environ['TF_ENABLE_ONEDNN_OPTS'] = '0'

import warnings
warnings.filterwarnings('ignore')

import tensorflow as tf
tf.get_logger().setLevel('ERROR')
tf.compat.v1.logging.set_verbosity(tf.compat.v1.logging.ERROR)

import numpy as np
from PIL import Image
import sys

def load_and_preprocess_image(image_path):
    """Load and preprocess image for model prediction"""
    try:
        img = Image.open(image_path)
        if img.mode != 'RGB':
            img = img.convert('RGB')
        img = img.resize((700, 200))
        img = img.resize((224, 224))
        img_array = np.array(img) / 255.0
        img_array = np.expand_dims(img_array, axis=0)
        return img_array
    except Exception as e:
        print(f"Error loading image: {e}")
        return None

def predict_parkinson(model_path, image_path):
    """Load model and make prediction"""
    try:
        model = tf.keras.models.load_model(model_path, compile=False)
        img_array = load_and_preprocess_image(image_path)
        if img_array is None:
            return
        
        prediction = model.predict(img_array, verbose=0)
        probability = prediction[0][0]
        
        if probability > 0.5:
            result = "Parkinson's Disease"
            confidence = probability * 100
        else:
            result = "Healthy"
            confidence = (1 - probability) * 100
        
        print(f"Image: {os.path.basename(image_path)}")
        print(f"Prediction: {result}")
        print(f"Confidence: {confidence:.2f}%")
        print(f"Raw probability: {probability:.4f}")
        
    except Exception as e:
        print(f"Error making prediction: {e}")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python predict.py <image_path>")
        sys.exit(1)
    
    model_path = "models/best_model.h5"
    image_path = sys.argv[1]
    
    if not os.path.exists(image_path):
        print(f"Error: Image file '{image_path}' not found")
        sys.exit(1)
    
    predict_parkinson(model_path, image_path)
