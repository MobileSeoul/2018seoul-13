package com.rollcake.indi.indiplace.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.android.volley.Request
import com.rollcake.indi.indiplace.AppApplication.*
import com.rollcake.indi.indiplace.R
import com.rollcake.indi.indiplace.utils.AppkeyManager
import com.rollcake.indi.indiplace.utils.Logs
import com.rollcake.indi.indiplace.utils.ServerNetworking
import kotlinx.android.synthetic.main.activity_add_profile.*
import org.json.JSONException
import org.json.JSONObject

class AddProfileActivity : BaseActivity() {

    val TAG = AddProfileActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_profile)

        val intent = intent
        val updateCheck = intent.getBooleanExtra("update", false)

        val locationList: ArrayList<String> = ArrayList()
        addLocation(locationList)

        val locationAdapter: ArrayAdapter<String> = ArrayAdapter(this, R.layout.simple_1line, locationList)
        location.adapter = locationAdapter
        location.setSelection(0)

        val ageList: ArrayList<String> = ArrayList()
        ageList.add("10")
        ageList.add("20")
        ageList.add("30")
        ageList.add("40")
        ageList.add("50")
        ageList.add("60")

        val ageAdapter: ArrayAdapter<String> = ArrayAdapter(this, R.layout.simple_1line, ageList)
        ageInput.adapter = ageAdapter
        ageInput.setSelection(0)

        emailInput.setText(AppkeyManager.getKey(this, EMAIL, ""))
        nickName.setText(AppkeyManager.getKey(this, NICK_NAME, ""))

        saveBtn.setOnClickListener {
            if (emptyCheck()) {
                AppkeyManager.setKet(this@AddProfileActivity, EMAIL, emailInput.text.toString())
                AppkeyManager.setKet(this@AddProfileActivity, NICK_NAME, nickName.text.toString())
                AppkeyManager.setKet(this@AddProfileActivity, AGE, ageList.get(ageInput.selectedItemPosition))
                AppkeyManager.setKet(this@AddProfileActivity, GENDER, getGenderString())
                AppkeyManager.setKet(this@AddProfileActivity, LOCATION, locationList.get(location.selectedItemPosition))
                AppkeyManager.setKet(this@AddProfileActivity, MEMBER, NORMAL)


                val keyArray = arrayOf(AppkeyManager.getKey(this@AddProfileActivity, LOGIN_TYPE, ""), "name", "emailAddress", "sex", "year", "location", "deviceToken")
                val valueArray = arrayOf(AppkeyManager.getKey(this@AddProfileActivity, LOGIN_TOKEN, ""),
                        AppkeyManager.getKey(this@AddProfileActivity, NICK_NAME, ""),
                        AppkeyManager.getKey(this@AddProfileActivity, EMAIL, ""),
                        AppkeyManager.getKey(this@AddProfileActivity, GENDER, "F"),
                        AppkeyManager.getKey(this@AddProfileActivity, AGE, "30"),
                        AppkeyManager.getKey(this@AddProfileActivity, LOCATION, "종로구"),
                        AppkeyManager.getKey(this@AddProfileActivity, TOKEN, "")
                )

                if (updateCheck) {
                    updateProfile(keyArray, valueArray)
                } else {
                    insertProfile(keyArray, valueArray)
                }
            }
        }


        if (updateCheck) {
            emailInput.isEnabled = false
            saveBtn.text = "수정하기"
            main_title.text = "회원 정보 수정"
            genderCheck(AppkeyManager.getKey(this@AddProfileActivity, GENDER, "F"))
            ageCheck(ageList, AppkeyManager.getKey(this@AddProfileActivity, AGE, "30"))
            locationCheck(locationList, AppkeyManager.getKey(this@AddProfileActivity, LOCATION, "종로구"))
        }else{
            back_Btn.visibility = View.GONE
        }


        back_Btn.setOnClickListener {
            finish()
        }

    }

    private fun updateProfile(keyArray: Array<String>, valueArray: Array<String>) {
        val memberId = AppkeyManager.getKey(this@AddProfileActivity, MEMBER_ID, "0")
        ServerNetworking.sendToMobileServer(this@AddProfileActivity, Request.Method.PUT, DOMAIN + "/member/" + memberId, keyArray, valueArray, object : ServerNetworking.getResult {
            override fun getResultText(text: String) {
                try {
                    val resultObj = JSONObject(text)
                    Logs.e(TAG, resultObj.toString())
                    if (resultObj.getBoolean("key")) {
                        val clientInfo = resultObj.getJSONObject("message")

                        AppkeyManager.setKet(this@AddProfileActivity, MEMBER_ID, clientInfo.getString("id").toString())
                        goToast(getString(R.string.save_profile))
                        AppkeyManager.setKet(this@AddProfileActivity, LOGIN_CHECK, true)
                        val intent = Intent(this@AddProfileActivity, MainActivity::class.java)
                        startActivity(intent)
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

    private fun insertProfile(keyArray: Array<String>, valueArray: Array<String>) {
        ServerNetworking.sendToMobileServer(this@AddProfileActivity, Request.Method.POST, DOMAIN + "/member", keyArray, valueArray, object : ServerNetworking.getResult {
            override fun getResultText(text: String) {
                try {
                    val resultObj = JSONObject(text)
                    Logs.e(TAG, resultObj.toString())
                    if (resultObj.getBoolean("key")) {
                        val clientInfo = resultObj.getJSONObject("message")

                        AppkeyManager.setKet(this@AddProfileActivity, MEMBER_ID, clientInfo.getString("id").toString())
                        goToast(getString(R.string.save_profile))
                        AppkeyManager.setKet(this@AddProfileActivity, LOGIN_CHECK, true)
                        val intent = Intent(this@AddProfileActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else if (resultObj.getString("message") == "kakaotalk 아이디 중복") {
                        updateProfile(keyArray, valueArray)
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

    private fun locationCheck(locationList: ArrayList<String>, key: String) {
        for (str in 0 until locationList.size) {
            if (locationList.get(str) == key) {
                location.setSelection(str)
                break
            }
        }
    }

    private fun ageCheck(ageList: ArrayList<String>, key: String) {
        for (str in 0 until ageList.size) {
            if (ageList.get(str) == key) {
                ageInput.setSelection(str)
                break
            }
        }
    }

    private fun genderCheck(key: String) {
        if (key == "M") {
            male.isChecked = true
            female.isChecked = false
        } else {
            male.isChecked = false
            female.isChecked = true
        }
    }

    private fun getGenderString(): String {
        if (genderSelection.checkedRadioButtonId == male.id) {
            return "M"
        } else {
            return "F"
        }
    }

    protected fun emptyCheck(): Boolean {
        if (emailInput.text.toString().isEmpty()) {
            return false
        }
        if (nickName.text.toString().isEmpty()) {
            return false
        }
        return true
    }

    override fun onBackPressed() {

    }


}
