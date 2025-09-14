package com.example.dating

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import com.example.dating.data.model.repository.AuthRepository

@HiltAndroidApp
class DatingApplication : Application() {
    @Inject
    lateinit var authRepository: AuthRepository

    override fun onTerminate() {
        super.onTerminate()
        authRepository.logout()
    }
}
