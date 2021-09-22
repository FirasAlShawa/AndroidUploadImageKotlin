package com.cool.icontest

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.*
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*


class MainActivity : AppCompatActivity() ,View.OnClickListener {

    private val PICK_IMAGE_REQUEST_CODE = 3
    private val READ_EXTERNAL_MEMORY_PERMISSION = 2

    lateinit var imageplace : ImageView
    lateinit var myimagetext : TextView
    lateinit var uploadbtn : Button
    var mBitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageplace = findViewById(R.id.imageplace)
        myimagetext = findViewById(R.id.myimage)
        myimagetext.setOnClickListener(this)
        uploadbtn = findViewById(R.id.uploadbtn)
        uploadbtn.setOnClickListener(this)
    }

    fun showImageChooser(activity: Activity){
        val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.myimage -> {
                if (ContextCompat.checkSelfPermission(
                                applicationContext,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED) {
                    val galleryIntent = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )

                    startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
                } else {
                    ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            READ_EXTERNAL_MEMORY_PERMISSION
                    )
                }
            }
            R.id.uploadbtn -> {
                if (this.mBitmap != null)
                    multipartImageUpload();
                else {
                    Toast.makeText(getApplicationContext(), "Bitmap is null. Try again", Toast.LENGTH_SHORT).show();
                }
            }
            }
        }

    private fun multipartImageUpload() {
        try {
            val filesDir = applicationContext.filesDir
            val file = File(filesDir, "image" + ".png")
            val bos = ByteArrayOutputStream()
            mBitmap?.compress(Bitmap.CompressFormat.PNG, 0, bos)
            val bitmapdata: ByteArray = bos.toByteArray()
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            val reqFile = RequestBody.create(MediaType.parse("image/*"), file)
            val body = MultipartBody.Part.createFormData("upload", file.name, reqFile)
            val name = RequestBody.create(MediaType.parse("text/plain"), "upload")
            var request =ApiService.buildService(EndPoints::class.java)

            val req: Call<ResponseBody> = request.postImage(body, name)
            req.enqueue(object : Callback<ResponseBody>{
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                    if (response.code() == 200) {
                        myimagetext.setText("Uploaded Successfully!");
                        myimagetext.setTextColor(Color.BLUE);
                    }

                    Toast.makeText(getApplicationContext(), "${response.code()} ", Toast.LENGTH_SHORT).show();
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    myimagetext.setText("Uploaded Failed!");
                    myimagetext.setTextColor(Color.RED);
                    Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT).show();
                    t.printStackTrace();
                }
            })
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST_CODE){
            if (data != null){

                    val filePath: String = getImageFilePath(data).toString()
                    if (filePath != null) {
                        this.mBitmap = decodeFile(filePath)
                        imageplace.setImageBitmap(this.mBitmap)
//                    Glide
//                            .with(this)
//                            .load(data.data!!)
//                            .centerCrop()
//                            .into(imageplace)
                }

            }
        }
    }


    private fun getImageFromFilePath(data: Intent?): String? {
        val isCamera = data == null || data.data == null
        return if (isCamera) getCaptureImageOutputUri()?.getPath() else getPathFromURI(data!!.data)
    }
    private fun getCaptureImageOutputUri(): Uri? {
        var outputFileUri: Uri? = null
        val getImage: File? = getExternalFilesDir("")
        if (getImage != null) {
            outputFileUri = Uri.fromFile(File(getImage.getPath(), "profile.png"))
        }
        return outputFileUri
    }

    fun getImageFilePath(data: Intent?): String? {
        return getImageFromFilePath(data)
    }
    private fun getPathFromURI(contentUri: Uri?): String? {
        val proj = arrayOf(MediaStore.Audio.Media.DATA)
        val cursor: Cursor? = contentUri?.let { contentResolver.query(it, proj, null, null, null) }
        val column_index: Int = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)!!
        cursor?.moveToFirst()
        return cursor?.getString(column_index)
    }

}



