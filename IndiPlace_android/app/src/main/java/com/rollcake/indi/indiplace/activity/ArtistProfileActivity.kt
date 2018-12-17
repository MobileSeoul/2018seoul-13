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
import kotlinx.android.synthetic.main.activity_artist_profile.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*


class ArtistProfileActivity : BaseActivity() {

    val TAG = ArtistProfileActivity::class.java.simpleName

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

        locationSpinner.adapter = locationAdapter
        generSpinner.adapter = generAdapter
        saveBtn.setOnClickListener {
            if (emptyCheck()) {
                AppkeyManager.setKet(this@ArtistProfileActivity, ARTIST_NAME, singerName.text.toString())
                AppkeyManager.setKet(this@ArtistProfileActivity, INSTA_ADDR, instaAddr.text.toString())
                AppkeyManager.setKet(this@ArtistProfileActivity, YOUTUBE_ADDR, youtubeAddr.text.toString())
                AppkeyManager.setKet(this@ArtistProfileActivity, FACEBOOK_ADDR, facebookAddr.text.toString())
                AppkeyManager.setKet(this@ArtistProfileActivity, GENER, generList.get(generSpinner.selectedItemPosition))
                AppkeyManager.setKet(this@ArtistProfileActivity, ARTIST_LOC, locationList.get(locationSpinner.selectedItemPosition))

                val keyArray = arrayOf("memberId", "artistLocation", "image", "name", "youtube", "facebook", "insta", "genre")
                val valueArray = arrayOf(
                        AppkeyManager.getKey(this@ArtistProfileActivity, MEMBER_ID, ""),
                        AppkeyManager.getKey(this@ArtistProfileActivity, ARTIST_LOC, ""),
                        getImageByte(),
                        AppkeyManager.getKey(this@ArtistProfileActivity, ARTIST_NAME, ""),
                        AppkeyManager.getKey(this@ArtistProfileActivity, YOUTUBE_ADDR, "F"),
                        AppkeyManager.getKey(this@ArtistProfileActivity, FACEBOOK_ADDR, "30"),
                        AppkeyManager.getKey(this@ArtistProfileActivity, INSTA_ADDR, "종로구"),
                        getGenreToInt(AppkeyManager.getKey(this@ArtistProfileActivity, GENER, "")).toString()
                )

                ServerNetworking.sendToMobileServer(this@ArtistProfileActivity, Request.Method.POST, DOMAIN + "/artist", keyArray, valueArray, object : ServerNetworking.getResult {
                    override fun getResultText(text: String) {
                        Logs.e(TAG, text);
                        try {
                            val resultObj = JSONObject(text)
                            if (resultObj.getBoolean("key")) {

                                goToast(getString(R.string.save_profile))
                                AppkeyManager.setKet(this@ArtistProfileActivity, LOGIN_CHECK, true)
                                AppkeyManager.setKet(this@ArtistProfileActivity, ARTIST_ID, resultObj.getJSONObject("message").getString("id"))
                                AppkeyManager.setKet(this@ArtistProfileActivity, ARTIST_IMG, AppApplication.DOMAIN + resultObj.getJSONObject("message").getString("image"))
                                AppkeyManager.setKet(this@ArtistProfileActivity, MEMBER, ARTIST)
                                val intent = Intent(this@ArtistProfileActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }else{
                                if(resultObj.getString("message") == "{\"memberId\":[\"This field must be unique.\"]}"){
                                    Logs.e(TAG , "this field must be unique");
                                    val intent = Intent(this@ArtistProfileActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
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
            val result = Utility.checkPermission(this@ArtistProfileActivity)

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
        Glide.with(this@ArtistProfileActivity).load(thumbnail).apply(RequestOptions.circleCropTransform()).into(profileImg)
    }

    private fun onSelectFromGalleryResult(data: Intent?) {
        if (data != null) {
            try {
                val original = MediaStore.Images.Media.getBitmap(this@ArtistProfileActivity.getContentResolver(), data.data)
                val height = original.getHeight()
                val width = original.getWidth()
                if (height > 118) {
                    thumbnail = Bitmap.createScaledBitmap(original, width * 118 / height, 118, true)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        Glide.with(this@ArtistProfileActivity).load(thumbnail).apply(RequestOptions.circleCropTransform()).into(profileImg)
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
