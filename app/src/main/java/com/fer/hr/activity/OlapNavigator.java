package com.fer.hr.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.fer.hr.R;
import com.fer.hr.activity.adapters.CubesListAdapter;
import com.fer.hr.activity.adapters.MyExpandableListAdapter;
import com.fer.hr.model.CubeMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class OlapNavigator extends AppCompatActivity {
    @Bind(R.id.btnBack)
    ImageButton btnBack;
    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.actionBtn)
    ImageButton actionBtn;
    @Bind(R.id.cubesTxt)
    TextView cubesTxt;
    @Bind(R.id.measuresTxt)
    TextView measuresTxt;
    @Bind(R.id.dimensionsTxt)
    TextView dimensionsTxt;

    private CubesListAdapter cubesAdapter;
    private ArrayAdapter<String> measuresAdapter;
    private SparseArray<Group> groups;
    private ExpandableListAdapter dimensionsAdapter;
    private String user = "admin";
    private List<CubeMeta> cubesMeta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_olap_navigator);
        ButterKnife.bind(this);



        cubesAdapter = new CubesListAdapter<String>(
                this, R.layout.list_row_text_radiobtn,
                Arrays.asList("Sales cube", "Very long cube name", "Super super long cube name")
        );

        measuresAdapter = new ArrayAdapter<String>(
                this, R.layout.list_row_text_checkbox, R.id.textLbl,
                Arrays.asList("Sales measure", "Min measure", "Max measure", "Avg measure", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a")
        );


        dimensionsAdapter = new MyExpandableListAdapter(this, groups = createGroupsData());

        initView();
        setActions();
    }

    private void initView() {
    }

    private void setActions() {
        cubesTxt.setOnClickListener(tv -> {
            Dialog d = new Dialog(this);
            d.setTitle(getString(R.string.cubes));
            d.setContentView(R.layout.dialog_cubes);
            ListView lv = (ListView) d.findViewById(R.id.lv);
            lv.setAdapter(cubesAdapter);
            lv.setOnItemClickListener((av, rv, p, i) -> {
                cubesAdapter.setSelectedItemIndx(p);
                cubesAdapter.notifyDataSetChanged();
            });
            d.show();
        });

        measuresTxt.setOnClickListener(tv -> {
            Dialog d = new Dialog(this);
            d.setTitle(getString(R.string.measures));
            d.setContentView(R.layout.dialog_measures);
            ListView lv = (ListView) d.findViewById(R.id.mesuresLst);
            lv.setAdapter(measuresAdapter);
            lv.setOnItemClickListener((av, rv, p, i) -> {
                CheckBox chkbox = (CheckBox) rv.findViewById(R.id.measureChkBox);
                chkbox.setChecked(!chkbox.isChecked());
            });
            d.show();
        });

        dimensionsTxt.setOnClickListener(tv -> {
            Dialog d = new Dialog(this);
            d.setTitle(getString(R.string.dimensions));
            d.setContentView(R.layout.dialog_dimensions);
            ExpandableListView lv = (ExpandableListView) d.findViewById(R.id.dimensionsLst);
            lv.setAdapter(dimensionsAdapter);
            lv.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
                System.out.println("igor groupPosition:" + groupPosition + " childPosition:" + childPosition);
                return true;
            });
            d.show();
        });
    }

    public SparseArray<Group> createGroupsData() {
        SparseArray<Group> groups = new SparseArray<>();
        for (int j = 0; j < 5; j++) {
            Group group = new Group("Test " + j);
            for (int i = 0; i < 5; i++) {
                group.children.add("Sub Item" + i);
            }
            groups.append(j, group);
        }
        return groups;
    }

    public static class Group {
        public String string;
        public final List<String> children = new ArrayList<String>();

        public Group(String string) {
            this.string = string;
        }
    }
}
