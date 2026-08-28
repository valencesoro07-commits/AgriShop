package com.example.data.cinetpay

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface CinetPayApiService {

    @Headers("Content-Type: application/json")
    @POST("payment")
    suspend fun initializePayment(
        @Body request: CinetPayInitRequest
    ): Response<CinetPayInitResponse>

    @Headers("Content-Type: application/json")
    @POST("payment/check")
    suspend fun checkPaymentStatus(
        @Body request: CinetPayCheckRequest
    ): Response<CinetPayCheckResponse>
}
