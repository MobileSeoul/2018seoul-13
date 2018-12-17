package com.rollcake.indi.indiplace.activity.view

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.rollcake.indi.indiplace.R
import kotlinx.android.synthetic.main.main_recyler_list.view.*

class ConcertPlaceAdapter(val item : ArrayList<ArtistInfo>, val context : Context, val result : getResult ) : RecyclerView.Adapter<ConcertPlaceAdapter.ViewHolder>() {

    interface getResult {
        fun getItemClick(result : Int)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.main_recyler_list, p0, false))
    }

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0?.title?.text = item.get(p1).title
        p0?.content?.text = item.get(p1).content
        Glide.with(context).load(item.get(p1).url).apply(RequestOptions.circleCropTransform()).into(p0?.image)


        p0?.listBack?.setOnClickListener {
            result.getItemClick(item.get(p1).artistId.toInt())
        }

    }

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val title = view.recycleTitle
        val content = view.recycleSubContent
        val image = view.recycleImage
        val listBack = view.list_back

    }

}