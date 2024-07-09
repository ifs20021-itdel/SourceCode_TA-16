package com.example.app_ta2

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class MainActivity : AppCompatActivity() {
    private lateinit var captureIV: ImageView
    private lateinit var imageUrl: Uri
    private lateinit var responseTextView: TextView

    private val solutionsMap = mapOf(
        "Healthy" to listOf("Tidak diperlukan tindakan, tanaman Anda sehat!"),
        "Gray Leaf Spot" to listOf(
            "Gunakan varietas tahan.",
            "Budidaya bersih mengurangi infeksi.",
            "Jangan mengisi terlalu banyak jagung.",
            "Gunakan fungisida sistemik, terutama karena mekar air muncul setiap 7-10 hari.",
            "Jangan menanam jagung dengan sitoplasma jantan yang mandul."
        ),
        "Common Rust" to listOf(
            "Varietas anti karat seperti Lamuru, Sukmaraga, Palakka, Bima-1 atau Semar-10.",
            "Memusnahkan (membuang) seluruh bagian tanaman mulai dari tanaman yang terserang penyakit daun atau penyakit gulma sampai ke akar-akarnya.",
            "Penyemprotan fungisida dengan bahan Benomyl. Dosis/kekuatan per instruksi paket."
        ),
        "Blight" to listOf(
            "Membersihkan tanaman sakit dan inang alternatif guna menekan sumber inokulum di lahan.",
            "Penggunaan varietas jagung yang tahan terhadap penyakit antara lain C-4, C-8, C-9, Andalas 4, P2, P10, P11, P14, P17, P19, P29, N 35, PAC 224, PAC 759, dan BIMA-8.",
            "Mengatur waktu tanam untuk menghindari musim penghujan yang memicu kondisi yang baik bagi patogen penyebab penyakit ini.",
            "Menyusun jarak tanam yang tepat agar tidak terlalu rapat, sehingga menciptakan lingkungan mikro yang tidak sesuai untuk perkembangan penyakit",
            "Menggunakan pengendalian hayati dengan memanfaatkan Bacillus subtilis dan Paenibacillus polymyxa.",
            "Melakukan perlakuan pada benih dengan memberikan metalaksil sebanyak 2,5 gram per kilogram benih dan pupuk NPK dengan tambahan Nordox56WP, yang dapat mengurangi serangan hawar daun yang disebabkan oleh Helminthosporium sp. pada fase vegetatif."
        )
    )

    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            captureIV.setImageURI(null)
            captureIV.setImageURI(imageUrl)
            sendImageToApi(imageUrl)
        }
    }

    //  untuk memilih gambar dari galeri / baru ku tambahin
    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Tampilkan dan kirim gambar yang dipilih
            captureIV.setImageURI(it)
            sendImageToApi(it)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        responseTextView = findViewById(R.id.responseTextView)
        val myButton: Button = findViewById(R.id.berandaBtn)
        myButton.setOnClickListener {
            val intent = Intent(this, Page2Activity::class.java)
            startActivity(intent)
        }

        imageUrl = createImageUri()
        captureIV = findViewById(R.id.captureImageView)
        findViewById<Button>(R.id.captureImageBtn).setOnClickListener {
            contract.launch(imageUrl)
        }
        // button mengambil dari galeri
        findViewById<Button>(R.id.selectImageBtn).setOnClickListener {
            selectImage.launch("image/*")
        }

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed() // Fungsi back
        }
    }

    private fun createImageUri(): Uri {
        val image = File(filesDir, "camera_photos.png")
        return FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", image)
    }

    //disini normalisasi image nya
    private fun uriToBitmap(imageUri: Uri): Bitmap? {
        return contentResolver.openInputStream(imageUri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }?.let { originalBitmap ->
            Bitmap.createScaledBitmap(originalBitmap, 224, 224, true)
        }
    }

    private fun normalizeBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val normalizedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val red = Color.red(pixel) / 255.0f
                val green = Color.green(pixel) / 255.0f
                val blue = Color.blue(pixel) / 255.0f
                val normalizedColor = Color.rgb((red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())
                normalizedBitmap.setPixel(x, y, normalizedColor)
            }
        }
        return normalizedBitmap
    }

    // disini ada ditambakan normalisasi
    private fun sendImageToApi(imageUri: Uri) {
        val imageBitmap = uriToBitmap(imageUri) ?: return

        // Normalisasi Bitmap
        val normalizedBitmap = normalizeBitmap(imageBitmap)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://revisisidang-7kforxnfiq-et.a.run.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val file = File(cacheDir, "temp_image") // File sementara
        FileOutputStream(file).use { output ->
            normalizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
        }

        val requestFile = RequestBody.create(
            contentResolver.getType(imageUri)?.toMediaTypeOrNull(),
            file
        )
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val call = apiService.uploadImage(body)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBodyString = response.body()?.string()
                    val jsonResponse = JSONObject(responseBodyString)

                    if (jsonResponse.has("message") && jsonResponse.getString("message") == "Tanaman tidak terdeteksi") {
                        runOnUiThread {
                            responseTextView.text = "Tanaman tidak terdeteksi atau ini mungkin berbeda dengan tanaman yang dimaksud."
                        }
                    } else {
                        val predictedClass = jsonResponse.getString("predicted_class")
                        val solutions = solutionsMap[predictedClass] ?: listOf("Tidak ada solusi yang tersedia.")
                        val solutionsText = solutions.joinToString("\n")
                        runOnUiThread {
                            responseTextView.text = "Detected: $predictedClass\nSolutions:\n$solutionsText"
                        }
                    }
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    runOnUiThread {
                        responseTextView.text = "Error ${response.code()}: $errorBodyString"
                    }
                }
            }


            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Upload", "Failure: ${t.message}")

                // Tampilkan failure message di TextView
                runOnUiThread {
                    responseTextView.text = "Failure: ${t.message}"
                }
            }
        })
    }
}