package com.eightsines.tgchallenge2019.feature.chart.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressWarnings("AbstractClassWithoutAbstractMethods")
public abstract class ChartValues<T extends Number & Comparable<T>> {
    protected T[] values;
    protected T emptyValue;

    @SuppressWarnings("unchecked")
    public ChartValues(@Nullable T[] values, @NonNull T emptyValue) {
        this.values = (values == null ? (T[])(new Object[0]) : values);
        this.emptyValue = emptyValue;
    }

    public boolean isEmpty() {
        return values.length == 0;
    }

    public int getLength() {
        return values.length;
    }

    public T getEmptyValue() {
        return emptyValue;
    }

    @NonNull
    public T getValueAtIndex(int index) {
        return (index < 0 || index >= values.length) ? emptyValue : values[index];
    }
}
