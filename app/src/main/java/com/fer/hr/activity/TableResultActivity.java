package com.fer.hr.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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

    @Bind(R.id.rootLayout)
    RelativeLayout rootLayout;
    @Bind(R.id.resultVerticalScrol)
    ScrollView vScroll;
    @Bind(R.id.resultHorizontalScrol)
    HorizontalScrollView hScroll;
    @Bind(R.id.btnBack)
    ImageButton btnBack;
    @Bind(R.id.actionBtn)
    ImageButton actionBtn;
    @Bind(R.id.title)
    TextView title;
    private TableLayout table;

    private static boolean isRunning = false;
    final static float STEP = 200;
    private static final int FONT_SIZE = 10;
    float mRatio = 1.0f;
    int mBaseDist;
    float mBaseRatio;
    private QueryResult queryResult;

    private float mx, my;

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
                    table = createTableLayout(null, queryResult, queryResult.getHeight(), 0);
                    hScroll.addView(table);
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
        if (event.getPointerCount() == 1) {
            float curX, curY;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mx = event.getX();
                    my = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    curX = event.getX();
                    curY = event.getY();
                    vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                    hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                    mx = curX;
                    my = curY;
                    break;
                case MotionEvent.ACTION_UP:
                    curX = event.getX();
                    curY = event.getY();
                    vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                    hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                    break;
            }
        } else if (event.getPointerCount() == 2) {
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
                zoom(mRatio, new PointF(0, 0));
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

        TableLayout table = new TableLayout(ctx);
        TableLayout.LayoutParams tableLayout = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        table.setLayoutParams(tableLayout);
//        table.setBackgroundColor(Color.BLACK);

        TableRow.LayoutParams rowLayout = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);

        TableRow.LayoutParams cellLayout = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        cellLayout.setMargins(1, 1, 1, 1);
//        cellLayout.weight = 1;

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
            tblRow.setLayoutParams(rowLayout);
            tblRow.setBackgroundColor(Color.BLACK);

            Cell[] rowData = queryResult.getCellset().get(i);
            for (Cell cellData : rowData) {
                TextView cell = new TextView(ctx);
                cell.setLayoutParams(cellLayout);
                cell.setTypeface(Typeface.MONOSPACE);
                cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE);
                cell.setBackgroundColor(Color.WHITE);
                cell.setGravity(Gravity.CENTER);
                cell.setText(cellData.getValue());
                cell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("igor " + cellData.getValue());
                    }
                });
                tblRow.addView(cell);
            }
            table.addView(tblRow);
        }

//        ScrollView sv = new ScrollView(this);
//        HorizontalScrollView hsv = new HorizontalScrollView(this);
//        hsv.addView(tableLayout);
//        sv.addView(hsv);
        return table;
    }

    int getDistance(MotionEvent event) {
        int dx = (int) (event.getX(0) - event.getX(1));
        int dy = (int) (event.getY(0) - event.getY(1));
        return (int) (Math.sqrt(dx * dx + dy * dy));
    }

    public void zoom(Float scaleXY, PointF pivot) {
        resizeView(scaleXY);
//        scaleView(scaleXY, pivot);
    }

    private void scaleView(Float scaleXY, PointF pivot) {
        table.setPivotX(pivot.x);
        table.setPivotY(pivot.y);
        table.setScaleX(scaleXY);
        table.setScaleY(scaleXY);

        rootLayout.requestLayout();
        rootLayout.invalidate();
//        mHandler.sendEmptyMessageDelayed(UPDATE_TABLE, 300);
    }

    private void resizeView(Float scaleXY) {
        for (int i = 0, rowsCnt = table.getChildCount(); i < rowsCnt; i++) {
            View rowView = table.getChildAt(i);
            if (rowView instanceof TableRow) {
                TableRow row = (TableRow) rowView;
                for (int j = 0, colsCnt = row.getChildCount(); j < colsCnt; j++) {
                    View cellView = row.getChildAt(j);
                    if (cellView instanceof TextView) {
                        TextView cell = (TextView) cellView;
                        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE + scaleXY);
                    }
                }
            }
        }
    }

}
