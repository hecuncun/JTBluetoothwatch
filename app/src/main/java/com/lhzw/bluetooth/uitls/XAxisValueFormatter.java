package com.lhzw.bluetooth.uitls;

import com.github.mikephil.charting.formatter.ValueFormatter;

/**
 * Created by heCunCun on 2019/11/21
 */
public class XAxisValueFormatter extends ValueFormatter {
    private final String[] mLabels;

    public XAxisValueFormatter(String[] labels) {
        mLabels = labels;
    }

    @Override
    public String getFormattedValue(float value) {
        return mLabels[(int) value];
    }
}
