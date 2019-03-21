package com.eightsines.tgchallenge2019.feature.chart.util;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import com.eightsines.tgchallenge2019.feature.util.AppTimeUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChartDateLabelsFormatter implements Function<Long, String> {
    private static final DateFormat DATE_FORMAT_MD = new SimpleDateFormat("MMM d", Locale.US);
    private static final DateFormat DATE_FORMAT_YM = new SimpleDateFormat("yyyy MMM", Locale.US);
    private static final DateFormat DATE_FORMAT_Y = new SimpleDateFormat("yyyy", Locale.US);

    private ChartDateLabelsValuesComputer valuesComputer;
    private Date dateCache = new Date();

    public ChartDateLabelsFormatter(ChartDateLabelsValuesComputer valuesComputer) {
        this.valuesComputer = valuesComputer;
    }

    @NonNull
    @Override
    public String apply(@NonNull Long value) {
        dateCache.setTime(value);
        long step = valuesComputer.getStep();

        if (step < AppTimeUtils.ABOUT_MONTH_MS) {
            return DATE_FORMAT_MD.format(dateCache);
        } else if (step < AppTimeUtils.ABOUT_YEAR_MS) {
            return DATE_FORMAT_YM.format(dateCache);
        } else {
            return DATE_FORMAT_Y.format(dateCache);
        }
    }
}
