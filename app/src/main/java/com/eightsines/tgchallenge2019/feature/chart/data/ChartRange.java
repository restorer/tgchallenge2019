package com.eightsines.tgchallenge2019.feature.chart.data;

import androidx.annotation.NonNull;

public class ChartRange<T extends Number & Comparable<T>> {
    private T from;
    private T to;

    public ChartRange(@NonNull T from, T to) {
        this.from = from;
        this.to = to;
    }

    public T getFrom() {
        return from;
    }

    public T getTo() {
        return to;
    }

    @NonNull
    public ChartRange<T> mergeWith(ChartRange<T> other) {
        return new ChartRange<>(
                from.compareTo(other.from) < 0 ? from : other.from,
                to.compareTo(other.to) > 0 ? to : other.to);
    }
}
