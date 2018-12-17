package com.rollcake.indi.indiplace.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.util.Base64
import android.widget.ArrayAdapter
import com.android.volley.Request
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.rollcake.indi.indiplace.AppApplication
import com.rollcake.indi.indiplace.AppApplication.*
import com.rollcake.indi.indiplace.R
import com.rollcake.indi.indiplace.utils.AppkeyManager
import com.rollcake.indi.indiplace.utils.Logs
import com.rollcake.indi.indiplace.utils.ServerNetworking
import com.rollcake.indi.indiplace.utils.Utility
import kotlinx.android.synthetic.main.activity_artist_update_profile.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*


class ArtistProfileUpdateActivity : BaseActivity() {

    val TAG = ArtistProfileUpdateActivity::class.java.simpleName

    var encodedImage = ""

    private val REQUEST_CAMERA = 0
    private val SELECT_FILE = 1
    private val userChoosenTask: String? = null
    var thumbnail: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist_profile)

        val locationList: ArrayList<String> = ArrayList()
        val generList: ArrayList<String> = ArrayList()
        addLocation(locationList)
        addGener(generList)

        val locationAdapter: ArrayAdapter<String> = ArrayAdapter(this, R.layout.simple_1line, locationList)
        val generAdapter: ArrayAdapter<String> = ArrayAdapter(this, R.layout.simple_1line, generList)


        singerName.isEnabled = false
        singerName.setText(AppkeyManager.getKey(this@ArtistProfileUpdateActivity, ARTIST_NAME, singerName.text.toString()))
        instaAddr.setText(AppkeyManager.getKey(this@ArtistProfileUpdateActivity, INSTA_ADDR, ""))
        youtubeAddr.setText(AppkeyManager.getKey(this@ArtistProfileUpdateActivity, YOUTUBE_ADDR, ""))
        facebookAddr.setText(AppkeyManager.getKey(this@ArtistProfileUpdateActivity, FACEBOOK_ADDR, ""))
        Glide.with(this@ArtistProfileUpdateActivity).load(AppApplication.DOMAIN + AppkeyManager.getKey(this@ArtistProfileUpdateActivity, ARTIST_IMG, "")).apply(RequestOptions.circleCropTransform()).into(profileImg)
        generCheck(generList, AppkeyManager.getKey(this@ArtistProfileUpdateActivity, GENER, ""))
        locationCheck(locationList, AppkeyManager.getKey(this@ArtistProfileUpdateActivity, ARTIST_LOC, "종로구"))

        generSpinner.isEnabled = false

        locationSpinner.adapter = locationAdapter
        generSpinner.adapter = generAdapter
        saveBtn.setOnClickListener {
            if (emptyCheck()) {
                AppkeyManager.setKet(this@ArtistProfileUpdateActivity, ARTIST_NAME, singerName.text.toString())
                AppkeyManager.setKet(this@ArtistProfileUpdateActivity, INSTA_ADDR, instaAddr.text.toString())
                AppkeyManager.setKet(this@ArtistProfileUpdateActivity, YOUTUBE_ADDR, youtubeAddr.text.toString())
                AppkeyManager.setKet(this@ArtistProfileUpdateActivity, FACEBOOK_ADDR, facebookAddr.text.toString())
                AppkeyManager.setKet(this@ArtistProfileUpdateActivity, GENER, generList.get(generSpinner.selectedItemPosition))
                AppkeyManager.setKet(this@ArtistProfileUpdateActivity, ARTIST_LOC, locationList.get(locationSpinner.selectedItemPosition))

                val keyArray = arrayOf("memberId", "artistLocation", "image", "name", "youtube", "facebook", "insta", "genre")
                val valueArray = arrayOf(
                        AppkeyManager.getKey(this@ArtistProfileUpdateActivity, MEMBER_ID, ""),
                        AppkeyManager.getKey(this@ArtistProfileUpdateActivity, ARTIST_LOC, ""),
                        getImageByte(),
                        AppkeyManager.getKey(this@ArtistProfileUpdateActivity, ARTIST_NAME, ""),
                        AppkeyManager.getKey(this@ArtistProfileUpdateActivity, YOUTUBE_ADDR, "F"),
                        AppkeyManager.getKey(this@ArtistProfileUpdateActivity, FACEBOOK_ADDR, "30"),
                        AppkeyManager.getKey(this@ArtistProfileUpdateActivity, INSTA_ADDR, "종로구"),
                        getGenreToInt(AppkeyManager.getKey(this@ArtistProfileUpdateActivity, GENER, "")).toString()
                )

                val artistId = AppkeyManager.getKey(this@ArtistProfileUpdateActivity, ARTIST_ID, "0");
                ServerNetworking.sendToMobileServer(this@ArtistProfileUpdateActivity, Request.Method.PUT, DOMAIN + "/artist/" + artistId, keyArray, valueArray, object : ServerNetworking.getResult {
                    override fun getResultText(text: String) {
                        Logs.e(TAG, text);
                        try {
                            val resultObj = JSONObject(text)
                            if (resultObj.getBoolean("key")) {

                                goToast(getString(R.string.save_profile))
                                AppkeyManager.setKet(this@ArtistProfileUpdateActivity, LOGIN_CHECK, true)
                                AppkeyManager.setKet(this@ArtistProfileUpdateActivity, ARTIST_ID, resultObj.getJSONObject("message").getString("id"))
                                AppkeyManager.setKet(this@ArtistProfileUpdateActivity, ARTIST_IMG, AppApplication.DOMAIN + resultObj.getJSONObject("message").getString("image"))
                                AppkeyManager.setKet(this@ArtistProfileUpdateActivity, MEMBER, ARTIST)
                                val intent = Intent(this@ArtistProfileUpdateActivity, MainActivity::class.java)
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
            } else {
                goToast(getString(R.string.empty_text))
            }
        }
        profileImg.setOnClickListener {
            selectImage()
        }
    }

    private fun getImageByte(): String {

        return getStringImage(thumbnail!!)
    }

    private fun locationCheck(locationList: ArrayList<String>, key: String) {
        for (str in 0 until locationList.size) {
            if (locationList.get(str) == key) {
                locationSpinner.setSelection(str)
                break
            }
        }
    }

    private fun generCheck(generList: ArrayList<String>, key: String) {
        for (str in 0 until generList.size) {
            if (generList.get(str) == key) {
                generSpinner.setSelection(str)
                break
            }
        }
    }

    private fun emptyCheck(): Boolean {
        val text = singerName.text.toString().trim()
        if (text.isEmpty()) {
            return false
        }

        if (thumbnail == null) {
            return false
        }

        return true
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (userChoosenTask.equals("Choose from Library"))
                    galleryIntent()
            } else {
                //code for deny
            }
        }
    }

    private fun selectImage() {
        val items = arrayOf<CharSequence>("갤러리에서 가져오기", "취소")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("사진 추가")
        builder.setItems(items, DialogInterface.OnClickListener { dialog, item ->
            val result = Utility.checkPermission(this@ArtistProfileUpdateActivity)

            if (items[item] == "갤러리에서 가져오기") {
                if (result)
                    galleryIntent()

            } else if (items[item] == "Cancel") {
                dialog.dismiss()
            }
        })
        builder.show()
    }

    private fun galleryIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE)
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data)
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data!!)
        }
    }

    private fun onCaptureImageResult(data: Intent) {
        thumbnail = (data.extras!!.get("data") as Bitmap)
        val bytes = ByteArrayOutputStream()
        thumbnail!!.compress(Bitmap.CompressFormat.JPEG, 90, bytes)

        val destination = File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis().toString() + ".jpg")

        val fo: FileOutputStream
        try {
            destination.createNewFile()
            fo = FileOutputStream(destination)
            fo.write(bytes.toByteArray())
            fo.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        profileImg.setImageBitmap(thumbnail)
    }

    private fun onSelectFromGalleryResult(data: Intent?) {
        if (data != null) {
            try {
                val original = MediaStore.Images.Media.getBitmap(this@ArtistProfileUpdateActivity.getContentResolver(), data.data)
                val height = original.getHeight()
                val width = original.getWidth()
                if (height > 118) {
                    thumbnail = Bitmap.createScaledBitmap(original, width * 118 / height, 118, true)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        Glide.with(this@ArtistProfileUpdateActivity).load(thumbnail).apply(RequestOptions.circleCropTransform()).into(profileImg)
    }

    fun getStringImage(bmp: Bitmap): String {
        try {
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 60, baos)
            val imageBytes = baos.toByteArray()
            encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            return encodedImage
        } catch (e: Exception) {
            e.printStackTrace()
            Logs.e(TAG, e.message)
        }

        return encodedImage
    }


}
