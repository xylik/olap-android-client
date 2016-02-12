package com.fer.hr.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.fer.hr.R;
import com.fer.hr.activity.fragments.DrillFragment;
import com.fer.hr.data.Profile;
import com.fer.hr.model.QueryBuilder;
import com.fer.hr.model.Report;
import com.fer.hr.rest.dto.discover.SaikuCube;
import com.fer.hr.rest.dto.query2.ThinQuery;
import com.fer.hr.rest.dto.queryResult.Cell;
import com.fer.hr.rest.dto.queryResult.QueryResult;
import com.fer.hr.services.ServiceProvider;
import com.fer.hr.services.authentication.IAuthenticate;
import com.fer.hr.services.common.Callback;
import com.fer.hr.services.repository.IRepository;
import com.fer.hr.utils.PixelUtil;

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
    private Stack<String> mdxHistory = new Stack<>();

    @Bind(R.id.rootLayout)
    RelativeLayout rootLayout;
    @Bind(R.id.resultVerticalScrol)
    ScrollView vScroll;
    @Bind(R.id.resultHorizontalScrol)
    HorizontalScrollView hScroll;
    @Bind(R.id.navBar)
    Toolbar navBar;
    private TableLayout table;

    private boolean isRunning = false;
    private Profile appProfile;
    final static float STEP = 200;
    private static final int FONT_SIZE = 10;
    float mRatio = 1.0f;
    int mBaseDist;
    float mBaseRatio;
    private FragmentManager frgMng;
    private SaikuCube cube;
    private IAuthenticate authenticationMng;
    private IRepository repository;
    private QueryBuilder queryBuilder;
    private int screenOrientation;
    private String lastExecutedMdx = "";
    private QueryResult lastResult;
    private int fontSize = FONT_SIZE;
    private String title = "RESULT";

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
        appProfile = new Profile(this);
        frgMng = getSupportFragmentManager();
        authenticationMng = (IAuthenticate) ServiceProvider.getService(ServiceProvider.AUTHENTICATION);
        repository = (IRepository) ServiceProvider.getService(ServiceProvider.REPOSITORY);

//        SaikuCube cube = loadCubeDefinitionFromAssets(CUBE_METADATA_PATH);
        Intent i = getIntent();
        queryBuilder = QueryBuilder.instance();
        cube = (SaikuCube) i.getSerializableExtra(CUBE_KEY);
        String mdx;
        if (!mdxHistory.isEmpty()) mdx = mdxHistory.pop();
        else mdx = i.getStringExtra(MDX_KEY);
        if(i.getStringExtra(TITLE_KEY) != null) title = i.getStringExtra(TITLE_KEY);
        screenOrientation = getResources().getConfiguration().orientation;

        initView();
        setActions();
        renderTable(mdx);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        mdxHistory.clear();
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);


        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.move_in);
//            slideIn.setDuration(600);
            slideIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //do nothing
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    navBar.setVisibility(View.GONE);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                    //do nothing
                }
            });
            navBar.setAnimation(slideIn);
        }
        else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.move_out);
//            slideOut.setDuration(600);
            slideOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    navBar.setVisibility(View.VISIBLE);
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    // do nothing
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                    //do nothing
                }
            });
            navBar.setAnimation(slideOut);
            navBar.setAnimation(slideOut);
        }

        screenOrientation = newConfig.orientation;
        table = createTableLayout(null, lastResult, lastResult.getHeight(), 0);
        hScroll.removeAllViews();
        hScroll.addView(table);
        table.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.table_result_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.mdxMItem) {
            Dialog d = new Dialog(this);
            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
            d.setContentView(R.layout.dialog_mdx);
            final EditText v = (EditText) d.findViewById(R.id.mdxTxt);
            v.setTypeface(Typeface.MONOSPACE);
            v.setText(lastExecutedMdx);
            d.show();
        }
        else if(item.getItemId() == R.id.saveMItem) {
            final AlertDialog d = new AlertDialog.Builder(this)
                    .setView(R.layout.dialog_save_report)
                    .setPositiveButton(getString(R.string.save), null)
                    .setNegativeButton(getString(R.string.cancel), null)
                    .create();
            d.setCanceledOnTouchOutside(false);
            d.setOnShowListener(di -> {
                d.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {
                            EditText reportTxt = (EditText) d.findViewById(R.id.reportNameTxt);
                            String reportName = reportTxt.getText().toString().trim();
                            if (!reportName.isEmpty()) {
                                appProfile.addPersonalReport(new Report(reportName, mdxHistory.peek(), cube));
                                Toast.makeText(this, "Report saved!", Toast.LENGTH_SHORT).show();
                                d.dismiss();
                            } else reportTxt.setError("Report name can't be empty!");
                        });
                d.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setOnClickListener(v -> {
                            d.dismiss();
                        });
            });
            d.show();
        }
        else if(item.getItemId() == R.id.logoutMItem) {
            authenticationMng.logout(null);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        navBar.setNavigationIcon(R.drawable.icon_navbar_back);
        navBar.setTitle(title);
        navBar.setTitleTextColor(getResources().getColor(R.color.white));
        navBar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_more_vert_white_24dp));
        setSupportActionBar(navBar);
    }

    private void setActions() {
        navBar.setNavigationOnClickListener(v -> onBackPressed());
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

        TableRow.LayoutParams rowLayout = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);

        int cellHeight;
