package com.eightsines.tgchallenge2019.screen.statistics;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import com.eightsines.tgchallenge2019.App;
import com.eightsines.tgchallenge2019.AppConfig;
import com.eightsines.tgchallenge2019.R;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartData;
import com.eightsines.tgchallenge2019.feature.chart.exception.ChartException;
import com.eightsines.tgchallenge2019.feature.preferences.AppPreferences;
import com.eightsines.tgchallenge2019.feature.tgchart.TgChartControlView;
import com.eightsines.tgchallenge2019.feature.tgchart.TgChartReader;
import com.eightsines.tgchallenge2019.feature.util.AppResUtils;
import java.io.IOException;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {
    private AppPreferences preferences;
    private TgChartControlView chartControlView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.statistics__activity);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

        preferences = new AppPreferences(this);
        updateTheme();

        chartControlView = findViewById(R.id.chart);
        List<ChartData<Long, Integer>> charts = getCharts();

        if (!charts.isEmpty()) {
            chartControlView.setChartData("Followers", charts.get(0));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.statistics__menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.toggle_night_mode:
                preferences.setNightMode(!preferences.isNightMode());
                updateTheme();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateTheme() {
        getDelegate().setLocalNightMode(preferences.isNightMode()
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private List<ChartData<Long, Integer>> getCharts() {
        List<ChartData<Long, Integer>> charts = App.getInstance().getStore().getCharts();

        if (!charts.isEmpty()) {
            return charts;
        }

        try {
            charts = TgChartReader.readListFromJson(AppResUtils.readToString(this, R.raw.chart_data));
        } catch (ChartException | IOException e) {
            Log.e(AppConfig.TAG, "Unable to read chart: " + e.toString(), e);
            Toast.makeText(this, R.string.statistics__read_chart_failed, Toast.LENGTH_LONG).show();
        }

        App.getInstance().getStore().setCharts(charts);
        return charts;
    }
}
