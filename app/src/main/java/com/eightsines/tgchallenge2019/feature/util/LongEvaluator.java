package com.eightsines.tgchallenge2019.feature.util;

import android.animation.TypeEvaluator;

public class LongEvaluator implements TypeEvaluator<Long> {
    @Override
    public Long evaluate(float fraction, Long startValue, Long endValue) {
        long startLong = startValue;
        return (long)(startLong + fraction * (endValue - startLong));
    }
}
