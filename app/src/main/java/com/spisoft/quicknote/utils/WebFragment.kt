package com.spisoft.quicknote.utils

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.spisoft.quicknote.R
import com.spisoft.quicknote.editor.MyWebView
import com.spisoft.quicknote.server.HttpServer
import com.spisoft.sync.Log

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class WebFragment : Fragment() {

    private lateinit var mServer2: HttpServer
    private val OPEN_MEDIA_REQUEST = 343
    private val REQUEST_SELECT_FILE = 344
    private val PERMISSIONS_REQUEST_RECORD_AUDIO = 345
    private var mUploadMessage: ValueCallback<*>? = null
    private var myRequest: PermissionRequest? = null
    var sNextExtension: String? = null

    internal var mClient: WebViewClient = object : WebViewClient() {
        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            handler.cancel()
        }

        /*
         **  Manage if the url should be load or not, and get the result of the request
         **
         */
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {


            return true
        }


        /*
         **  Catch the error if an error occurs
         **
         */
        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
            super.onReceivedError(view, errorCode, description, failingUrl)

        }


        /*
         **  Display a dialog when the page start
         **
         */
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }


        /*
         **  Remove the dialog when the page finish loading
         **
         */
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)

        }
    }

    private var url: String? = null
    private var mWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            url = it.getString(ARG_URL)
        }
        Log.d("WebFragment","onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_web, container, false)
    }


    fun askForPermission(origin: String, permission: String, requestCode: Int) {
        Log.d("WebView", "inside askForPermission for" + origin + "with" + permission)

        if (ContextCompat.checkSelfPermission(context!!.getApplicationContext(),
                        permission) != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(context as Activity,
                    arrayOf(permission),
                    requestCode)

        } else {
            if (Build.VERSION.SDK_INT >= 21)
                myRequest?.grant(myRequest?.getResources())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mServer2 = HttpServer(context)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        mWebView = MyWebView(context)
        mWebView?.requestFocus()
        mWebView?.setVerticalScrollBarEnabled(false)
        mWebView?.setHorizontalScrollBarEnabled(false)
        mWebView?.getSettings()?.javaScriptEnabled = true
        mWebView?.getSettings()?.domStorageEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(Log.isDebug)
        }

        //mWebView.getSettings().setSupportZoom(false);
        mWebView?.setWebViewClient(mClient)
        mWebView?.addJavascriptInterface(WebViewJavaScriptInterface(context, mServer2), "app")

        mWebView?.setWebChromeClient(object : WebChromeClient() {

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionRequest(request: PermissionRequest) {
                myRequest = request

                for (permission in request.resources) {
                    when (permission) {
                        "android.webkit.resource.AUDIO_CAPTURE" -> {
                            askForPermission(request.origin.toString(), Manifest.permission.RECORD_AUDIO, PERMISSIONS_REQUEST_RECORD_AUDIO)
                        }
                    }
                }
            }

            // For 3.0+ Devices (Start)
            // onActivityResult attached before constructor
            protected fun openFileChooser(uploadMsg: ValueCallback<*>, acceptType: String) {
                mUploadMessage = uploadMsg
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "image/*"
                (context as Activity).startActivityForResult(Intent.createChooser(i, "File Browser"), OPEN_MEDIA_REQUEST)
            }


            // For Lollipop 5.0+ Devices
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onShowFileChooser(mWebView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams): Boolean {
                if (mUploadMessage != null) {
                    mUploadMessage?.onReceiveValue(null)
                    mUploadMessage = null
                }

                mUploadMessage = filePathCallback

                val intent = fileChooserParams.createIntent()
                try {
                    (context as Activity).startActivityForResult(intent, REQUEST_SELECT_FILE)
                } catch (e: ActivityNotFoundException) {
                    mUploadMessage = null
                    Toast.makeText((context as Activity).applicationContext, "Cannot Open File Chooser", Toast.LENGTH_LONG).show()
                    return false
                }

                return true
            }

            //For Android 4.1 only
            protected fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) {
                mUploadMessage = uploadMsg
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                (context as Activity).startActivityForResult(Intent.createChooser(intent, "File Browser"), OPEN_MEDIA_REQUEST)
            }

            protected fun openFileChooser(uploadMsg: ValueCallback<Uri>) {
                mUploadMessage = uploadMsg
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "image/*"
                (context as Activity).startActivityForResult(Intent.createChooser(i, "File Chooser"), OPEN_MEDIA_REQUEST)
            }
        })
        mWebView?.loadUrl(mServer2.getUrl(url))
        (view as FrameLayout).addView(mWebView,params)
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == OPEN_MEDIA_REQUEST || requestCode == REQUEST_SELECT_FILE) && resultCode == Activity.RESULT_OK) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (requestCode == REQUEST_SELECT_FILE) {
                    sNextExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context!!.getContentResolver().getType(data?.getData()!!))
                    if (mUploadMessage == null)
                        return
                    val value = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
                    (mUploadMessage as ValueCallback<Array<Uri>>)?.onReceiveValue(value)
                    mUploadMessage = null
                }
            } else if (requestCode == OPEN_MEDIA_REQUEST) {
                if (null == mUploadMessage)
                    return
                // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
                // Use RESULT_OK only if you're implementing WebView inside an Activity
                val result = if (data == null || resultCode != Activity.RESULT_OK) null else data.data
                (mUploadMessage as ValueCallback<Uri>)?.onReceiveValue(result)
                mUploadMessage = null
            }
        }
    }

    companion object {
        const val ARG_URL = "url"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param url .
         * @return A new instance of fragment WebFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(url: String) =
                WebFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_URL, url)
                    }
                }
    }
}

class WebViewJavaScriptInterface(context: Context?, private val server:HttpServer) {
    @JavascriptInterface
    fun getRequestToken(): String {
        return server.generateID()
    }
}
