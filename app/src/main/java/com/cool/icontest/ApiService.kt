package com.cool.icontest

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiService {

    val client = OkHttpClient.Builder().build()
    val BASE_URL = "http://192.168.43.126:3000/"
    var retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    fun<T> buildService(service:Class<T>):T{
        return  retrofit.create(service)
    }

}