package com.eightsines.tgchallenge2019.feature.chart.controller;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import com.eightsines.tgchallenge2019.feature.util.AppMathUtils;

public class ChartYValuesController {
    private int colorR;
    private int colorG;
    private int colorB;
    private float alpha = 1.0f;
    private boolean enabled = true;
    private ValueAnimator animator;
    private Runnable onUpdatedListener;

    private Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            animator = null;
        }
    };

    private ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            alpha = (float)animation.getAnimatedValue();

            if (onUpdatedListener != null) {
                onUpdatedListener.run();
            }
        }
    };

    public ChartYValuesController(int color) {
        colorR = Color.red(color);
        colorG = Color.green(color);
        colorB = Color.blue(color);
    }

    @SuppressWarnings("MagicNumber")
    @ColorInt
    public int getColor() {
        return Color.argb((int)(alpha * 255.0f + 0.5f), colorR, colorG, colorB);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled, long animationDuration) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;

        if (animator != null) {
            animator.cancel();
        }

        if (animationDuration <= 0L) {
            alpha = enabled ? 1.0f : 0.0f;

            if (onUpdatedListener != null) {
                onUpdatedListener.run();
            }

            return;
        }

        animator = ValueAnimator.ofFloat(alpha, enabled ? 1.0f : 0.0f).setDuration(animationDuration);
        animator.addListener(animatorListener);
        animator.addUpdateListener(animatorUpdateListener);
        animator.start();
    }

    public boolean isVisible() {
        return alpha >= AppMathUtils.EPSILON_F;
    }

    public void setOnUpdatedListener(@Nullable Runnable onUpdatedListener) {
        this.onUpdatedListener = onUpdatedListener;
    }
}
