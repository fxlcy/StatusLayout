package cn.fxlcy.widget.statuslayout.demo

import android.content.Context
import android.content.res.XmlResourceParser
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.fxlcy.widget.statuslayout.IStatusChildView
import cn.fxlcy.widget.statuslayout.StatusChildViewConstructor

class DefaultStatusViewConstructor : StatusChildViewConstructor {
    override fun inflate(context: Context?, parser: XmlResourceParser?) {
    }

    private fun View.statusChildView(): IStatusChildView {
        return IStatusChildView { this@statusChildView }
    }

    override fun newErrorView(context: Context?): IStatusChildView {
        return TextView(context).apply {
            this.text = "加载失败"
            this.textSize = 30f
            this.gravity = Gravity.CENTER
            this.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }.statusChildView()
    }

    override fun newEmptyView(context: Context?): IStatusChildView {
        return TextView(context).apply {
            this.text = "空数据"
            this.textSize = 30f
            this.gravity = Gravity.CENTER
            this.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }.statusChildView()
    }

    override fun newLoadingView(context: Context?): IStatusChildView {
        return TextView(context).apply {
            this.text = "加载中..."
            this.textSize = 30f
            this.gravity = Gravity.CENTER
            this.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }.statusChildView()
    }
}