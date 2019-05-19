package com.photomanager.ui.splash

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.photomanager.R
import com.photomanager.ui.login.LoginActivity
import com.photomanager.ui.main.MainActivity
import com.photomanager.util.launchActivity


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            if (FirebaseAuth.getInstance().currentUser == null) {
                launchActivity<LoginActivity>(clear = true) {  }
            }else{
                launchActivity<MainActivity>(clear = true) {  }
            }
        }, 2200)
    }
}
