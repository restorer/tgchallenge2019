package com.eightsines.tgchallenge2019.feature.chart.controller;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;

public class ChartRangeController<T extends Number & Comparable<T>> {
    private static final String PROP_FROM = "PROP_FROM";
    private static final String PROP_TO = "PROP_TO";

    private ChartRange<T> range;
    private TypeEvaluator<T> evaluator;
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
        @SuppressWarnings("unchecked")
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            range.setRange((T)animation.getAnimatedValue(PROP_FROM),
                    (T)animation.getAnimatedValue(PROP_TO));

            if (onUpdatedListener != null) {
                onUpdatedListener.run();
            }
        }
    };

    public ChartRangeController(@NonNull ChartRange<T> range, @NonNull TypeEvaluator<T> evaluator) {
        this.range = range;
        this.evaluator = evaluator;
    }

    public void setRange(@NonNull ChartRange<T> range, long animationDuration) {
        if (this.range.equals(range)) {
            return;
        }

        if (animator != null) {
            animator.cancel();
        }

        animator = ValueAnimator.ofPropertyValuesHolder(
                PropertyValuesHolder.ofObject(PROP_FROM, evaluator, this.range.getFrom(), range.getFrom()),
                PropertyValuesHolder.ofObject(PROP_TO, evaluator, this.range.getTo(), range.getTo()));

        animator.setDuration(animationDuration);
        animator.addListener(animatorListener);
        animator.addUpdateListener(animatorUpdateListener);
        animator.start();
    }

    public void setOnUpdatedListener(@Nullable Runnable onUpdatedListener) {
        this.onUpdatedListener = onUpdatedListener;
    }
}
