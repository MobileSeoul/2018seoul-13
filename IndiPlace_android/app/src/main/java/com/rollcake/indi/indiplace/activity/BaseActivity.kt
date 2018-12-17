package com.rollcake.indi.indiplace.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.rollcake.indi.indiplace.AppApplication

open class BaseActivity : AppCompatActivity() {

    val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    protected fun addLocation(locationList: ArrayList<String>) {
        locationList.add("강남구")
        locationList.add("강동구")
        locationList.add("강북구")
        locationList.add("강서구")
        locationList.add("관악구")
        locationList.add("광진구")
        locationList.add("구로구")
        locationList.add("금천구")
        locationList.add("노원구")
        locationList.add("도봉구")
        locationList.add("동대문구")
        locationList.add("동작구")
        locationList.add("마포구")
        locationList.add("서대문구")
        locationList.add("서초구")
        locationList.add("성동구")
        locationList.add("성북구")
        locationList.add("송파구")
        locationList.add("양천구")
        locationList.add("영등포구")
        locationList.add("용산구")
        locationList.add("은평구")
        locationList.add("종로구")
        locationList.add("중구")
        locationList.add("중랑구")
    }

    protected fun addGener(locationList: ArrayList<String>) {
        locationList.add("기악")
        locationList.add("퍼포먼스")
        locationList.add("전통")
        locationList.add("음악")
    }

    protected fun goToast(text: CharSequence) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    protected fun setImmersiveMode() {
        this.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    protected fun activityFinish(view :View){
        finish()
    }

    protected fun serverError(errorString : String ){
        Toast.makeText(this@BaseActivity , errorString , Toast.LENGTH_LONG).show()
        finish()
    }

    fun showdialog(finishCheck : Boolean) {
        val builder = AlertDialog.Builder(this@BaseActivity)
        builder.setTitle("인디플레이스")
        builder.setMessage("로그인을 하시겠습니까?")
        builder.setPositiveButton("네 알겠습니다") { _, which ->
            val intent: Intent = Intent(this@BaseActivity, LoginActivity::class.java).putExtra("key", true)
            startActivity(intent)
            if(finishCheck){
                finish()
            }
        }
        builder.setNegativeButton("아니요") { dialog, which ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun getGenreToInt(value :String) : Int {
        if(value == "기악"){
            return 1
        }else if(value == "음악"){
            return 2
        }else if(value == "퍼포먼스"){
            return 3
        }else if(value == "전통"){
            return 4
        }else{
            return 5
        }
    }

    fun getGenreToString(value :Int ) : String {
        if(value == 1){
            return "기악"
        }else if(value == 2){
            return "음악"
        }else if(value == 3){
            return "퍼포먼스"
        }else{
            return "전통"
        }
    }

}