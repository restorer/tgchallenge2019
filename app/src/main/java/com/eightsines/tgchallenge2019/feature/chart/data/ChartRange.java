package com.eightsines.tgchallenge2019.feature.chart.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Objects;

public class ChartRange<T extends Number & Comparable<T>> {
    private T from;
    private T to;
    private Runnable onUpdatedListener;

    public ChartRange(@NonNull T from, @NonNull T to) {
        this.from = from;
        this.to = to;
    }

    public ChartRange(@NonNull ChartRange<T> range) {
        this.from = range.from;
        this.to = range.to;
    }

    @NonNull
    public T getFrom() {
        return from;
    }

    public void setFrom(@NonNull T from) {
        this.from = from;

        if (onUpdatedListener != null) {
            onUpdatedListener.run();
        }
    }

    @NonNull
    public T getTo() {
        return to;
    }

    public void setTo(@NonNull T to) {
        this.to = to;

        if (onUpdatedListener != null) {
            onUpdatedListener.run();
        }
    }

    public void setRange(@NonNull T from, @NonNull T to) {
        this.from = from;
        this.to = to;

        if (onUpdatedListener != null) {
            onUpdatedListener.run();
        }
    }

    public void setOnUpdatedListener(@Nullable Runnable onUpdatedListener) {
        this.onUpdatedListener = onUpdatedListener;
    }

    public void mergeWith(@NonNull ChartRange<T> other) {
        if (from.compareTo(other.from) > 0) {
            from = other.from;
        }

        if (to.compareTo(other.to) < 0) {
            to = other.to;
        }

        if (onUpdatedListener != null) {
            onUpdatedListener.run();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ChartRange)) {
            return false;
        }

        ChartRange<?> lhs = (ChartRange<?>)o;
        return Objects.equals(from, lhs.from) && Objects.equals(to, lhs.to);
    }

    @NonNull
    @Override
    public String toString() {
        return "ChartRange(from=" + String.valueOf(from) + ", to=" + String.valueOf(to) + ")";
    }
}
