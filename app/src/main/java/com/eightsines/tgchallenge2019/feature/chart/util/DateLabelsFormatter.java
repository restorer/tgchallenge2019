package com.eightsines.tgchallenge2019.feature.chart.util;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateLabelsFormatter implements Function<Long, String> {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM d", Locale.US);

    private Date dateCache = new Date();

    @NonNull
    @Override
    public String apply(@NonNull Long value) {
        dateCache.setTime(value);
        return DATE_FORMAT.format(dateCache);
    }
}
