package com.example.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.PaymentProvider
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class PaymentInitiationResult(
    val transactionId: String,
    val paymentUrl: String?,
    val deepLinkUri: String?,
    val isDirectAppLaunch: Boolean,
    val provider: PaymentProvider,
    val amountCfa: Int,
    val status: String
)

object PaymentGatewayService {

    fun initiateWavePayment(
        amountCfa: Int,
        clientPhone: String,
        description: String
    ): PaymentInitiationResult {
        val txId = "WAVE-${System.currentTimeMillis().toString().takeLast(6)}"
        val encodedDesc = Uri.encode(description)
        // Standard Wave CI payment URL & deep link
        val waveWebCheckout = "https://pay.wave.com/m/M_ci_agrishop_official/c/ci/?amount=$amountCfa&client_reference=$txId"
        val waveDeepLink = "wave://pay?amount=$amountCfa&recipient=22507889911&memo=$encodedDesc&ref=$txId"

        return PaymentInitiationResult(
            transactionId = txId,
            paymentUrl = waveWebCheckout,
            deepLinkUri = waveDeepLink,
            isDirectAppLaunch = true,
            provider = PaymentProvider.WAVE,
            amountCfa = amountCfa,
            status = "PENDING"
        )
    }

    fun initiateCinetPay(
        amountCfa: Int,
        provider: PaymentProvider,
        customerPhone: String,
        description: String
    ): PaymentInitiationResult {
        val txId = "CP-${provider.name.take(3)}-${System.currentTimeMillis().toString().takeLast(6)}"
        val checkoutUrl = "https://checkout.cinetpay.com/payment/v2?apikey=agrishop_live_ci&site_id=584920&notify_url=https://agrishop.ci/api/webhook&amount=$amountCfa&currency=XOF&trans_id=$txId"

        return PaymentInitiationResult(
            transactionId = txId,
            paymentUrl = checkoutUrl,
            deepLinkUri = null,
            isDirectAppLaunch = false,
            provider = provider,
            amountCfa = amountCfa,
            status = "PENDING"
        )
    }

    suspend fun verifyPaymentStatus(transactionId: String): Boolean {
        // Simulates network gateway handshake & verification
        delay(1500)
        return true
    }

    fun launchPaymentIntent(context: Context, initiation: PaymentInitiationResult) {
        try {
            if (initiation.deepLinkUri != null) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(initiation.deepLinkUri))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } else if (initiation.paymentUrl != null) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(initiation.paymentUrl))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            // Fallback to browser URL
            initiation.paymentUrl?.let { url ->
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                fallbackIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(fallbackIntent)
            }
        }
    }
}
