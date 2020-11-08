package com.spisoft.quicknote.editor

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.SparseIntArray
import android.view.Surface
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.spisoft.quicknote.Note
import com.spisoft.quicknote.R
import com.spisoft.quicknote.databases.KeywordsHelper
import com.spisoft.quicknote.databases.NoteManager
import com.spisoft.quicknote.databases.RecentHelper
import com.spisoft.quicknote.utils.CustomOrientationEventListener
import com.spisoft.quicknote.utils.FileUtils
import com.spisoft.quicknote.utils.PictureUtils
import com.spisoft.quicknote.utils.ZipUtils
import com.spisoft.sync.Log
import kotlinx.android.synthetic.main.activity_image.*
import org.json.JSONException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ExecutorService

class ImageActivity : AppCompatActivity(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private var mShootButton: View? = null
    private var mTmpDir: File? = null


    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return
        viewFinder!!.animate().alpha(0f).setDuration(100).start()

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), object: ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                viewFinder!!.animate().alpha(1f).setDuration(100).start()

                handlePhotoBitmap(getBitmap(image))
                image.close()
            }

        });

    }

    private fun getBitmap(image: ImageProxy): Bitmap? {
        val buffer: ByteBuffer = image.planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        val clonedBytes = bytes.clone()
        return BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.size)
    }
    private var mImageListView: LinearLayout? = null
    private var mAvailableKeywordsLinearLayout: LinearLayout? = null
    private var mAvailableKeywords: MutableList<String>? = null
    private var mSelectedKeywordsLinearLayout: LinearLayout? = null
    private var mKeywordET: EditText? = null
    private var mCreateButton: View? = null
    private var mNextButton: View? = null
    private var mFlashButton: ImageButton? = null
    private var mExternalCameraButton: View? = null
    private var customOrientationEventListener: CustomOrientationEventListener? = null

    companion object {
        const val ROOT_PATH = "root_path"
        val DISPLAY_ORIENTATIONS = SparseIntArray()

        init {
            DISPLAY_ORIENTATIONS.put(Surface.ROTATION_0, 0)
            DISPLAY_ORIENTATIONS.put(Surface.ROTATION_90, 90)
            DISPLAY_ORIENTATIONS.put(Surface.ROTATION_180, 180)
            DISPLAY_ORIENTATIONS.put(Surface.ROTATION_270, 270)
        }
    }

    private var mCurrentOrientation = 0
    private var mHasAlreadyAsked = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mTmpDir = File(this.cacheDir.absolutePath + "/image_dir")
        if (mTmpDir!!.exists()) {
            //delete
            FileUtils.deleteRecursive(mTmpDir)
        }
        mTmpDir!!.mkdirs()
        setContentView(R.layout.activity_image)
        mAvailableKeywords = ArrayList()
        mAvailableKeywordsLinearLayout = findViewById(R.id.available_keywords)
        mSelectedKeywordsLinearLayout = findViewById(R.id.selected_keywords)
        mKeywordET = findViewById(R.id.keyword)
        mKeywordET!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                refreshAvailableKeywords()
            }
        })
        mImageListView = findViewById(R.id.image_list)

        mExternalCameraButton = findViewById(R.id.external_camera_button)
        mExternalCameraButton?.setOnClickListener(this)
        mNextButton = findViewById(R.id.next)
        mNextButton?.setOnClickListener(this)
        mFlashButton = findViewById(R.id.flash_button)
        mFlashButton?.setOnClickListener(this)
        mShootButton = findViewById(R.id.shoot_button)
        mShootButton?.setOnClickListener(this)
        mCreateButton = findViewById(R.id.create_button)
        mCreateButton?.setOnClickListener(this)
        try {
            val keywordsJson = KeywordsHelper.getInstance(this).json
            val array = keywordsJson.getJSONArray("data")
            if (array.length() > 0) for (i in array.length() - 1 downTo 0) {
                val action = array.getJSONObject(i).getString("action")
                if (action != "move" && action != "delete") {
                    val keyword = array.getJSONObject(i).getString("keyword")
                    if (!mAvailableKeywords!!.contains(keyword)) mAvailableKeywords?.add(keyword)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        refreshAvailableKeywords()
    }

    private fun refreshAvailableKeywords() {
        mAvailableKeywordsLinearLayout!!.removeAllViews()
        val text = mKeywordET!!.text.toString()
        if (text.isEmpty()) {
            var i = 0
            while (i < 5 && i < mAvailableKeywords!!.size) {
                mAvailableKeywordsLinearLayout!!.addView(createKeywordView(mAvailableKeywords!![i]))
                i++
            }
        } else {
            mAvailableKeywordsLinearLayout!!.addView(createKeywordView(text))
            for (i in mAvailableKeywords!!.indices) {
                if (mAvailableKeywordsLinearLayout!!.childCount > 5) break
                if (mAvailableKeywords!![i].toLowerCase().startsWith(text.toLowerCase())) mAvailableKeywordsLinearLayout!!.addView(createKeywordView(mAvailableKeywords!![i]))
            }
        }
    }

    private fun createKeywordView(s: String): View {
        val view = CheckBox(this)
        view.text = s
        view.setOnCheckedChangeListener(this)
        return view
    }


    public override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            if (mHasAlreadyAsked) finish()
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1001)
            mHasAlreadyAsked = true
        } else startCamera()
        customOrientationEventListener = object : CustomOrientationEventListener(this) {
            override fun onSimpleOrientationChanged(orientation: Int) {
                var orientation = orientation
                if (orientation == 1) orientation = 3 else if (orientation == 3) orientation = 1
                mCurrentOrientation = orientation
            }
        }
        customOrientationEventListener!!.enable()
    }


    public override fun onPause() {
        super.onPause()
        customOrientationEventListener!!.disable()

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                    .build()
            imageCapture = ImageCapture.Builder()
                    .build()
            // Select back camera
            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture)
                camera!!.cameraControl.enableTorch(PreferenceManager.getDefaultSharedPreferences(this).getInt("last_camera_flash", TorchState.ON)==TorchState.ON)
                preview?.setSurfaceProvider(viewFinder.createSurfaceProvider(camera?.cameraInfo))
                refreshFlashButton()

            } catch(exc: Exception) {
                Log.d("ImageActivity", "Use case binding failed" + exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun refreshFlashButton() {
        camera ?: return;
        if (camera!!.cameraInfo.torchState.value == TorchState.OFF) {
            mFlashButton!!.setImageResource(R.drawable.flash_off)
        } else {
            mFlashButton!!.setImageResource(R.drawable.flash_on)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1002 && resultCode == Activity.RESULT_OK) {
            handlePhotoBitmap(data!!.extras!!["data"] as Bitmap?)
        }
    }

    private fun handlePhotoBitmap(bitmap: Bitmap?) {
        object: AsyncTask<Void?, Void?, View?>() {
            protected override fun doInBackground(vararg voids: Void?): View? {
                val matrix = Matrix()
                matrix.postRotate(if (mCurrentOrientation == 0 || mCurrentOrientation == 2) DISPLAY_ORIENTATIONS[mCurrentOrientation].toFloat() +90 else DISPLAY_ORIENTATIONS[mCurrentOrientation].toFloat()-90)

                val scaledBitmap = Bitmap.createScaledBitmap(bitmap!!, bitmap.width, bitmap.height, true)

                val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
                val dataF = File(mTmpDir, "data/")
                dataF.mkdirs()
                val file = File(dataF, System.currentTimeMillis().toString() + ".jpg")
                try {
                    val fileOutputStream = FileOutputStream(file)
                    rotatedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                    fileOutputStream.flush()
                    fileOutputStream.close()
                    rotatedBitmap.recycle()
                    val v = ImageView(this@ImageActivity)
                    v.adjustViewBounds = true
                    val preview = File(dataF, "preview_" + file.name + ".jpg") //previews have twice .jpg .... not changing that otherwise deleted from editor will fail...
                    try {
                        PictureUtils.resize(file.absolutePath, preview.absolutePath, NoteManager.PREVIEW_WIDTH, NoteManager.PREVIEW_HEIGHT)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    v.setImageURI(Uri.fromFile(preview))
                    v.tag = file.name
                    v.setOnClickListener(this@ImageActivity)
                    v.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)
                    return v
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
                return null
            }

            override fun onPostExecute(v: View?) {
                if (v != null) mImageListView!!.addView(v)
                mNextButton!!.isEnabled = true
            }
        }.execute()
    }

    override fun onClick(v: View) {
        if (v === mFlashButton) {
            if (camera!!.cameraInfo.torchState.value == TorchState.OFF) {
                camera!!.cameraControl.enableTorch(true)
            } else {
                camera!!.cameraControl.enableTorch(false)
            }
            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("last_camera_flash", camera!!.cameraInfo.torchState.value!!).apply()
            refreshFlashButton()
        } else if (v === mExternalCameraButton) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, 1002)
            }
        } else if (v === mShootButton) {
            takePhoto()
        } else if (v === mNextButton) {
            findViewById<View>(R.id.note_params).visibility = View.VISIBLE
            findViewById<View>(R.id.photo_taking_container).visibility = View.GONE

        } else if (v === mCreateButton) {
            val metadata = Note.Metadata()
            metadata.creation_date = System.currentTimeMillis()
            metadata.last_modification_date = System.currentTimeMillis()
            val keywords: MutableList<String> = ArrayList()
            for (i in 0 until mSelectedKeywordsLinearLayout!!.childCount) {
                keywords.add((mSelectedKeywordsLinearLayout!!.getChildAt(i) as TextView).text.toString())
            }
            metadata.keywords.addAll(keywords)
            FileUtils.writeToFile(File(mTmpDir, "metadata.json").absolutePath, metadata.toJsonObject().toString())
            FileUtils.writeToFile(File(mTmpDir, "index.html").absolutePath, NoteManager.getDefaultHTML())
            //reduce quality when needed
            val qualityValues = resources.getIntArray(R.array.photo_qualities_values)
            val quality = findViewById<Spinner>(R.id.quality_selector).selectedItemPosition
            val qualityValue = qualityValues[quality]
            if(qualityValue != -1){
                val images = File(mTmpDir, "data/").listFiles()
                Log.d("ResizeDebug","resizing to "+qualityValue)
                if (images != null) {
                    for (image in images) {
                        if (image.name.startsWith("preview_")) continue
                        val tmpImage = File(image.parentFile, "tmp.jpg")
                        PictureUtils.resize(image.absolutePath, tmpImage.absolutePath, qualityValue, qualityValue, 90);
                        image.delete()
                        tmpImage.renameTo(image)
                    }
                }

            }
            if ((findViewById<View>(R.id.one_note_per_photo_cb) as CheckBox).isChecked) {
                val images = File(mTmpDir, "data/").listFiles()
                if (images != null) {
                    for (image in images) {
                        if (image.name.startsWith("preview_")) continue
                        val noteFolder = File(mTmpDir, "note")
                        if (noteFolder.exists()) FileUtils.deleteRecursive(noteFolder)
                        val data = File(noteFolder, "data/")
                        data.mkdirs()
                        image.renameTo(File(data, image.name))
                        val preview = File(image.parentFile, "preview_" + image.name + ".jpg") //previews have twice .jpg .... not changing that otherwise deleted from editor will fail...
                        if (preview.exists()) {
                            preview.renameTo(File(data, preview.name))
                        }
                        try {
                            FileUtils.copy(FileInputStream(File(mTmpDir, "index.html")), FileOutputStream(File(noteFolder, "index.html")))
                            FileUtils.copy(FileInputStream(File(mTmpDir, "metadata.json")), FileOutputStream(File(noteFolder, "metadata.json")))
                            val note = NoteManager.createNewNote(intent.getStringExtra(ROOT_PATH))
                            ZipUtils.zipFolder(noteFolder, note.path, ArrayList())
                            RecentHelper.getInstance(this).addNote(note)
                            for (keyword in keywords) {
                                KeywordsHelper.getInstance(this).addKeyword(keyword, note)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        if (noteFolder.exists()) FileUtils.deleteRecursive(noteFolder)
                    }
                }
            } else {
                val note = NoteManager.createNewNote(intent.getStringExtra(ROOT_PATH))
                ZipUtils.zipFolder(mTmpDir, note.path, ArrayList())
                RecentHelper.getInstance(this).addNote(note)
                for (keyword in keywords) {
                    KeywordsHelper.getInstance(this).addKeyword(keyword, note)
                }
            }
            setResult(Activity.RESULT_OK)
            finish()
        } else if (v is ImageView) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.delete_confirm)
                    .setPositiveButton(android.R.string.ok) { dialog, which ->
                        val name = v.getTag() as String
                        File(mTmpDir, "data/$name").delete()
                        File(mTmpDir, "data/preview_$name.jpg").delete() //previews have twice .jpg .... not changing that otherwise deleted from editor will fail...
                        (v.getParent() as LinearLayout).removeView(v)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        buttonView.postDelayed({
            if (isChecked) {
                mAvailableKeywordsLinearLayout!!.removeView(buttonView)
                mSelectedKeywordsLinearLayout!!.addView(buttonView)
            } else {
                mSelectedKeywordsLinearLayout!!.removeView(buttonView)
                mAvailableKeywordsLinearLayout!!.addView(buttonView)
            }
        }, 500)
    }
}