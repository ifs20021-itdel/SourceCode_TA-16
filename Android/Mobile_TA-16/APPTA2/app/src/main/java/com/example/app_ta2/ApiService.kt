package com.example.app_ta2

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @Multipart
    @POST("/detection") // Pastikan ini sesuai dengan endpoint yang ditetapkan dalam API FastAPI
    fun uploadImage(@Part image: MultipartBody.Part): Call<ResponseBody>
}
