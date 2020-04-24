package com.lhzw.bluetooth.ui.fragment

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.hwangjr.rxbus.annotation.Subscribe
import com.hwangjr.rxbus.annotation.Tag
import com.hwangjr.rxbus.thread.EventThread
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.adapter.SportAdapter
import com.lhzw.bluetooth.adapter.SportTypeAdapter
import com.lhzw.bluetooth.base.BaseFragment
import com.lhzw.bluetooth.bean.SportInfoAddrBean
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.ui.activity.SportInfoActivity
import com.lhzw.bluetooth.uitls.BaseUtils
import kotlinx.android.synthetic.main.fragment_sports.*

/**
 * Created by hecuncun on 2019/11/13
 */
class SportsFragment : BaseFragment(), SportTypeAdapter.OnItemClickListener {
    private var list: List<SportInfoAddrBean>? = null
    private var filter_list: Array<String>? = null
    private var adapter: SportAdapter? = null

    companion object {
        fun getInstance(): SportsFragment = SportsFragment()
    }

    override fun attachLayoutRes(): Int = R.layout.fragment_sports

    override fun initView(view: View) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RxBus.getInstance().register(this)
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = [Tag("reflesh")])
    fun reflesh(str: String) {
        if (hasLoadData) {
            Log.e("Tag", "reflesh ...")
            list = CommOperation.query(SportInfoAddrBean::class.java)
            adapter?.run {
                setNewData(list)
                notifyDataSetChanged()
            }
        }
    }

    override fun lazyLoad() {
        Log.e("Tag", "lazyLoad ...")
        filter_list = resources.getStringArray(R.array.sport_type_list)
        val manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        recycler_title.layoutManager = manager
        BaseUtils.ifNotNull(activity, filter_list) { v, list ->
            val titleAdapter = SportTypeAdapter(v, list, this@SportsFragment)
            recycler_title.adapter = titleAdapter
        }

        list = CommOperation.query(SportInfoAddrBean::class.java)
        adapter = SportAdapter(list!!)
        adapter?.openLoadAnimation { view ->
            arrayOf(ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.1f, 1.0f), ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.1f, 1.0f))
        }
        adapter?.setOnItemClickListener { _, view, position ->
            body(view, position)
        }
        recyclerview.layoutManager = LinearLayoutManager(context)
        recyclerview.adapter = adapter
    }

    private var body: (view: View, position: Int) -> Any = { _, position ->
        list?.get(position)?.apply {
            var date = BaseUtils.formatData(activity_start, activity_end)
            val intent = Intent(context, SportInfoActivity::class.java)
            intent.putExtra("mark", sport_detail_mark)
            intent.putExtra("ymt", date[0])
            intent.putExtra("type", activity_type)
            intent.putExtra("duration", date[2])
            startActivity(intent)
        }
        // ToastUtils.toastSuccess("OK")
        Log.e("Tag", "点击事件 ： $position")
    }

    override fun onDestroy() {
        super.onDestroy()
        RxBus.getInstance().unregister(this)
        list = null
        adapter = null
    }

    override fun onItemClick(pos: Int) {
        Log.e("Tag", "onClick pos  $pos")
    }
}