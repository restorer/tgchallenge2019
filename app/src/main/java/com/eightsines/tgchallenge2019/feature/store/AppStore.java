package com.eightsines.tgchallenge2019.feature.store;

import androidx.annotation.NonNull;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartData;
import java.util.ArrayList;
import java.util.List;

public class AppStore {
    private List<ChartData<Long, Integer>> charts = new ArrayList<>();

    @NonNull
    public List<ChartData<Long, Integer>> getCharts() {
        return charts;
    }

    public void setCharts(@NonNull List<ChartData<Long, Integer>> charts) {
        this.charts = charts;
    }
}
