package com.tangzy.tzymvp.view.toast;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.tangzy.tzymvp.R;
import com.tangzy.tzymvp.util.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by RickBerg on 2018/5/9 0009.
 *从界面顶部边缘短暂出现的轻量级bar 的基类
 *
 * @param <B> 轻量级顶部bar的子类
 */

public class BaseTransienTopBarCus<B extends BaseTransienTopBarCus<B>>{
    final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();

    /**
     * BaseTransienTopBar 基类的回调
     *
     * @param <B> 轻量级顶部bar的子类
     * @see BaseTransienTopBarCus#addCallback(BaseCallback)
     */
    public abstract static class BaseCallback<B>{
        public static final int DISMISS_EVENT_SWIPE = 0;
        public static final int DISMISS_EVENT_ACTION = 1;
        public static final int DISMISS_EVENT_TIMEOUT =2;
        public static final int DISMISS_EVENT_MANUAL = 3;
        public static final int DISMISS_EVENT_CONSECUTIVE = 4;

        @IntDef({DISMISS_EVENT_SWIPE, DISMISS_EVENT_ACTION, DISMISS_EVENT_TIMEOUT,
                DISMISS_EVENT_MANUAL, DISMISS_EVENT_CONSECUTIVE})
        @Retention(RetentionPolicy.SOURCE)
        public @interface DismissEvent{}

        public void onDismissed(B transientTopBar, @DismissEvent int event){}
        public void onShown(B transientTopBar){}
    }

    public interface ContentViewCallback{
        void animateContentIn(int delay, int duration);
        void animateContentOut(int delay, int duration);
    }

    @IntDef({LENGTH_INDEFINITE, LENGTH_SHORT, LENGTH_LONG})
//    @IntRange(from = 1)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {}
    public static final int LENGTH_INDEFINITE = -2;
    public static final int LENGTH_SHORT = -1;
    public static final int LENGTH_LONG = 0;

    static final int ANIMATION_DURATION = 250;
    static final int ANIMATION_FADE_DURATION = 180;

    static final Handler sHandler;
    static final int MSG_SHOW = 0;
    static final int MSG_DISMISS = 1;
//    private static final boolean USE_OFFSET_API = (Build.VERSION.SDK_INT >= 16) && (Build.VERSION.SDK_INT <= 19);

