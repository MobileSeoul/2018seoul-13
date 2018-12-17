package com.rollcake.indi.indiplace.activity.view

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.rollcake.indi.indiplace.AppApplication
import com.rollcake.indi.indiplace.R
import com.rollcake.indi.indiplace.utils.Logs
import kotlinx.android.synthetic.main.main_recyler_list.view.*

class PlaceConcertPlaceAdapter(val item : ArrayList<ConcertInfo>, val context : Context, val result : getResult ) : RecyclerView.Adapter<PlaceConcertPlaceAdapter.ViewHolder>() {

    val TAG = PlaceConcertPlaceAdapter::class.java.simpleName

    interface getResult {
        fun getItemClick(result : ConcertInfo)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.main_recyler_list, p0, false))
    }

    override fun getItemCount(): Int {
        return item.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0?.title?.text = item.get(p1).artistName
        p0?.content?.text = item.get(p1).location + " " + item.get(p1).placeContent + " " + getTime(item.get(p1).startTime , item.get(p1).endTime)
        Logs.e(TAG , AppApplication.DOMAIN+item.get(p1).artistImg);
        Glide.with(context).load(AppApplication.DOMAIN+item.get(p1).artistImg).into(p0?.artistImage)
        p0?.listBack?.setOnClickListener {
            result.getItemClick(item.get(p1))
        }

    }

    private fun getTime(startTime: String?, endTime: String?): Any? {

        return startTime?.substring(5,13)+"~"+ endTime?.subSequence(12,13)
    }

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val title = view.recycleTitle
        val content = view.recycleSubContent
        val image = view.recycleImage
        val listBack = view.list_back
        val artistImage = view.artist_image

    }

}