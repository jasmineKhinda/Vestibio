package com.andreyaleev.metrosong.fragments;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.andreyaleev.metrosong.R;
import com.andreyaleev.metrosong.adapters.SessionsAdapter;
import com.andreyaleev.metrosong.db.SessionsDataSource;
import com.andreyaleev.metrosong.metronome.Session;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by jkhinda on 22/06/18.
 */

public class GraphFragment extends Fragment {


    @BindView(R.id.graph)
    GraphView graph;
    LineGraphSeries<DataPoint> series;

    private SessionsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Session> sessions;
    private ArrayList<Session> dizzySessions;

    private SessionsDataSource dataSource;


    public static GraphFragment newInstance() {
        GraphFragment frag = new GraphFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph, container, false);
        GraphView graph = (GraphView) view.findViewById(R.id.graph);
        GraphView graphDizzy = (GraphView) view.findViewById(R.id.graphDizzy);
        CardView cardGraphBpm = (CardView) view.findViewById(R.id.graphCard);
        CardView cardGraphDizzy = (CardView) view.findViewById(R.id.graphDizzyCard);
        TextView graphText = (TextView) view.findViewById(R.id.textgraph);
        TextView graphTextDizzy = (TextView) view.findViewById(R.id.textGraphDizzy);
        dataSource = new SessionsDataSource(getActivity());
        dataSource.open();
        getSessions();


        if(sessions.size()>0){
            graph.setAlpha(1f);
            graphDizzy.setAlpha(1f);
            graphText.setVisibility(View.INVISIBLE);
            graphTextDizzy.setVisibility(View.INVISIBLE);
        }else{
            graph.setAlpha(0.2f);
            graphDizzy.setAlpha(0.2f);
            graphText.setVisibility(View.VISIBLE);
            graphTextDizzy.setVisibility(View.VISIBLE);
        }

//        if(sessions.size()>0){
//            graphDizzy.setAlpha(1f);
//        }else{
//            graphDizzy.setAlpha(0.2f);
//        }


        DataPoint[] dp = new DataPoint[sessions.size()];
        int index=0;
        for (int i =sessions.size()-1; i >= 0; i--){
            dp[index] = new DataPoint(index, sessions.get(i).getBpm());
            Log.d("Vestibio", "index " + sessions.get(i).getBpm());
            Log.d("Vestibio", "dp i " + dp[i]);
            index++;
        }

