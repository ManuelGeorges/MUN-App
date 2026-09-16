package com.mianu.paymentapp.network

import com.mianu.paymentapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Cloudflare Workers live production edge endpoint
    const val ENDPOINT_HOST = "mianu-backend.karimshacker1234.workers.dev"
    private const val BASE_URL = "https://$ENDPOINT_HOST/api/v1/"

    @Volatile
    private var authToken: String? = null

    val isAuthenticated: Boolean get() = authToken != null

    fun setToken(token: String) {
        authToken = token
    }

    fun clearToken() {
        authToken = null
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder().apply {
                    authToken?.let { header("Authorization", "Bearer $it") }
                }.build()
                chain.proceed(request)
            }
            .apply {
                // Body-level logging prints tokens and card UIDs, so keep it out of release builds.
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        },
                    )
                }
            }
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
