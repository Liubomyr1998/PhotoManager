package com.photomanager.ui.register

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.photomanager.R
import com.photomanager.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_registration.*
import pers.victor.ext.click


class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        toolbar_registration.title = "Реєстрація"
        setSupportActionBar(toolbar_registration)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        var  firebaseAuth = FirebaseAuth.getInstance()

        btn_registr.click {
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
            if (edit_password.text.toString().length < 6) {
                til_password.error = "Більше 6 символів"
                return@click
            } else {
                til_password.error = null
            }

            firebaseAuth?.createUserWithEmailAndPassword(edit_email.text.toString(), edit_password.text.toString())?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this,"Помилка реєстрації",Toast.LENGTH_SHORT).show()
                    }
                }
                ?.addOnFailureListener {
                    Toast.makeText(this,it.message,Toast.LENGTH_LONG).show()
                }
        }

        toolbar_registration.setNavigationOnClickListener {
            finish()
        }
    }
}