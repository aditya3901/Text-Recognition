package com.practice.practiceapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

private lateinit var photoFile: File
private lateinit var img: Bitmap
class MainActivity : AppCompatActivity() {

    companion object{
        private const val CAMERA_PERMISSION_CODE = 1
        private const val CAMERA_REQUEST_CODE = 2
        private const val FILE_NAME = "photo.jpg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Check for permission and open Camera
        captureBtn.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){

                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                photoFile = getPhotoFile(FILE_NAME)

                val fileProvider = FileProvider.getUriForFile(this,
                    "com.practice.practiceapp.fileprovider", photoFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            }
            else{
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            }
        }

        detectBtn.setOnClickListener {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(img, 0)
            val result = recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    for (block in visionText.textBlocks) {

                        for (line in block.lines) {
                            val lineText = line.text
                            detectedText.text = lineText
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Text Scan Failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getPhotoFile(fileName: String): File {
        //Use 'getExternalFilesDir' on Context to access package-specific directories.
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else{
                Toast.makeText(this, "Camera permissions denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK ){
            if(requestCode == CAMERA_REQUEST_CODE){
//                val thumbnail: Bitmap = data?.extras?.get("data") as Bitmap
                img = BitmapFactory.decodeFile(photoFile.absolutePath)
                if(img.width > img.height){
                    val matrix = Matrix()
                    matrix.postRotate(90f)
                    img = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
                }
                imageView.setImageBitmap(img)
            }
        }
    }
}