package com.scann.thekami.ads

import android.view.LayoutInflater
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.scann.thekami.R

@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var loadFailed by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        NativeAdLoader.load(
            context = context,
            onLoaded = { ad ->
                nativeAd?.destroy()
                nativeAd = ad
            },
            onFailed = {
                loadFailed = true
            }
        )
        onDispose {
            nativeAd?.destroy()
        }
    }

    val currentAd = nativeAd
    if (currentAd != null) {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { ctx ->
                val inflater = LayoutInflater.from(ctx)
                val adView = inflater.inflate(R.layout.native_ad_layout, null) as NativeAdView
                bindNativeAd(adView, currentAd)
                adView
            },
            update = { adView ->
                bindNativeAd(adView, currentAd)
            }
        )
    }
}

private fun bindNativeAd(adView: NativeAdView, nativeAd: NativeAd) {
    val headlineView = adView.findViewById<android.widget.TextView>(R.id.ad_headline)
    val bodyView = adView.findViewById<android.widget.TextView>(R.id.ad_body)
    val advertiserView = adView.findViewById<android.widget.TextView>(R.id.ad_advertiser)
    val iconView = adView.findViewById<android.widget.ImageView>(R.id.ad_app_icon)
    val ctaView = adView.findViewById<android.widget.Button>(R.id.ad_call_to_action)
    val mediaView = adView.findViewById<com.google.android.gms.ads.nativead.MediaView>(R.id.ad_media)

    headlineView.text = nativeAd.headline
    adView.headlineView = headlineView

    if (nativeAd.body.isNullOrBlank()) {
        bodyView.visibility = android.view.View.GONE
    } else {
        bodyView.visibility = android.view.View.VISIBLE
        bodyView.text = nativeAd.body
    }
    adView.bodyView = bodyView

    if (nativeAd.callToAction.isNullOrBlank()) {
        ctaView.visibility = android.view.View.GONE
    } else {
        ctaView.visibility = android.view.View.VISIBLE
        ctaView.text = nativeAd.callToAction
    }
    adView.callToActionView = ctaView

    val icon = nativeAd.icon
    if (icon == null) {
        iconView.visibility = android.view.View.GONE
    } else {
        iconView.visibility = android.view.View.VISIBLE
        iconView.setImageDrawable(icon.drawable)
    }
    adView.iconView = iconView

    if (nativeAd.advertiser.isNullOrBlank()) {
        advertiserView.visibility = android.view.View.GONE
    } else {
        advertiserView.visibility = android.view.View.VISIBLE
        advertiserView.text = nativeAd.advertiser
    }
    adView.advertiserView = advertiserView

    adView.mediaView = mediaView
    nativeAd.mediaContent?.let { mediaView.mediaContent = it }

    adView.setNativeAd(nativeAd)
}
