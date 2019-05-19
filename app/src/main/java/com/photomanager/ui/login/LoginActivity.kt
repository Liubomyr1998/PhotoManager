package com.photomanager.ui.login

import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.photomanager.R
import com.photomanager.ui.main.MainActivity
import com.photomanager.ui.register.RegistrationActivity
import com.photomanager.util.launchActivity
import kotlinx.android.synthetic.main.activity_login.*
import pers.victor.ext.click


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        text_registration.paintFlags = text_registration.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        text_registration.click {
            launchActivity<RegistrationActivity> { }
        }

        var firebaseAuth = FirebaseAuth.getInstance()

        btn_login.click {
            if (TextUtils.isEmpty(edit_email.text.toString())) {
                til_email.error = "Обовязково для заповненя"
                return@click
            } else {
                til_email.error = null
            }
            if (TextUtils.isEmpty(edit_password.text.toString())) {
                til_password.error = "Обовязково для заповненя"
                return@click
            } else {
                til_password.error = null
            }

            firebaseAuth.signInWithEmailAndPassword(edit_email.text.toString(), edit_password.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        launchActivity<MainActivity>(clear = true) { }
                    } else {
                        Toast.makeText(this,"Помилка входу", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
