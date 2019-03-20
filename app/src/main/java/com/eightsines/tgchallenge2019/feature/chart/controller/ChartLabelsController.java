package com.eightsines.tgchallenge2019.feature.chart.controller;

import android.animation.Animator;
import android.animation.ValueAnimator;
import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChartLabelsController<T extends Number & Comparable<T>> {
    public class Label {
        private T value;
        private String title;
        private boolean active;
        private float alpha;
        private ValueAnimator animator;

        private Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
            private boolean wasNotCancelled = true;

            @Override
            public void onAnimationStart(Animator animation) {
                wasNotCancelled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                wasNotCancelled = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animator = null;

                if (wasNotCancelled && !active) {
                    labelsMap.remove(title);
                }
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

        private Label(T value, String title, boolean active) {
            this.value = value;
            this.title = title;
            this.active = active;

            alpha = (active ? 1.0f : 0.0f);
        }

        public T getValue() {
            return value;
        }

        public String getTitle() {
            return title;
        }

        @SuppressWarnings("MagicNumber")
        public int getIntAlpha() {
            return (int)(alpha * 255.0f + 0.5f);
        }

        private void setActive(boolean active, long animationDuration) {
            if (this.active == active) {
                return;
            }

            this.active = active;

            if (animator != null) {
                animator.cancel();
            }

            animator = ValueAnimator.ofFloat(alpha, active ? 1.0f : 0.0f).setDuration(animationDuration);
            animator.addListener(animatorListener);
            animator.addUpdateListener(animatorUpdateListener);
            animator.start();
        }
    }

    private Function<T, String> labelsFormatter;
    private Function<ChartRange<T>, List<T>> labelsValuesComputer;
    private Map<String, Label> labelsMap = new HashMap<>();
    private ChartRange<T> lastRange;
    private Runnable onUpdatedListener;

    public ChartLabelsController(@NonNull Function<T, String> labelsFormatter,
            @NonNull Function<ChartRange<T>, List<T>> labelsValuesComputer,
            ChartRange<T> range) {

        this.labelsFormatter = labelsFormatter;
        this.labelsValuesComputer = labelsValuesComputer;

        for (T value : labelsValuesComputer.apply(range)) {
            Label label = new Label(value, labelsFormatter.apply(value), true);
            labelsMap.put(label.title, label);
        }
    }

    public Collection<Label> getLabels() {
        return labelsMap.values();
    }

    public void updateRange(@NonNull ChartRange<T> range, long animationDuration) {
        if (lastRange == null) {
            lastRange = new ChartRange<>(range);
        } else if (lastRange.equals(range)) {
            return;
        } else {
            lastRange.setRange(range.getFrom(), range.getTo());
        }

        Set<String> activeNameSet = new HashSet<>();

        for (T value : labelsValuesComputer.apply(range)) {
            String name = labelsFormatter.apply(value);
            activeNameSet.add(name);

            Label label = labelsMap.get(name);

            if (label == null) {
                label = new Label(value, name, false);
                labelsMap.put(name, label);
            }

            label.value = value;
            label.setActive(true, animationDuration);
        }

        for (Label label : labelsMap.values()) {
            if (!activeNameSet.contains(label.title)) {
                label.setActive(false, animationDuration);
            }
        }
    }

    public void setOnUpdatedListener(Runnable onUpdatedListener) {
        this.onUpdatedListener = onUpdatedListener;
    }
}
