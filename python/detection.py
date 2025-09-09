import os
import time
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

MODEL_PATH = "models/best_model.h5"

def load_and_preprocess_image(image_path):
    try:
        img = Image.open(image_path)
        if img.mode != 'RGB':
            img = img.convert('RGB')
        img = img.resize((224, 224))
        img_array = np.array(img) / 255.0
        img_array = np.expand_dims(img_array, axis=0)
        return img_array
    except Exception as e:
        print(f"Error loading image: {e}")
        return None

def get_model_info(model_path, model):
    try:
        size_bytes = os.path.getsize(model_path)
        size_mb = size_bytes / (1024 * 1024)
        num_layers = len(model.layers)
        total_params = np.sum([tf.keras.backend.count_params(w) for w in model.weights])
        return size_mb, num_layers, total_params
    except Exception:
        return None, None, None

def predict_parkinson(model_path, image_path):
    try:
        model = tf.keras.models.load_model(model_path, compile=False)
        img_array = load_and_preprocess_image(image_path)
        if img_array is None:
            return

        start_time = time.time()
        prediction = model.predict(img_array, verbose=0)
        inference_time = (time.time() - start_time) * 1000  # ms

        probability = float(prediction[0][0])

        if probability > 0.5:
            result = "Parkinson's Disease"
            confidence = probability * 100
        else:
            result = "Healthy"
            confidence = (1 - probability) * 100

        size_mb, num_layers, total_params = get_model_info(model_path, model)

        # Print raw values only (each in new line)
        print(result)
        print(f"{confidence:.2f}")
        print(f"{probability:.4f}")
        print(f"{inference_time:.2f}")
        print(f"{size_mb:.2f}")
        print(num_layers)
        print(total_params)

    except Exception as e:
        print(f"Error making prediction: {e}")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python predict.py <image_path>")
        sys.exit(1)

    image_path = sys.argv[1]

    if not os.path.exists(image_path):
        print(f"Error: Image file '{image_path}' not found")
        sys.exit(1)

    predict_parkinson(MODEL_PATH, image_path)
