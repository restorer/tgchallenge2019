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

    private ChartRange<T> controlledRange;
    private TypeEvaluator<T> evaluator;
    private ValueAnimator animator;
    private Runnable onUpdatedListener;
    private ChartRange<T> lastRange;

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
            controlledRange.setRange((T)animation.getAnimatedValue(PROP_FROM),
                    (T)animation.getAnimatedValue(PROP_TO));

            if (onUpdatedListener != null) {
                onUpdatedListener.run();
            }
        }
    };

    public ChartRangeController(@NonNull ChartRange<T> controlledRange, @NonNull TypeEvaluator<T> evaluator) {
        this.controlledRange = controlledRange;
        this.evaluator = evaluator;
    }

    public void setRange(@NonNull ChartRange<T> range, long animationDuration) {
        if (lastRange == null) {
            lastRange = new ChartRange<>(range);
        } else if (lastRange.equals(range)) {
            return;
        } else {
            lastRange.setRange(range.getFrom(), range.getTo());
        }

        if (animator != null) {
            animator.cancel();
        }

        if (animationDuration <= 0L) {
            controlledRange.setRange(range.getFrom(), range.getTo());

            if (onUpdatedListener != null) {
                onUpdatedListener.run();
            }

            return;
        }

        animator = ValueAnimator.ofPropertyValuesHolder(
                PropertyValuesHolder.ofObject(PROP_FROM, evaluator, controlledRange.getFrom(), range.getFrom()),
                PropertyValuesHolder.ofObject(PROP_TO, evaluator, controlledRange.getTo(), range.getTo()));

        animator.setDuration(animationDuration);
        animator.addListener(animatorListener);
        animator.addUpdateListener(animatorUpdateListener);
        animator.start();
    }

    public void setOnUpdatedListener(@Nullable Runnable onUpdatedListener) {
        this.onUpdatedListener = onUpdatedListener;
    }
}
