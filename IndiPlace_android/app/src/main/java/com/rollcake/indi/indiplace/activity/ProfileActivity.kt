package com.rollcake.indi.indiplace.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.volley.Request
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.rollcake.indi.indiplace.AppApplication
import com.rollcake.indi.indiplace.AppApplication.DOMAIN
import com.rollcake.indi.indiplace.R
import com.rollcake.indi.indiplace.activity.view.ArtistInfo
import com.rollcake.indi.indiplace.activity.view.SlimConcertPlaceAdapter
import com.rollcake.indi.indiplace.utils.Logs
import com.rollcake.indi.indiplace.utils.ServerNetworking
import kotlinx.android.synthetic.main.activity_profile.*
import org.json.JSONException
import org.json.JSONObject

class ProfileActivity : BaseActivity() {

    val TAG = ProfileActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val intent = intent
        val memberId = intent.getStringExtra("memberId")
        favorite_star.layoutManager = LinearLayoutManager(this)

        ServerNetworking.sendToMobileServer(this@ProfileActivity, Request.Method.GET, AppApplication.DOMAIN + "/member/" + memberId, null, null, object : ServerNetworking.getResult {
            override fun getResultText(text: String) {
                Logs.e(TAG, text)
                try {
                    val resultObj = JSONObject(text)
                    if (resultObj.getBoolean("key")) {
                        val result = resultObj.getJSONObject("message")
                        val name = result.getString("name")
                        val emailAddress = result.getString("emailAddress")
                        val sex = result.getString("sex")
                        val year = result.getString("year")
                        val loc = result.getString("location")
                        val memberType = result.getString("memberType")

                        if (memberType == "U") {
                            Glide.with(this@ProfileActivity).load(R.drawable.normal_icon).apply(RequestOptions.circleCropTransform()).into(image)
                        } else {
                            Glide.with(this@ProfileActivity).load(R.drawable.star_icon).apply(RequestOptions.circleCropTransform()).into(image)
                        }

                        val type = getType(memberType)

                        email.text = emailAddress
                        gender_year.text = getGenderAndAge(sex, year)
                        location.text = loc
                        nickName_type.text = "$name($type)"

                    }
                } catch (e: JSONException) {
                    Logs.e(TAG, e.message)
                    serverError("서버 통신 에러입니다")
                }

            }

            override fun fail(Error: String) {
                Logs.e(TAG, Error)
                serverError("서버 통신 에러입니다")
            }
        })

        backBtn.setOnClickListener {
            finish()
        }

        updateBtn.setOnClickListener {
            val intent: Intent = Intent(this@ProfileActivity, AddProfileActivity::class.java).putExtra("update" , true)
            startActivity(intent)
            finish()
        }

        initFavoritList(memberId)
    }

    private fun initFavoritList(memberId : String) {

        ServerNetworking.sendToMobileServer(this@ProfileActivity, Request.Method.GET, AppApplication.DOMAIN + "/favorite/" + memberId, null, null, object : ServerNetworking.getResult {
            override fun getResultText(text: String) {
                Logs.e(TAG, text)
                try {
                    val resultObj = JSONObject(text)
                    if (resultObj.getBoolean("key")) {

                        var info: ArrayList<ArtistInfo> = ArrayList()
                        val arrayList = resultObj.getJSONArray("message")

                        if (arrayList.length() == 0) {
                            favorite_text.text = "즐겨찾기한 아티스트가 없습니다"
                            favorite_text.visibility = View.VISIBLE
                        } else {
                            favorite_text.visibility = View.GONE
                        }

                        for (i in 0 until arrayList.length()) {
                            val id = arrayList.getJSONObject(i).getJSONObject("artistId").getString("id")
                            val name = arrayList.getJSONObject(i).getJSONObject("artistId").getString("name")
                            val image = arrayList.getJSONObject(i).getJSONObject("artistId").getString("image")
                            val location = arrayList.getJSONObject(i).getJSONObject("artistId").getString("artistLocation")
                            val genre = arrayList.getJSONObject(i).getJSONObject("artistId").getInt("genre")
                            val allowed = arrayList.getJSONObject(i).getJSONObject("artistId").getBoolean("is_allowed")
                            info.add(ArtistInfo(name, location, DOMAIN + image, id , genre.toString()))
                        }

                        favorite_star.adapter = SlimConcertPlaceAdapter(info, this@ProfileActivity, object : SlimConcertPlaceAdapter.getResult {
                            override fun getItemClick(result: Int) {
                                val intent = Intent(this@ProfileActivity, ArtistActivity::class.java).putExtra("memberId", result)
                                startActivity(intent)
                            }
                        })

                    }
                } catch (e: JSONException) {
                    Logs.e(TAG, e.message)
                    serverError("서버 통신 에러입니다")
                }
            }
            override fun fail(Error: String) {
                Logs.e(TAG, Error)
                serverError("서버 통신 에러입니다")
            }
        })
    }

    private fun getType(memberType: String?): Any {
        if(memberType == "U"){
            return "일반 회원"
        }else{
            return "아티스트"
        }
    }

    private fun getGenderAndAge(sex: String?, year: String?): CharSequence? {
        val result: String
        if (sex == "M") {
            result = "성별 : 남성 , 나이대 : $year"
        } else {
            result = "성별 : 여성 , 나이대 : $year"
        }
        return result
    }
}
