package com.rollcake.indi.indiplace.activity.view

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rollcake.indi.indiplace.R
import kotlinx.android.synthetic.main.comment_recyler_list.view.*

class CommentAdapter(val item : ArrayList<String>, val context : Context) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.comment_recyler_list, p0, false))
    }

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0?.title?.text = item.get(p1)
    }

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val title = view.comment_text

    }

}