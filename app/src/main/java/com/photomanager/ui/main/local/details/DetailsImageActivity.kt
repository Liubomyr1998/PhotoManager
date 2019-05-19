package com.photomanager.ui.main.local.details

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.photomanager.R
import com.photomanager.model.RemoteImage
import kotlinx.android.synthetic.main.activity_image_details.*
import org.apache.commons.io.FileUtils
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.toast
import pyxis.uzuki.live.richutilskt.utils.getSizeByMb
import java.io.*
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*

class DetailsImageActivity : AppCompatActivity() {

    var dataBase: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var uploadTask: UploadTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_details)

        dataBase = FirebaseDatabase.getInstance()
        var reference = dataBase?.getReference("folders")

        progress.gone()

        photo_view.setImage(ImageSource.uri(Uri.fromFile(File(intent.getStringExtra("path")))))

        toolbar_details.title = ""
        setSupportActionBar(toolbar_details)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar_details.setNavigationOnClickListener {
            finish()
        }

        storage = FirebaseStorage.getInstance("gs://photomanager-f8426.appspot.com")

        var foldersPath = intent.getStringArrayListExtra("foldersPath")
        var folders = intent.getStringArrayListExtra("folders")

        image_option.click {
            var menu = PopupMenu(this, it)
            menu.menu.add(getString(R.string.upload_to_server))
            menu.menu.add(getString(R.string.remove))
            menu.menu.add(getString(R.string.details))
//            menu.menu.add(getString(R.string.move))
            menu.setOnMenuItemClickListener {
                when (it.title) {
                    getString(R.string.upload_to_server) -> {
                        reference?.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {

                            }

                            override fun onDataChange(data: DataSnapshot) {
                                if (data.hasChildren()) {
                                    var folders: ArrayList<String> = arrayListOf()
                                    data.children.forEach { children ->
                                        folders.add(children.key!!)
                                    }
                                    AlertDialog.Builder(this@DetailsImageActivity)
                                        .setTitle(getString(R.string.select_folder))
                                        .setItems(folders.toTypedArray()) { dialog, which ->
                                            dialog.dismiss()
                                            val folder = folders[which]
                                            uploadImage(folder)
                                        }.show()
                                } else {
                                    var createFolderDialog = AlertDialog.Builder(this@DetailsImageActivity)
                                        .setMessage(getString(R.string.upload_warning))
                                        .setPositiveButton(getString(R.string.create)) { dialog, which ->
                                            dialog.dismiss()
                                            showCreateFolderDialog()
                                        }.create()

                                    createFolderDialog.setOnShowListener {
                                        createFolderDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                            .setTextColor(
                                                ContextCompat.getColor(
                                                    this@DetailsImageActivity,
                                                    R.color.colorPrimary
                                                )
                                            )
                                    }
                                    createFolderDialog.show()
                                }
                            }
                        })
                    }
                    getString(R.string.remove) -> {
                        var file = File(intent.getStringExtra("path"))
                        var delete = file.delete()
                        if (delete) {
                            MediaScannerConnection.scanFile(this, arrayOf(intent.getStringExtra("path")), null, null)
                            Toast.makeText(this, getString(R.string.success_remove_file), Toast.LENGTH_SHORT).show()

                            var data = Intent()
                            data.putExtra("position", intent?.getIntExtra("position", -1))
                            setResult(Activity.RESULT_OK, data)
                            finish()
                        } else {
                            Toast.makeText(this, getString(R.string.remove_error), Toast.LENGTH_SHORT).show()
                        }

                    }
                    getString(R.string.details) -> {
                        var file = File(intent.getStringExtra("path"))
                        var infoDialog = AlertDialog.Builder(this@DetailsImageActivity)
                            .setMessage(getString(R.string.details_info, file.name, file.getSizeByMb(), file.path))
                            .setPositiveButton(getString(R.string.close)) { dialog, which ->
                                dialog.dismiss()
                            }.create()

                        infoDialog.setOnShowListener {
                            infoDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                .setTextColor(
                                    ContextCompat.getColor(
                                        this@DetailsImageActivity,
                                        R.color.colorPrimary
                                    )
                                )
                        }
                        infoDialog.show()
                    }
//                    getString(R.string.move) -> {
//                        var from = File(intent.getStringExtra("path"))
//
//                        var foldersArray: ArrayList<String> = arrayListOf()
//                        folders.forEach { it ->
//                            foldersArray.add(it)
//                        }
//                        AlertDialog.Builder(this@DetailsImageActivity)
//                            .setTitle(getString(R.string.select_folder))
//                            .setItems(foldersArray.toTypedArray()) { dialog, which ->
//                                try {
//                                    FileUtils.copyFile(from, File(foldersPath[which] + "/" + from.name))
//                                    dialog.dismiss()
//                                } catch (e: IOException) {
//                                    e.printStackTrace()
//                                }
//                            }.show()
//                    }
                }
                true
            }
            menu.show()
        }
    }

    private fun uploadImage(folder: String) {
        var file = File(intent.getStringExtra("path"))
        uploadTask = storage?.reference?.child(file.name)?.putFile(Uri.fromFile(file))

        var progressDialog = ProgressDialog(this, R.style.MyProgressDialog)
        progressDialog?.setCancelable(false)
        progressDialog?.setMessage(getString(R.string.load))
        progressDialog?.show()

        uploadTask?.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, getString(R.string.upload_error), Toast.LENGTH_SHORT).show()
        }
            ?.addOnCompleteListener {
                progressDialog.dismiss()

                if (it.isSuccessful) {
                    Toast.makeText(this, getString(R.string.photo_uploaded) + " " + folder, Toast.LENGTH_SHORT).show()
                    dataBase?.getReference("folders")?.child(folder)
                        ?.child(SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS", Locale.getDefault()).format(Date().time))
                        ?.setValue(RemoteImage(file.name))

                }
            }
    }

    private fun showCreateFolderDialog() {
        var createFolderLayout = LayoutInflater.from(this).inflate(R.layout.create_folder_layout, null, false)
        var createFolderDialog = AlertDialog.Builder(this).setView(createFolderLayout).create()

        createFolderDialog.show()

        createFolderLayout.findViewById<Button>(R.id.btn_create).click {
            var folderName = createFolderLayout.findViewById<EditText>(R.id.edit_folder).text.toString()
            if (folderName.isEmpty()) {
                Toast.makeText(this, getString(R.string.write_folder_name), Toast.LENGTH_SHORT).show()
            } else {
                createFolderDialog.dismiss()
                uploadImage(folderName)
            }
        }
    }

    override fun onStop() {
        if (uploadTask != null) {
            uploadTask?.cancel()
        }
        super.onStop()
    }
}
