package com.fer.hr.activity.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fer.hr.R;
import com.fer.hr.activity.TableResultActivity;
import com.fer.hr.model.QueryBuilder;
import com.fer.hr.rest.dto.queryResult.Cell;
import com.fer.hr.services.ServiceProvider;
import com.fer.hr.services.repository.IRepository;

import butterknife.Bind;
import butterknife.ButterKnife;


public class DrillFragment extends DialogFragment {
    public static final String TAG = DrillFragment.class.getSimpleName();
    public static enum AxisPosition {ROWS, COLS};

    @Bind(R.id.drillUp)
    TextView drillUp;
    @Bind(R.id.drillDown)
    TextView drillDown;

    private Cell drillCell;
    private QueryBuilder queryBuilder;
    private String drillMdx;

    private Dialog dialog;
    private TableResultActivity parentActivity;

    public DrillFragment(Cell drillCell, QueryBuilder queryBuilder) {
        this.drillCell = drillCell;
        this.queryBuilder = queryBuilder;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                dismiss();
            }
        };

        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof TableResultActivity)) throw new ClassCastException("Expected activity of OlapNavigator!");
        parentActivity = (TableResultActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parentActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_drill_member, container, false);
        ButterKnife.bind(this, view);
        setActions();
        dialog.setTitle(drillCell.getValue());
        return view;
    }

    private void setActions() {
//        drillUp.setOnClickListener(v -> {
//            drillMdx = queryBuilder.drillDown(drillCell);
//            parentActivity.renderTable(drillMdx);
//            dialog.dismiss();
//        });

        drillDown.setOnClickListener(v -> {
            drillMdx = queryBuilder.drillDown(drillCell);
            parentActivity.renderTable(drillMdx);
            dialog.dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
