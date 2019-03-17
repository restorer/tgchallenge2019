package com.eightsines.tgchallenge2019.feature.chart;

import android.graphics.Color;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartData;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartXData;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartYData;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class ChartReader {
    private static final String KEY_COLUMNS = "columns";
    private static final String KEY_TYPES = "types";
    private static final String KEY_NAMES = "names";
    private static final String KEY_COLORS = "colors";

    private static final String TYPE_X = "x";
    private static final String TYPE_LINE = "line";

    private ChartReader() {
    }

    public static List<ChartData> readListFromJson(String jsonString) throws ChartException {
        try {
            return readListFromJson(new JSONArray(jsonString));
        } catch (JSONException e) {
            throw new ChartException("Unable to parse json: " + e.toString(), e);
        }
    }

    public static List<ChartData> readListFromJson(JSONArray jsonArray) throws ChartException {
        List<ChartData> result = new ArrayList<>();

        for (int index = 0, length = jsonArray.length(); index < length; index++) {
            try {
                result.add(readDataFromJson(jsonArray.getJSONObject(index)));
            } catch (JSONException e) {
                throw new ChartException("Unable to read from list: " + e.toString(), e);
            }
        }

        return result;
    }

    public static ChartData readDataFromJson(JSONObject chartObject) throws ChartException {
        try {
            JSONArray columnsArray = chartObject.getJSONArray(KEY_COLUMNS);
            JSONObject typesObject = chartObject.getJSONObject(KEY_TYPES);
            JSONObject namesObject = chartObject.getJSONObject(KEY_NAMES);
            JSONObject colorsObject = chartObject.getJSONObject(KEY_COLORS);

            long[] xValues = null;
            List<ChartYData> yDataList = new ArrayList<>();

            for (int columnIndex = 0, columnsLength = columnsArray.length();
                    columnIndex < columnsLength;
                    columnIndex++) {

                JSONArray valuesArray = columnsArray.getJSONArray(columnIndex);
                int valuesLength = valuesArray.length();

                String columnKey = valuesArray.getString(0);
                String columnType = typesObject.getString(columnKey);

                switch (columnType) {
                    case TYPE_X: {
                        if (xValues != null) {
                            throw new ChartException("More than one x-column is not supported");
                        }

                        xValues = new long[valuesLength - 1];

                        for (int valueIndex = 1; valueIndex < valuesLength; valueIndex++) {
                            xValues[valueIndex - 1] = valuesArray.getLong(valueIndex);
                        }

                        break;
                    }

                    case TYPE_LINE: {
                        int[] yValues = new int[valuesLength - 1];

                        for (int valueIndex = 1; valueIndex < valuesLength; valueIndex++) {
                            yValues[valueIndex - 1] = valuesArray.getInt(valueIndex);
                        }

                        yDataList.add(new ChartYData(namesObject.getString(columnKey),
                                parseColor(colorsObject.getString(columnKey)),
                                yValues));

                        break;
                    }

                    default:
                        throw new ChartException("Unknown column type \"" + columnType + "\"");
                }
            }

            return new ChartData(new ChartXData(xValues), yDataList);
        } catch (JSONException e) {
            throw new ChartException("Unable to parse chart data: " + e.toString(), e);
        }
    }

    private static int parseColor(String colorString) throws ChartException {
        try {
            return Color.parseColor(colorString);
        } catch (IllegalArgumentException e) {
            throw new ChartException("Unable to parse color string \"" + colorString + "\": " + e.toString(), e);
        }
    }
}
