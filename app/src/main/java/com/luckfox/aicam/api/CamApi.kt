package com.luckfox.aicam.api
import com.luckfox.aicam.model.*
import retrofit2.Response
import retrofit2.http.*

interface CamApi {
  @GET("/config") suspend fun getConfig(): CamConfig
  @POST("/config") suspend fun setConfig(@Body c: CamConfig): Response<Unit>
  @GET("/alerts") suspend fun alerts(@Query("after") after: Long): Alerts
}
