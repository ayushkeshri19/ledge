package com.ayush.network.data.deeplink

import android.content.Intent
import com.ayush.common.auth.PasswordRecoveryStateHolder
import com.ayush.common.deeplink.DeepLinkHandler
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import javax.inject.Inject

class SupabaseDeepLinkHandler @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val passwordRecoveryStateHolder: PasswordRecoveryStateHolder
) : DeepLinkHandler {

    override fun handle(intent: Intent) {
        if (isRecoveryLink(intent)) {
            passwordRecoveryStateHolder.onRecoveryDetected()
        }
        supabaseClient.handleDeeplinks(intent)
    }

    private fun isRecoveryLink(intent: Intent): Boolean {
        val fragment = intent.data?.fragment ?: return false
        return fragment.split('&').any { it == "type=recovery" }
    }
}
