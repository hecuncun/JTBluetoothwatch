package com.lhzw.bluetooth.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.bean.SportInfoAddrBean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.uitls.BaseUtils

/**
 *
@author：created by xtqb
@description:
@date : 2019/11/18 11:10
 *
 */
class SportAdapter(data: List<SportInfoAddrBean>) : BaseQuickAdapter<SportInfoAddrBean, BaseViewHolder>(R.layout.item_sport_list, data) {
    override fun convert(helper: BaseViewHolder, item: SportInfoAddrBean?) {
        item?.apply {
            when (activity_type) {
                Constants.ACTIVITY_HIKING -> {
                    helper.setImageResource(R.id.im_portrait, R.mipmap.icon_hiking_sport)
                }
                Constants.ACTIVITY_INDOOR -> {
                    helper.setImageResource(R.id.im_portrait, R.mipmap.icon_indoor_sport)
                }
                Constants.ACTIVITY_RUNNING -> {
                    helper.setImageResource(R.id.im_portrait, R.mipmap.icon_running_sport)
                }
            }
            var date = BaseUtils.formatData(item.activity_start, item.activity_end)
            helper.setText(R.id.tv_ymt, "${date[0]}")
            helper.setText(R.id.tv_time, "${date[1]}")
            helper.setText(R.id.tv_duration, "${date[2]}")
        }
    }
}