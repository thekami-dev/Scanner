package com.scann.thekami.ads

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.scann.thekami.BuildConfig

private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
private const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-4015145068033513/3866251443"

@Composable
fun BannerAdCard(modifier: Modifier = Modifier) {
    var status by remember { mutableStateOf("ad: loading") }

    Column(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = PROD_BANNER_AD_UNIT_ID
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            status = "ad: loaded"
                        }
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.e("AdsDebug", "code=${error.code} msg=${error.message}")
                            status = "ad error ${error.code}: ${error.message}"
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
        Text(status) // TEMP: debug শেষে মুছে দিন
    }
}
