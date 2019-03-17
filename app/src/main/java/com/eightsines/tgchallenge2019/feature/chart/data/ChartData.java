package com.eightsines.tgchallenge2019.feature.chart.data;

import androidx.annotation.NonNull;
import com.eightsines.tgchallenge2019.feature.chart.ChartException;
import java.util.List;

public class ChartData {
    private ChartXData xData;
    private List<ChartYData> yDataList;

    public ChartData(@NonNull ChartXData xData, @NonNull List<ChartYData> yDataList) throws ChartException {
        this.xData = xData;
        this.yDataList = yDataList;

        initialize();
    }

    @NonNull
    public ChartXData getXData() {
        return xData;
    }

    public int getYDataCount() {
        return yDataList.size();
    }

    @NonNull
    public ChartYData getYData(int position) {
        return yDataList.get(position);
    }

    @NonNull
    public ChartRange<Integer> computeYRange(int fromIndex, int toIndex, int expandStep) {
        ChartRange<Integer> result = null;

        for (ChartYData yData : yDataList) {
            if (!yData.isEnabled()) {
                continue;
            }

            ChartRange<Integer> range = yData.computeRange(fromIndex, toIndex, expandStep);

            if (result == null) {
                result = range;
            } else {
                result = result.mergeWith(range);
            }
        }

        return result == null ? new ChartRange<>(0, 0) : result;
    }

    private void initialize() throws ChartException {
        int maxIndex = xData.getMaxIndex();

        for (ChartYData yData : yDataList) {
            if (yData.getMaxIndex() != maxIndex) {
                throw new ChartException("Values count in all columns must be equal");
            }
        }

        if (!xData.isEmpty()) {
            sortData(0, xData.getMaxIndex());
        }
    }

    private void sortData(int fromIndex, int toIndex) {
        if (fromIndex >= toIndex) {
            return;
        }

        long pivot = xData.values[toIndex];
        int partitionIndex = fromIndex - 1;

        for (int index = fromIndex; index < toIndex; index++) {
            if (xData.values[index] > pivot) {
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
        long tmpX = xData.values[fromIndex];
        xData.values[fromIndex] = xData.values[toIndex];
        xData.values[toIndex] = tmpX;

        for (ChartYData yData : yDataList) {
            int tmpY = yData.values[fromIndex];
            yData.values[fromIndex] = yData.values[toIndex];
            yData.values[toIndex] = tmpY;
        }
    }
}
