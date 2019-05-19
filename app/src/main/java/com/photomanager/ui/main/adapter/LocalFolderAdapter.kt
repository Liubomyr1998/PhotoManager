package com.photomanager.ui.main.adapter

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.photomanager.R
import com.photomanager.model.FolderModel

class LocalFolderAdapter : BaseQuickAdapter<FolderModel, BaseViewHolder>(R.layout.layout_image_folder, null) {
    override fun convert(helper: BaseViewHolder, item: FolderModel) {
        helper.setText(R.id.tv_folder, item.folderName + " (" + item.folderSize + ")")

        Glide.with(mContext).load("file://" + item.previewImage)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(helper.getView(R.id.iv_image))
    }
}
