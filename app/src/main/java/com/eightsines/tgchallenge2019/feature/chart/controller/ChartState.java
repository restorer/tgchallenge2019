package com.eightsines.tgchallenge2019.feature.chart.controller;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.customview.view.AbsSavedState;

public class ChartState extends AbsSavedState {
    private Number xVisibleRangeFrom;
    private Number xVisibleRangeTo;
    private boolean[] yValuesEnabledList;

    public ChartState(@Nullable Parcelable superState,
            @NonNull Number xVisibleRangeFrom,
            @NonNull Number xVisibleRangeTo,
            @NonNull boolean[] yValuesEnabledList) {

        super(superState == null ? EMPTY_STATE : superState);
        this.xVisibleRangeFrom = xVisibleRangeFrom;
        this.xVisibleRangeTo = xVisibleRangeTo;
        this.yValuesEnabledList = yValuesEnabledList;
    }

    private ChartState(@NonNull Parcel source, @Nullable ClassLoader loader) {
        super(source, loader);

        xVisibleRangeFrom = (Number)source.readSerializable();
        xVisibleRangeTo = (Number)source.readSerializable();
        yValuesEnabledList = source.createBooleanArray();
    }

    public Number getXVisibleRangeFrom() {
        return xVisibleRangeFrom;
    }

    public Number getXVisibleRangeTo() {
        return xVisibleRangeTo;
    }

    public boolean[] getYValuesEnabledList() {
        return yValuesEnabledList;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeSerializable(xVisibleRangeFrom);
        dest.writeSerializable(xVisibleRangeTo);
        dest.writeBooleanArray(yValuesEnabledList);
    }

    public static final Creator<ChartState> CREATOR = new ClassLoaderCreator<ChartState>() {
        @Override
        public ChartState createFromParcel(Parcel source, ClassLoader loader) {
            return new ChartState(source, loader);
        }

        @Override
        public ChartState createFromParcel(Parcel source) {
            return new ChartState(source, null);
        }

        @Override
        public ChartState[] newArray(int size) {
            return new ChartState[size];
        }
    };
}
