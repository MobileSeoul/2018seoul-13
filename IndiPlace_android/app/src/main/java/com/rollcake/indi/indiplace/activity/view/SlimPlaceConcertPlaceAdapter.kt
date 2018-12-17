package com.rollcake.indi.indiplace.activity.view

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rollcake.indi.indiplace.R
import kotlinx.android.synthetic.main.main_recyler_list.view.*

class SlimPlaceConcertPlaceAdapter(val item : ArrayList<ConcertInfo>, val context : Context, val result : getResult ) : RecyclerView.Adapter<SlimPlaceConcertPlaceAdapter.ViewHolder>() {

    interface getResult {
        fun getItemClick(result : ConcertInfo)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.slim_place_main_recyler_list, p0, false))
    }

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0?.title?.text = item.get(p1).artistName
        p0?.content?.text = item.get(p1).location

        p0?.listBack?.setOnClickListener {
            result.getItemClick(item.get(p1))
        }

    }

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val title = view.recycleTitle
        val content = view.recycleSubContent
        val image = view.recycleImage
        val listBack = view.list_back

    }

}