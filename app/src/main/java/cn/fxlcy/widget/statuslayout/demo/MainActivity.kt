package cn.fxlcy.widget.statuslayout.demo

import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import cn.fxlcy.widget.statuslayout.StatusLayout

class MainActivity: AppCompatActivity() {
    private lateinit var mSl:StatusLayout
    private lateinit var mWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        mSl = findViewById(R.id.sl)
        mSl.setOnErrorRetryClickListener {
            mWebView.reload()
        }

        mWebView = findViewById(R.id.webview)

        mWebView.webViewClient = object :WebViewClient(){

            private var mError = false

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                mError = false
                super.onPageStarted(view, url, favicon)
                mSl.loading()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                if(mError){
                    mSl.error()
                }else {
                    mSl.normal()
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                mError = true
                super.onReceivedError(view, request, error)
            }
        }
        mWebView.loadUrl("https://github.com/fxlcy/StatusLayout")
    }
}