//        if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE)
        cellHeight = TableLayout.LayoutParams.WRAP_CONTENT;
//        else{
//            cellHeight = (int) PixelUtil.dpToPx(30, this);
//            fontSize = 12;
//        }


        for (int i = startPos, rowCnt = queryResult.getHeight(), end = i + rowsPerPage; i < rowCnt && i < end; i++) {
            TableRow tblRow = new TableRow(ctx);
            tblRow.setLayoutParams(rowLayout);
            tblRow.setBackgroundColor(getResources().getColor(R.color.tableDivider));

            Cell[] rowData = queryResult.getCellset().get(i);
            int cellPosition = -1;
            for (Cell cellData : rowData) {
                cellPosition++;
                TextView cell = new TextView(ctx);
                TableRow.LayoutParams cellLayout = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, cellHeight);

                cell.setTypeface(Typeface.MONOSPACE);
                cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                cell.setTextColor(getResources().getColor(R.color.black));
                cell.setText(cellData.getValue());
                int minPadding = getResources().getDimensionPixelSize(R.dimen.paddingExtraSmall);
                int dpPadding = getResources().getDimensionPixelSize(R.dimen.dp);
                String cellType = cellData.getType();

                if(cellData.getType().endsWith("_HEADER_HEADER")) {
                    cell.setBackgroundColor(getResources().getColor(R.color.headerHeader));
                    cell.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                    cell.setPadding(dpPadding, 0, minPadding, 0);
                }
                else if(cellData.getType().endsWith("_HEADER")) {
                    cell.setBackgroundColor(getResources().getColor(R.color.header));
                    cell.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                    if(cellType.equals("COLUMN_HEADER"))cell.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                    cell.setPadding(dpPadding, 0, minPadding, 0);
                }
                else {
                    cell.setBackgroundColor(Color.WHITE);
                    cell.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                    int p = getResources().getDimensionPixelSize(R.dimen.paddingMedium);
                    cell.setPadding(p, 0, dpPadding, 0);
                }


                if( i==0 && cellPosition >= 1 && cellPosition < rowData.length-1) cellLayout.setMargins(0, 2, 2, 1);
                else if( i==0 && cellPosition == 0 )cellLayout.setMargins(2, 2, 2, 1);
                else if( i==0 && cellPosition == rowData.length-1)cellLayout.setMargins(0, 2, 0, 1);
                else if(cellPosition == 0 && i >= 1 && i < rowCnt-1)cellLayout.setMargins(2, 0, 2, 1);
                else if(cellPosition == 0 && i == rowCnt-1)cellLayout.setMargins(2, 0, 2, 0);
                else if(i == rowCnt-1 && cellType.endsWith("_HEADER"))cellLayout.setMargins(0, 0, 2, 0);
                else if(cellType.endsWith("_HEADER") && cellPosition != rowData.length-1) cellLayout.setMargins(0, 0, 2, 1);
                else if(i == rowCnt-1) cellLayout.setMargins(0, 0, 0, 0);
                else cellLayout.setMargins(0, 0, 0, 1);
                cell.setLayoutParams(cellLayout);

                if (cellData.getType().equals(QueryBuilder.ROW_H) || cellData.getType().equals(QueryBuilder.COL_H)) {
                    cell.setBackgroundResource(R.drawable.btnbgd_none_gray);
                    cell.setOnClickListener(v -> renderTable(queryBuilder.drillDown(cellData)));
                }

                tblRow.addView(cell);
            }
            table.addView(tblRow);
        }
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
                        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize + scaleXY);
//                        ((TextView) cellView).setHeight(baseHeight + scaleXY.intValue());
                    }
                }
            }
        }
//        mHandler.sendEmptyMessageDelayed(UPDATE_TABLE, 300);
    }

    public void renderTable(String mdx) {
        lastExecutedMdx = mdx;
        repository.executeThinQuery(mdx, cube, queryResultCallback);
    }

    private final Callback<QueryResult> queryResultCallback = new Callback<QueryResult>() {
        @Override
        public void success(QueryResult result) {
            if (isRunning) {
                if (result.getCellset() == null) {
                    Toast.makeText(TableResultActivity.this, "MDX Syntax/Semantic error!", Toast.LENGTH_SHORT).show();
                    return;
                }
                mdxHistory.push(lastExecutedMdx);
                lastResult = result;
                table = createTableLayout(null, result, result.getHeight(), 0);
                hScroll.removeAllViews();
                hScroll.addView(table);
            }
        }

        @Override
        public void failure(Exception e) {
            if (isRunning) {
                lastExecutedMdx = mdxHistory.peek();
                Toast.makeText(TableResultActivity.this, "Server error!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    };
}