        //the following are settings for the BPM graph
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dp);
        graph.addSeries(series);
        graph.setTitle("BPM/Session");
        graph.setTitleColor(getResources().getColor(R.color.grey_600));
        graph.setTitleTextSize(65);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);
        series.setThickness(5);
        series.setColor(getResources().getColor(R.color.themeGradientPrimary));
        series.setAnimated(true);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);

        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(208);
        viewport.setMinX(0);
        viewport.setMaxX(sessions.size());
        viewport.setScrollable(true);


        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setVerticalAxisTitle("BPM");
        gridLabel.setHorizontalAxisTitle("Session #");
        gridLabel.setHorizontalAxisTitleColor(getResources().getColor(R.color.grey_600));
        gridLabel.setVerticalAxisTitleColor(getResources().getColor(R.color.grey_600));
        gridLabel.setVerticalLabelsColor(getResources().getColor(R.color.themeGradientPrimary));
        gridLabel.setHorizontalLabelsColor(getResources().getColor(R.color.themeGradientPrimary));
        gridLabel.setGridColor(getResources().getColor(R.color.themeGradientLight));



        //the following are settings for the Dizzyness graph

        DataPoint[] dpDizzy = new DataPoint[sessions.size()];
        Log.d("Vestibio", "dpDizzy is "+ dpDizzy.length);
        int indexDizzy=0;
        for (int i =sessions.size()-1; i >= 0; i--){
            dpDizzy[indexDizzy] = new DataPoint(indexDizzy, sessions.get(i).getDizzynesslevel());
            Log.d("Vestibio", "index " + sessions.get(i).getDizzynesslevel());
            Log.d("Vestibio", "dp i " + dp[i]);
            indexDizzy++;
        }
        Log.d("Vestibio", "null?"+ dpDizzy.toString());
        LineGraphSeries<DataPoint> seriesDizzy = new LineGraphSeries<>(dpDizzy);
        graphDizzy.addSeries(seriesDizzy);
        graphDizzy.setTitle("Dizziness/Session");
        graphDizzy.setTitleColor(getResources().getColor(R.color.grey_600));
        graphDizzy.setTitleTextSize(65);
        graphDizzy.getViewport().setScalable(true);
        graphDizzy.getViewport().setScalableY(true);
        seriesDizzy.setThickness(5);
        seriesDizzy.setColor(getResources().getColor(R.color.themeGradientPrimary));
        seriesDizzy.setAnimated(true);
        seriesDizzy.setDrawDataPoints(true);
        seriesDizzy.setDataPointsRadius(10);

        Viewport viewportDizzy = graphDizzy.getViewport();
        viewportDizzy.setYAxisBoundsManual(true);
        viewportDizzy.setXAxisBoundsManual(true);
        viewportDizzy.setMinY(0);
        viewportDizzy.setMaxY(10);
        viewportDizzy.setMinX(0);
        viewportDizzy.setMaxX(sessions.size());

        graphDizzy.getViewport().setScalable(true);
        graphDizzy.getViewport().setScalableY(true);
        graphDizzy.getViewport().setScrollable(true);
        graphDizzy.getViewport().setScrollableY(true);


        GridLabelRenderer gridLabelDizzy = graphDizzy.getGridLabelRenderer();
        gridLabelDizzy.setVerticalAxisTitle("Dizziness Level");
        gridLabelDizzy.setHorizontalAxisTitle("Session #");
        gridLabelDizzy.setHorizontalAxisTitleColor(getResources().getColor(R.color.grey_600));
        gridLabelDizzy.setVerticalAxisTitleColor(getResources().getColor(R.color.grey_600));
        gridLabelDizzy.setVerticalLabelsColor(getResources().getColor(R.color.themeGradientPrimary));
        gridLabelDizzy.setHorizontalLabelsColor(getResources().getColor(R.color.themeGradientPrimary));
        gridLabelDizzy.setGridColor(getResources().getColor(R.color.themeGradientLight));





        seriesDizzy.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                //Toast.makeText(MainActivity.this, "Series1: On Data Point clicked: " + dataPoint, Toast.LENGTH_SHORT).show();
                double pointY = dataPoint.getY();
                double pointX = dataPoint.getX();
                if(pointY==0){
                    Toast.makeText(getActivity(), " Session # "+ ((int) pointX)+", Dizziness: "+"N/A", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(getActivity(), " Session # "+ ((int) pointX)+", Dizziness: "+ ((int) pointY), Toast.LENGTH_SHORT).show();
                }
               }
        });




        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                //Toast.makeText(MainActivity.this, "Series1: On Data Point clicked: " + dataPoint, Toast.LENGTH_SHORT).show();
                double pointY = dataPoint.getY();
                double pointX = dataPoint.getX();
                Toast.makeText(getActivity(), " Session # "+ ((int) pointX)+", BPM: "+ pointY, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void getSessions() {
        sessions = new ArrayList<>();
        dataSource.open();

        //debug
        dataSource.getAllSessions();

        sessions = dataSource.getAllSessions();
        for (Session session : sessions) {
            Log.d("Vestibio", session.toString());
        }


    }


    private void getDizzySessions() {
        dizzySessions = new ArrayList<>();
        dataSource.open();
        Log.d("Vestibio", "BLAH");

        //debug
        dataSource.getsAllDizzySessions();
        Log.d("Vestibio", "BLAH2");


        dizzySessions = dataSource.getsAllDizzySessions();
        for (Session session : dizzySessions) {
            Log.d("Vestibio", "is there dizzyness?" +session.getDizzynesslevel());
        }


    }


    @Override
    public void onResume() {
        dataSource.open();
        super.onResume();
    }

    @Override
    public void onPause() {
        dataSource.close();
        super.onPause();
    }

}
