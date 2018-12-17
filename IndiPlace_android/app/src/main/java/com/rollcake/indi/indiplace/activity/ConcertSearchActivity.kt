package com.rollcake.indi.indiplace.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.widget.LinearLayoutManager
import android.view.View.GONE
import android.widget.ArrayAdapter
import com.android.volley.Request
import com.rollcake.indi.indiplace.AppApplication
import com.rollcake.indi.indiplace.R
import com.rollcake.indi.indiplace.activity.view.ConcertInfo
import com.rollcake.indi.indiplace.activity.view.PlaceConcertPlaceAdapter
import com.rollcake.indi.indiplace.utils.Logs
import com.rollcake.indi.indiplace.utils.ServerNetworking
import kotlinx.android.synthetic.main.activity_concert_search.*
import org.json.JSONException
import org.json.JSONObject

class ConcertSearchActivity : BaseActivity() {

    val TAG = ArtistSearchActivity::class.java.simpleName;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_concert_search)

        concertRecyclerView.layoutManager = LinearLayoutManager(this)

        val locationList: ArrayList<String> = ArrayList()
        val generList: ArrayList<String> = ArrayList()
        addLocation(locationList)
        addGener(generList)
        locationList.add("전체")
        generList.add("전체")

        val locationAdapter: ArrayAdapter<String> = ArrayAdapter(this, R.layout.simple_1line, locationList)
        location.adapter = locationAdapter
        location.setSelection(0)

        val generAdapter: ArrayAdapter<String> = ArrayAdapter(this, R.layout.simple_1line, generList)
        gener.adapter = generAdapter
        gener.setSelection(0)

        backBtn.setOnClickListener {
            finish()
        }

        searchBtn.setOnClickListener {
            val locationString = locationList.get(location.selectedItemPosition)
            val generString = getGenreToInt(generList.get(gener.selectedItemPosition))

            var url: String
            if (generString == 5) {
                url = AppApplication.DOMAIN + "/performance?location=" + locationString
            } else {
                url = AppApplication.DOMAIN + "/performance?location=" + locationString + "&genre=" + generString
            }
            ServerNetworking.sendToMobileServer(this@ConcertSearchActivity, Request.Method.GET, url, null, null, object : ServerNetworking.getResult {
                @RequiresApi(Build.VERSION_CODES.KITKAT)
                override fun getResultText(text: String) {
                    try {
                        val resultObj = JSONObject(text)
                        if (resultObj.getBoolean("key")) {
                            val info: ArrayList<ConcertInfo> = ArrayList()

                            val arrayList = resultObj.getJSONArray("message")

                            if (arrayList.length() == 0) {
                                countText.text = getString(R.string.zero_count)
                            } else {
                                countText.visibility = GONE
                            }

                            for (i in 0 until arrayList.length()) {
                                val name = arrayList.getJSONObject(i).getJSONObject("artistId").get("name").toString()
                                val image = arrayList.getJSONObject(i).getJSONObject("artistId").get("image").toString()
                                val location = arrayList.getJSONObject(i).get("location").toString()
                                val lat = arrayList.getJSONObject(i).get("lat").toString()
                                val loc = arrayList.getJSONObject(i).get("lot").toString()
                                val startTime = arrayList.getJSONObject(i).get("startTime").toString()
                                val endTime = arrayList.getJSONObject(i).get("endTime").toString()
                                val place = arrayList.getJSONObject(i).get("place").toString()
                                val memberId = arrayList.getJSONObject(i).getJSONObject("artistId").get("id").toString()
                                info.add(ConcertInfo(name, image, location, startTime, endTime, lat.toDouble(), loc.toDouble(), place))
                            }
                            concertRecyclerView.adapter = PlaceConcertPlaceAdapter(info, this@ConcertSearchActivity, object : PlaceConcertPlaceAdapter.getResult {
                                override fun getItemClick(result: ConcertInfo) {
                                    val intent = Intent(this@ConcertSearchActivity, ConcertInfoActivity::class.java)
                                            .putExtra("loc", result.loc)
                                            .putExtra("lat", result.lat)
                                            .putExtra("title", result.artistName)
                                            .putExtra("start", result.startTime)
                                            .putExtra("end", result.endTime)
                                            .putExtra("place", result.location)
                                            .putExtra("placecontent", result.placeContent)
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

    }
}
