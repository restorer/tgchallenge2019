package com.eightsines.tgchallenge2019.feature.chart.data;

import androidx.annotation.NonNull;
import java.util.Objects;

public class ChartRange<T extends Number & Comparable<T>> {
    private T from;
    private T to;

    public ChartRange(@NonNull T from, @NonNull T to) {
        this.from = from;
        this.to = to;
    }

    public boolean isEmpty() {
        return to.compareTo(from) <= 0;
    }

    public T getFrom() {
        return from;
    }

    public void setFrom(T from) {
        this.from = from;
    }

    public T getTo() {
        return to;
    }

    public void setTo(T to) {
        this.to = to;
    }

    public void mergeWith(ChartRange<T> other) {
        if (from.compareTo(other.from) > 0) {
            from = other.from;
        }

        if (to.compareTo(other.to) < 0) {
            to = other.to;
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
}
