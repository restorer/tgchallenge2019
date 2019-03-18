package com.eightsines.tgchallenge2019.feature.chart.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.eightsines.tgchallenge2019.feature.chart.exception.ChartOutOfBoundsException;

public class ChartValues<T extends Number & Comparable<T>> {
    protected T[] values;

    public ChartValues(@Nullable T[] values) {
        //noinspection unchecked
        this.values = (values == null ? (T[])(new Object[0]) : values);
    }

    public boolean isEmpty() {
        return values.length == 0;
    }

    public int getLength() {
        return values.length;
    }

    public int getMaxIndex() {
        return values.length - 1;
    }

    @NonNull
    public T getValueAtIndex(int index) throws ChartOutOfBoundsException {
        if (index < 0 || index >= values.length) {
            throw new ChartOutOfBoundsException("Index " + index + " is out of bounds (length = " + values.length + ")");
        }

        return values[index];
    }
}
