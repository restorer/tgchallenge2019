package com.eightsines.tgchallenge2019.feature.store;

import androidx.annotation.NonNull;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartData;
import java.util.ArrayList;
import java.util.List;

public class AppStore {
    private List<ChartData> charts = new ArrayList<>();

    @NonNull
    public List<ChartData> getCharts() {
        return charts;
    }

    public void setCharts(@NonNull List<ChartData> charts) {
        this.charts = charts;
    }
}
