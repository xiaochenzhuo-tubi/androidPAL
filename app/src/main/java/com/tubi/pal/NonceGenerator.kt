package com.tubi.pal

import android.content.Context
import android.util.Log
import com.google.ads.interactivemedia.pal.ConsentSettings
import com.google.ads.interactivemedia.pal.NonceLoader
import com.google.ads.interactivemedia.pal.NonceManager
import com.google.ads.interactivemedia.pal.NonceRequest
import com.google.android.tv.ads.SignalCollector

/**
 * references:
 * https://developers.google.com/ad-manager/pal/android
 * https://developers.google.com/ad-manager/pal/android/download
 */
class NonceGenerator(private val context: Context, private val storageInterface: ConsentStorageInterface) {

    companion object {
        private val TAG = NonceGenerator::class.simpleName
        private const val MAX_RETRY_COUNT = 3

        private const val PARAM_PLAYER_TYPE = "ExoPlayer"
        private const val PARAM_PLAYER_VERSION = "2.19.0"
        private const val PARAM_PLAYER_HEIGHT = 1080
        private const val PARAM_PLAYER_WIDTH = 1920
    }

    private var mNonceManager: NonceManager? = null
    private var mConsentToStore: Boolean
    private var mNonceLoader: NonceLoader

    init {
        mConsentToStore = storageInterface.getConsentToStorage()

        val consentSettings = ConsentSettings.builder()
            .allowStorage(mConsentToStore)
            .build()
        // It is important to instantiate the NonceLoader as early as possible to
        // allow it to initialize and preload data for a faster experience when
        // loading the NonceManager. A new NonceLoader will need to be instantiated
        // if the ConsentSettings change for the user.
        mNonceLoader = NonceLoader(context, consentSettings)
    }

    fun generateNonce(nonceCallback: NonceCallback) {
        generateNonceForAdRequest(0, nonceCallback)
    }

    private fun generateNonceForAdRequest(retryCount: Int, nonceCallback: NonceCallback) {
        if (retryCount > MAX_RETRY_COUNT) {
            return
        }
        val builder = NonceRequest.builder()
        val nonceRequest = builder
            .playerType(PARAM_PLAYER_TYPE)
            .playerVersion(PARAM_PLAYER_VERSION)
            .videoPlayerHeight(PARAM_PLAYER_HEIGHT)
            .videoPlayerWidth(PARAM_PLAYER_WIDTH)
            .willAdAutoPlay(false)
            .willAdPlayMuted(false)
            .iconsSupported(true)
            .platformSignalCollector(SignalCollector())
            .build()
        if (mConsentToStore != storageInterface.getConsentToStorage()) {
            // reset nonceLoader if the consentToStore is changed
            mConsentToStore = storageInterface.getConsentToStorage()
            val consentSettings = ConsentSettings.builder().allowStorage(mConsentToStore).build()
            mNonceLoader = NonceLoader(context, consentSettings)
        }
        Log.i(TAG, "generateNonceForAdRequest: ${storageInterface.getConsentToStorage()}")
        mNonceLoader.loadNonceManager(nonceRequest)
            .addOnSuccessListener { nonceManager ->
                mNonceManager = nonceManager
                val nonceString = nonceManager.nonce
                nonceCallback.onSuccess(nonceString)
            }.addOnFailureListener { exception ->
                mNonceManager = null
                if (retryCount < MAX_RETRY_COUNT) {
                    generateNonceForAdRequest(retryCount + 1, nonceCallback)
                } else {
                    nonceCallback.onFailure(exception)
                }
            }
    }

    //    /**
    //     * Return false only when the user/device opt out is True.
    //     * False means when user opts out of tracking, we don’t allow Google personalize ads for users
    //     * Return true otherwise
    //     */
    //    private fun getConsentToStorage(): Boolean {
    //    }

    fun release() {
        mNonceLoader.release()
    }
}