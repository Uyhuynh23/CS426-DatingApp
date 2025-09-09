package com.example.dating

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.dating.navigation.AppNavGraph
import com.example.dating.ui.theme.DatingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val fbCallbackManager: com.facebook.CallbackManager by lazy {
        com.facebook.CallbackManager.Factory.create()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Forward to Facebook
        if (com.facebook.FacebookSdk.isInitialized()) {
            fbCallbackManager.onActivityResult(requestCode, resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.facebook.FacebookSdk.sdkInitialize(applicationContext)
        com.facebook.appevents.AppEventsLogger.activateApp(application)
        setContent {
            DatingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}
