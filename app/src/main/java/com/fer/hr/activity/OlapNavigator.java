package com.fer.hr.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.annimon.stream.Stream;
import com.fer.hr.R;
import com.fer.hr.activity.adapters.CubesAdapter;
import com.fer.hr.activity.adapters.DimensionsAdapter;
import com.fer.hr.activity.adapters.MeasuresAdapter;
import com.fer.hr.activity.adapters.SelectionAdapter;
import com.fer.hr.activity.fragments.DimensionsFragment;
import com.fer.hr.model.Dimension;
import com.fer.hr.model.Hierarchy;
import com.fer.hr.model.Level;
import com.fer.hr.model.QueryBuilder;
import com.fer.hr.model.SelectionEntity;
import com.fer.hr.model.SelectionGroup;
import com.fer.hr.rest.dto.discover.SaikuCube;
import com.fer.hr.rest.dto.discover.SaikuDimension;
import com.fer.hr.rest.dto.discover.SaikuMeasure;
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
    private static boolean isRunning;
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
    private FragmentManager frgMng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_olap_navigator);
        ButterKnife.bind(this);
        repository = (IRepository) ServiceProvider.getService(ServiceProvider.REPOSITORY);
        queryBuilder = new QueryBuilder();
        isRunning = true;
        frgMng = getSupportFragmentManager();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    private void setActions() {
        cubeImgBtn.setOnClickListener(tv -> {
            final int oldSelectedCubeIndx = cubesAdapter.getSelectedItemIndx();
            Dialog d = createDialog(getString(R.string.cubes), R.layout.dialog_cubes);
            d.setOnDismissListener(di -> {
                final int currentSelectedCubeIndx = cubesAdapter.getSelectedItemIndx();
                if (oldSelectedCubeIndx != currentSelectedCubeIndx) {
                    SaikuCube selectedCube = cubes.get(currentSelectedCubeIndx);
                    queryBuilder.clear();
                    queryBuilder.setCube(selectedCube);

                    cubeMeasures.clear();
                    cubeMeasures.addAll(repository.getMeasuresForCube(selectedCube));
                    measuresAdapter.notifyDataSetChanged();
                    List<SaikuDimension> dimensions = repository.getDimensionsForCube(selectedCube);
                    cubeDimensions.clear();
                    cubeDimensions.putAll(CubeMetaConverterUtil.convertToNestedListFormat(dimensions));
                    dimensionsAdapter.notifyDataSetChanged();
                }
                refreshSelectionList();
            });
            ListView lv = (ListView) d.findViewById(R.id.lv);
            lv.setAdapter(cubesAdapter);
            lv.setOnItemClickListener((av, rv, p, i) -> {
                cubesAdapter.setSelectedItemIndx(p);
                cubesAdapter.notifyDataSetChanged();
            });
            d.show();
        });

        measureImgBtn.setOnClickListener(tv -> {
            Dialog d = createDialog(getString(R.string.measures), R.layout.dialog_measures);
            ListView lv = (ListView) d.findViewById(R.id.mesuresLst);
            lv.setAdapter(measuresAdapter);
            lv.setOnItemClickListener((av, rv, p, i) -> {
                measuresAdapter.setCheckedItem(p);

                CheckBox chkbox = (CheckBox) rv.findViewById(R.id.itemChkBox);
                boolean isSelected = !chkbox.isChecked();
                chkbox.setChecked(isSelected);

                SaikuMeasure selectedMeasure = cubeMeasures.get(p);
                if (isSelected) queryBuilder.putOnMeasures(selectedMeasure);
                else queryBuilder.removeFromMeasures(selectedMeasure);
            });
            d.show();
        });

        dimensionImgBtn.setOnClickListener(tv -> {
            DimensionsFragment f = new DimensionsFragment(cubes.get(cubesAdapter.getSelectedItemIndx()), dimensionsAdapter, queryBuilder);
            f.show(frgMng, DimensionsFragment.TAG);
        });

        mdxImgBtn.setOnClickListener(iv -> {
            Dialog d = new Dialog(this);
            d.setTitle("MDX");
            d.setContentView(R.layout.dialog_mdx);
            final EditText v = (EditText) d.findViewById(R.id.mdxTxt);
            v.setText(queryBuilder.buildMdx());
            d.setOnDismissListener(di -> {
                queryBuilder.updateMdx(v.getText().toString().trim());
            });
            d.show();
        });

        playImgBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, TableResultActivity.class);
            i.putExtra(TableResultActivity.MDX_KEY, queryBuilder.buildMdx());
            i.putExtra(TableResultActivity.CUBE_KEY, cubes.get(cubesAdapter.getSelectedItemIndx()));
            startActivity(i);
        });
    }

    private Dialog createDialog(String title, int layoutResourceId) {
        Dialog d = new Dialog(this);
        d.setTitle(title);
        d.setContentView(layoutResourceId);
        d.setOnDismissListener(dialogInterface -> {
            refreshSelectionList();
        });
        return d;
    }

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
                    //[Store].[Stores].[Canada] -> member uniqueName
                    long sameLevelFilterCnt = Stream.of(selectedEntites.get(GroupPosition.Filters.ordinal()).getEntities())
                            .filter(e -> e.getLevelUniqueName().equals(entity.getLevelUniqueName()))
                            .count();
                    if (sameLevelFilterCnt - 1 == 0) {
                        for (Dimension d : cubeDimensions.values())
                            for (Hierarchy h : d.getHierarchies())
                                for (Level l : h.getLevels())
                                    if (l.getData().getUniqueName().equals(entity.getLevelUniqueName()))
                                        l.setState(Level.State.NEUTRAL);
                    }
                    break;
            }
            queryBuilder.removeEntity(entity);
            refreshSelectionList();
        }
    };

    public void refreshSelectionList() {
        selectedEntites.clear();
        selectedEntites.putAll(queryBuilder.getEntitySelection());
        selectionAdapter.notifyDataSetChanged();
    }
}
