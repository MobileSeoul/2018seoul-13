package com.rollcake.indi.indiplace.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import com.android.volley.Request
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.rollcake.indi.indiplace.AppApplication
import com.rollcake.indi.indiplace.AppApplication.*
import com.rollcake.indi.indiplace.R
import com.rollcake.indi.indiplace.activity.view.*
import com.rollcake.indi.indiplace.utils.AppkeyManager
import com.rollcake.indi.indiplace.utils.Logs
import com.rollcake.indi.indiplace.utils.ServerNetworking
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import org.json.JSONException
import org.json.JSONObject

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    val TAG = MainActivity::class.java.simpleName

    var finishCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        favorite_recyclerView.layoutManager = LinearLayoutManager(this)
        place_recyclerView.layoutManager = LinearLayoutManager(this)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.title = ""

        nav_view.setNavigationItemSelectedListener(this)
        nav_view.itemIconTintList = null
        val headView = nav_view.getHeaderView(0)
        init(headView)

        headView.setOnClickListener {
            if (!AppkeyManager.getKey(this@MainActivity, LOGIN_CHECK, false)) {
                showdialog(false)
            } else {
                val intent: Intent = Intent(this@MainActivity, ProfileActivity::class.java).putExtra("memberId", AppkeyManager.getKey(this@MainActivity, MEMBER_ID, "1"))
                startActivity(intent)
            }
        }
    }

    private fun init(headView: View) {
        if (!AppkeyManager.getKey(this@MainActivity, LOGIN_CHECK, false)) {
            headView.nickName.text = "Guest"
            headView.email.text = "회원가입 부탁드립니다"
            Glide.with(this@MainActivity).load(R.drawable.guest_icon).apply(RequestOptions.circleCropTransform()).into(headView.imageView)
            initRecentlyPlace()
            initSeletedPlace()
        } else if (AppkeyManager.getKey(this@MainActivity, MEMBER, NORMAL) == NORMAL) {
            headView.nickName.text = AppkeyManager.getKey(this@MainActivity, EMAIL, "")
            headView.email.text = AppkeyManager.getKey(this@MainActivity, NICK_NAME, "")
            Glide.with(this@MainActivity).load(R.drawable.normal_icon).apply(RequestOptions.circleCropTransform()).into(headView.imageView)
            initSeletedPlace()
            myFavoriteList()
            initFavoritePlace(AppkeyManager.getKey(this@MainActivity, MEMBER_ID, "0"))
        } else {
            headView.nickName.text = AppkeyManager.getKey(this@MainActivity, EMAIL, "")
            headView.email.text = AppkeyManager.getKey(this@MainActivity, NICK_NAME, "")
            Glide.with(this@MainActivity).load(AppkeyManager.getKey(this@MainActivity, ARTIST_IMG, "")).apply(RequestOptions.circleCropTransform()).into(headView.imageView)
            initSeletedPlace()
            myFavoriteList()
            initFavoritePlace(AppkeyManager.getKey(this@MainActivity, MEMBER_ID, "0"))
        }
    }

    private fun initFavoritePlace(key: String) {
        ServerNetworking.sendToMobileServer(this@MainActivity, Request.Method.GET, AppApplication.DOMAIN + "/performance/favor/" + key, null, null, object : ServerNetworking.getResult {
            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun getResultText(text: String) {
                try {
                    val resultObj = JSONObject(text)
                    if (resultObj.getBoolean("key")) {

                        val info: ArrayList<ConcertInfo> = ArrayList()
                        val arrayList = resultObj.getJSONArray("message")
                        if (arrayList.length() == 0) {
                            artist_info_text.text = getString(R.string.zero_count)
                            artist_info_text.visibility = View.VISIBLE
                        } else {
                            artist_info_text.visibility = View.GONE
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
                            info.add(ConcertInfo(name, image, location, startTime, endTime, lat.toDouble(), loc.toDouble() , place))
                        }

                        favorite_recyclerView.adapter = PlaceConcertPlaceAdapter(info, this@MainActivity, object : PlaceConcertPlaceAdapter.getResult {
                            override fun getItemClick(result: ConcertInfo) {
                                val intent = Intent(this@MainActivity, ConcertInfoActivity::class.java)
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
                    } else {
                        artist_info_text.text = "서버 통신 실패"
                        artist_info_text.visibility = View.VISIBLE
                    }
                } catch (e: JSONException) {
                    Logs.e(TAG, e.message)
                    artist_info_text.text = "서버 통신 실패"
                    artist_info_text.visibility = View.VISIBLE
                }

            }

            override fun fail(Error: String) {
                Logs.e(TAG, Error)
                artist_info_text.text = "서버 통신 실패"
                artist_info_text.visibility = View.VISIBLE
            }
        })
    }

    private fun initSeletedPlace() {
        ServerNetworking.sendToMobileServer(this@MainActivity, Request.Method.GET, AppApplication.DOMAIN + "/performance/main?location=" + AppkeyManager.getKey(this@MainActivity, LOCATION, "강남구"), null, null, object : ServerNetworking.getResult {
            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun getResultText(text: String) {
                try {
                    val resultObj = JSONObject(text)
                    if (resultObj.getBoolean("key")) {

                        val info: ArrayList<ConcertInfo> = ArrayList()
                        val arrayList = resultObj.getJSONArray("message")
                        if (arrayList.length() == 0) {
                            place_info_text.text = getString(R.string.zero_count)
                            place_info_text.visibility = View.VISIBLE
                        } else {
                            place_info_text.visibility = View.GONE
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
                            info.add(ConcertInfo(name, image, location, startTime, endTime, lat.toDouble(), loc.toDouble() , place))
                        }

                        place_recyclerView.adapter = PlaceConcertPlaceAdapter(info, this@MainActivity, object : PlaceConcertPlaceAdapter.getResult {
                            override fun getItemClick(result: ConcertInfo) {
                                val intent = Intent(this@MainActivity, ConcertInfoActivity::class.java)
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

                    } else {
                        place_info_text.text = "서버 통신 에러입니다"
                        place_info_text.visibility = View.VISIBLE
                    }
                } catch (e: JSONException) {
                    Logs.e(TAG, e.message)
                    place_info_text.text = "서버 통신 에러입니다"
                    place_info_text.visibility = View.VISIBLE
                }

            }

            override fun fail(Error: String) {
                place_info_text.text = "서버 통신 에러입니다"
                place_info_text.visibility = View.VISIBLE
            }
        })
    }

    private fun initRecentlyPlace() {
        ServerNetworking.sendToMobileServer(this@MainActivity, Request.Method.GET, AppApplication.DOMAIN + "/performance/recently", null, null, object : ServerNetworking.getResult {
            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun getResultText(text: String) {
                try {
                    val resultObj = JSONObject(text)
                    if (resultObj.getBoolean("key")) {

                        val info: ArrayList<ConcertInfo> = ArrayList()
                        val arrayList = resultObj.getJSONArray("message")
                        if (arrayList.length() == 0) {
                            place_info_text.text = getString(R.string.zero_count)
                            place_info_text.visibility = View.VISIBLE
                        } else {
                            place_info_text.visibility = View.GONE
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

                        favorite_recyclerView.adapter = PlaceConcertPlaceAdapter(info, this@MainActivity, object : PlaceConcertPlaceAdapter.getResult {
                            override fun getItemClick(result: ConcertInfo) {
                                val intent = Intent(this@MainActivity, ConcertInfoActivity::class.java)
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


//                        val info: ArrayList<ArtistInfo> = ArrayList()
//                        val arrayList = resultObj.getJSONArray("message")
//
//
//                        if (arrayList.length() == 0) {
//                            artist_info_text.text = getString(R.string.zero_count)
//                            artist_info_text.visibility = View.VISIBLE
//                        } else {
//                            artist_info_text.visibility = View.GONE
//                        }
//
//                        for (i in 0 until arrayList.length()) {
//                            val name = arrayList.getJSONObject(i).getJSONObject("artistId").get("name").toString()
//                            val image = arrayList.getJSONObject(i).getJSONObject("artistId").get("image").toString()
//                            val location = arrayList.getJSONObject(i).get("location").toString()
//                            val memberId = arrayList.getJSONObject(i).getJSONObject("artistId").get("id").toString()
//                            info.add(ArtistInfo(name, location, DOMAIN + image, memberId))
//                        }
//                        favorite_recyclerView.adapter = ConcertPlaceAdapter(info, this@MainActivity, object : ConcertPlaceAdapter.getResult {
//                            override fun getItemClick(result: Int) {
//                                val intent = Intent(this@MainActivity, ArtistActivity::class.java).putExtra("memberId", result)
//                                startActivity(intent)
//                            }
//                        })

                    } else {
                        artist_info_text.text = "서버 통신 실패"
                        artist_info_text.visibility = View.VISIBLE
                    }
                } catch (e: JSONException) {
                    Logs.e(TAG, e.message)
                    artist_info_text.text = "서버 통신 실패"
                    artist_info_text.visibility = View.VISIBLE
                }

            }

            override fun fail(Error: String) {
                Logs.e(TAG, Error)
                artist_info_text.text = "서버 통신 실패"
                artist_info_text.visibility = View.VISIBLE
            }
        })
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            if (finishCount == 0) {
                goToast(getString(R.string.one_more_key_finish))
                finishCount++
            } else {
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        finishCount = 0
        val headView = nav_view.getHeaderView(0)
        init(headView)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_singer -> {
                val intent = Intent(this, ArtistSearchActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_concert -> {
                val intent = Intent(this, ConcertSearchActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_artist_insert -> {
                if (!AppkeyManager.getKey(this@MainActivity, LOGIN_CHECK, false)) {
                    showdialog(false)
                } else if (AppkeyManager.getKey(this@MainActivity, MEMBER, NORMAL) == NORMAL) {
                    showArtistdialog("아티스트로 등록 하시겠습니까?")
                } else {
                    goToast(getString(R.string.already_star))
                }
            }
            R.id.nav_setting -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_insert -> {
                if (!AppkeyManager.getKey(this@MainActivity, LOGIN_CHECK, false)) {
                    showdialog(false)
                } else if (AppkeyManager.getKey(this@MainActivity, MEMBER, NORMAL) == NORMAL) {
                    showArtistdialog("아티스트로 등록 후 사용 가능합니다 아트스토로 등록하시겠습니까?")
                } else {
                    val intent = Intent(this, InsertConcertActivity::class.java)
                    startActivity(intent)
                }

            }
            R.id.nav_artist_profile -> {
                if (!AppkeyManager.getKey(this@MainActivity, LOGIN_CHECK, false)) {
                    showdialog(false)
                } else if (AppkeyManager.getKey(this@MainActivity, MEMBER, NORMAL) == NORMAL) {
                    showArtistdialog("아티스트로 등록 후 사용 가능합니다 아트스토로 등록하시겠습니까?")
                } else {
                    val intent = Intent(this, ArtistProfileUpdateActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


    fun showArtistdialog(message: String) {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("인디플레이스")
        builder.setMessage(message)
        builder.setPositiveButton("네 알겠습니다") { _, which ->
            val intent: Intent = Intent(this@MainActivity, ArtistProfileActivity::class.java).putExtra("key", true)
            startActivity(intent)
        }
        builder.setNegativeButton("아니요") { dialog, which ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun myFavoriteList() {
        val mid = AppkeyManager.getKey(this, MEMBER_ID, "0")
        ServerNetworking.sendToMobileServer(this@MainActivity, Request.Method.GET, AppApplication.DOMAIN + "/favorite/" + mid, null, null, object : ServerNetworking.getResult {
            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun getResultText(text: String) {
                try {
                    val resultObj = JSONObject(text)
                    if (resultObj.getBoolean("key")) {
                        val info: ArrayList<ArtistFavorite> = ArrayList()
                        val arrayList = resultObj.getJSONArray("message")

                        if (arrayList.length() != 0) {
                            for (i in 0 until arrayList.length()) {
                                val pkValue = arrayList.getJSONObject(i).getInt("id").toString()
                                val name = arrayList.getJSONObject(i).getJSONObject("artistId").get("name").toString()
                                val id = arrayList.getJSONObject(i).getJSONObject("artistId").get("id").toString()
                                info.add(ArtistFavorite(pkValue, id, name))
                            }
                        }
                        val appApplication = getApplication() as AppApplication
                        appApplication.setArtistFavorites(info)
                    } else {

                    }
                } catch (e: JSONException) {
                    Logs.e(TAG, e.message)
                }

            }

            override fun fail(Error: String) {
                Logs.e(TAG, Error)
                artist_info_text.text = "서버 통신 실패"
                artist_info_text.visibility = View.VISIBLE
            }
        })
    }

//    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
//        val registerMenu = menu.getItem(0)
//        val profileMenu = menu.getItem(1)
//        val insertMenu = menu.getItem(2)
//
//        if (!AppkeyManager.getKey(this@MainActivity, LOGIN_CHECK, false)) {
//            registerMenu.isVisible = false
//            profileMenu.isVisible = false
//            insertMenu.isVisible = false
//        } else if (AppkeyManager.getKey(this@MainActivity, MEMBER, NORMAL) == NORMAL) {
//            registerMenu.isVisible = true
//            profileMenu.isVisible = false
//            insertMenu.isVisible = false
//        } else {
//            registerMenu.isVisible = false
//            profileMenu.isVisible = true
//            insertMenu.isVisible = true
//        }
//        return super.onPrepareOptionsMenu(menu)
//    }

}
