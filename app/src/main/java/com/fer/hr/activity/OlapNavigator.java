package com.fer.hr.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.fer.hr.R;
import com.fer.hr.activity.adapters.CubesAdapter;
import com.fer.hr.activity.adapters.DimensionsAdapter;
import com.fer.hr.activity.adapters.MeasuresAdapter;
import com.fer.hr.activity.adapters.SelectionAdapter;
import com.fer.hr.model.Dimension;
import com.fer.hr.model.Level;
import com.fer.hr.model.QueryBuilder;
import com.fer.hr.model.SelectionEntity;
import com.fer.hr.model.SelectionGroup;
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
    @Bind(R.id.cubeImgBtn)
    ImageButton cubeImgBtn;
    @Bind(R.id.measureImgBtn)
    ImageButton measureImgBtn;
    @Bind(R.id.dimensionImgBtn)
    ImageButton dimensionImgBtn;
    @Bind(R.id.playImgBtn)
    ImageButton playImgBtn;
    @Bind(R.id.selectionLst)
    ExpandableListView selectionLst;
    @Bind(R.id.mdxImgBtn)
    ImageButton mdxImgBtn;

    private IRepository repository;
    private QueryBuilder queryBuilder;

    private List<SaikuCube> cubes = new ArrayList<>();
    private List<SaikuMeasure> cubeMeasures = new ArrayList<>();
    private HashMap<Integer, Dimension> cubeDimensions = new HashMap<>();
    private HashMap<Integer, SelectionGroup> selectedEntites = CubeMetaConverterUtil.getEmptySelectionGroup();

    private CubesAdapter cubesAdapter;
    private MeasuresAdapter measuresAdapter;
    private DimensionsAdapter dimensionsAdapter;
    private SelectionAdapter selectionAdapter;

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
            queryBuilder.setCube(defaultCube);
            cubeMeasures = repository.getMeasuresForCube(defaultCube);
            List<SaikuDimension> dimensions = repository.getDimensionsForCube(defaultCube);
            cubeDimensions = CubeMetaConverterUtil.convertToNestedListFormat(dimensions);
        }

        cubesAdapter = new CubesAdapter(this, cubes);
        measuresAdapter = new MeasuresAdapter(this, cubeMeasures);
        dimensionsAdapter = new DimensionsAdapter(this, cubeDimensions);
        selectionAdapter = new SelectionAdapter(this, selectedEntites, selectionListener);
        selectionLst.setAdapter(selectionAdapter);

        setActions();
    }

    private void setActions() {
        cubeImgBtn.setOnClickListener(tv -> {
            Dialog d = createDialog(getString(R.string.cubes), R.layout.dialog_cubes);
            ListView lv = (ListView) d.findViewById(R.id.lv);
            lv.setAdapter(cubesAdapter);
            lv.setOnItemClickListener((av, rv, p, i) -> {
                cubesAdapter.setSelectedItemIndx(p);
                cubesAdapter.notifyDataSetChanged();
                queryBuilder.setCube(cubes.get(p));
            });
            d.show();
        });

        measureImgBtn.setOnClickListener(tv -> {
            Dialog d = createDialog(getString(R.string.measures), R.layout.dialog_measures);
            ListView lv = (ListView) d.findViewById(R.id.mesuresLst);
            lv.setAdapter(measuresAdapter);
            lv.setOnItemClickListener((av, rv, p, i) -> {
                measuresAdapter.setCheckedItem(p);

                CheckBox chkbox = (CheckBox) rv.findViewById(R.id.measureChkBox);
                boolean isSelected = !chkbox.isChecked();
                chkbox.setChecked(isSelected);

                SaikuMeasure selectedMeasure = cubeMeasures.get(p);
                if (isSelected) queryBuilder.putOnMeasures(selectedMeasure);
                else queryBuilder.removeFromMeasures(selectedMeasure);
            });
            d.show();
        });

        dimensionsAdapter.setOnChildClickListener(dimensionClickListener);
        dimensionImgBtn.setOnClickListener(tv -> {
            Dialog d = createDialog(getString(R.string.dimensions), R.layout.dialog_dimensions);
            ExpandableListView lv = (ExpandableListView) d.findViewById(R.id.dimensionsLst);
            lv.setAdapter(dimensionsAdapter);
            d.show();
        });

        mdxImgBtn.setOnClickListener(iv -> {
            Dialog d = new Dialog(this);
            d.setTitle("MDX");
            d.setContentView(R.layout.dialog_mdx);
            final EditText v = (EditText)d.findViewById(R.id.mdxTxt);
            v.setText(queryBuilder.buildMdx());
            d.setOnDismissListener(di -> {
                queryBuilder.updateMdx(v.getText().toString().trim());
            });
            d.show();
        });

        playImgBtn.setOnClickListener( v -> {
            Intent i = new Intent(this, TableResultActivity.class);
            i.putExtra(MdxActivity.MDX_KEY, queryBuilder.buildMdx());
            startActivity(i);
        });
    }

    private Dialog createDialog(String title, int layoutResourceId) {
        Dialog d = new Dialog(this);
        d.setTitle(title);
        d.setContentView(layoutResourceId);
        d.setOnDismissListener(dialog -> {
            refreshSelectionList();
        });
        return d;
    }

    private final DimensionsAdapter.OnChildItemClickListener dimensionClickListener = new DimensionsAdapter.OnChildItemClickListener() {
        @Override
        public void onChildClick(View view, int groupPosition, int childPosition, Level.State newState) {
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
                    else if (currentState == Level.State.FILTER) {
                        SaikuLevel sl = (SaikuLevel) l.getData();
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
                    SaikuLevel sl = (SaikuLevel) l.getData();
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
        }
    };

    private enum GroupPosition {Measures, Collumns, Rows, Filters};
    private final SelectionAdapter.OnChildItemClickListener selectionListener = new SelectionAdapter.OnChildItemClickListener() {
        @Override
        public void onChildClick(SelectionEntity entity, int groupPosition, int childPosition) {
            switch (GroupPosition.values()[groupPosition]) {
                case Measures:
                    for (int i = 0, end = cubeMeasures.size(); i < end; i++) {
                        if (cubeMeasures.get(i).getUniqueName().equals(entity.getUniqueName())) {
                            measuresAdapter.setCheckedItem(i);
                            break;
                        }
                    }
                    break;
                case Filters:
                    for (Dimension d : cubeDimensions.values()) {
                        Stream.of(d.getLevels())
                                .filter(f -> f.getData().getUniqueName().equals(entity.getUniqueName()))
                                .findFirst().ifPresent(f -> f.setState(Level.State.NEUTRAL));
                    }
                    break;
            }
            queryBuilder.removeEntity(entity);
            refreshSelectionList();
        }
    };

    private void refreshSelectionList() {
        selectedEntites.clear();
        selectedEntites.putAll(queryBuilder.getEntitySelection());
        selectionAdapter.notifyDataSetChanged();
    }
}
