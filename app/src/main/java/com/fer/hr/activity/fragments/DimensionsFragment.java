package com.fer.hr.activity.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.fer.hr.R;
import com.fer.hr.activity.OlapNavigator;
import com.fer.hr.activity.adapters.DimensionsAdapter;
import com.fer.hr.activity.adapters.FilterAdapter;
import com.fer.hr.model.Level;
import com.fer.hr.model.QueryBuilder;
import com.fer.hr.rest.dto.discover.SaikuCube;
import com.fer.hr.rest.dto.discover.SaikuLevel;
import com.fer.hr.rest.dto.discover.SaikuMember;
import com.fer.hr.rest.dto.discover.SimpleCubeElement;
import com.fer.hr.services.ServiceProvider;
import com.fer.hr.services.common.Callback;
import com.fer.hr.services.repository.IRepository;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;


public class DimensionsFragment extends DialogFragment {
    public static final String TAG = DimensionsFragment.class.getSimpleName();
    private static boolean areMembersShowing = false;

    @Bind(R.id.dimensionsExpLst)
    ExpandableListView dimensionsExpLst;
    @Bind(R.id.filterLst)
    ListView filterLst;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;

    private OlapNavigator parentActivity;
    private SaikuCube cube;
    private DimensionsAdapter dimensionsAdapter;
    private QueryBuilder queryBuilder;
    private IRepository repository;
    private FilterAdapter filterAdapter;
    private List<SimpleCubeElement> filterMembers;
    private Dialog dialog;
    private Level selectedLevel;
    private ImageView filterImgV;

    public DimensionsFragment(SaikuCube cube, DimensionsAdapter dimensionsAdapter, QueryBuilder queryBuilder) {
        this.cube = cube;
        this.dimensionsAdapter = dimensionsAdapter;
        this.queryBuilder = queryBuilder;
        repository = (IRepository) ServiceProvider.getService(ServiceProvider.REPOSITORY);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                if (areMembersShowing) {
                    dimensionsExpLst.setVisibility(View.VISIBLE);
                    filterLst.setVisibility(View.GONE);
                    if (filterAdapter.getCheckedItems().size() > 0) {
                        filterImgV.setImageResource(R.drawable.delete_icon);
                        selectedLevel.setState(Level.State.FILTER);
                    } else {
                        filterImgV.setImageResource(R.drawable.filter_icon);
                        selectedLevel.setState(Level.State.NEUTRAL);
                    }
                    selectedLevel = null;
                    filterImgV = null;
                    filterAdapter = null;
                    filterMembers = null;
                    areMembersShowing = false;
                } else{
                    parentActivity.refreshSelectionList();
                    dismiss();
                }
            }
        };

//        dialog.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
//        dialog.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.search_bar);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof OlapNavigator)) throw new ClassCastException("Expected activity of OlapNavigator!");
        parentActivity = (OlapNavigator) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parentActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dimensions, container, false);
        ButterKnife.bind(this, view);
        dimensionsExpLst.setAdapter(dimensionsAdapter);
        dimensionsAdapter.setOnChildClickListener(dimensionClickListener);

//        dialog.setTitle(getString(R.string.dimensions));
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        dimensionsAdapter.setOnChildClickListener(null);
    }

    private final DimensionsAdapter.OnChildItemClickListener dimensionClickListener = new DimensionsAdapter.OnChildItemClickListener() {
        @Override
        public void onChildClick(View view, Level l, Level.State newState) {
            ImageView v = (ImageView) view;
            RelativeLayout parentView = (RelativeLayout) (v.getParent());
            ImageView c = (ImageView) parentView.findViewById(R.id.collsImg);
            ImageView r = (ImageView) parentView.findViewById(R.id.rowsImg);
            ImageView f = (ImageView) parentView.findViewById(R.id.filterImg);
            Level.State currentState = l.getState();

            boolean isModified = false;
            switch (newState) {
                case NEUTRAL: {
                    if (currentState == Level.State.COLLUMNS) queryBuilder.removeFromColumns(l);
                    else if (currentState == Level.State.ROWS) queryBuilder.removeFromRows(l);
                    else if (currentState == Level.State.FILTER) {
                        SaikuLevel sl = (SaikuLevel) l.getData();
                        SaikuMember filter = new SaikuMember(
                                sl.getDimensionUniqueName(),
                                sl.getHierarchyUniqueName(),
                                sl.getUniqueName(),
                                sl.getUniqueName(),
                                sl.getUniqueName(),
                                sl.getCaption());
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
                    SaikuMember fakeFilter = new SaikuMember(
                            sl.getDimensionUniqueName(),
                            sl.getHierarchyUniqueName(),
                            sl.getUniqueName(),
                            sl.getUniqueName(),
                            sl.getUniqueName(),
                            sl.getCaption());
                    isModified = queryBuilder.putOnFilters(fakeFilter);
                    if (isModified) {
                        selectedLevel = l;
                        filterImgV = f;
                        isModified = false;
                        areMembersShowing = true;
                        queryBuilder.removeFromFilters(fakeFilter);
                        dimensionsExpLst.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        dialog.setTitle(sl.getCaption());
                        repository.getMembersForLevel(cube, sl, filterMemberCallback);
                    }
                }
            }
            if (isModified) l.setState(newState);
            else return;

            updateIcons(v, c, r, f, currentState);
        }
    };

    @OnItemClick(R.id.filterLst)
    void filterLstClick(View rv, int p) {
        filterAdapter.setCheckedItem(p);

        CheckBox chkbox = (CheckBox) rv.findViewById(R.id.itemChkBox);
        boolean isSelected = !chkbox.isChecked();
        chkbox.setChecked(isSelected);

        SimpleCubeElement m = filterMembers.get(p);
        SaikuLevel l = selectedLevel.getData();
        SaikuMember selection = new SaikuMember(
                l.getDimensionUniqueName(),
                l.getHierarchyUniqueName(),
                l.getUniqueName(),
                m.getUniqueName(),
                m.getName(),
                m.getCaption());
        if (isSelected) queryBuilder.putOnFilters(selection);
        else queryBuilder.removeFromFilters(selection);
    }

    private void updateIcons(ImageView v, ImageView c, ImageView r, ImageView f, Level.State currentState) {
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

    private final Callback<List<SimpleCubeElement>> filterMemberCallback = new Callback<List<SimpleCubeElement>>() {
        @Override
        public void success(List<SimpleCubeElement> result) {
            if (getActivity() != null && isAdded()) {
                filterMembers = result;
                filterAdapter = new FilterAdapter(getContext(), result);
                filterLst.setVisibility(View.VISIBLE);
                filterLst.setAdapter(filterAdapter);
                progressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void failure(Exception e) {
            if (getActivity() != null && isAdded()) {
                areMembersShowing = false;
                dimensionsExpLst.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                filterLst.setVisibility(View.GONE);
            }
        }
    };
}
