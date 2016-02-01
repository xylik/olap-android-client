package com.fer.hr.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
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

import com.fer.hr.R;
import com.fer.hr.activity.fragments.DrillFragment;
import com.fer.hr.model.QueryBuilder;
import com.fer.hr.rest.dto.discover.SaikuCube;
import com.fer.hr.rest.dto.query2.ThinQuery;
import com.fer.hr.rest.dto.queryResult.Cell;
import com.fer.hr.rest.dto.queryResult.QueryResult;
import com.fer.hr.services.ServiceProvider;
import com.fer.hr.services.common.Callback;
import com.fer.hr.services.repository.IRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TableResultActivity extends AppCompatActivity {
    private static final String CUBE_METADATA_PATH = "queries/testCube.properties";
    public static final String CUBE_KEY = "CUBE_KEY";
    public static final String MDX_KEY = "MDX_KEY";
    public static final String TITLE_KEY = "TITLE_KEY";
    public static Stack<String> mdxHistory = new Stack<>();

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
    private FragmentManager frgMng;
    private SaikuCube cube;
    private IRepository repository;
    private QueryBuilder queryBuilder;
    private int screenOrientation;

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
        frgMng = getSupportFragmentManager();
        repository = (IRepository) ServiceProvider.getService(ServiceProvider.REPOSITORY);

        initView();
        setActions();

//        SaikuCube cube = loadCubeDefinitionFromAssets(CUBE_METADATA_PATH);
        queryBuilder = QueryBuilder.instance();
        cube = (SaikuCube) getIntent().getSerializableExtra(CUBE_KEY);
        String mdx;
        if(!mdxHistory.isEmpty()) mdx = mdxHistory.pop();
        else mdx = getIntent().getStringExtra(MDX_KEY);
        renderTable(mdx);

        screenOrientation = getResources().getConfiguration().orientation;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    @Override
    public void onBackPressed() {
        if (mdxHistory.size() >= 2) {
            mdxHistory.pop(); //remove current mdx
            String previousMdx = mdxHistory.pop(); //remove previosMdx
            renderTable(previousMdx);
        } else super.onBackPressed();
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

        int cellHeight = (int)getResources().getDimension(R.dimen.itemHeightSmall);
        TableRow.LayoutParams cellLayout = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, cellHeight);
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
                cell.setBackgroundResource(R.drawable.btnbgd_none_white);

                DrillFragment.AxisPosition position = null;
                if (cellData.getType().equals(QueryBuilder.ROW_H))
                    position = DrillFragment.AxisPosition.ROWS;
                else if (cellData.getType().equals(QueryBuilder.COL_H))
                    position = DrillFragment.AxisPosition.COLS;
                if (position != null) {
                    if(screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                        cell.setOnLongClickListener(c -> {
                            DrillFragment f = new DrillFragment(cellData, queryBuilder);
                            f.show(frgMng, f.TAG);
                            return true;
                        });
                    }
                    else {
//                        cell.setOnClickListener(new View.OnClickListener() {
//                            private int k = 0;
//
//                            @Override
//                            public void onClick(View v) {
//                                // TODO Auto-generated method stub
//                                k++;
//                                Handler handler = new Handler();
//                                Runnable r = new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        k = 0;
//                                    }
//                                };
//
//                                if (k == 1) {
//                                    //Single click
//                                    handler.postDelayed(r, 250);
//                                } else if (k == 2) {
//                                    //Double click
//                                    k = 0;
//                                    renderTable( queryBuilder.drillDown(cellData) );
//                                }
//                            }
//                        });
                        cell.setOnClickListener( v -> renderTable( queryBuilder.drillDown(cellData) ));
                    }
                }
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

    private int getDistance(MotionEvent event) {
        int dx = (int) (event.getX(0) - event.getX(1));
        int dy = (int) (event.getY(0) - event.getY(1));
        return (int) (Math.sqrt(dx * dx + dy * dy));
    }

    private void zoom(Float scaleXY, PointF pivot) {
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
//        int baseHeight = (int)getResources().getDimensionPixelSize(R.dimen.itemHeightExtraSmall);
//        System.out.println("igorOlap:baseHeight:" + baseHeight);
        for (int i = 0, rowsCnt = table.getChildCount(); i < rowsCnt; i++) {
            View rowView = table.getChildAt(i);
            if (rowView instanceof TableRow) {
                TableRow row = (TableRow) rowView;
                for (int j = 0, colsCnt = row.getChildCount(); j < colsCnt; j++) {
                    View cellView = row.getChildAt(j);
                    if (cellView instanceof TextView) {
                        TextView cell = (TextView) cellView;
                        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE + scaleXY);
//                        ((TextView) cellView).setHeight(baseHeight + scaleXY.intValue());
                    }
                }
            }
        }
//        mHandler.sendEmptyMessageDelayed(UPDATE_TABLE, 300);
    }

    public void renderTable(String mdx) {
        mdxHistory.push(mdx);
        ThinQuery tq = new ThinQuery(UUID.randomUUID().toString(), cube, mdx);
        repository.executeThinQuery(tq, queryResultCallback);
    }

    private final Callback<QueryResult> queryResultCallback = new Callback<QueryResult>() {
        @Override
        public void success(QueryResult result) {
            if (isRunning) {
                if (result.getCellset() == null) {
                    Toast.makeText(TableResultActivity.this, "MDX Syntax/Semantic error!", Toast.LENGTH_SHORT).show();
                    return;
                }
                table = createTableLayout(null, result, result.getHeight(), 0);
                hScroll.removeAllViews();
                hScroll.addView(table);
            }
        }

        @Override
        public void failure(Exception e) {
            if (isRunning) {
                Toast.makeText(TableResultActivity.this, "Server error!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    };
}
