package cn.fxlcy.widget;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.AnimatorRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.fxlcy.widget.statuslayout.R;

public class StatusLayout extends FrameLayout {
    private @LayoutStatus
    int mStatus;
    private View mErrorView;
    private View mEmptyView;
    private View mNormalView;
    private View mLoadingView;

    private SparseArray<View> mStatusViews;

    private View mCurrentView;

    private boolean mIsAnimable = true;
    private OnHierarchyChangeListener mOnHierarchyChangeListener;

    private CharSequence mErrorText;

    private @AnimatorRes
    final
    int mShowAnimRes;
    private @AnimatorRes
    final
    int mHideAnimRes;

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

        mStatus = a.getInt(R.styleable.StatusLayout_status, LayoutStatus.NORMAL);

        if (isInEditMode()) {
            mStatus = a.getInt(R.styleable.StatusLayout_toolsStatus, mStatus);
        }

        mShowAnimRes = a.getResourceId(R.styleable.StatusLayout_showAnimRes, -1);
        mHideAnimRes = a.getResourceId(R.styleable.StatusLayout_hideAnimRes, -1);

        a.recycle();


        super.setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                if (parent == StatusLayout.this) {
                    addChild(child);
                }

                if (mOnHierarchyChangeListener != null) {
                    mOnHierarchyChangeListener.onChildViewAdded(parent, child);
                }
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                if (parent == StatusLayout.this) {
                    removeChild(child);
                }

                if (mOnHierarchyChangeListener != null) {
                    mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
                }
            }
        });
    }


    @Override
    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        mOnHierarchyChangeListener = listener;
    }

    private void removeChild(View view) {
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        final int status = params.mStatus;

        switch (status) {
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
            default:
                if (mStatusViews != null) {
                    mStatusViews.remove(status);
                }
                break;
        }
    }

    private void addChild(View view) {
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        final int status = params.mStatus;

        switch (status) {
            case LayoutStatus.NORMAL:
                if (mNormalView != null) {
                    throwAlreadyEx(status);
                }
                mNormalView = view;
                adjustStatus(view, LayoutStatus.NORMAL);
                break;
            case LayoutStatus.EMPTY:
                if (mEmptyView != null) {
                    throwAlreadyEx(status);
                }
                mEmptyView = view;
                adjustStatus(view, LayoutStatus.EMPTY);
                break;
            case LayoutStatus.ERROR:
                if (mErrorView != null) {
                    throwAlreadyEx(status);
                }
                mErrorView = view;
                adjustStatus(view, LayoutStatus.ERROR);
                break;
            case LayoutStatus.LOADING:
                if (mLoadingView != null) {
                    throwAlreadyEx(status);
                }
                mLoadingView = view;
                adjustStatus(view, LayoutStatus.LOADING);
                break;
            default:
                if (mStatusViews != null && mStatusViews.get(status) != null) {
                    throwAlreadyEx(status);
                }
                if (mStatusViews == null) {
                    mStatusViews = new SparseArray<>();
                }
                mStatusViews.put(status, view);
                adjustStatus(view, status);
                break;

        }
    }

    private void throwAlreadyEx(int status) {
        throw new RuntimeException("status(" + status + ") view already exists");
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
            animator.setDuration(300);
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
            animator.setDuration(300);
            animator.setProperty(View.ALPHA);
            animator.setFloatValues(1f, 0f);

            return animator;
        } else {
            return AnimatorInflater.loadAnimator(getContext(), mHideAnimRes);
        }
    }


    public int getStatus() {
        return mStatus;
    }

    public int getWhenStatus() {
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
        switchStatusLayout(LayoutStatus.ERROR);

        if (mErrorText == null && mErrorView instanceof ErrorView) {
            mErrorText = ((ErrorView) mErrorView).getText();
        }

        if (errorText != null && mErrorView instanceof ErrorView) {
            ((ErrorView) mErrorView).setText(errorText);
        }
    }


    public void empty() {
        switchStatusLayout(LayoutStatus.EMPTY);
    }

    public void loading() {
        switchStatusLayout(LayoutStatus.LOADING);
    }

    public void normal() {
        switchStatusLayout(LayoutStatus.NORMAL);
    }

    public void detach() {
        ViewGroup viewGroup = (ViewGroup) this.getParent();

        View normal = mNormalView;
        if (normal != null && viewGroup != null) {
            int index = viewGroup.indexOfChild(this);
            this.removeView(normal);
            viewGroup.addView(normal, index);
        }
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

    public interface OnStatusChangedListener {
        void onStatusChanged(int status);
    }

    public void switchStatusLayout(final int status) {
        if (((mStartingAnimStatus == status && mIsStartingAnim) || (!mIsStartingAnim && mStatus == status))) {
            return;
        }

        final View view = getViewByStatus(status);

        if (!mIsAnimable) {
            setStatus(status);
            if (mOnStatusChangedListener != null) {
                mOnStatusChangedListener.onStatusChanged(status);
            }
            return;
        }

        if (mHideAnim != null) {
            mHideAnim.cancel();
            mIsStartingAnim = false;
            mHideAnim = null;
        }

        if (mShowAnim != null) {
            mShowAnim.cancel();
            mIsStartingAnim = false;
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

                        if (mOnStatusChangedListener != null) {
                            mOnStatusChangedListener
                                    .onStatusChanged(status);
                        }
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

    public void setRetryOnClickListener(final OnClickListener l) {
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLoadingView != null) {
                    loading();
                }
                l.onClick(view);
            }
        };
        if (mErrorView instanceof ErrorView) {
            ((ErrorView) mErrorView).setRetryOnClickListener(listener);
        } else {
            mErrorView.setOnClickListener(listener);
        }
    }

    public View getViewByStatus(int status) {
        switch (status) {
            case LayoutStatus.NORMAL:
                return requireNonNull(mNormalView);
            case LayoutStatus.EMPTY:
                return requireNonNull(mEmptyView);
            case LayoutStatus.ERROR:
                return requireNonNull(mErrorView);
            case LayoutStatus.LOADING:
                return requireNonNull(mLoadingView);
            default:
                View view = mStatusViews.get(status);
                if (view != null) {
                    return view;
                }
        }
        throw new ArithmeticException("status error");
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


    @IntDef({LayoutStatus.NORMAL, LayoutStatus.ERROR, LayoutStatus.EMPTY, LayoutStatus.LOADING, LayoutStatus.NONE})
    public @interface LayoutStatus {
        int NONE = -1;
        int NORMAL = -2;
        int ERROR = -3;
        int EMPTY = -4;
        int LOADING = -5;
    }


    public static class LayoutParams extends FrameLayout.LayoutParams {
        private int mStatus = LayoutStatus.NONE;

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

    private static <T> T requireNonNull(T obj) {
        if (obj == null)
            throw new NullPointerException();
        return obj;
    }

}
