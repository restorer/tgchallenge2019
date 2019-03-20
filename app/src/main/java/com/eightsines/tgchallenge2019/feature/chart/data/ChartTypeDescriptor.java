package com.eightsines.tgchallenge2019.feature.chart.data;

import android.animation.TypeEvaluator;
import androidx.annotation.NonNull;

public class ChartTypeDescriptor<T extends Number & Comparable<T>> {
    private T emptyValue;
    private TypeEvaluator<T> typeEvaluator;

    public ChartTypeDescriptor(@NonNull T emptyValue, @NonNull TypeEvaluator<T> typeEvaluator) {
        this.emptyValue = emptyValue;
        this.typeEvaluator = typeEvaluator;
    }

    public T getEmptyValue() {
        return emptyValue;
    }

    public TypeEvaluator<T> getTypeEvaluator() {
        return typeEvaluator;
    }
}
