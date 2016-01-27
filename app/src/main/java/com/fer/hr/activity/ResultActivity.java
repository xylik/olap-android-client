package com.fer.hr.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
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
import java.util.Properties;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ResultActivity extends AppCompatActivity {
    private static final String CUBE_METADATA_PATH = "queries/testCube.properties";
    @Bind(R.id.btnBack)
    ImageButton btnBack;
    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.actionBtn)
    ImageButton actionBtn;
    @Bind(R.id.resultTxt)
    TextView resultTxt;

    final static float STEP = 200;
    float mRatio = 1.0f;
    int mBaseDist;
    float mBaseRatio;
    float fontsize = 5;

    private static boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ButterKnife.bind(this);
        isRunning = true;

        initView();
        setActions();

        String mdxQuery = getIntent().getStringExtra(TableResultActivity.MDX_KEY);
        SaikuCube cube = loadCubeDefinitionFromAssets(CUBE_METADATA_PATH);
        ThinQuery tq = new ThinQuery(UUID.randomUUID().toString(), cube, mdxQuery);

        App.api.executeThinQuery(tq, new Callback<QueryResult>() {
            @Override
            public void success(QueryResult queryResult, Response response) {
                if (isRunning) {
                    if (queryResult.getCellset() == null) {
                        Toast.makeText(ResultActivity.this, "MDX Syntax/Semantic error!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String table = createTableFromQuery(queryResult);
                    resultTxt.setText(table);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isRunning) {
                    Toast.makeText(ResultActivity.this, "Server error!", Toast.LENGTH_SHORT).show();
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
                resultTxt.setTextSize(mRatio + fontsize);
            }
        }
        super.dispatchTouchEvent(event);
        return false;
    }

    private void initView() {
        title.setText("Result");
        actionBtn.setVisibility(View.GONE);
        resultTxt.setTextSize(mRatio + fontsize);
    }

    private void setActions() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
//        vScroll.setOnTouchListener(touchListener);
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

    private String createTableFromQuery(QueryResult queryResult) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbBorder = new StringBuilder("|");

        String cellFormat = "%-15s|";
        String cellBorder = "---------------|";
        for (int i = 0; i < queryResult.getCellset().get(0).length; i++)
            sbBorder.append(cellBorder);

        for (Cell[] cells : queryResult.getCellset()) {
            sb.append(sbBorder.toString() + "\n");
            sb.append("|");
            for (Cell c : cells) {
                sb.append(String.format(cellFormat, c.getValue().trim()));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
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
                    resultTxt.setTextSize(mRatio + fontsize);
                }
            }
            return true;
        }
    };

    int getDistance(MotionEvent event) {
        int dx = (int) (event.getX(0) - event.getX(1));
        int dy = (int) (event.getY(0) - event.getY(1));
        return (int) (Math.sqrt(dx * dx + dy * dy));
    }

}
