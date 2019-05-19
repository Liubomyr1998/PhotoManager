package com.photomanager.ui.main.remote

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.photomanager.R
import com.photomanager.model.RemoteFolderModel
import com.photomanager.ui.main.adapter.LocalFolderAdapter
import com.photomanager.ui.main.adapter.RemoteFolderAdapter
import com.photomanager.ui.main.remote.list.RemoteImagesLIstActivity
import com.photomanager.util.launchActivity
import kotlinx.android.synthetic.main.fragment_remote_gallery.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import java.util.*

class RemoteGalleryFragment : Fragment() {
    var folderAdapter: RemoteFolderAdapter? = null
    var dataBase: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var root = inflater.inflate(R.layout.fragment_remote_gallery, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        folderAdapter = RemoteFolderAdapter()
        rv_folders.adapter = folderAdapter
        rv_folders.layoutManager = GridLayoutManager(activity!!, 2)

        dataBase = FirebaseDatabase.getInstance()
        var reference = dataBase?.getReference("folders")
        storage = FirebaseStorage.getInstance("gs://photomanager-f8426.appspot.com")

        reference?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                var a = ""
            }

            override fun onDataChange(data: DataSnapshot) {
                if (data.hasChildren()) {
                    var folders: ArrayList<RemoteFolderModel> = arrayListOf()
                    data.children.forEach { children ->
                        var remoteFolderModel = RemoteFolderModel()
                        remoteFolderModel.folderName = children.key

                        if (children.hasChildren()) {
                            remoteFolderModel.folderSize = children.children.count()
                        } else {
                            remoteFolderModel.folderSize = 0
                        }

                        folders.add(remoteFolderModel)
                    }
                    if (activity == null) return

                    folderAdapter?.setNewData(folders)

                    rv_folders?.visiable()
                    text_empty?.gone()
                } else {
                    rv_folders?.gone()
                    text_empty?.visiable()
                }
            }
        })


        add_dir.click {
            showCreateFolderDialog()
        }
        folderAdapter?.setOnItemClickListener { adapter, view, position ->
            launchActivity<RemoteImagesLIstActivity> {
                putExtra("folder", folderAdapter?.getItem(position)?.folderName)
            }
        }
        folderAdapter?.setOnItemLongClickListener { adapter, view, position ->
            var removeDirectoryDialog = AlertDialog.Builder(activity!!)
                .setTitle("Видалити папку " + folderAdapter?.getItem(position)?.folderName + "?")
                .setPositiveButton("Видалити") { dialog, which ->
                    dialog.dismiss()
                    dataBase?.getReference("folders")?.child(folderAdapter?.getItem(position)?.folderName!!)
                        ?.removeValue()
                }
                .create()
            removeDirectoryDialog.setOnShowListener {
                removeDirectoryDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(
                        ContextCompat.getColor(
                            activity!!,
                            R.color.colorPrimary
                        )
                    )
            }
            removeDirectoryDialog.show()

            true
        }
    }


    private fun showCreateFolderDialog() {
        var createFolderLayout = LayoutInflater.from(activity!!).inflate(R.layout.create_folder_layout, null, false)
        var createFolderDialog = AlertDialog.Builder(activity!!).setView(createFolderLayout).create()

        createFolderDialog.show()

        createFolderLayout.findViewById<Button>(R.id.btn_create).click {
            var folderName = createFolderLayout.findViewById<EditText>(R.id.edit_folder).text.toString()
            if (folderName.isEmpty()) {
                Toast.makeText(activity!!, "Введіть назву папки", Toast.LENGTH_SHORT).show()
            } else {
                createFolderDialog.dismiss()
                dataBase?.getReference("folders")?.child(folderName)?.setValue("")
            }
        }
    }
}
