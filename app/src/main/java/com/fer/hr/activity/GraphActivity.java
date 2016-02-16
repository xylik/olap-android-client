package com.fer.hr.activity;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.BoringLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.androidplot.Plot;
import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.DynamicTableModel;
import com.androidplot.ui.Size;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetric;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.annimon.stream.Stream;
import com.fer.hr.R;
import com.fer.hr.model.GraphData;
import com.fer.hr.utils.PixelUtil;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GraphActivity extends AppCompatActivity implements View.OnTouchListener {
    public static final String GRAPH_DATA_KEY = "GRAPH_DATA_KEY";
    public static final String GRAPH_TITLE_KEY = "GRAPH_TITLE_KEY";

    @Bind(R.id.plot)
    XYPlot plot;

    private GraphData graphData;
    private List<String> domainValues;
    private List<List<Number>> rangeValues;
    private List<String> legendTitles;
    private String graphTitle;

    private Button resetButton;
    private List<SimpleXYSeries> seriesList = null;
    private int slSize = -1;
    private PointF minXY;
    private PointF maxXY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        ButterKnife.bind(this);

        graphData = (GraphData)getIntent().getSerializableExtra(GRAPH_DATA_KEY);
        graphTitle = getIntent().getStringExtra(GRAPH_TITLE_KEY);
        prepareDomainValues();
        prepareRangeValues();
        prepareLegendTitles();
        renderGraph();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.graph_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.resetMItem) {
            minXY.x = seriesList.get(0).getX(0).floatValue();
            maxXY.x = seriesList.get(slSize-1).getX(seriesList.get(slSize-1).size() - 1).floatValue();
            plot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
            plot.redraw();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void prepareDomainValues() {
        domainValues = new ArrayList<>();

        for(List<String> row: graphData.getxLabels()) {
            StringBuilder sb = new StringBuilder();

            for(String s: row) sb.append("~" + s);
            sb.delete(0, 1);
            domainValues.add(sb.toString());
        }
    }

    private void prepareLegendTitles() {
        legendTitles = new ArrayList<>();

        for(List<String> title : graphData.getyLabels()) {
            StringBuilder sb = new StringBuilder();
            for(String part: title) sb.append("~").append(part);
            sb.delete(0, 1);
            legendTitles.add(sb.toString());
        }
    }

    private void prepareRangeValues() {
        List<List<Number>> yValues = graphData.getyValues();

        List<List<Number>> formattedYValues = new ArrayList<>();

        for(int col=0, colCnt=yValues.get(0).size(); col<colCnt; col++) {
            for(int row=0, rowCnt = yValues.size(); row<rowCnt; row++) {
                List<Number> rVals = yValues.get(row);

                for(int cellPos=0, cellCnt=rVals.size(); cellPos<cellCnt; cellPos++) {
                    if(row == 0 && cellPos == col) formattedYValues.add( new ArrayList<>());

                    if(cellPos == col) {
                        formattedYValues.get(col).add(rVals.get(cellPos));
                    }
                }
            }
        }
        rangeValues = formattedYValues;
    }

    private void renderGraph() {
        plot.setTitle(graphTitle);

        seriesList = new ArrayList<>();
        for(List<Number> series: rangeValues) {
            slSize++;
            SimpleXYSeries s =new SimpleXYSeries(legendTitles.get(slSize));
            for(int x=0, endx=series.size(); x<endx; x++) {
                s.addLast(x, series.get(x));
            }
//            new SimpleXYSeries(series, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "");
            seriesList.add(s);
            plot.addSeries(s, getLineFormatter(slSize));
//            plot.addSeries(s, getBarFormatter(slSize));
        }

        plot.setDomainValueFormat(new NumberFormat() {
            @Override
            public StringBuffer format(double value, StringBuffer buffer, FieldPosition field) {
                return new StringBuffer(domainValues.get((int) value));
            }
            @Override
            public StringBuffer format(long value, StringBuffer buffer, FieldPosition field) {
                return null;
            }
            @Override
            public Number parse(String string, ParsePosition position) {
                return null;
            }
        });

        plot.setTicksPerRangeLabel(2);
        plot.setRangeValueFormat(new DecimalFormat("#"));
        plot.getGraphWidget().setRangeTickLabelHorizontalOffset(PixelUtil.dpToPx(12, this));
        plot.getGraphWidget().getRangeTickLabelPaint().setTextSize(12);
        plot.getGraphWidget().getRangeOriginTickLabelPaint().setTextSize(12);

        plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
//        plot.setDomainValueFormat(new DecimalFormat("#"));
//        plot.setTicksPerDomainLabel(2);
        plot.getGraphWidget().setDomainLabelOrientation(-45);
        plot.getGraphWidget().getDomainTickLabelPaint().setTextSize(14);
        plot.getGraphWidget().getDomainTickLabelPaint().setTextAlign(Paint.Align.RIGHT);
        plot.getGraphWidget().getDomainOriginTickLabelPaint().setTextSize(14);
        plot.getGraphWidget().getDomainOriginTickLabelPaint().setTextAlign(Paint.Align.RIGHT);

        plot.setOnTouchListener(this);
        plot.setBorderStyle(Plot.BorderStyle.NONE, null, null);
        plot.redraw();
        plot.calculateMinMaxVals();
        minXY = new PointF(plot.getCalculatedMinX().floatValue(), plot.getCalculatedMinY().floatValue());
        maxXY = new PointF(plot.getCalculatedMaxX().floatValue(), plot.getCalculatedMaxY().floatValue());

        plot.getLegendWidget().setTableModel(new DynamicTableModel(1, legendTitles.size()));
        // adjust the legend size so there is enough room
        // to draw the new legend grid:
        int numCharsLongestWord = Stream.of(legendTitles).max((a, b) -> {
            return a.length() > b.length() ? 1 : (a.length() == b.length() ? 0 : -1);
        }).orElse("").length();
//        plot.getLegendWidget().setSize(new Size(PixelUtil.dpToPx(legendTitles.size() * 5, this), SizeLayoutType.ABSOLUTE,
//                PixelUtil.dpToPx(numCharsLongestWord * 3, this), SizeLayoutType.ABSOLUTE));
        int legendIconOffset = 7;
        int legendTextWidth = PixelUtil.dpToPx(legendIconOffset + numCharsLongestWord * 5, this);
        int legendTextHeight = PixelUtil.dpToPx(legendTitles.size() * 10, this);
        Size legendSize = new Size(legendTextHeight, SizeLayoutType.ABSOLUTE, legendTextWidth, SizeLayoutType.ABSOLUTE);
        plot.getLegendWidget().setSize(legendSize);
        // add a semi-transparent black background to the legend
        // so it's easier to see overlaid on top of our plot:
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.BLACK);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAlpha(50);
        plot.getLegendWidget().setBackgroundPaint(bgPaint);
        // adjust the padding of the legend widget to look a little nicer:
        int dpInPixels = PixelUtil.dpToPx(1, this);
        plot.getLegendWidget().setPadding(PixelUtil.dpToPx(2, this), dpInPixels, dpInPixels, dpInPixels);
        // reposition the grid so that it rests above the top-right
        int legendWidgetOffset = PixelUtil.dpToPx(10, this);
        plot.getLegendWidget().position(
                legendWidgetOffset,
                XLayoutStyle.ABSOLUTE_FROM_RIGHT,
                legendWidgetOffset,
                YLayoutStyle.ABSOLUTE_FROM_TOP,
                AnchorPosition.RIGHT_TOP);
    }

    private LineAndPointFormatter getLineFormatter(int seriesPos) {
        LineAndPointFormatter sf = new LineAndPointFormatter();
        sf.setPointLabeler(new PointLabeler() {
            DecimalFormat df = new DecimalFormat("#");

            @Override
            public String getLabel(XYSeries series, int index) {
                return df.format(series.getY(index));
            }
        });
        Paint linePaint = sf.getLinePaint();
        Paint vertexPaint = sf.getVertexPaint();

        if(seriesPos == 0) {
            linePaint.setColor(getResources().getColor(R.color.green));
            vertexPaint.setColor(getResources().getColor(R.color.green_accent));
        }else if(seriesPos == 1) {
            linePaint.setColor(getResources().getColor(R.color.orange));
            vertexPaint.setColor(getResources().getColor(R.color.orange_accent));
        }else if(seriesPos == 2) {
            linePaint.setColor(getResources().getColor(R.color.purple));
            vertexPaint.setColor(getResources().getColor(R.color.purple_accent));
        }else if(seriesPos == 3) {
            linePaint.setColor(getResources().getColor(R.color.brown));
            vertexPaint.setColor(getResources().getColor(R.color.brown_accent));
        } else {
            int lr = new Double(Math.random() * 255).intValue();
            int lg = new Double(Math.random() * 255).intValue();
            int lb = new Double(Math.random() * 255).intValue();
            linePaint.setColor(Color.rgb(lr, lg, lb));

            int vr = lr - 25 >= 0 ? lr-25 : (lr-10 >= 0 ? lr-10 : lr);
            int vg = lg-25 >= 0 ? lg-25 : (lg-10 >= 0 ? lg-10: lg);
            int vb = lb-25 >= 0 ? lb-25 : (lb-10 >= 0 ? lb-10: lb);
            vertexPaint.setColor(Color.rgb(vr, vg, vb));
        }

        sf.getFillPaint().setColor(Color.TRANSPARENT);
        linePaint.setStrokeWidth(PixelUtil.dpToPx(2, this));
        vertexPaint.setStrokeWidth(PixelUtil.dpToPx(5, this));
//            seriesFormat.setPointLabelFormatter(new PointLabelFormatter());
//            seriesFormat.configure(getApplicationContext(), R.xml.line_point_formatter_with_labels);
        PointLabelFormatter  plf = new PointLabelFormatter();
        plf.getTextPaint().setTextSize(14);
        plf.getTextPaint().setColor(Color.BLACK);
        sf.setPointLabelFormatter(plf);

        sf.setInterpolationParams(new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Uniform));
        return sf;
    }

    private BarFormatter getBarFormatter(int seriesPos) {
        BarFormatter bf = new BarFormatter(Color.rgb(51, 181, 229), Color.TRANSPARENT);
        return bf;
    }

    // Definition of the touch states
    static final int NONE = 0;
    static final int ONE_FINGER_DRAG = 1;
    static final int TWO_FINGERS_DRAG = 2;
    int mode = NONE;

    PointF firstFinger;
    float distBetweenFingers;
    boolean stopThread = false;

    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: // Start gesture
                firstFinger = new PointF(event.getX(), event.getY());
                mode = ONE_FINGER_DRAG;
                stopThread = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN: // second finger
                distBetweenFingers = spacing(event);
                // the distance check is done to avoid false alarms
                if (distBetweenFingers > 5f) {
                    mode = TWO_FINGERS_DRAG;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ONE_FINGER_DRAG) {
                    PointF oldFirstFinger = firstFinger;
                    firstFinger = new PointF(event.getX(), event.getY());
                    scroll(oldFirstFinger.x - firstFinger.x);
                    plot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
                    plot.redraw();
                } else if (mode == TWO_FINGERS_DRAG) {
                    float oldDist = distBetweenFingers;
                    distBetweenFingers = spacing(event);
                    zoom(oldDist / distBetweenFingers);
                    plot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
                    plot.redraw();
                }
                break;
        }
        return true;
    }

    private void zoom(float scale) {
        float domainSpan = maxXY.x - minXY.x;
        float domainMidPoint = maxXY.x - domainSpan / 2.0f;
        float offset = domainSpan * scale / 2.0f;

        minXY.x = domainMidPoint - offset;
        maxXY.x = domainMidPoint + offset;

//        minXY.x = Math.min(minXY.x, series[3].getX(series[3].size() - 3).floatValue());
//        maxXY.x = Math.max(maxXY.x, series[0].getX(1).floatValue());
        minXY.x = Math.min(minXY.x, seriesList.get(slSize-1).getX(seriesList.get(slSize - 1).size() - (slSize - 1)).floatValue());
        maxXY.x = Math.max(maxXY.x, seriesList.get(0).getX(1).floatValue());
        clampToDomainBounds(domainSpan);
    }

    private void scroll(float pan) {
        float domainSpan = maxXY.x - minXY.x;
        float step = domainSpan / plot.getWidth();
        float offset = pan * step;
        minXY.x = minXY.x + offset;
        maxXY.x = maxXY.x + offset;
        clampToDomainBounds(domainSpan);
    }

    private void clampToDomainBounds(float domainSpan) {
//        float leftBoundary = series[0].getX(0).floatValue();
//        float rightBoundary = series[3].getX(series[3].size() - 1).floatValue();
        float leftBoundary = seriesList.get(0).getX(0).floatValue();
        float rightBoundary = seriesList.get(slSize-1).getX(seriesList.get(slSize-1).size() - 1).floatValue();
        // enforce left scroll boundary:
        if (minXY.x < leftBoundary) {
            minXY.x = leftBoundary;
            maxXY.x = leftBoundary + domainSpan;
//        } else if (maxXY.x > series[3].getX(series[3].size() - 1).floatValue()) {
        } else if (maxXY.x >seriesList.get(slSize-1).getX(seriesList.get(slSize - 1).size() - 1).floatValue()) {
            maxXY.x = rightBoundary;
            minXY.x = rightBoundary - domainSpan;
        }
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.hypot(x, y);
    }
}
