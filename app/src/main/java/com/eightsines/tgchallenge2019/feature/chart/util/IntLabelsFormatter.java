package com.eightsines.tgchallenge2019.feature.chart.util;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;

public class IntLabelsFormatter implements Function<Integer, String> {
    @NonNull
    @Override
    public String apply(@NonNull Integer value) {
        return String.valueOf(value);
    }
}
