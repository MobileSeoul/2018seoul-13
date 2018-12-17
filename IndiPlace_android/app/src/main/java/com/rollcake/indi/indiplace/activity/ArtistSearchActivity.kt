package com.rollcake.indi.indiplace.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.widget.LinearLayoutManager
import android.view.View.GONE
import com.android.volley.Request
import com.rollcake.indi.indiplace.AppApplication
import com.rollcake.indi.indiplace.R
import com.rollcake.indi.indiplace.activity.view.ArtistInfo
import com.rollcake.indi.indiplace.activity.view.SlimConcertPlaceAdapter
import com.rollcake.indi.indiplace.utils.Logs
import com.rollcake.indi.indiplace.utils.ServerNetworking
import kotlinx.android.synthetic.main.activity_artist_search.*
import org.json.JSONException
import org.json.JSONObject

class ArtistSearchActivity : BaseActivity() {

    val TAG = ArtistSearchActivity::class.java.simpleName;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist_search)

        artistRecyclerView.layoutManager = LinearLayoutManager(this)

        backBtn.setOnClickListener {
            finish()
        }

        searchBtn.setOnClickListener {
            ServerNetworking.sendToMobileServer(this@ArtistSearchActivity, Request.Method.GET, AppApplication.DOMAIN + "/artist?keyword=" + artistSerchBar.text.toString(), null, null, object : ServerNetworking.getResult {
                @RequiresApi(Build.VERSION_CODES.KITKAT)
                override fun getResultText(text: String) {
                    try {
                        val resultObj = JSONObject(text)
                        if (resultObj.getBoolean("key")) {
                            val info: ArrayList<ArtistInfo> = ArrayList()
                            val arrayList = resultObj.getJSONArray("message")

                            if(arrayList.length() == 0){
                                countText.text = getString(R.string.zero_count)
                            }else{
                                countText.visibility = GONE
                            }

                            for (i in 0 until arrayList.length()) {
                                val name = arrayList.getJSONObject(i).get("name").toString()
                                val location = arrayList.getJSONObject(i).get("artistLocation").toString()
                                val image = arrayList.getJSONObject(i).get("image").toString()
                                val genre = arrayList.getJSONObject(i).get("genre").toString()
                                val memberId = arrayList.getJSONObject(i).get("id").toString()
                                info.add(ArtistInfo(name, location, AppApplication.DOMAIN + image, memberId , genre))
                            }

                            artistRecyclerView.adapter = SlimConcertPlaceAdapter(info, this@ArtistSearchActivity , object : SlimConcertPlaceAdapter.getResult{
                                override fun getItemClick(result: Int) {
                                    val intent = Intent(this@ArtistSearchActivity , ArtistActivity::class.java).putExtra("memberId" , result)
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
                }
            })
        }

        artistRecyclerView.setRecyclerListener {

        }

    }
}
