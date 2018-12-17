package com.rollcake.indi.indiplace.activity.view

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.rollcake.indi.indiplace.AppApplication
import com.rollcake.indi.indiplace.R
import com.rollcake.indi.indiplace.utils.Logs
import kotlinx.android.synthetic.main.slim_main_recyler_list.view.*

class SlimConcertPlaceAdapter(val item : ArrayList<ArtistInfo>, val context : Context, val result : getResult ) : RecyclerView.Adapter<SlimConcertPlaceAdapter.ViewHolder>() {

    interface getResult {
        fun getItemClick(result : Int)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.slim_main_recyler_list, p0, false))
    }

    override fun getItemCount(): Int {
        return item.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0?.title?.text = item.get(p1).title
        p0?.content?.text = item.get(p1).content + " / " +getGener(item.get(p1).gener)
        Glide.with(context).load(item.get(p1).url).into(p0?.image)
        p0?.listBack?.setOnClickListener {
            result.getItemClick(item.get(p1).artistId.toInt())
        }

    }

    private fun getGener(gener: String?): Any? {
        return if (gener == "!") {
            "기악"
        } else if (gener == "2") {
            "음악"
        } else if (gener == "3") {
            "퍼포먼스"
        } else if (gener == "4") {
            "전통"
        } else {
            "전부"
        }
    }

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val title = view.recycleTitle
        val content = view.recycleSubContent
        val image = view.artist_image
        val listBack = view.list_back

    }

}