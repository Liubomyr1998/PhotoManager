package com.photomanager.ui.main.local.list

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import com.photomanager.R
import com.photomanager.ui.main.adapter.ImageAdapter
import com.photomanager.ui.main.local.details.DetailsImageActivity
import com.photomanager.util.launchActivity
import kotlinx.android.synthetic.main.activity_remote_images.*
import java.io.File


class ImagesLIstActivity : AppCompatActivity() {

    var imageAdapter: ImageAdapter? = null
    var originData: MutableList<File> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.photomanager.R.layout.activity_local_images)

        toolbar_images.title = intent.getStringExtra("folder")
        setSupportActionBar(toolbar_images)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        imageAdapter = ImageAdapter()
        rv_images.adapter = imageAdapter
        rv_images.layoutManager = GridLayoutManager(this, 2)

        intent.getStringArrayListExtra("images").forEach {
            originData.add(File(it))
        }
        imageAdapter?.setNewData(originData)

        toolbar_images.setNavigationOnClickListener {
            finish()
        }

        imageAdapter?.setOnItemClickListener { adapter, view, position ->
            launchActivity<DetailsImageActivity>(requestCode = 101) {
                putExtra("path", imageAdapter?.getItem(position)?.absolutePath)
                putExtra("position", position)
                putExtra("foldersPath", intent.getStringArrayListExtra("foldersPath"))
                putExtra("folders", intent.getStringArrayListExtra("folders"))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            imageAdapter?.data?.removeAt(data?.getIntExtra("position", -1)!!)
            imageAdapter?.notifyDataSetChanged()
        }
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
        var filteredList: MutableList<File> = arrayListOf()
        originData.forEach {
            if(it.name?.toLowerCase()?.contains(p0?.toLowerCase()!!)!!){
                filteredList.add(it)
            }
        }
        imageAdapter?.setNewData(filteredList)
    }
}