# Gunakan image Python resmi yang ringan.
# https://hub.docker.com/_/python
FROM python:3.8-slim

# Atur direktori kerja di dalam container
WORKDIR /app

# Salin file dependensi ke direktori kerja
COPY requirements.txt .

# Instal dependensi
RUN pip install --no-cache-dir -r requirements.txt


COPY . .


# Perintah untuk menjalankan aplikasi
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8080"]

