package cn.fxlcy.widget;

import android.view.View;

public interface ErrorView {

    void setRetryOnClickListener(View.OnClickListener listener);

    CharSequence getText();

    void setText(CharSequence text);
}
