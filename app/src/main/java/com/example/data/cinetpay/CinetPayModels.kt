package com.example.data.cinetpay

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CinetPayInitRequest(
    @Json(name = "apikey") val apikey: String,
    @Json(name = "site_id") val siteId: String,
    @Json(name = "transaction_id") val transactionId: String,
    @Json(name = "amount") val amount: Long,
    @Json(name = "currency") val currency: String = "XOF",
    @Json(name = "description") val description: String,
    @Json(name = "customer_name") val customerName: String = "Client",
    @Json(name = "customer_surname") val customerSurname: String = "AgriShop",
    @Json(name = "customer_phone_number") val customerPhone: String = "+22507000000",
    @Json(name = "customer_email") val customerEmail: String = "client@agrishop.ci",
    @Json(name = "customer_address") val customerAddress: String = "Yamoussoukro",
    @Json(name = "customer_city") val customerCity: String = "Yamoussoukro",
    @Json(name = "customer_country") val customerCountry: String = "CI",
    @Json(name = "customer_state") val customerState: String = "CI",
    @Json(name = "customer_zip_code") val customerZipCode: String = "00225",
    @Json(name = "notify_url") val notifyUrl: String = "https://api-checkout.cinetpay.com/v2/test/notify",
    @Json(name = "return_url") val returnUrl: String = "https://api-checkout.cinetpay.com/v2/test/return",
    @Json(name = "channels") val channels: String = "ALL",
    @Json(name = "metadata") val metadata: String = "agri_shop_app"
)

@JsonClass(generateAdapter = true)
data class CinetPayData(
    @Json(name = "payment_token") val paymentToken: String? = null,
    @Json(name = "payment_url") val paymentUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class CinetPayInitResponse(
    @Json(name = "code") val code: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "data") val data: CinetPayData? = null,
    @Json(name = "api_response_id") val apiResponseId: String? = null
)

@JsonClass(generateAdapter = true)
data class CinetPayCheckRequest(
    @Json(name = "apikey") val apikey: String,
    @Json(name = "site_id") val siteId: String,
    @Json(name = "transaction_id") val transactionId: String
)

@JsonClass(generateAdapter = true)
data class CinetPayCheckData(
    @Json(name = "amount") val amount: String? = null,
    @Json(name = "currency") val currency: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "payment_method") val paymentMethod: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "metadata") val metadata: String? = null,
    @Json(name = "operator_id") val operatorId: String? = null,
    @Json(name = "payment_date") val paymentDate: String? = null
)

@JsonClass(generateAdapter = true)
data class CinetPayCheckResponse(
    @Json(name = "code") val code: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: CinetPayCheckData? = null
)

sealed class CinetPayExecutionResult {
    data class Success(
        val transactionId: String,
        val paymentUrl: String,
        val paymentToken: String,
        val amount: Long,
        val description: String,
        val isLiveMode: Boolean,
        val rawMessage: String
    ) : CinetPayExecutionResult()

    data class Verified(
        val transactionId: String,
        val status: String,
        val operatorId: String,
        val amount: Long
    ) : CinetPayExecutionResult()

    data class Error(
        val code: String,
        val errorMessage: String,
        val fallbackTransactionId: String? = null
    ) : CinetPayExecutionResult()
}
