package com.fer.hr.activity.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ExpandableListView;

import com.fer.hr.R;

public class TestActivity extends AppCompatActivity {
    ExpandableListView explvlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        explvlist = (ExpandableListView) findViewById(R.id.dimensionsExpLst);
        explvlist.setAdapter(new ParentLevel(this));
    }
}
