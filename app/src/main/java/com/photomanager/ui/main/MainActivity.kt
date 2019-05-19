package com.photomanager.ui.main

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.photomanager.R
import com.photomanager.ui.login.LoginActivity
import com.photomanager.ui.main.local.LocalGalleryFragment
import com.photomanager.ui.main.remote.RemoteGalleryFragment
import com.photomanager.util.launchActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar_main.title = getString(R.string.local_gallery)
        setSupportActionBar(toolbar_main)

        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar_main,
            com.photomanager.R.string.navigation_drawer_open,
            com.photomanager.R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        nav_view.setCheckedItem(R.id.nav_local)

        nav_view.getHeaderView(0).findViewById<TextView>(com.photomanager.R.id.text_name).text =
            FirebaseAuth.getInstance().currentUser?.email

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, LocalGalleryFragment())
        transaction.commit()
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.nav_local -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, LocalGalleryFragment())
                transaction.commit()
                supportActionBar?.title = p0.title
            }
            R.id.nav_remote -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, RemoteGalleryFragment())
                transaction.commit()
                supportActionBar?.title = p0.title
            }
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                launchActivity<LoginActivity>(clear = true) { }
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
