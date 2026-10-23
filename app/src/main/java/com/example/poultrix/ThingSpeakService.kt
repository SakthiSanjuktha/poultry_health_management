package com.example.poultrix

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class ThingSpeakResponse(
    @SerializedName("channel") val channel: Any?,
    @SerializedName("feeds") val feeds: List<ThingSpeakFeed>?
)

data class ThingSpeakFeed(
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("field1") val field1: String?, // Temperature
    @SerializedName("field2") val field2: String?, // Humidity
    @SerializedName("field3") val field3: String?, // Latitude
    @SerializedName("field4") val field4: String?, // Longitude
    @SerializedName("field5") val field5: String?  // Vibration
)

interface ThingSpeakApi {
    @GET("channels/{channelId}/feeds.json")
    suspend fun getLatestFeed(
        @Path("channelId") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("results") results: Int
    ): ThingSpeakResponse
}

object ThingSpeakClient {
    private const val BASE_URL = "https://api.thingspeak.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val instance: ThingSpeakApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ThingSpeakApi::class.java)
    }
}
