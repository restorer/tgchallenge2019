package com.eightsines.tgchallenge2019.feature.chart.data;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public class ChartYData {
    private String name;
    private int color;
    int[] values;
    private boolean enabled = true;

    public ChartYData(@NonNull String name, @ColorInt int color, @NonNull int[] values) {
        this.name = name;
        this.color = color;
        this.values = values;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @ColorInt
    public int getColor() {
        return color;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ChartYData setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public int getMaxIndex() {
        return values.length - 1;
    }

    public int getValueAtIndex(int index) {
        return (index < 0 || index >= values.length) ? 0 : values[index];
    }

    @NonNull
    public ChartRange<Integer> computeRange(int fromIndex, int toIndex, double expandStep) {
        if (fromIndex < 0 || toIndex < 0) {
            return new ChartRange<>(0, 0);
        }

        int value = getValueAtIndex(fromIndex);
        int min = value;
        int max = value;

        for (int index = fromIndex + 1; index <= toIndex; index++) {
            value = getValueAtIndex(index);
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        return new ChartRange<>(
                (int)(Math.floor((double)min / expandStep) * expandStep),
                (int)(Math.floor(((double)max + expandStep - 1.0) / expandStep) * expandStep));
    }
}
