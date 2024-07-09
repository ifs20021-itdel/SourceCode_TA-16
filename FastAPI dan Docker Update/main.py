from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import FileResponse
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing import image
import numpy as np
from io import BytesIO
from PIL import Image
import os
import logging
import uvicorn


logging.basicConfig(filename='app.log', level=logging.DEBUG, format='%(asctime)s %(levelname)s %(name)s %(message)s')
logger = logging.getLogger("uvicorn.info")

app = FastAPI()

MODEL_PATH = 'RevisiSidang_model.h5'

# Memuat model secara async
model = None

@app.on_event("startup")
async def load_model_async():
    global model
    model = load_model(MODEL_PATH)

class_names = {
    0: "Blight",
    1: "Common Rust",
    2: "Gray Leaf Spot",
    3: "Healthy"
}

@app.get("/")
async def root():
    return {"message": "Welcome to the plant disease detection API! (Update Model)"}

@app.post("/detection")
async def predict(file: UploadFile = File(...)):
    try:
        contents = await file.read()
        pil_image = Image.open(BytesIO(contents)).convert('RGB')
        img = pil_image.resize((224, 224))

        img_array = image.img_to_array(img)
        img_array_expanded_dims = np.expand_dims(img_array, axis=0)
        img_preprocessed = img_array_expanded_dims / 255.0  # Normalisasi sesuai pelatihan

        # Lakukan prediksi
        prediction = model.predict(img_preprocessed)
        predicted_probability = np.max(prediction)  # Mengambil probabilitas tertinggi

        if predicted_probability < 0.8:
            return {"message": "Tanaman tidak terdeteksi"}
        else:
            predicted_class = np.argmax(prediction, axis=1)
            predicted_class_name = class_names[int(predicted_class[0])]
            return {"predicted_class": predicted_class_name, "probability": float(predicted_probability)}
    except Exception as e:
        logger.error(f"Error processing request: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8080))
    uvicorn.run(app, host="0.0.0.0", port=port)