    static {
        sHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_SHOW:
                        ((BaseTransienTopBarCus) message.obj).showView();
                        return true;
                    case MSG_DISMISS:
                        ((BaseTransienTopBarCus) message.obj).hideView(message.arg1);
                        return true;
                }
                return false;
            }
        });
    }

    private final ViewGroup mTargetParent;
    private final Context mContext;
    final SnackbarBaseLayout mView;
    private final ContentViewCallback mContentViewCallback;
    private int mDuration;

    private List<BaseCallback<B>> mCallbacks;

    private final AccessibilityManager mAccessibilityManager;

    interface OnLayoutChangeListener {
        void onLayoutChange(View view, int left, int top, int right, int bottom);
    }

    interface OnAttachStateChangeListener {
        void onViewAttachedToWindow(View v);
        void onViewDetachedFromWindow(View v);
    }

    protected BaseTransienTopBarCus(@NonNull ViewGroup parent, @NonNull View content,
                                    @NonNull ContentViewCallback contentViewCallback) {
        if (parent == null) {
            throw new IllegalArgumentException("Transient Top bar must have non-null parent");
        }
        if (content == null) {
            throw new IllegalArgumentException("Transient Top bar must have non-null content");
        }
        if (contentViewCallback == null) {
            throw new IllegalArgumentException("Transient Top bar must have non-null callback");
        }

        mTargetParent = parent;
        mContentViewCallback = contentViewCallback;
        mContext = parent.getContext();

//        ThemeUtils.checkAppCompatTheme(mContext);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        // Note that for backwards compatibility reasons we inflate a layout that is defined
        // in the extending Snackbar class. This is to prevent breakage of apps that have custom
        // coordinator layout behaviors that depend on that layout.
        mView = (SnackbarBaseLayout) inflater.inflate(
                R.layout.design_layout_snackbar_, mTargetParent, false);
        mView.addView(content);

        ViewCompat.setAccessibilityLiveRegion(mView,
                ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
        ViewCompat.setImportantForAccessibility(mView,
                ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);

        // Make sure that we fit system windows and have a listener to apply any insets
        ViewCompat.setFitsSystemWindows(mView, true);
        ViewCompat.setOnApplyWindowInsetsListener(mView,
                new OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        // Copy over the bottom inset as padding so that we're displayed
                        // above the navigation bar
                        v.setPadding(v.getPaddingLeft(), v.getPaddingTop(),
                                v.getPaddingRight(), insets.getSystemWindowInsetBottom());
                        return insets;
                    }
                });

        mAccessibilityManager = (AccessibilityManager)
                mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    @NonNull
    public B setDuration(@Duration int duration){
        mDuration = duration;
        return (B) this;
    }

    @Duration
    public int getDuration(){
        return mDuration;
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

    @NonNull
    public View getView() {
        return mView;
    }

    public void show() {
        SnackbarManagerCus.getInstance().show(mDuration, mManagerCallback);
    }

    public void dismiss() {
        dispatchDismiss(BaseCallback.DISMISS_EVENT_MANUAL);
    }

    void dispatchDismiss(@BaseCallback.DismissEvent int event) {
        Logger.d("hhhhhhh", "dispatchDismiss");
        SnackbarManagerCus.getInstance().dismiss(mManagerCallback, event);
    }

    private void removeView() {
        Logger.d("hhhhhhh", "removeView");
        try {
            WindowManager windowManager = (WindowManager)mTargetParent.getContext().getSystemService(Context.WINDOW_SERVICE);
            mTargetParent.removeAllViews();
            windowManager.removeView(mTargetParent);
        }catch (Exception e){

        }
    }

    public B addCallback(@NonNull BaseCallback<B> callback) {
        if (callback == null) {
            return (B) this;
        }
        if (mCallbacks == null) {
            mCallbacks = new ArrayList<BaseCallback<B>>();
        }
        mCallbacks.add(callback);
        return (B) this;
    }

    @NonNull
    public B removeCallback(@NonNull BaseCallback<B> callback) {
        if (callback == null) {
            return (B) this;
        }
        if (mCallbacks == null) {
            // This can happen if this method is called before the first call to addCallback
            return (B) this;
        }
        mCallbacks.remove(callback);
        return (B) this;
    }

    public boolean isShown() {
        return SnackbarManagerCus.getInstance().isCurrent(mManagerCallback);
    }

    public boolean isShownOrQueued() {
        return SnackbarManagerCus.getInstance().isCurrentOrNext(mManagerCallback);
    }

    final SnackbarManagerCus.Callback mManagerCallback = new SnackbarManagerCus.Callback() {
        @Override
        public void show() {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_SHOW, BaseTransienTopBarCus.this));
        }

        @Override
        public void dismiss(int event) {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_DISMISS, event, 0,
                    BaseTransienTopBarCus.this));
        }
    };

    final void showView() {
        Logger.d("hhhhhhh", "showView");
        if (mView.getParent() == null) {
            final ViewGroup.LayoutParams lp = mView.getLayoutParams();

//            if (lp instanceof CoordinatorLayout.LayoutParams) {
//                // If our LayoutParams are from a CoordinatorLayout, we'll setup our Behavior
//                final CoordinatorLayout.LayoutParams clp = (CoordinatorLayout.LayoutParams) lp;
//
//                final Behavior behavior = new Behavior();
//                behavior.setStartAlphaSwipeDistance(0.1f);
//                behavior.setEndAlphaSwipeDistance(0.6f);
//                behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);
//                behavior.setListener(new SwipeDismissBehavior.OnDismissListener() {
//                    @Override
//                    public void onDismiss(View view) {
//                        view.setVisibility(View.GONE);
//                        dispatchDismiss(BaseCallback.DISMISS_EVENT_SWIPE);
//                    }
//
//                    @Override
//                    public void onDragStateChanged(int state) {
//                        switch (state) {
//                            case SwipeDismissBehavior.STATE_DRAGGING:
//                            case SwipeDismissBehavior.STATE_SETTLING:
//                                // If the view is being dragged or settling, pause the timeout
//                                SnackbarManager.getInstance().pauseTimeout(mManagerCallback);
//                                break;
//                            case SwipeDismissBehavior.STATE_IDLE:
//                                // If the view has been released and is idle, restore the timeout
//                                SnackbarManager.getInstance()
//                                        .restoreTimeoutIfPaused(mManagerCallback);
//                                break;
//                        }
//                    }
//                });
//                clp.setBehavior(behavior);
//                // Also set the inset edge so that views can dodge the bar correctly
//                clp.insetEdge = Gravity.BOTTOM;
//            }

            mTargetParent.addView(mView);
        }

        mView.setOnAttachStateChangeListener(
                new BaseTransienTopBarCus.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {}

                    @Override
                    public void onViewDetachedFromWindow(View v) {
                        if (isShownOrQueued()) {
                            // If we haven't already been dismissed then this event is coming from a
                            // non-user initiated action. Hence we need to make sure that we callback
                            // and keep our state up to date. We need to post the call since
                            // removeView() will call through to onDetachedFromWindow and thus overflow.
                            sHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onViewHidden(BaseCallback.DISMISS_EVENT_MANUAL);
                                }
                            });
                        }
                    }
                });

        if (ViewCompat.isLaidOut(mView)) {
            if (shouldAnimate()) {
                // If animations are enabled, animate it in
                animateViewIn();
            } else {
                // Else if anims are disabled just call back now
                onViewShown();
            }
        } else {
            // Otherwise, add one of our layout change listeners and show it in when laid out
            mView.setOnLayoutChangeListener(new BaseTransienTopBarCus.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom) {
                    mView.setOnLayoutChangeListener(null);

                    if (shouldAnimate()) {
                        // If animations are enabled, animate it in
                        animateViewIn();
                    } else {
                        // Else if anims are disabled just call back now
                        onViewShown();
                    }
                }
            });
        }
    }
   private int getTitleBarHeight(){
        /**
         * 获取状态栏高度——方法1
         * */
        int statusBarHeight1 = -1;
//获取status_bar_height资源的ID
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        Logger.d("hhhhhhh", "状态栏-方法1:" + statusBarHeight1);
        return statusBarHeight1;
    }

    void animateViewIn() {
//        int viewHeight = mView.getHeight()/4 - getTitleBarHeight();
        final int viewHeight = -10;
        Logger.d("hhhhhhh", "mView.getHeight() = "+mView.getHeight());
        Logger.d("hhhhhhh", "viewHeight = "+viewHeight);
//        if (USE_OFFSET_API) {
//            ViewCompat.offsetTopAndBottom(mView, viewHeight);
//        } else {
            mView.setTranslationY(viewHeight);
//        }
        final ValueAnimator animator = new ValueAnimator();
        animator.setIntValues(new int[]{0, viewHeight});
//        animator.setIntValues(0, mContext.getResources().getDimensionPixelOffset(R.dimen.dp_75));
        animator.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
        animator.setDuration(ANIMATION_DURATION);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                Logger.d("hhhhhhh", "onAnimationStart");
                mContentViewCallback.animateContentIn(
                        ANIMATION_DURATION - ANIMATION_FADE_DURATION,
                        ANIMATION_FADE_DURATION);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Logger.d("hhhhhhh", "onAnimationEnd");
                final int viewHeight1 = mView.getHeight()/4 - getTitleBarHeight();
                Logger.d("hhhhhhh", "viewHeight1 = "+viewHeight1);
                onViewShown();
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private int mPreviousAnimatedIntValue = viewHeight;

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int currentAnimatedIntValue = (int) animator.getAnimatedValue();
//                if (USE_OFFSET_API) {
//                    ViewCompat.offsetTopAndBottom(mView,
//                            currentAnimatedIntValue - mPreviousAnimatedIntValue);
//                } else {
                    mView.setTranslationY(currentAnimatedIntValue);
//                }
                mPreviousAnimatedIntValue = currentAnimatedIntValue;
            }
        });
        animator.start();
    }

    private void animateViewOut(final int event) {
//        final  int viewHeight = mView.getHeight()/4 - getTitleBarHeight();
        final int viewHeight = -10;
        final ValueAnimator animator = new ValueAnimator();
        animator.setIntValues(viewHeight, -mView.getHeight());
        animator.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
        animator.setDuration(ANIMATION_DURATION);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                Logger.d("hhhhhhh", "animateViewOut -> onAnimationStart");
                mContentViewCallback.animateContentOut(0, ANIMATION_FADE_DURATION);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Logger.d("hhhhhhh", "animateViewOut -> onAnimationEnd");
                onViewHidden(event);
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private int mPreviousAnimatedIntValue = 0;

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int currentAnimatedIntValue = (int) animator.getAnimatedValue();
//                if (USE_OFFSET_API) {
//                    ViewCompat.offsetTopAndBottom(mView,
//                            currentAnimatedIntValue - mPreviousAnimatedIntValue);
//                } else {
                    mView.setTranslationY(currentAnimatedIntValue);
//                }
                mPreviousAnimatedIntValue = currentAnimatedIntValue;
            }
        });
        animator.start();
    }

    final void hideView(@BaseCallback.DismissEvent final int event) {
        Logger.d("hhhhhhh", "hideView");
        if (shouldAnimate() && mView.getVisibility() == View.VISIBLE) {
            Logger.d("hhhhhhh", "animateViewOut");
            animateViewOut(event);
        } else {
            Logger.d("hhhhhhh", "onViewHidden");
            // If anims are disabled or the view isn't visible, just call back now
            onViewHidden(event);
        }
//        sHandler.sendMessage(sHandler.obtainMessage(MSG_REMOVE, BaseTransienTopBarCus.this));
    }

    void onViewShown() {
        SnackbarManagerCus.getInstance().onShown(mManagerCallback);
        if (mCallbacks != null) {
            // Notify the callbacks. Do that from the end of the list so that if a callback
            // removes itself as the result of being called, it won't mess up with our iteration
            int callbackCount = mCallbacks.size();
            for (int i = callbackCount - 1; i >= 0; i--) {
                mCallbacks.get(i).onShown((B) this);
            }
        }
    }

    void onViewHidden(int event) {
        // First tell the SnackbarManager that it has been dismissed
        SnackbarManagerCus.getInstance().onDismissed(mManagerCallback);
        if (mCallbacks != null) {
            // Notify the callbacks. Do that from the end of the list so that if a callback
            // removes itself as the result of being called, it won't mess up with our iteration
            int callbackCount = mCallbacks.size();
            for (int i = callbackCount - 1; i >= 0; i--) {
                mCallbacks.get(i).onDismissed((B) this, event);
            }
        }
        if (Build.VERSION.SDK_INT < 11) {
            // We need to hide the Snackbar on pre-v11 since it uses an old style Animation.
            // ViewGroup has special handling in removeView() when getAnimation() != null in
            // that it waits. This then means that the calculated insets are wrong and the
            // any dodging views do not return. We workaround it by setting the view to gone while
            // ViewGroup actually gets around to removing it.
            mView.setVisibility(View.GONE);
        }
        // Lastly, hide and remove the view from the parent (if attached)
        final ViewParent parent = mView.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(mView);
//            removeView();
        }
    }

    /**
     * Returns true if we should animate the Snackbar view in/out.
     */
    boolean shouldAnimate() {
        return true;
//        return !mAccessibilityManager.isEnabled();
    }

    static class SnackbarBaseLayout extends FrameLayout {
        private BaseTransienTopBarCus.OnLayoutChangeListener mOnLayoutChangeListener;
        private BaseTransienTopBarCus.OnAttachStateChangeListener mOnAttachStateChangeListener;

        SnackbarBaseLayout(Context context) {
            this(context, null);
        }

        SnackbarBaseLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SnackbarLayout);
            if (a.hasValue(R.styleable.SnackbarLayout_elevation)) {
                ViewCompat.setElevation(this, a.getDimensionPixelSize(
                        R.styleable.SnackbarLayout_elevation, 0));
            }
            a.recycle();

            setClickable(true);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (mOnLayoutChangeListener != null) {
                mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
            }
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewAttachedToWindow(this);
            }

            ViewCompat.requestApplyInsets(this);
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewDetachedFromWindow(this);
            }
        }

        void setOnLayoutChangeListener(
                BaseTransienTopBarCus.OnLayoutChangeListener onLayoutChangeListener) {
            mOnLayoutChangeListener = onLayoutChangeListener;
        }

        void setOnAttachStateChangeListener(
                BaseTransienTopBarCus.OnAttachStateChangeListener listener) {
            mOnAttachStateChangeListener = listener;
        }
    }

//    final class Behavior extends SwipeDismissBehavior<SnackbarBaseLayout> {
//        @Override
//        public boolean canSwipeDismissView(View child) {
//            return child instanceof SnackbarBaseLayout;
//        }
//
//        @Override
//        public boolean onInterceptTouchEvent(CoordinatorLayout parent, SnackbarBaseLayout child,
//                                             MotionEvent event) {
//            switch (event.getActionMasked()) {
//                case MotionEvent.ACTION_DOWN:
//                    // We want to make sure that we disable any Snackbar timeouts if the user is
//                    // currently touching the Snackbar. We restore the timeout when complete
//                    if (parent.isPointInChildBounds(child, (int) event.getX(),
//                            (int) event.getY())) {
//                        SnackbarManager.getInstance().pauseTimeout(mManagerCallback);
//                    }
//                    break;
//                case MotionEvent.ACTION_UP:
//                case MotionEvent.ACTION_CANCEL:
//                    SnackbarManager.getInstance().restoreTimeoutIfPaused(mManagerCallback);
//                    break;
//            }
//            return super.onInterceptTouchEvent(parent, child, event);
//        }
//    }
}
