package com.eightsines.tgchallenge2019.feature.chart.util;

import com.eightsines.tgchallenge2019.feature.chart.data.ChartData;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartXValues;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartYValues;
import com.eightsines.tgchallenge2019.feature.chart.exception.ChartException;
import com.eightsines.tgchallenge2019.feature.util.AppMathUtils;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ChartDecimator {
    private ChartDecimator() {
    }

    public static <X extends Number & Comparable<X>, Y extends Number & Comparable<Y>> ChartData<X, Y> decimate(
            ChartData<X, Y> chartData, float ratio) {

        if (chartData.isEmpty() && chartData.getXValues().getLength() < 3) {
            return chartData;
        }

        DouglasPeuckerData<X, Y> dpData = new DouglasPeuckerData<>(chartData);
        return performDouglasPeucker(dpData, computeEpsilons(dpData, ratio)).toChartData(chartData);
    }

    private static <X extends Number & Comparable<X>, Y extends Number & Comparable<Y>> DouglasPeuckerData<X, Y> performDouglasPeucker(
            DouglasPeuckerData<X, Y> dpData,
            float[] epsilons) {

        int maxIndex = dpData.xValues.length - 1;
        float sx = dpData.xValues[0].floatValue();
        float ex = dpData.xValues[maxIndex].floatValue();

        int selectedIndex = -1;
        float maxDistanceSum = 0.0f;

        for (int index = 1; index < maxIndex; index++) {
            boolean isFound = true;
            float distanceSum = 0.0f;

            for (int position = 0, count = epsilons.length; position < count; position++) {
                Y[] yValues = dpData.yValuesList.get(position);

                float distance = calculateDistance(sx,
                        yValues[0].floatValue(),
                        ex,
                        yValues[maxIndex].floatValue(),
                        dpData.xValues[index].floatValue(),
                        yValues[index].floatValue());

                if (distance <= epsilons[position]) {
                    isFound = false;
                    break;
                }

                distanceSum += distance;
            }

            if (isFound && distanceSum > maxDistanceSum) {
                maxDistanceSum = distanceSum;
                selectedIndex = index;
            }
        }

        if (selectedIndex < 0) {
            return dpData.copyExtremePoints();
        }

        DouglasPeuckerData<X, Y> dpLeftData = performDouglasPeucker(dpData.copyRange(0, selectedIndex), epsilons);

        DouglasPeuckerData<X, Y> dpRightData = performDouglasPeucker(
                dpData.copyRange(selectedIndex, maxIndex),
                epsilons);

        return new DouglasPeuckerData<>(dpLeftData, dpRightData);
    }

    private static <X extends Number & Comparable<X>, Y extends Number & Comparable<Y>> float[] computeEpsilons(
            DouglasPeuckerData<X, Y> dpData, float ratio) {

        int maxIndex = dpData.xValues.length - 1;
        float sx = dpData.xValues[0].floatValue();
        float ex = dpData.xValues[maxIndex].floatValue();
        float[] epsilons = new float[dpData.yValuesList.size()];

        for (int position = 0, count = epsilons.length; position < count; position++) {
            Y[] yValues = dpData.yValuesList.get(position);
            float sy = yValues[0].floatValue();
            float ey = yValues[maxIndex].floatValue();

            for (int index = 1; index < maxIndex; index++) {
                float distance = calculateDistance(sx,
                        sy,
                        ex,
                        ey,
                        dpData.xValues[index].floatValue(),
                        yValues[index].floatValue());

                if (distance > epsilons[position]) {
                    epsilons[position] = distance;
                }
            }

            epsilons[position] *= ratio;
        }

        return epsilons;
    }

    private static float calculateDistance(float sx, float sy, float ex, float ey, float x, float y) {
        float dx = sx - ex;
        float dy = sy - ey;
        float length = (float)Math.sqrt(dx * dx + dy * dy);

        return (length < AppMathUtils.EPSILON_F)
                ? AppMathUtils.INFINITY_F
                : Math.abs(dy * x - dx * y + sx * ey - ex * sy) / length;
    }

    private static class DouglasPeuckerData<X extends Number & Comparable<X>, Y extends Number & Comparable<Y>> {
        X[] xValues;
        List<Y[]> yValuesList;

        DouglasPeuckerData(X[] xValues, List<Y[]> yValuesList) {
            this.xValues = xValues;
            this.yValuesList = yValuesList;
        }

        @SuppressWarnings("unchecked")
        DouglasPeuckerData(DouglasPeuckerData<X, Y> dpLeftData, DouglasPeuckerData<X, Y> dpRightData) {
            int totalLength = dpLeftData.xValues.length + dpRightData.xValues.length - 1;
            xValues = (X[])Array.newInstance(dpLeftData.xValues.getClass().getComponentType(), totalLength);

            for (int i = 0, len = dpLeftData.xValues.length; i < len; i++) {
                xValues[i] = dpLeftData.xValues[i];
            }

            for (int i = 1, len = dpRightData.xValues.length; i < len; i++) {
                xValues[dpLeftData.xValues.length + i - 1] = dpRightData.xValues[i];
            }

            yValuesList = new ArrayList<>();

            for (int position = 0, count = dpLeftData.yValuesList.size(); position < count; position++) {
                Y[] yValuesLeft = dpLeftData.yValuesList.get(position);
                Y[] yValuesRight = dpRightData.yValuesList.get(position);
                Y[] yValues = (Y[])Array.newInstance(yValuesLeft.getClass().getComponentType(), totalLength);

                for (int i = 0, len = yValuesLeft.length; i < len; i++) {
                    yValues[i] = yValuesLeft[i];
                }

                for (int i = 1, len = yValuesRight.length; i < len; i++) {
                    yValues[yValuesLeft.length + i - 1] = yValuesRight[i];
                }

                yValuesList.add(yValues);
            }
        }

        DouglasPeuckerData(ChartData<X, Y> chartData) {
            xValues = Arrays.copyOf(chartData.getXValues().getRawValues(), chartData.getXValues().getLength());
            yValuesList = new ArrayList<>();

            for (ChartYValues<Y> yValues : chartData.getYValuesList()) {
                yValuesList.add(Arrays.copyOf(yValues.getRawValues(), yValues.getLength()));
            }
        }

        @SuppressWarnings("unchecked")
        DouglasPeuckerData<X, Y> copyExtremePoints() {
            X[] xValuesCopy = (X[])Array.newInstance(xValues.getClass().getComponentType(), 2);
            xValuesCopy[0] = xValues[0];
            xValuesCopy[1] = xValues[xValues.length - 1];

            List<Y[]> yValuesListCopy = new ArrayList<>();

            for (Y[] yValues : yValuesList) {
                Y[] yValuesCopy = (Y[])Array.newInstance(yValues.getClass().getComponentType(), 2);
                yValuesCopy[0] = yValues[0];
                yValuesCopy[1] = yValues[yValues.length - 1];

                yValuesListCopy.add(yValuesCopy);
            }

            return new DouglasPeuckerData<>(xValuesCopy, yValuesListCopy);
        }

        DouglasPeuckerData<X, Y> copyRange(int fromIndex, int toIndex) {
            List<Y[]> yValuesListCopy = new ArrayList<>();

            for (Y[] yValues : yValuesList) {
                yValuesListCopy.add(Arrays.copyOfRange(yValues, fromIndex, toIndex + 1));
            }

            return new DouglasPeuckerData<>(Arrays.copyOfRange(xValues, fromIndex, toIndex + 1), yValuesListCopy);
        }

        ChartData<X, Y> toChartData(ChartData<X, Y> originalChartData) {
            List<ChartYValues<Y>> yValuesListCopy = new ArrayList<>();

            for (int position = 0, count = yValuesList.size(); position < count; position++) {
                ChartYValues<Y> yValuesOriginal = originalChartData.getYValuesList().get(position);

                yValuesListCopy.add(new ChartYValues<>(yValuesList.get(position),
                        originalChartData.getYTypeDescriptor().getEmptyValue(),
                        yValuesOriginal.getName(),
                        yValuesOriginal.getColor()));
            }

            try {
                return new ChartData<>(originalChartData.getXTypeDescriptor(),
                        originalChartData.getYTypeDescriptor(),
                        new ChartXValues<>(xValues, originalChartData.getXTypeDescriptor().getEmptyValue()),
                        yValuesListCopy);
            } catch (ChartException e) {
                // Should not happen
                throw new RuntimeException(e);
            }
        }
    }
}
