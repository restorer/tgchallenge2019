package com.eightsines.tgchallenge2019.feature.chart.controller;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartYValues;
import com.eightsines.tgchallenge2019.feature.chart.exception.ChartOutOfBoundsException;
import com.eightsines.tgchallenge2019.feature.util.AppMathUtils;

public class ChartYValuesController<T extends Number & Comparable<T>> {
    private ChartYValues<T> yValues;
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

    public ChartYValuesController(ChartYValues<T> yValues) {
        this.yValues = yValues;

        colorR = Color.red(yValues.getColor());
        colorG = Color.green(yValues.getColor());
        colorB = Color.blue(yValues.getColor());
    }

    public ChartYValues<T> getYValues() {
        return yValues;
    }

    @NonNull
    public String getName() {
        return yValues.getName();
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

    public ChartRange<T> computeRange(int fromIndex, int toIndex) throws ChartOutOfBoundsException {
        return yValues.computeRange(fromIndex, toIndex);
    }
}
