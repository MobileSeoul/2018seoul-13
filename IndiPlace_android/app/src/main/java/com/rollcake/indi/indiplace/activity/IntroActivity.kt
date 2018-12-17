package com.rollcake.indi.indiplace.activity

import android.content.Intent
import android.os.Bundle
import com.android.volley.Request
import com.facebook.AccessToken
import com.rollcake.indi.indiplace.AppApplication
import com.rollcake.indi.indiplace.AppApplication.*
import com.rollcake.indi.indiplace.R
import com.rollcake.indi.indiplace.utils.AppkeyManager
import com.rollcake.indi.indiplace.utils.Logs
import com.rollcake.indi.indiplace.utils.ServerNetworking
import org.json.JSONException
import org.json.JSONObject


class IntroActivity : BaseActivity() {

    val TAG = IntroActivity::class.java.simpleName

    fun facebookLoginCheck() : Boolean{
        val accessToken  = AccessToken.getCurrentAccessToken();
        val isLoggedIn = accessToken != null && !accessToken.isExpired();
        return isLoggedIn
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        setImmersiveMode()

        val keyArray = arrayOf("accountType", "id")
        var valueArray = arrayOf(getLoginString(AppkeyManager.getKey(this@IntroActivity, LOGIN_TYPE, "kakaotalk")), AppkeyManager.getKey(this@IntroActivity, AppApplication.LOGIN_TOKEN, "0")
        )

//        if(facebookLoginCheck()){
//            valueArray[0] = "facebook"
//        }

        val runnable = Runnable {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        val loginRunnable = Runnable {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        ServerNetworking.sendToMobileServer(this@IntroActivity, Request.Method.POST, AppApplication.DOMAIN + "/login", keyArray, valueArray, object : ServerNetworking.getResult {
            override fun getResultText(text: String) {
                Logs.e(TAG , text)
                try {
                    val resultObj = JSONObject(text)
                    if (resultObj.getBoolean("key")) {

                        val result = resultObj.getJSONObject("message")
                        val id = result.getString("id")
                        val name = result.getString("name")
                        val emailAddress = result.getString("emailAddress")
                        val sex = result.getString("sex")
                        val year = result.getString("year")
                        val loc = result.getString("location")
                        val memberType = result.getString("memberType")

                        if (!result.isNull("artist_info")) {
                            AppkeyManager.setKet(this@IntroActivity, ARTIST_IMG, AppApplication.DOMAIN + result.getJSONObject("artist_info").getString("image"))
                            AppkeyManager.setKet(this@IntroActivity, ARTIST_NAME, result.getJSONObject("artist_info").getString("name"))
                            AppkeyManager.setKet(this@IntroActivity, ARTIST_ID, result.getJSONObject("artist_info").getString("id"))
                        }
                        AppkeyManager.setKet(this@IntroActivity, MEMBER_ID, id)
                        AppkeyManager.setKet(this@IntroActivity, EMAIL, emailAddress)
                        AppkeyManager.setKet(this@IntroActivity, NICK_NAME, name)
                        AppkeyManager.setKet(this@IntroActivity, LOCATION, loc)
                        AppkeyManager.setKet(this@IntroActivity, GENDER, sex)
                        AppkeyManager.setKet(this@IntroActivity, AGE, year)
                        AppkeyManager.setKet(this@IntroActivity, LOGIN_CHECK, true)

                        Logs.e(TAG , memberType)
                        if (memberType == "U") {
                            AppkeyManager.setKet(this@IntroActivity, MEMBER, NORMAL)
                        } else {
                            AppkeyManager.setKet(this@IntroActivity, MEMBER, ARTIST)
                        }

                        handler.postDelayed(runnable, 1500)
                    } else {
                        handler.postDelayed(loginRunnable, 1500)
                    }
                } catch (e: JSONException) {
                    serverError("서버 통신 에러입니다")
                }

            }

            override fun fail(Error: String) {
                Logs.e(TAG , Error)
                serverError("서버 통신 에러입니다")
            }
        })


    }

    private fun getLoginString(key: String): String {
        if (key == KAKAO)
            return "kakaotalk"
        else
            return "facebook"
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        setImmersiveMode()
    }

}