package com.photomanager.ui.main.local

import android.Manifest
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.photomanager.R
import com.photomanager.model.FolderModel
import com.photomanager.model.ImageModel
import com.photomanager.ui.main.adapter.LocalFolderAdapter
import com.photomanager.ui.main.local.list.ImagesLIstActivity
import com.photomanager.util.launchActivity
import kotlinx.android.synthetic.main.fragment_local_gallery.*

class LocalGalleryFragment : Fragment() {
    var folderAdapter: LocalFolderAdapter? = null
    var allImages: HashMap<String, ArrayList<ImageModel>> = HashMap()
    var foldersPathList: HashMap<String?, String?> = HashMap()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var root = inflater.inflate(R.layout.fragment_local_gallery, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        folderAdapter = LocalFolderAdapter()
        rv_folders.adapter = folderAdapter
        rv_folders.layoutManager = GridLayoutManager(activity!!, 2)

        val request = permissionsBuilder(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ).build()

        request.listeners {
            onAccepted { permissions ->
                getFoldersWithImages()
            }

            onDenied { permissions ->
                Toast.makeText(activity!!, getString(R.string.no_pemission), Toast.LENGTH_SHORT).show()
            }

            onPermanentlyDenied { permissions ->
            }

            onShouldShowRationale { permissions, nonce ->
            }
        }
        request.send()


        folderAdapter?.setOnItemClickListener { adapter, view, position ->
            launchActivity<ImagesLIstActivity> {
                val imagesPath: ArrayList<String> = arrayListOf()
                val foldersPath: ArrayList<String> = arrayListOf()
                val folders: ArrayList<String> = arrayListOf()
                allImages[folderAdapter?.getItem(position)?.folderName]?.forEach {
                    imagesPath.add(it.path!!)
                }
                foldersPathList.keys?.forEach {
                    foldersPath.add(it!!)
                    folders.add(foldersPathList[it]!!)
                }
                putExtra("images", imagesPath)
                putExtra("folder", folderAdapter?.getItem(position)?.folderName)
                putExtra("foldersPath", foldersPath)
                putExtra("folders", folders)
            }
        }

        swr_folders.setOnRefreshListener {
            request.send()

            swr_folders.isRefreshing = false
        }
    }


    private fun getFoldersWithImages() {
        allImages.clear()

        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor?
        val column_index_data: Int
        val column_index_folder_name: Int
        var isFolder = false
        val projection = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

        val orderBy = MediaStore.Images.Media.DATE_TAKEN
        cursor = activity?.contentResolver?.query(uri, projection, null, null, "$orderBy DESC")

        column_index_data = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

        while (cursor.moveToNext()) {
            val absolutePathOfImage = cursor.getString(column_index_data)

            val folderName = cursor.getString(column_index_folder_name)
            val folderPath =
                absolutePathOfImage.substring(0, absolutePathOfImage.indexOf(folderName) + folderName.length)

            foldersPathList[folderPath] = folderName

            allImages.keys.forEach {
                if (it == cursor.getString(column_index_folder_name)) {
                    isFolder = true
                    return@forEach
                } else {
                    isFolder = false
                }
            }

            if (isFolder) {
                allImages[folderName]?.add(ImageModel(folderName, folderPath, absolutePathOfImage))
            } else {
                val folderModel = FolderModel()
                folderModel.folderName = cursor.getString(column_index_folder_name)
                folderModel.previewImage = absolutePathOfImage

                allImages[folderName] = arrayListOf()
                allImages[folderName]?.add(ImageModel(folderName, folderPath, absolutePathOfImage))
            }
        }

        val folders: ArrayList<FolderModel> = arrayListOf()
        allImages.keys.forEach {
            folders.add(FolderModel(it, allImages[it]!![0].folderPath, allImages[it]?.size, allImages[it]!![0].path))
        }
        folderAdapter?.setNewData(folders)
    }
}
