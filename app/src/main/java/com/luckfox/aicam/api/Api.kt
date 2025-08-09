package com.luckfox.aicam.api
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object Api {
  fun client(baseUrl: String): CamApi = Retrofit.Builder()
    .baseUrl(baseUrl)
    .addConverterFactory(MoshiConverterFactory.create())
    .build().create(CamApi::class.java)
}
