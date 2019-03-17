package com.eightsines.tgchallenge2019.feature.chart;

public class ChartException extends Exception {
    public ChartException(String message) {
        super(message);
    }

    public ChartException(Throwable cause) {
        super(cause);
    }

    public ChartException(String message, Throwable cause) {
        super(message, cause);
    }
}
