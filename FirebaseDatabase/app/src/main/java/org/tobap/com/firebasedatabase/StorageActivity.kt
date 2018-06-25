package org.tobap.com.firebasedatabase

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_storage.*
import java.io.ByteArrayOutputStream

class StorageActivity : AppCompatActivity() {
    var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage)

        imageView_storage.background = ShapeDrawable(OvalShape())
        //imageView_storage.clipToOutline = true
        imageView_storage.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        button_upload.setOnClickListener {
            if (bitmap != null)
            {
                uploadImage(bitmap!!)
            }
        }

        button_delete.setOnClickListener {
            deleteImage()
        }

        button_image_read.setOnClickListener {
            imageLoad()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK)
        {
            var imageUrl = data!!.data
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUrl)
            imageView_storage.setImageBitmap(bitmap)

        }
    }

    fun uploadImage(bitmap: Bitmap)
    {
        var baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        var data = baos.toByteArray()
        FirebaseStorage.getInstance().reference.child("users").child(editText_filename.text.toString()).putBytes(data)
                .addOnCompleteListener {
                    task->
                    if (task.isSuccessful)
                    {
                        Toast.makeText(this, "업로드에 성공하였습니다", Toast.LENGTH_LONG).show()
                        saveUrlToDatabase(task.result.downloadUrl.toString())
                    }
                    else
                    {
                        Toast.makeText(this, task.exception.toString(), Toast.LENGTH_LONG).show()
                    }
                }
    }

    fun saveUrlToDatabase(url: String)
    {
        var map = HashMap<String, Any>()
        map["profile_image_url"] = url
        FirebaseFirestore.getInstance().collection("users").document(editText_document_id.text.toString()).update(map)
    }

    fun deleteImage()
    {
        FirebaseStorage.getInstance().reference.child("users").child(editText_filename.text.toString()).delete().addOnCompleteListener {
            task ->
            if (task.isSuccessful)
            {
                Toast.makeText(this, "파일 삭제가 완료 되었습니다.", Toast.LENGTH_LONG).show()

                var map = HashMap<String, Any>()
                map["profile_image_url"] = FieldValue.delete()
                FirebaseFirestore.getInstance().collection("users").document(editText_document_id.toString()).update(map)
            }
            else
            {
                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun imageLoad()
    {
        FirebaseFirestore.getInstance().collection("users").document(editText_document_read.toString()).get().addOnCompleteListener {
            task ->
            if (task.isSuccessful)
            {
                var userDTO = task.result.toObject(UserDTO::class.java)
                println(userDTO.profileUrl)
                Picasso.get().load(userDTO.profileUrl).into(imageView_server)
            }
        }
    }
}
