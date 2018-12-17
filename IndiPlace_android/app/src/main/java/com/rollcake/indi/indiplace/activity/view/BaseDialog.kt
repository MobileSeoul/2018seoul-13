package com.rollcake.indi.indiplace.activity.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.rollcake.indi.indiplace.R

class BaseDialog(context: Context?) : Dialog(context) {

    private var title: TextView? = null
    private var content: TextView? = null
    private var okBtn: Button? = null
    private var noBtn: Button? = null
    private var listener: getOkBtnListener? = null
    private var titleMsg: String? = null
    private var contentMsg: String? = null
    private var yesStr: String? = null
    private var noStr: String? = null
    private var resourceImg: Int = 0
    private var popupImg: ImageView? = null

    interface getOkBtnListener {
        fun ok()
        fun no()
    }


    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_base)
        title = findViewById(R.id.title) as TextView
        content = findViewById(R.id.content) as TextView
        noBtn = findViewById(R.id.noBtn) as Button
        okBtn = findViewById(R.id.okBtn) as Button
        popupImg = findViewById(R.id.popup_img) as ImageView

        title!!.text = titleMsg
        content!!.setText(contentMsg)
        okBtn!!.text = yesStr
        noBtn!!.setText(noStr)

        if (resourceImg == 0) {
            popupImg!!.visibility = View.GONE
        } else {
            popupImg!!.setImageResource(resourceImg)
        }

        noBtn!!.setOnClickListener(View.OnClickListener { dismiss() })

        okBtn!!.setOnClickListener {
            if (listener != null) {
                listener!!.ok()
            }
            dismiss()
        }
    }


    fun setTitle(msg: String) {
        this.titleMsg = msg
    }

    fun setContent(msg: String) {
        this.contentMsg = msg
    }

    fun initDialog(titleMsg: String, contentMsg: String, resource: Int) {
        this.titleMsg = titleMsg
        this.contentMsg = contentMsg

        if (resource != 0) {
            this.resourceImg = resource
        } else {
            resourceImg = 0
        }
    }

    fun buttonInit(yesStr: String, noStr: String) {
        this.yesStr = yesStr
        this.noStr = noStr
    }

    fun checkListenr(listener: getOkBtnListener) {
        this.listener = listener
    }

    fun getNoBtn(): Button? {
        return noBtn
    }

    fun getPopupImg(): ImageView? {
        return popupImg
    }

}