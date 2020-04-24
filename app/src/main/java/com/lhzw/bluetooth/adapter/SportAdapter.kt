package com.lhzw.bluetooth.adapter

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
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
class SportAdapter(data: List<SportInfoAddrBean>) : BaseQuickAdapter<SportInfoAddrBean, BaseViewHolder>(R.layout.item_sport_list, data), View.OnTouchListener {
    @SuppressLint("ResourceType")
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
            helper.convertView.setOnTouchListener(this@SportAdapter)
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    v?.findViewById<TextView>(R.id.tv_duration_title)?.setTextColor(v?.context?.resources?.getColor(R.color.white)!!)
                    v?.findViewById<TextView>(R.id.tv_duration)?.setTextColor(v?.context?.resources?.getColor(R.color.white)!!)
                    v?.findViewById<TextView>(R.id.tv_calorie_title)?.setTextColor(v?.context?.resources?.getColor(R.color.white)!!)
                    v?.findViewById<TextView>(R.id.tv_calorie)?.setTextColor(v?.context?.resources?.getColor(R.color.white)!!)
                    v?.findViewById<TextView>(R.id.tv_allocation_speed_title)?.setTextColor(v?.context?.resources?.getColor(R.color.white)!!)
                    v?.findViewById<TextView>(R.id.tv_allocation_speed)?.setTextColor(v?.context?.resources?.getColor(R.color.white)!!)
                    v?.findViewById<TextView>(R.id.tv_steps_title)?.setTextColor(v?.context?.resources?.getColor(R.color.white)!!)
                    v?.findViewById<TextView>(R.id.tv_steps)?.setTextColor(v?.context?.resources?.getColor(R.color.white)!!)
                    v?.findViewById<View>(R.id.splite)?.setBackgroundColor(v?.context?.resources?.getColor(R.color.white)!!)
                    v?.findViewById<TextView>(R.id.tv_distance)?.setTextColor(v?.context?.resources?.getColor(R.color.white)!!)
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    v?.findViewById<TextView>(R.id.tv_duration_title)?.setTextColor(v?.context?.resources?.getColor(R.color.gray)!!)
                    v?.findViewById<TextView>(R.id.tv_duration)?.setTextColor(v?.context?.resources?.getColor(R.color.gray)!!)
                    v?.findViewById<TextView>(R.id.tv_calorie_title)?.setTextColor(v?.context?.resources?.getColor(R.color.gray)!!)
                    v?.findViewById<TextView>(R.id.tv_calorie)?.setTextColor(v?.context?.resources?.getColor(R.color.gray)!!)
                    v?.findViewById<TextView>(R.id.tv_allocation_speed_title)?.setTextColor(v?.context?.resources?.getColor(R.color.gray)!!)
                    v?.findViewById<TextView>(R.id.tv_allocation_speed)?.setTextColor(v?.context?.resources?.getColor(R.color.gray)!!)
                    v?.findViewById<TextView>(R.id.tv_steps_title)?.setTextColor(v?.context?.resources?.getColor(R.color.gray)!!)
                    v?.findViewById<TextView>(R.id.tv_steps)?.setTextColor(v?.context?.resources?.getColor(R.color.gray)!!)
                    v?.findViewById<View>(R.id.splite)?.setBackgroundColor(v?.context?.resources?.getColor(R.color.gray)!!)
                    v?.findViewById<TextView>(R.id.tv_distance)?.setTextColor(v?.context?.resources?.getColor(R.color.gray)!!)
                }
                else -> {

                }
            }
        }
        return false
    }

}

//class SportAdapter(val mContext: Context, var data: List<SportInfoAddrBean>?) : RecyclerView.Adapter<SportAdapter.Viewholder>(), View.OnClickListener {
//
//    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val im_portrait = itemView.findViewById<ImageView>(R.id.im_portrait)
//        val tv_duration = itemView.findViewById<TextView>(R.id.tv_duration)
//    }
//
//    override fun onCreateViewHolder(group: ViewGroup, position: Int): Viewholder {
//        val holder = Viewholder(LayoutInflater.from(mContext).inflate(R.layout.item_sport_list, null))
//        holder.itemView.setOnClickListener(this)
//        return holder
//    }
//
//    override fun getItemCount(): Int {
//        if (data == null) return 0
//        return data?.size!!
//    }
//
//    override fun onBindViewHolder(holder: Viewholder, position: Int) {
//        data?.let {
//            when (it[position].activity_type) {
//                Constants.ACTIVITY_HIKING -> {
//                    holder.im_portrait.setImageResource(R.mipmap.icon_hiking_sport)
//                }
//                Constants.ACTIVITY_INDOOR -> {
//                    holder.im_portrait.setImageResource(R.mipmap.icon_indoor_sport)
//                }
//                Constants.ACTIVITY_RUNNING -> {
//                    holder.im_portrait.setImageResource(R.mipmap.icon_running_sport)
//                }
//            }
//            val date = BaseUtils.formatData(it[position].activity_start, it[position].activity_end)
//            holder.tv_duration.setText("${date[2]}")
//        }
//    }
//
//    override fun onClick(v: View?) {
//
//    }


//}