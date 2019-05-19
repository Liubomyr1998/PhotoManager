package com.photomanager.ui.main.remote.list

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.photomanager.R
import com.photomanager.model.RemoteImage
import com.photomanager.model.RemoteListImageModel
import com.photomanager.ui.main.adapter.RemoteImageAdapter
import com.photomanager.ui.main.remote.details.RemoteDetailsImageActivity
import com.photomanager.util.launchActivity
import kotlinx.android.synthetic.main.activity_remote_images.*
import pers.victor.ext.gone
import pers.victor.ext.visiable
import java.io.File


class RemoteImagesLIstActivity : AppCompatActivity() {
    var dataBase: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var imageAdapter: RemoteImageAdapter? = null
    var originData: MutableList<RemoteListImageModel>? = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_images)

        toolbar_images.title = intent.getStringExtra("folder")
        setSupportActionBar(toolbar_images)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        dataBase = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance("gs://photomanager-f8426.appspot.com")

        imageAdapter = RemoteImageAdapter()
        rv_images.adapter = imageAdapter
        rv_images.layoutManager = GridLayoutManager(this, 2)

        imageAdapter?.setNewData(arrayListOf())

        getImages()

        toolbar_images.setNavigationOnClickListener {
            finish()
        }

        imageAdapter?.setOnItemClickListener { adapter, view, position ->
            launchActivity<RemoteDetailsImageActivity>(requestCode = 100) {
                putExtra("uri", imageAdapter?.getItem(position)?.uri)
                putExtra("path", imageAdapter?.getItem(position)?.path)
                putExtra("child", imageAdapter?.getItem(position)?.child)
                putExtra("folder", imageAdapter?.getItem(position)?.folder)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            getImages()
        }
    }
    private fun getImages(){
        dataBase?.getReference("folders")?.child(intent.getStringExtra("folder"))?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChildren()) {
                    p0.children.forEach { data ->
                        storage?.reference?.child(data.getValue(RemoteImage::class.java)?.path!!)
                            ?.downloadUrl?.addOnSuccessListener {

                            imageAdapter?.data?.add(  RemoteListImageModel(
                                it,
                                data.getValue(RemoteImage::class.java)?.path!!,
                                data.key,
                                intent.getStringExtra("folder")
                            ))
                            imageAdapter?.notifyDataSetChanged()
                        }?.addOnCompleteListener {
                            if(it.isSuccessful){
                                originData = imageAdapter?.data
                            }
                        }
                    }
                    rv_images?.visiable()
                    text_empty?.gone()
                    progress?.gone()
                } else {
                    progress?.gone()
                    rv_images?.gone()
                    text_empty?.visiable()
                }
            }
        })
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_search, menu)

        val searchItem = menu?.findItem(com.photomanager.R.id.action_search)

        val searchManager = this.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        var searchView: SearchView? = null
        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
        }
        if (searchView != null) {
            searchView!!.setSearchableInfo(searchManager.getSearchableInfo(this.componentName))
        }
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                filter(p0)
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun filter(p0: String?) {
        var filteredList: MutableList<RemoteListImageModel> = arrayListOf()
        originData?.forEach {
            if(it.path?.toLowerCase()?.contains(p0?.toLowerCase()!!)!!){
                filteredList.add(it)
            }
        }
        imageAdapter?.setNewData(filteredList)
    }
}