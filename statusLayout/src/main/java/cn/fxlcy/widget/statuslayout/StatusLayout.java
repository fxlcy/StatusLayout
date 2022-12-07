package cn.fxlcy.widget.statuslayout;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.AnimatorRes;
import androidx.annotation.CallSuper;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class StatusLayout extends FrameLayout {

    private final static String TAG = "StatusLayout";

    private @LayoutStatus
    int mStatus;
    private View mErrorView;
    private View mEmptyView;
    private View mNormalView;
    private View mLoadingView;

    private View mCurrentView;

    private boolean mIsAnimable = true;

    private CharSequence mErrorText;
    private CharSequence mEmptyText;
    private CharSequence mLoadingText;

    private final @AnimatorRes
    int mShowAnimRes;
    private final @AnimatorRes
    int mHideAnimRes;
    private int mAnimDuration = 300;

    private View.OnClickListener mOnErrorRetryClickListener;
    private View.OnClickListener mOnEmptyRetryClickListener;

    private final String mStatusViewConstructorClassname;
    private final int mStatusViewConstructorInflaterXml;

    private StatusChildViewConstructor mConstructor;

    public void setStatus(@LayoutStatus int status) {
        final View view = getViewByStatus(status);
        mCurrentView.setVisibility(GONE);
        view.setVisibility(VISIBLE);
        mCurrentView = view;
        mStatus = status;

    }

    public StatusLayout(@NonNull Context context) {
        this(context, null);
    }

    public StatusLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StatusLayout);

        if (isInEditMode()) {
            mStatus = a.getInt(R.styleable.StatusLayout_status, LayoutStatus.NORMAL);
        } else {
            mStatus = a.getInt(R.styleable.StatusLayout_status, LayoutStatus.LOADING);
        }

        mShowAnimRes = a.getResourceId(R.styleable.StatusLayout_showAnimRes, -1);
        mHideAnimRes = a.getResourceId(R.styleable.StatusLayout_hideAnimRes, -1);

        mAnimDuration = a.getInt(R.styleable.StatusLayout_animDuration, mAnimDuration);
        mStatusViewConstructorClassname = a.getString(R.styleable.StatusLayout_statusViewConstructorClassname);
        mStatusViewConstructorInflaterXml = a.getResourceId(R.styleable.StatusLayout_statusViewConstructorInflaterXml, 0);

        a.recycle();

        dispatchStatusChanged(mStatus);
    }


    @CallSuper
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initStatusView();
    }

    private void initStatusView() {
        switch (mStatus) {
            case LayoutStatus.EMPTY:
                if (mEmptyView == null) {
                    ensureEmptyView();
                }
                break;
            case LayoutStatus.LOADING:
                if (mLoadingView == null) {
                    ensureLoadingView();
                }
                break;
            case LayoutStatus.ERROR:
                if (mErrorView == null) {
                    ensureErrorView();
                }
                break;
            case LayoutStatus.NONE:
            case LayoutStatus.NORMAL:
                break;
        }
    }

    private boolean mAttached = false;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mAttached) {
            dispatchStatusChanged(mStatus);
            mAttached = true;
        }
    }

    @CallSuper
    @Override
    public void onViewAdded(View child) {
        addChild(child);
    }

    @CallSuper
    @Override
    public void onViewRemoved(View child) {
        removeChild(child);
    }

    private void removeChild(View view) {
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        switch (params.mStatus) {
            case LayoutStatus.NORMAL:
                mNormalView = null;
                break;
            case LayoutStatus.EMPTY:
                mEmptyView = null;
                break;
            case LayoutStatus.ERROR:
                mErrorView = null;
                break;
            case LayoutStatus.LOADING:
                mLoadingView = null;
                break;
            case LayoutStatus.NONE:
                break;
        }
    }

    private void addChild(View view) {
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        switch (params.mStatus) {
            case LayoutStatus.NORMAL:
                if (mNormalView != null) {
                    throw new RuntimeException("already exists normalView!");
                }
                mNormalView = view;
                adjustStatus(view, LayoutStatus.NORMAL);
                break;
            case LayoutStatus.EMPTY:
                if (mEmptyView != null) {
                    throw new RuntimeException("already exists empty`View!");
                }
                mEmptyView = view;
                setOnRetryClickListenerInternal(mEmptyView, mOnEmptyRetryClickListener);
                adjustStatus(view, LayoutStatus.EMPTY);
                break;
            case LayoutStatus.ERROR:
                if (mErrorView != null) {
                    throw new RuntimeException("already exists errorView!");
                }
                mErrorView = view;
                setOnRetryClickListenerInternal(mErrorView, mOnErrorRetryClickListener);
                adjustStatus(view, LayoutStatus.ERROR);
                break;
            case LayoutStatus.LOADING:
                if (mLoadingView != null) {
                    throw new RuntimeException("already exists loadingView!");
                }
                mLoadingView = view;
                adjustStatus(view, LayoutStatus.LOADING);
                break;
            case LayoutStatus.NONE:
                break;
        }
    }

    private void adjustStatus(View view, @LayoutStatus int status) {
        if (status == mStatus) {
            view.setVisibility(VISIBLE);
            mCurrentView = view;
        } else {
            view.setVisibility(GONE);
        }
    }

    private Animator obtainShowAnim() {
        if (mShowAnimRes == -1) {
            ObjectAnimator animator = new ObjectAnimator();
            animator.setDuration(mAnimDuration);
            animator.setProperty(View.ALPHA);
            animator.setFloatValues(0f, 1f);

            return animator;
        } else {
            return AnimatorInflater.loadAnimator(getContext(), mShowAnimRes);
        }
    }


    private Animator obtainHideAnim() {
        if (mHideAnimRes == -1) {
            ObjectAnimator animator = new ObjectAnimator();
            animator.setDuration(mAnimDuration);
            animator.setProperty(View.ALPHA);
            animator.setFloatValues(1f, 0f);

            return animator;
        } else {
            return AnimatorInflater.loadAnimator(getContext(), mHideAnimRes);
        }
    }


    public @LayoutStatus
    int getStatus() {
        return mStatus;
    }

    public @LayoutStatus
    int getWhenStatus() {
        int status;
        if (mIsStartingAnim) {
            status = mStartingAnimStatus;
        } else {
            status = mStatus;
        }
        return status;
    }

    public void error(Object obj) {
        if (obj instanceof Integer) {
            error(getContext().getString((Integer) obj));
        } else if (obj instanceof CharSequence) {
            error((CharSequence) obj);
        } else {
            error();
        }
    }


    public void error() {
        error(null);
    }

    private void error(CharSequence errorText) {
        ensureErrorView();

        changeStatus(LayoutStatus.ERROR);

        setStatusText(mErrorView, errorText, new IText() {
            @Override
            public void setText(CharSequence text) {
                mErrorText = text;
            }

            @Override
            public CharSequence getText() {
                return mErrorText;
            }
        });
    }

    private void ensureErrorView() {
        if (mErrorView == null) {
            //添加默认errorView
            IStatusChildView errorView = getStatusViewConstructor().newErrorView(getContext());
            View view = errorView.getView();
            setViewStatus(view, LayoutStatus.ERROR);
            addView(view);
        }
    }

    public void empty(Object obj) {
        if (obj instanceof Integer) {
            empty(getContext().getString((Integer) obj));
        } else if (obj instanceof CharSequence) {
            empty((CharSequence) obj);
        } else {
            empty();
        }
    }

    public void empty() {
        empty(null);
    }

    private void empty(CharSequence emptyText) {
        ensureEmptyView();

        changeStatus(LayoutStatus.EMPTY);

        setStatusText(mEmptyView, emptyText, new IText() {
            @Override
            public void setText(CharSequence text) {
                mEmptyText = text;
            }

            @Override
            public CharSequence getText() {
                return mEmptyText;
            }
        });
    }

    private void ensureEmptyView() {
        if (mEmptyView == null) {
            //添加默认emptyView
            IStatusChildView emptyView = getStatusViewConstructor().newEmptyView(getContext());
            View view = emptyView.getView();
            setViewStatus(view, LayoutStatus.EMPTY);
            addView(view);
        }
    }

    private void setStatusText(View view, CharSequence text, IText currentText) {
        if (view instanceof IText) {
            if (currentText.getText() == null) {
                currentText.setText(((IText) view).getText());
            }

            if (text == null) {
                text = currentText.getText();
            }

            if (text != null) {
                ((IText) view).setText(text);
            }
        }
    }

    public void loading(Object obj) {
        if (obj instanceof Integer) {
            empty(getContext().getString((Integer) obj));
        } else if (obj instanceof CharSequence) {
            empty((CharSequence) obj);
        } else {
            empty();
        }
    }

    public void loading(CharSequence loadingText) {
        ensureLoadingView();

        changeStatus(LayoutStatus.LOADING);

        setStatusText(mEmptyView, loadingText, new IText() {
            @Override
            public void setText(CharSequence text) {
                mLoadingText = text;
            }

            @Override
            public CharSequence getText() {
                return mLoadingText;
            }
        });
    }

    public void loading() {
        loading(null);
    }

    private void ensureLoadingView() {
        if (mLoadingView == null) {
            //添加默认errorView
            IStatusChildView loadingView = getStatusViewConstructor().newLoadingView(getContext());
            View view = loadingView.getView();
            setViewStatus(view, LayoutStatus.LOADING);
            addView(view);
        }
    }

    public void normal() {
        changeStatus(LayoutStatus.NORMAL);
    }


    //是否可以执行状态切换动画
    public void setAnimable(boolean animable) {
        mIsAnimable = animable;
    }


    private Animator mShowAnim;
    private Animator mHideAnim;

    private boolean mIsStartingAnim = false;
    private int mStartingAnimStatus = LayoutStatus.NONE;


    private OnStatusChangedListener mOnStatusChangedListener;

    public void setOnStatusChangedListener(OnStatusChangedListener mOnStatusChangedListener) {
        this.mOnStatusChangedListener = mOnStatusChangedListener;
    }

    @CallSuper
    protected void dispatchStatusChanged(int status) {
        if (mOnStatusChangedListener != null) {
            mOnStatusChangedListener.onStatusChanged(status);
        }
    }

    public interface OnStatusChangedListener {
        void onStatusChanged(int status);
    }

    private void changeStatus(@LayoutStatus final int status) {
        Log.e(TAG, "mStartingAnimStatus:" + mStartingAnimStatus
                + ",mIsStartingAnim:" + mIsStartingAnim + ",mStatus:" + mStatus + ",status:" + status);

        if (((mStartingAnimStatus == status && mIsStartingAnim) || (!mIsStartingAnim && mStatus == status))) {
            return;
        }

        final View view = getViewByStatus(status);

        if (!mIsAnimable) {
            setStatus(status);
            dispatchStatusChanged(status);
            return;
        }

        if (mHideAnim != null) {
            mHideAnim.cancel();
            mIsStartingAnim = false;
            mHideAnim = null;
        }

        if (mShowAnim != null) {
            mShowAnim.cancel();
            mShowAnim = null;
        }

        mIsStartingAnim = true;
        mStartingAnimStatus = status;

        mHideAnim = obtainHideAnim();
        mHideAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentView.setVisibility(GONE);
                mShowAnim = obtainShowAnim();
                mShowAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mIsStartingAnim = false;
                        mShowAnim = null;

                        dispatchStatusChanged(status);
                    }
                });

                view.setEnabled(true);
                view.setVisibility(VISIBLE);
                mShowAnim.setTarget(view);
                mShowAnim.start();

                mStatus = status;
                mCurrentView = view;

                mHideAnim = null;
            }
        });

        mCurrentView.setEnabled(false);
        mHideAnim.setTarget(mCurrentView);
        mHideAnim.start();
    }

    public void setOnErrorRetryClickListener(final OnClickListener l) {
        mOnErrorRetryClickListener = view -> {
            if (mLoadingView != null) {
                loading();
            }
            l.onClick(view);
        };

        setOnRetryClickListenerInternal(mErrorView, mOnErrorRetryClickListener);
    }

    public void setOnEmptyRetryClickListener(final OnClickListener l) {
        mOnEmptyRetryClickListener = view -> {
            if (mLoadingView != null) {
                loading();
            }
            l.onClick(view);
        };

        setOnRetryClickListenerInternal(mEmptyView, mOnEmptyRetryClickListener);
    }


    private void setOnRetryClickListenerInternal(View view, OnClickListener clickListener) {
        if (clickListener != null) {
            if (view instanceof IRetryChildView) {
                ((IRetryChildView) view).setOnRetryClickListener(clickListener);
            } else if (view != null) {
                view.setOnClickListener(clickListener);
            }
        }
    }

    public View getViewByStatus(@LayoutStatus int status) {
        switch (status) {
            case LayoutStatus.NORMAL:
                return Objects.requireNonNull(mNormalView);
            case LayoutStatus.EMPTY:
                return Objects.requireNonNull(mEmptyView);
            case LayoutStatus.ERROR:
                return Objects.requireNonNull(mErrorView);
            case LayoutStatus.LOADING:
                return Objects.requireNonNull(mLoadingView);
        }

        throw new RuntimeException("status error");
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new LayoutParams(super.generateLayoutParams(lp));
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    private StatusChildViewConstructor getStatusViewConstructor() {
        if (mConstructor != null) return mConstructor;

        if (mStatusViewConstructorClassname != null) {
            try {
                mConstructor = (StatusChildViewConstructor) Class.forName(mStatusViewConstructorClassname).newInstance();
                int xml = mStatusViewConstructorInflaterXml;
                if (xml != 0) {
                    try (XmlResourceParser parser = getResources().getXml(xml)) {
                        mConstructor.inflate(getContext(), parser);
                    }
                }
            } catch (Throwable e) {
                mConstructor = HOLDER;
            }

            return mConstructor;
        }

        throw new RuntimeException("please set attr statusViewConstructorClassname");
    }


    private void setViewStatus(View view, int status) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null) {
            params = generateLayoutParams(params);
        } else {
            params = generateDefaultLayoutParams();
        }

        ((LayoutParams) params).mStatus = status;

        view.setLayoutParams(params);

    }

    @IntDef({LayoutStatus.NORMAL, LayoutStatus.ERROR, LayoutStatus.EMPTY, LayoutStatus.LOADING, LayoutStatus.NONE})
    public @interface LayoutStatus {
        int NORMAL = 0;
        int ERROR = 1;
        int EMPTY = 2;
        int LOADING = 3;
        int NONE = -1;
    }


    public static class LayoutParams extends FrameLayout.LayoutParams {
        private @LayoutStatus
        int mStatus = LayoutStatus.NONE;

        public LayoutParams(@NonNull Context c, @Nullable AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.StatusLayout_Layout);

            mStatus = a.getInt(R.styleable.StatusLayout_Layout_layout_status, LayoutStatus.NONE);

            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(@NonNull ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(@NonNull MarginLayoutParams source) {
            super(source);
        }


        public void setStatus(@LayoutStatus int status) {
            mStatus = status;
        }
    }

    private static final StatusChildViewConstructor HOLDER = new StatusChildViewConstructor() {
        @Override
        public void inflate(Context context, XmlResourceParser parser) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IStatusChildView newErrorView(Context context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IStatusChildView newEmptyView(Context context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IStatusChildView newLoadingView(Context context) {
            throw new UnsupportedOperationException();
        }
    };
}
