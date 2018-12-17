package com.rollcake.indi.indiplace.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.volley.Request
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.rollcake.indi.indiplace.AppApplication
import com.rollcake.indi.indiplace.AppApplication.DOMAIN
import com.rollcake.indi.indiplace.AppApplication.MEMBER_ID
import com.rollcake.indi.indiplace.R
import com.rollcake.indi.indiplace.activity.view.ArtistFavorite
import com.rollcake.indi.indiplace.activity.view.CommentAdapter
import com.rollcake.indi.indiplace.utils.AppkeyManager
import com.rollcake.indi.indiplace.utils.Logs
import com.rollcake.indi.indiplace.utils.ServerNetworking
import kotlinx.android.synthetic.main.activity_artist_info.*
import org.json.JSONException
import org.json.JSONObject

class ArtistActivity : BaseActivity() {

    val TAG = ArtistActivity::class.java.simpleName
    var facebook: String? = null
    var youtube: String? = null
    var insta: String? = null

    var favoriteCheck = false
    var deletePkValue : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist_info)

        val intent = intent
        val memberId = intent.getIntExtra("memberId", 0)

        message.layoutManager = LinearLayoutManager(this)

        initArtist(memberId)
        initComment(memberId)

        facebookBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(facebook))
            startActivity(intent)
        }

        instaBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(insta))
            startActivity(intent)
        }

        youtubeBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtube))
            startActivity(intent)
        }
        backBtn.setOnClickListener {
            finish()
        }

        val appApplication = getApplication() as AppApplication

        if (appApplication.artistFavorites.size != 0) {
            val list = appApplication.artistFavorites
            for (str in list) {
                if (str.id == memberId.toString()) {
                    favoriteCheck = true
                    deletePkValue = str.pkValue
                    favoriteBtn.setImageResource(R.drawable.ic_star)
                    break
                }
            }
        }

        //todo 즐겨찾기 로직 수정
        favoriteBtn.setOnClickListener {
            if (!AppkeyManager.getKey(this@ArtistActivity, AppApplication.LOGIN_CHECK, false)) {
                showdialog(true)
            } else {
                if (!favoriteCheck) {
                    favoritCheck(memberId)
                } else {
                    favoritRemoveCheck(memberId)
                }
            }
        }

        sendBtn.setOnClickListener {

            if (!AppkeyManager.getKey(this@ArtistActivity, AppApplication.LOGIN_CHECK, false)) {
                showdialog(true)
            } else {
                val keyArray = arrayOf("memberId", "artistId", "content")
                val valueArray = arrayOf(AppkeyManager.getKey(this@ArtistActivity, MEMBER_ID, ""),
                        memberId.toString(),
                        sendMsg.text.toString()
                )

                ServerNetworking.sendToMobileServer(this@ArtistActivity, Request.Method.POST, AppApplication.DOMAIN + "/comment", keyArray, valueArray, object : ServerNetworking.getResult {
                    override fun getResultText(text: String) {
                        try {
                            val resultObj = JSONObject(text)
                            if (resultObj.getBoolean("key")) {
                                goToast(getString(R.string.sucess_comment))
                                initComment(memberId)
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
        }
    }

    private fun favoritRemoveCheck(memberId: Int) {

        ServerNetworking.sendToMobileServer(this@ArtistActivity, Request.Method.DELETE, AppApplication.DOMAIN + "/favorite/"+deletePkValue, null, null, object : ServerNetworking.getResult {
            override fun getResultText(text: String) {
                try {
                    val resultObj = JSONObject(text)
                    if (resultObj.getBoolean("key")) {
                        goToast(getString(R.string.remove_favorite))
                        finish()
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

    private fun favoritCheck(memberId: Int) {
        val keyArray = arrayOf("artistId", "memberId")
        val valueArray = arrayOf(memberId.toString(), AppkeyManager.getKey(this@ArtistActivity, MEMBER_ID, "")
        )

        ServerNetworking.sendToMobileServer(this@ArtistActivity, Request.Method.POST, AppApplication.DOMAIN + "/favorite", keyArray, valueArray, object : ServerNetworking.getResult {
            override fun getResultText(text: String) {
                try {
                    val resultObj = JSONObject(text)
                    if (resultObj.getBoolean("key")) {
                        goToast(getString(R.string.sucess_favorite))
                        finish()
                    } else {
                        goToast(getString(R.string.already_favorite))
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

    private fun initComment(memberId: Int) {
        ServerNetworking.sendToMobileServer(this@ArtistActivity, Request.Method.GET, AppApplication.DOMAIN + "/comment/" + memberId, null, null, object : ServerNetworking.getResult {
            override fun getResultText(text: String) {
                try {
                    val resultObj = JSONObject(text)
                    if (resultObj.getBoolean("key")) {
                        val info: ArrayList<String> = ArrayList()
                        val arrayList = resultObj.getJSONArray("message")

                        if (arrayList.length() == 0) {
                            empty_text.text = "최초로 댓글을 등록해보세요\n(욕설 및 부적절한 단어는 사용하지 마세요)"
                            empty_text.visibility = View.VISIBLE
                        } else {
                            empty_text.visibility = View.GONE
                        }

                        for (i in 0 until arrayList.length()) {
                            val content = arrayList.getJSONObject(i).getString("content")
                            Logs.e(TAG, content)
                            info.add(content)
                        }
                        message.adapter = CommentAdapter(info, this@ArtistActivity)

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

    private fun initArtist(memberId: Int) {
        ServerNetworking.sendToMobileServer(this@ArtistActivity, Request.Method.GET, AppApplication.DOMAIN + "/artist/" + memberId, null, null, object : ServerNetworking.getResult {
            @SuppressLint("SetTextI18n")
            override fun getResultText(text: String) {
                Logs.e(TAG, text);
                try {
                    val resultObj = JSONObject(text)
                    if (resultObj.getBoolean("key")) {
                        val name = resultObj.getJSONObject("message").getString("name")
                        val loc = resultObj.getJSONObject("message").getString("artistLocation")
                        facebook = resultObj.getJSONObject("message").getString("faceBook")
                        youtube = resultObj.getJSONObject("message").getString("youtube")
                        insta = resultObj.getJSONObject("message").getString("instagram")
                        val image = resultObj.getJSONObject("message").getString("image")
                        val gener = resultObj.getJSONObject("message").getString("genre")
                        Glide.with(this@ArtistActivity).load(DOMAIN + image).apply(RequestOptions.circleCropTransform()).into(artist_image)
                        singerName.text = name
                        generData.text = "주 활동 구역 : $loc 장르 : " + getGenreToString(gener.toInt())

                        if (facebook == "") {
                            facebookBtn.visibility = View.GONE
                        }
                        if (youtube == "") {
                            youtubeBtn.visibility = View.GONE
                        }
                        if (insta == "") {
                            instaBtn.visibility = View.GONE
                        }
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
}
