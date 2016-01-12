package com.fer.hr.demo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.fer.hr.App;
import com.fer.hr.R;
import com.fer.hr.rest.dto.discover.SaikuCube;
import com.fer.hr.rest.dto.query2.ThinQuery;
import com.fer.hr.rest.dto.queryResult.Cell;
import com.fer.hr.rest.dto.queryResult.QueryResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TableResultActivity extends AppCompatActivity {
    private static final String CUBE_METADATA_PATH = "queries/testCube.properties";

    @Bind(R.id.btnBack)
    ImageButton btnBack;
    @Bind(R.id.actionBtn)
    ImageButton actionBtn;
    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.rootLayout)
    RelativeLayout rootLayout;
    @Bind(R.id.resultHorizontalScrol)
    HorizontalScrollView resultHorizontalScrol;
    @Bind(R.id.measureView)
    View measureView;
    private TableLayout tableLyt;

    private static boolean isRunning = false;
    final static float STEP = 200;
    float mRatio = 1.0f;
    int mBaseDist;
    float mBaseRatio;
    private QueryResult queryResult;
    private int mw = -1;
    private int th = -1;

    private static int UPDATE_TABLE;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_TABLE) {
                rootLayout.requestLayout();
                rootLayout.invalidate();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_result);
        ButterKnife.bind(this);
        isRunning = true;

        initView();
        setActions();

        String mdxQuery = getIntent().getStringExtra(MdxActivity.MDX_KEY);
        SaikuCube cube = loadCubeDefinitionFromAssets(CUBE_METADATA_PATH);
        ThinQuery tq = new ThinQuery(UUID.randomUUID().toString(), cube, mdxQuery);

        App.api.executeThinQuery(tq, new Callback<QueryResult>() {
            @Override
            public void success(QueryResult queryResult, Response response) {
                if (isRunning) {
                    if (queryResult.getCellset() == null) {
                        Toast.makeText(TableResultActivity.this, "MDX Syntax/Semantic error!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    TableResultActivity.this.queryResult = queryResult;
                    tableLyt = createTableLayout(null, queryResult, queryResult.getHeight(), 0);
                    resultHorizontalScrol.addView(tableLyt);
//                    mw = measureView.getWidth();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isRunning) {
                    Toast.makeText(TableResultActivity.this, "Server error!", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            int action = event.getAction();
            int pureaction = action & MotionEvent.ACTION_MASK;
            if (pureaction == MotionEvent.ACTION_POINTER_DOWN) {
                mBaseDist = getDistance(event);
                mBaseRatio = mRatio;
            } else {
                float delta = (getDistance(event) - mBaseDist) / STEP;
                float multi = (float) Math.pow(2, delta);
                mRatio = Math.min(1024.0f, Math.max(0.1f, mBaseRatio * multi));
                //fontsize + mratio
                zoom(mRatio, mRatio, new PointF(0, 0));
            }
        }
        super.dispatchTouchEvent(event);
        return false;
    }

    private void initView() {
        title.setText("Result");
        actionBtn.setVisibility(View.GONE);
    }

    private void setActions() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private SaikuCube loadCubeDefinitionFromAssets(String filePath) {
        InputStream is = null;
        try {
            is = getAssets().open(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Properties p = new Properties();
        try {
            p.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean isVisible = p.getProperty("VISIBLE").equalsIgnoreCase("true") ? true : false;
        return new SaikuCube(
                p.getProperty("CONNECTION"),
                p.getProperty("UNIQUE_NAME"),
                p.getProperty("NAME"),
                p.getProperty("CAPTION"),
                p.getProperty("CATALOG"),
                p.getProperty("SCHEMA"),
                isVisible);
    }

    private TableLayout createTableLayout(List<String> attributeNames, QueryResult queryResult, int rowsPerPage, int startPos) {
        Context ctx = this;
        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams();
        TableLayout tableLayout = new TableLayout(ctx);
        tableLayout.setBackgroundColor(Color.BLACK);

        TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams();
        tableRowParams.setMargins(1, 1, 1, 1);
        tableRowParams.weight = 1;

//        TableRow tblHeader = new TableRow(ctx);
//        tblHeader.setBackgroundColor(Color.BLACK);
//        for (String s : attributeNames) {
//            TextView cell = new TextView(ctx);
//            cell.setBackgroundColor(Color.WHITE);
//            cell.setGravity(Gravity.CENTER);
//            cell.setText(s);
//            tblHeader.addView(cell, tableRowParams);
//        }
//        tableLayout.addView(tblHeader, tableLayoutParams);

        for (int i = startPos, resSize = queryResult.getHeight(), end = i + rowsPerPage; i < resSize && i < end; i++) {
            TableRow tblRow = new TableRow(ctx);
            tblRow.setBackgroundColor(Color.BLACK);

            Cell[] row = queryResult.getCellset().get(i);
            for (Cell c : row) {
                TextView cell = new TextView(ctx);
                cell.setBackgroundColor(Color.WHITE);
                cell.setGravity(Gravity.CENTER);
                cell.setText(c.getValue());
                tblRow.addView(cell, tableRowParams);
            }
            tableLayout.addView(tblRow, tableLayoutParams);
        }

//        ScrollView sv = new ScrollView(this);
//        HorizontalScrollView hsv = new HorizontalScrollView(this);
//        hsv.addView(tableLayout);
//        sv.addView(hsv);
        return tableLayout;
    }

    public void zoom(Float scaleX, Float scaleY, PointF pivot) {
        updateView2(scaleX, scaleY, pivot);
    }

    private void updateView2(Float scaleX, Float scaleY, PointF pivot) {
//        if(th == -1) th = tableLyt.getHeight();
//        th *= scaleY;
        tableLyt.setPivotX(pivot.x);
        tableLyt.setPivotY(pivot.y);
        tableLyt.setScaleX(scaleX);
        tableLyt.setScaleY(scaleY);

//        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(mw - 100, th);
//        resultHorizontalScrol.setLayoutParams(rlp);
        rootLayout.requestLayout();
        rootLayout.invalidate();
        mHandler.sendEmptyMessageDelayed(UPDATE_TABLE, 300);
    }

//    private void updateView1(Float scaleX, Float scaleY, PointF pivot) {
//        tableContainer.removeAllViews();
//        ViewGroup tableViewParent = (ViewGroup) tableLyt.getParent();
//        if (tableViewParent != null) tableViewParent.removeAllViews();
//
//        tableLyt.setPivotX(pivot.x);
//        tableLyt.setPivotY(pivot.y);
//        tableLyt.setScaleX(scaleX);
//        tableLyt.setScaleY(scaleY);
//
//        ScrollView sv = new ScrollView(this);
//        HorizontalScrollView hsv = new HorizontalScrollView(this);
//        hsv.addView(tableLyt);
//        sv.addView(hsv);
//
//        tableContainer.addView(sv);
//        rootLayout.requestLayout();
//        rootLayout.invalidate();
//    }

    int getDistance(MotionEvent event) {
        int dx = (int) (event.getX(0) - event.getX(1));
        int dy = (int) (event.getY(0) - event.getY(1));
        return (int) (Math.sqrt(dx * dx + dy * dy));
    }


}
