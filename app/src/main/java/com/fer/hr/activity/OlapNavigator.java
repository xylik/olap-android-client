package com.fer.hr.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fer.hr.R;
import com.fer.hr.activity.adapters.CubesListAdapter;
import com.fer.hr.activity.adapters.MeasuresAdapter;
import com.fer.hr.activity.adapters.DimensionsExpandableListAdapter;
import com.fer.hr.model.Dimension;
import com.fer.hr.model.Level;
import com.fer.hr.model.QueryBuilder;
import com.fer.hr.rest.dto.discover.SaikuCube;
import com.fer.hr.rest.dto.discover.SaikuDimension;
import com.fer.hr.rest.dto.discover.SaikuLevel;
import com.fer.hr.rest.dto.discover.SaikuMeasure;
import com.fer.hr.rest.dto.discover.SaikuMember;
import com.fer.hr.services.ServiceProvider;
import com.fer.hr.services.repository.IRepository;
import com.fer.hr.utils.CubeMetaConverterUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class OlapNavigator extends AppCompatActivity {
    private static final int DEFAULT_CUBE_INDX = 0;
    @Bind(R.id.cubesTxt)
    TextView cubesTxt;
    @Bind(R.id.measuresTxt)
    TextView measuresTxt;
    @Bind(R.id.dimensionsTxt)
    TextView dimensionsTxt;

    private IRepository repository;
    private QueryBuilder queryBuilder;

    private List<SaikuCube> cubes = new ArrayList<>();
    private List<SaikuMeasure> cubeMeasures = new ArrayList<>();
    private HashMap<Integer, Dimension> cubeDimensions = new HashMap<>();

    private CubesListAdapter cubesAdapter;
    private MeasuresAdapter measuresAdapter;
    private DimensionsExpandableListAdapter dimensionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_olap_navigator);
        ButterKnife.bind(this);
        repository = (IRepository) ServiceProvider.getService(ServiceProvider.REPOSITORY);
        queryBuilder = new QueryBuilder();

        cubes.addAll(repository.getCubesFromAllConnections());
        if (cubes.size() > 0) {
            SaikuCube defaultCube = cubes.get(DEFAULT_CUBE_INDX);
            cubeMeasures = repository.getMeasuresForCube(defaultCube);
            List<SaikuDimension> dimensions = repository.getDimensionsForCube(defaultCube);
            cubeDimensions = CubeMetaConverterUtil.convertToNestedListFormat(dimensions);
        }

        cubesAdapter = new CubesListAdapter(this, cubes);
        measuresAdapter = new MeasuresAdapter(this, cubeMeasures);
        dimensionsAdapter = new DimensionsExpandableListAdapter(this, cubeDimensions);

        setActions();
    }

    private void setActions() {
        cubesTxt.setOnClickListener(tv -> {
            Dialog d = createDialog(getString(R.string.cubes), R.layout.dialog_cubes);
            ListView lv = (ListView) d.findViewById(R.id.lv);
            lv.setAdapter(cubesAdapter);
            lv.setOnItemClickListener((av, rv, p, i) -> {
                cubesAdapter.setSelectedItemIndx(p);
                cubesAdapter.notifyDataSetChanged();
            });
            d.show();
        });

        measuresTxt.setOnClickListener(tv -> {
            Dialog d = createDialog(getString(R.string.measures), R.layout.dialog_measures);
            ListView lv = (ListView) d.findViewById(R.id.mesuresLst);
            lv.setAdapter(measuresAdapter);
            lv.setOnItemClickListener((av, rv, p, i) -> {
                CheckBox chkbox = (CheckBox) rv.findViewById(R.id.measureChkBox);
                chkbox.setChecked(!chkbox.isChecked());
            });
            d.show();
        });

        dimensionsAdapter.setOnChildClickListener(dimensionClickListener);
        dimensionsTxt.setOnClickListener(tv -> {
            Dialog d = createDialog(getString(R.string.dimensions), R.layout.dialog_dimensions);
            ExpandableListView lv = (ExpandableListView) d.findViewById(R.id.dimensionsLst);
            lv.setAdapter(dimensionsAdapter);
            d.show();
        });
    }

    private Dialog createDialog(String title, int layoutResourceId) {
        Dialog d = new Dialog(this);
        d.setTitle(title);
        d.setContentView(layoutResourceId);
        return d;
    }

    private final DimensionsExpandableListAdapter.OnChildItemClickListener dimensionClickListener = (view, groupPosition, childPosition, newState) -> {
        ImageView v = (ImageView) view;
        RelativeLayout parentView = (RelativeLayout) (v.getParent());
        ImageView c = (ImageView) parentView.findViewById(R.id.collsImg);
        ImageView r = (ImageView) parentView.findViewById(R.id.rowsImg);
        ImageView f = (ImageView) parentView.findViewById(R.id.filterImg);
        Level l = (Level) dimensionsAdapter.getChild(groupPosition, childPosition);
        Level.State currentState = l.getState();

        boolean isModified = false;
        switch (newState) {
            case NEUTRAL: {
                if (currentState == Level.State.COLLUMNS) queryBuilder.removeFromColumns(l);
                else if (currentState == Level.State.ROWS) queryBuilder.removeFromRows(l);
                else if(currentState == Level.State.FILTER) {
                    SaikuLevel sl = (SaikuLevel)l.getData();
                    SaikuMember filter = new SaikuMember(sl.getDimensionUniqueName(), sl.getHierarchyUniqueName(), sl.getUniqueName(), sl.getCaption());
                    queryBuilder.removeFromFilters(filter);
                }
                isModified = true;
                break;
            }
            case COLLUMNS:
                isModified = queryBuilder.putOnColumns(l);
                break;
            case ROWS:
                isModified = queryBuilder.putOnRows(l);
                break;
            case FILTER: {
                SaikuLevel sl = (SaikuLevel)l.getData();
                SaikuMember filter = new SaikuMember(sl.getDimensionUniqueName(), sl.getHierarchyUniqueName(), sl.getUniqueName(), sl.getCaption());
                isModified = queryBuilder.putOnFilters(filter);
            }
        }
        if (isModified) l.setState(newState);
        else return;

        switch (currentState) {
            case NEUTRAL:
                v.setImageResource(R.drawable.delete_icon);
                break;
            case COLLUMNS:
                c.setImageResource(R.drawable.column_icon);
                if (c != v) v.setImageResource(R.drawable.delete_icon);
                break;
            case ROWS:
                r.setImageResource(R.drawable.row_icon);
                if (r != v) v.setImageResource(R.drawable.delete_icon);
                break;
            case FILTER:
                f.setImageResource(R.drawable.filter_icon);
                if (f != v) v.setImageResource(R.drawable.delete_icon);
                break;
        }
    };
}
