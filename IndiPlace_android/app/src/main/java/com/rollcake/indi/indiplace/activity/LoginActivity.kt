package com.rollcake.indi.indiplace.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import com.android.volley.Request
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.kakao.auth.AuthType
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.network.ErrorResult
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeResponseCallback
import com.kakao.usermgmt.response.model.UserProfile
import com.kakao.util.exception.KakaoException
import com.kakao.util.helper.Utility.getPackageInfo
import com.kakao.util.helper.log.Logger
import com.rollcake.indi.indiplace.AppApplication
import com.rollcake.indi.indiplace.AppApplication.*
import com.rollcake.indi.indiplace.R
import com.rollcake.indi.indiplace.utils.AppkeyManager
import com.rollcake.indi.indiplace.utils.Logs
import com.rollcake.indi.indiplace.utils.ServerNetworking
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class LoginActivity : BaseActivity() {

    val TAG: String = LoginActivity::class.java.simpleName
    val kakaocallback = SessionCallback()
    val callbackManager = CallbackManager.Factory.create()

    var sessionCheck = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_facebook_button.setReadPermissions("email")

        kakaoLoginBtn.setOnClickListener {
            Session.getCurrentSession().addCallback(kakaocallback)
            Session.getCurrentSession().checkAndImplicitOpen()
            Session.getCurrentSession().open(AuthType.KAKAO_LOGIN_ALL, this@LoginActivity)
            com_kakao_login.performClick()
        }

        facebookBtn.setOnClickListener {
            login_facebook_button.performClick()
        }

        skipLoginBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


        login_facebook_button.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                AppkeyManager.setKet(this@LoginActivity, LOGIN_TOKEN, loginResult.accessToken.userId + "f")
                AppkeyManager.setKet(this@LoginActivity, LOGIN_TYPE, FACEBOOK)

                loginAPICheck()

//                val intent = Intent(this@LoginActivity, AddProfileActivity::class.java).putExtra("update" , false)
//                startActivity(intent)
//                finish()
            }

            override fun onCancel() {
                // App code
            }

            override fun onError(exception: FacebookException) {
                // App code
            }
        })

//        if(facebookLoginCheck()){
//            val intent = Intent(this@LoginActivity, AddProfileActivity::class.java).putExtra("update" , false)
//            startActivity(intent)
//            finish()
//        }

    }

    inner class SessionCallback : ISessionCallback {
        override fun onSessionOpened() {
            Logs.e(TAG , "onSessionOpened")
            if(!sessionCheck) {
                sessionCheck = true
                requestMe()
            }
        }

        override fun onSessionOpenFailed(exception: KakaoException?) {
            if (exception != null) {
                Logger.e(exception)
            }
            Logs.e(TAG , "fail")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return
        }

        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun requestMe() {
        Logs.e(TAG , "requestME Call");
        UserManagement.getInstance().requestMe(object : MeResponseCallback() {
            override fun onFailure(errorResult: ErrorResult?) {
                val message = "failed to get user info. msg=" + errorResult!!
                Logs.e(TAG + "fail", message)
            }

            override fun onSessionClosed(errorResult: ErrorResult) {
                Logs.e(TAG, "close")
            }

            override fun onSuccess(userProfile: UserProfile) {
                Logs.e(TAG, "onSuccess")
                AppkeyManager.setKet(this@LoginActivity, LOGIN_TOKEN, userProfile.id.toString())
                AppkeyManager.setKet(this@LoginActivity, LOGIN_TYPE, KAKAO)
                AppkeyManager.setKet(this@LoginActivity, NICK_NAME, userProfile.nickname)
                AppkeyManager.setKet(this@LoginActivity, EMAIL, userProfile.email)

                Session.getCurrentSession().removeCallback(kakaocallback)

                loginAPICheck()
//                val intent = Intent(this@LoginActivity, AddProfileActivity::class.java).putExtra("update" , false)
//                startActivity(intent)
//                finish()
            }

            override fun onNotSignedUp() {
                goToast("onNotSignedUp")
            }
        })
    }
//
//    fun getKeyHash(context: Context): String? {
//        val packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES) ?: return null
//
//        for (signature in packageInfo!!.signatures) {
//            try {
//                val md = MessageDigest.getInstance("SHA")
//                md.update(signature.toByteArray())
//                return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
//            } catch (e: NoSuchAlgorithmException) {
//                Log.w(TAG, "Unable to get MessageDigest. signature=$signature", e)
//            }
//
//        }
//        return null
//    }
//


    private fun loginAPICheck() {

        val keyArray = arrayOf("accountType", "id")
        var valueArray = arrayOf(getLoginString(AppkeyManager.getKey(this@LoginActivity, LOGIN_TYPE, "kakaotalk")), AppkeyManager.getKey(this@LoginActivity, AppApplication.LOGIN_TOKEN, "0")
        )

        ServerNetworking.sendToMobileServer(this@LoginActivity, Request.Method.POST, AppApplication.DOMAIN + "/login", keyArray, valueArray, object : ServerNetworking.getResult {
            override fun getResultText(text: String) {
                try {
                    val resultObj = JSONObject(text)
                    Logs.e(TAG , resultObj.toString())
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
                            AppkeyManager.setKet(this@LoginActivity, ARTIST_IMG, AppApplication.DOMAIN + result.getJSONObject("artist_info").getString("image"))
                            AppkeyManager.setKet(this@LoginActivity, ARTIST_NAME, result.getJSONObject("artist_info").getString("name"))
                            AppkeyManager.setKet(this@LoginActivity, ARTIST_ID, result.getJSONObject("artist_info").getString("id"))
                        }
                        AppkeyManager.setKet(this@LoginActivity, MEMBER_ID, id)
                        AppkeyManager.setKet(this@LoginActivity, EMAIL, emailAddress)
                        AppkeyManager.setKet(this@LoginActivity, NICK_NAME, name)
                        AppkeyManager.setKet(this@LoginActivity, LOCATION, loc)
                        AppkeyManager.setKet(this@LoginActivity, GENDER, sex)
                        AppkeyManager.setKet(this@LoginActivity, AGE, year)

                        if (memberType == "U") {
                            AppkeyManager.setKet(this@LoginActivity, MEMBER, NORMAL)
                        } else {
                            AppkeyManager.setKet(this@LoginActivity, MEMBER, ARTIST)
                        }

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val intent = Intent(this@LoginActivity, AddProfileActivity::class.java).putExtra("update" , false)
                        startActivity(intent)
                        finish()
                    }
                } catch (e: JSONException) {
                    serverError("서버 통신 에러입니다")
                }

            }

            override fun fail(Error: String) {
                Logs.e(TAG, Error)
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

}