package com.scann.thekami.ads

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd

object NativeAdLoader {

    // Google's official Native Advanced test ad unit ID.
    // Switch to the real ID (BuildConfig-driven) before production release.
    private const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
    private const val PROD_NATIVE_AD_UNIT_ID = "ca-app-pub-4015145068033513/9997360195"

    private const val USE_TEST_ADS = true

    fun currentAdUnitId(): String = if (USE_TEST_ADS) TEST_NATIVE_AD_UNIT_ID else PROD_NATIVE_AD_UNIT_ID

    fun load(
        context: Context,
        onLoaded: (NativeAd) -> Unit,
        onFailed: (String) -> Unit
    ) {
        val adLoader = AdLoader.Builder(context, currentAdUnitId())
            .forNativeAd { nativeAd ->
                onLoaded(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    onFailed(error.message)
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }
}
