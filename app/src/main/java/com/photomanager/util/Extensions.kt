package com.photomanager.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment

inline fun <reified T : Any> Activity.launchActivity(
    requestCode: Int = -1,
    clear: Boolean = false,
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}) {

    var intent = newIntent<T>(this)
    if (options != null) intent.putExtras(options)

    if (clear) {
        intent = newClearIntent<T>(this)
    }

    intent.init()
    if (requestCode != -1) {
        startActivityForResult(intent, requestCode)
    } else {
        startActivity(intent)
    }

}

inline fun <reified T : Any> Fragment.launchActivity(
    requestCode: Int = -1,
    clear: Boolean = false,
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}) {

    var intent = newIntent<T>(activity!!)
    if (options != null) intent.putExtras(options)

    if (clear) {
        intent = newClearIntent<T>(activity!!)
    }

    intent.init()
    if (requestCode != -1) {
        startActivityForResult(intent, requestCode)
    } else {
        startActivity(intent)
    }

}

inline fun <reified T : Any> newIntent(context: Context): Intent = Intent(context, T::class.java)

inline fun <reified T : Any> newClearIntent(context: Context): Intent {
    var intent = Intent(context, T::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    return intent
}
