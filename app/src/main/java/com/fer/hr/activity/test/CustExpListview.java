package com.fer.hr.activity.test;

import android.content.Context;
import android.widget.ExpandableListView;

import com.fer.hr.utils.PixelUtil;

public class CustExpListview extends ExpandableListView {

    public CustExpListview(Context context) {
        super(context);
        setPadding(10,0,0,0);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(960, MeasureSpec.AT_MOST);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(600, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}