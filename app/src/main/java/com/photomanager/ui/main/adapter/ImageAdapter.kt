package com.photomanager.ui.main.adapter

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.photomanager.R
import java.io.File


class ImageAdapter : BaseQuickAdapter<File, BaseViewHolder>(R.layout.layout_image, null) {
    override fun convert(helper: BaseViewHolder, item: File) {

        Glide.with(mContext).load("file://" + item)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(helper.getView(R.id.iv_image))
    }
}