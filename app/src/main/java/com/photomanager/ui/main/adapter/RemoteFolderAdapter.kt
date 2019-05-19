package com.photomanager.ui.main.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.photomanager.R
import com.photomanager.model.RemoteFolderModel

class RemoteFolderAdapter :
    BaseQuickAdapter<RemoteFolderModel, BaseViewHolder>(R.layout.layout_remote_image_folder, null) {

    override fun convert(helper: BaseViewHolder, item: RemoteFolderModel) {
        helper.setText(R.id.tv_folder, item.folderName + " (" + item.folderSize + ")")
    }
}
