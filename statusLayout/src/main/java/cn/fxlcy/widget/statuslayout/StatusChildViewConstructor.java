package cn.fxlcy.widget.statuslayout;

import android.content.Context;
import android.content.res.XmlResourceParser;

public interface StatusChildViewConstructor {
    void inflate(Context context, XmlResourceParser parser);

    IStatusChildView newErrorView(Context context);

    IStatusChildView newEmptyView(Context context);

    IStatusChildView newLoadingView(Context context);
}
