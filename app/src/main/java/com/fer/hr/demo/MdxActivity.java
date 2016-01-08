package com.fer.hr.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.fer.hr.R;
import com.fer.hr.data.Profile;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MdxActivity extends AppCompatActivity {
    public static final String MDX_KEY = "MDX_KEY";
    @Bind(R.id.btnBack)
    ImageButton btnBack;
    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.actionBtn)
    ImageButton actionBtn;
    @Bind(R.id.mdxSpn)
    Spinner mdxSpn;
    @Bind(R.id.mdxTxt)
    EditText mdxTxt;
    @Bind(R.id.executeMdxBtn)
    Button executeMdxBtn;

    private Profile appProfile;
    private ArrayList<Query> queries;
    private ArrayAdapter<Query> mdxSpnAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdx);
        ButterKnife.bind(this);
        appProfile = new Profile(this);

        queries = getQueriesFromResources("queries");
        initView();
        setActions();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private ArrayList<Query> getQueriesFromResources(String path) {
        ArrayList<Query> queries = new ArrayList<>();
        String[] fileList;
        try {
            fileList = getAssets().list(path);
            if (fileList.length > 0) {
                // This is a folder
                for (String file : fileList) {
                    if(file.endsWith(".mdx")) {
                        InputStream is = getAssets().open(path + "/" + file);
                        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        String line = br.readLine();
                        StringBuilder sbRaw = new StringBuilder(line);
                        StringBuilder sbFormatted = new StringBuilder(line + '\n');
                        while( (line = br.readLine()) != null) {
                            sbRaw.append(' ' + line);
                            sbFormatted.append(line + '\n');
                        }
                        queries.add(new Query(file, sbRaw.toString(), sbFormatted.toString()) );
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return queries;
    }

    private void initView() {
        title.setText("MDX Query");
        mdxSpnAdapter = new ArrayAdapter<Query>(this, R.layout.spinner_header, queries);
        mdxSpnAdapter.setDropDownViewResource(R.layout.spinner_item);
        mdxSpn.setAdapter(mdxSpnAdapter);
    }

    private void setActions() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appProfile.setAuthenticationToken(null);
                startActivity(new Intent(MdxActivity.this, LoginActivity.class));
                finish();
            }
        });

        mdxSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mdxTxt.setText(queries.get(position).queryFormated);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });

        executeMdxBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MdxActivity.this, ResultActivity.class);
//              i.putExtra(MDX_KEY, queries.get(mdxSpn.getSelectedItemPosition()).queryRaw);
                i.putExtra(MDX_KEY, mdxTxt.getText().toString());

                startActivity(i);
            }
        });
    }

    private static class Query {
        public String queryName;
        public String queryRaw;
        public String queryFormated;

        public Query(String queryName, String queryRaw, String queryFormated) {
            this.queryName = queryName;
            this.queryRaw = queryRaw;
            this.queryFormated = queryFormated;
        }

        @Override
        public String toString() {
            return queryName;
        }
    }

}
