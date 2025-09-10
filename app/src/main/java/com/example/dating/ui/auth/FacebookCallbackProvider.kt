package com.example.dating.ui.auth

import androidx.compose.runtime.*
import com.facebook.CallbackManager

val LocalFacebookCallbackManager = staticCompositionLocalOf<CallbackManager> {
    error("No CallbackManager provided")
}

@Composable
fun FacebookCallbackProvider(
    callbackManager: CallbackManager,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalFacebookCallbackManager provides callbackManager) {
        content()
    }
}
