package com.eightsines.tgchallenge2019.feature.chart.data;

import androidx.annotation.NonNull;
import com.eightsines.tgchallenge2019.feature.chart.exception.ChartException;
import com.eightsines.tgchallenge2019.feature.chart.exception.ChartOutOfBoundsException;
import java.util.List;

public class ChartData<X extends Number & Comparable<X>, Y extends Number & Comparable<Y>> {
    private ChartXValues<X> xValues;
    private List<ChartYValues<Y>> yValuesList;

    public ChartData(@NonNull ChartXValues<X> xValues, @NonNull List<ChartYValues<Y>> yValuesList) throws
            ChartException {

        this.xValues = xValues;
        this.yValuesList = yValuesList;

        initialize();
    }

    @NonNull
    public ChartXValues<X> getXValues() {
        return xValues;
    }

    @NonNull
    public List<ChartYValues<Y>> getYValuesList() {
        return yValuesList;
    }

    public boolean isEmpty() {
        return xValues.isEmpty() || yValuesList.isEmpty();
    }

    @NonNull
    public ChartRange<X> getXRange() throws ChartOutOfBoundsException {
        return xValues.getRange();
    }

    private void initialize() throws ChartException {
        int xLength = xValues.getLength();

        for (ChartYValues<Y> yValues : yValuesList) {
            if (yValues.getLength() != xLength) {
                throw new ChartException("Values length in all columns must be equal");
            }
        }

        if (!xValues.isEmpty()) {
            sortData(0, xValues.getMaxIndex());
        }
    }

    private void sortData(int fromIndex, int toIndex) {
        if (fromIndex >= toIndex) {
            return;
        }

        X pivot = xValues.values[toIndex];
        int partitionIndex = fromIndex - 1;

        for (int index = fromIndex; index < toIndex; index++) {
            if (xValues.values[index].compareTo(pivot) > 0) {
                continue;
            }

            partitionIndex++;
            swapData(partitionIndex, index);
        }

        swapData(partitionIndex + 1, toIndex);

        sortData(fromIndex, partitionIndex);
        sortData(partitionIndex + 2, toIndex);
    }

    private void swapData(int fromIndex, int toIndex) {
        X tmpX = xValues.values[fromIndex];
        xValues.values[fromIndex] = xValues.values[toIndex];
        xValues.values[toIndex] = tmpX;

        for (ChartYValues<Y> yValues : yValuesList) {
            Y tmpY = yValues.values[fromIndex];
            yValues.values[fromIndex] = yValues.values[toIndex];
            yValues.values[toIndex] = tmpY;
        }
    }
}
