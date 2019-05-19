package com.photomanager.ui.main.remote.details

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.photomanager.R
import kotlinx.android.synthetic.main.activity_image_details.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.toast
import pyxis.uzuki.live.richutilskt.utils.drawableToBitmap
import java.io.File

class RemoteDetailsImageActivity : AppCompatActivity() {

    var dataBase: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var uploadTask: UploadTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_details)

        progress.indeterminateDrawable.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN )

        dataBase = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance("gs://photomanager-f8426.appspot.com")

        Glide.with(this).load(intent.getParcelableExtra("uri") as Uri)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    progress.gone()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    progress.gone()
                    photo_view.setImage(ImageSource.bitmap(drawableToBitmap(resource!!)))
                    return false
                }
            })
            .into(image_mok)

        toolbar_details.title = ""
        setSupportActionBar(toolbar_details)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar_details.setNavigationOnClickListener {
            finish()
        }

        image_option.click {
            var menu = PopupMenu(this, it)
            menu.menu.add("Видалити")
            menu.setOnMenuItemClickListener {
                storage?.reference?.child(intent.getStringExtra("path"))?.delete()
                    ?.addOnSuccessListener {
                        dataBase?.getReference("folders")?.child(intent.getStringExtra("folder"))
                            ?.child(intent.getStringExtra("child"))?.removeValue()
                            ?.addOnSuccessListener {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                            ?.addOnFailureListener {
                                Toast.makeText(this, getString(R.string.remove_error), Toast.LENGTH_SHORT).show()
                            }

                    }?.addOnFailureListener {
                        Toast.makeText(this,  getString(R.string.remove_error), Toast.LENGTH_SHORT).show()
                    }
                true
            }
            menu.show()
        }
    }
}
