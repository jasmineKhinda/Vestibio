package com.amagesoftware.vestibio.fragments;

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

import com.amagesoftware.vestibio.R;
import com.amagesoftware.vestibio.adapters.SessionsAdapter;
import com.amagesoftware.vestibio.db.SessionsDataSource;
import com.amagesoftware.vestibio.metronome.Session;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.util.ArrayList;

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
        GraphView graph40= (GraphView) view.findViewById(R.id.graph40);
        GraphView graphDizzy40 = (GraphView) view.findViewById(R.id.graphDizzy40);
        TextView graphText40= (TextView) view.findViewById(R.id.textgraph40);
        CardView cardGraphBpm = (CardView) view.findViewById(R.id.graphCard);
        CardView cardGraphDizzy = (CardView) view.findViewById(R.id.graphDizzyCard);
        TextView graphText = (TextView) view.findViewById(R.id.textgraph);
        TextView graphTextDizzy = (TextView) view.findViewById(R.id.textGraphDizzy);
        TextView graphTextDizzy40 = (TextView) view.findViewById(R.id.textGraphDizzy40);
        dataSource = new SessionsDataSource(getActivity());
        dataSource.open();
        getSessions();


        if(sessions.size()>0){
            graph.setAlpha(1f);
            graphDizzy.setAlpha(1f);
            graph40.setAlpha(1f);
            graphDizzy40.setAlpha(1f);
            graphText.setVisibility(View.INVISIBLE);
            graphTextDizzy.setVisibility(View.INVISIBLE);
            graphText40.setVisibility(View.INVISIBLE);
            graphTextDizzy40.setVisibility(View.INVISIBLE);

        }else{
            graph.setAlpha(0.2f);
            graphDizzy.setAlpha(0.2f);
            graph40.setAlpha(0.2f);
            graphDizzy40.setAlpha(0.2f);
            graphText.setVisibility(View.VISIBLE);
            graphTextDizzy.setVisibility(View.VISIBLE);
            graphText40.setVisibility(View.VISIBLE);
            graphTextDizzy40.setVisibility(View.VISIBLE);
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

        Log.d("Vestibio", "session size " + sessions.size());
        DataPoint[] dp40;
//        if (sessions.size() >39){
//             dp40 = new DataPoint[40];
//        }else{
             dp40 = new DataPoint[sessions.size()];
 //       }

        int index40=0;
//        if (sessions.size() >39){
//            for (int i =39; i >= 0; i--){
//                dp40[index40] = new DataPoint(index40, sessions.get(i).getBpm());
//                Log.d("Vestibio1", "bpm " + sessions.get(i).getBpm());
//                Log.d("Vestibio1", "index " + index40);
//                Log.d("Vestibio1", "dp i " + dp40[index40]);
//                index40++;
//            }
//        }else{
            for (int i =sessions.size()-1; i >= 0; i--){
                dp40[index40] = new DataPoint(index40, sessions.get(i).getBpm());
                Log.d("Vestibio2", "index " + sessions.get(i).getBpm());
                Log.d("Vestibio2", "dp i " + dp40[i]);
                index40++;
            }
       //     }
        Log.d("Vestibio", "index40 is " +dp40.length);


        //the following are settings for the BPM graph last 40 sessions
        LineGraphSeries<DataPoint> series1 = new LineGraphSeries<>(dp40);
        graph40.addSeries(series1);
            graph40.setTitle(getResources().getString(R.string.graphBpmTitle_40Sessions));
            graph40.setTitleColor(getResources().getColor(R.color.black));
            //graph40.setTitleTextSize(45f);
            graph40.getViewport().setScalable(true);
            graph40.getViewport().setScalableY(true);
        series1.setThickness(5);
        series1.setColor(getResources().getColor(R.color.themeGradientPrimary));
        series1.setAnimated(true);
        series1.setDrawDataPoints(true);
        series1.setDataPointsRadius(10);

        Viewport viewport1 = graph40.getViewport();
        viewport1.setYAxisBoundsManual(true);
        viewport1.setXAxisBoundsManual(true);
        viewport1.setMinY(0);
        viewport1.setMaxY(208);
        if (sessions.size() >40){
            viewport1.setMinX((sessions.size()-40));
            viewport1.setMaxX(sessions.size()+1);
            Log.d("Vestibio", "min x " +(sessions.size()-40)+ "   maxx "+ sessions.size());
        }else{
            viewport1.setMinX(0);
            viewport1.setMaxX(40);
        }


        viewport1.setScrollable(true);


        GridLabelRenderer gridLabel1 = graph40.getGridLabelRenderer();
        gridLabel1.setVerticalAxisTitle(getResources().getString(R.string.graph_Bpm));
        gridLabel1.setHorizontalAxisTitle(getResources().getString(R.string.graph_sessionNumber));
        gridLabel1.setHorizontalAxisTitleColor(getResources().getColor(R.color.grey_600));
        gridLabel1.setVerticalAxisTitleColor(getResources().getColor(R.color.grey_600));
        gridLabel1.setVerticalLabelsColor(getResources().getColor(R.color.themeGradientPrimary));
        gridLabel1.setHorizontalLabelsColor(getResources().getColor(R.color.themeGradientPrimary));
        gridLabel1.setGridColor(getResources().getColor(R.color.themeGradientLight));



        //the following are settings for the BPM graph ALL sessions
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dp);
        graph.addSeries(series);
        graph.setTitle(getResources().getString(R.string.graphBpmTitle_AllSessions));
        graph.setTitleColor(getResources().getColor(R.color.grey_600));
       // graph.setTitleTextSize(25f);
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
        viewport.setMaxX(sessions.size()+1);
        viewport.setScrollable(true);


        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setVerticalAxisTitle(getResources().getString(R.string.graph_Bpm));
        gridLabel.setHorizontalAxisTitle(getResources().getString(R.string.graph_sessionNumber));
        gridLabel.setHorizontalAxisTitleColor(getResources().getColor(R.color.grey_600));
        gridLabel.setVerticalAxisTitleColor(getResources().getColor(R.color.grey_600));
        gridLabel.setVerticalLabelsColor(getResources().getColor(R.color.themeGradientPrimary));
        gridLabel.setHorizontalLabelsColor(getResources().getColor(R.color.themeGradientPrimary));
        gridLabel.setGridColor(getResources().getColor(R.color.themeGradientLight));


        //the following are settings for the Dizzyness graph all sessions

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
        graphDizzy.setTitle(getResources().getString(R.string.graphDizzyTitle_AllSessions));
        graphDizzy.setTitleColor(getResources().getColor(R.color.grey_600));
       // graphDizzy.setTitleTextSize(25f);
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
        viewportDizzy.setMaxX(sessions.size()+1);

        graphDizzy.getViewport().setScalable(true);
        graphDizzy.getViewport().setScalableY(true);
        graphDizzy.getViewport().setScrollable(true);
        graphDizzy.getViewport().setScrollableY(true);


        GridLabelRenderer gridLabelDizzy = graphDizzy.getGridLabelRenderer();
        gridLabelDizzy.setVerticalAxisTitle(getResources().getString(R.string.graph_dizzyLevel));
        gridLabelDizzy.setHorizontalAxisTitle(getResources().getString(R.string.graph_sessionNumber));
        gridLabelDizzy.setHorizontalAxisTitleColor(getResources().getColor(R.color.grey_600));
        gridLabelDizzy.setVerticalAxisTitleColor(getResources().getColor(R.color.grey_600));
        gridLabelDizzy.setVerticalLabelsColor(getResources().getColor(R.color.themeGradientPrimary));
        gridLabelDizzy.setHorizontalLabelsColor(getResources().getColor(R.color.themeGradientPrimary));
        gridLabelDizzy.setGridColor(getResources().getColor(R.color.themeGradientLight));


        //the following are settings for the Dizzyness graph for last 40 sessions
        DataPoint[] dpDizzy40;
//        if(sessions.size() >39){
//            dpDizzy40 = new DataPoint[40];
//        }else{
           dpDizzy40 = new DataPoint[sessions.size()];
//        }

        Log.d("Vestibio", "dpDizzy is "+ dpDizzy.length);
        int indexDizzy40=0;
//        if (sessions.size() >39){
//
//            for (int i =39; i >= 0; i--){
//                dpDizzy40[indexDizzy40] = new DataPoint(indexDizzy40, sessions.get(i).getDizzynesslevel());
//                Log.d("Vestibio", "index " + sessions.get(i).getDizzynesslevel());
//                Log.d("Vestibio", "dp i " + dpDizzy40[i]);
//                indexDizzy40++;
//            }
//
//
//        }else{
            for (int i =sessions.size()-1; i >= 0; i--){
                dpDizzy40[indexDizzy40] = new DataPoint(indexDizzy40, sessions.get(i).getDizzynesslevel());
                Log.d("Vestibio", "index " + sessions.get(i).getDizzynesslevel());
                Log.d("Vestibio", "dp i " + dpDizzy40[i]);
                indexDizzy40++;
            }

 //       }

        Log.d("Vestibio", "null?"+ dpDizzy.toString());
        LineGraphSeries<DataPoint> seriesDizzy1 = new LineGraphSeries<>(dpDizzy40);
        graphDizzy40.addSeries(seriesDizzy1);
        graphDizzy40.setTitle(getResources().getString(R.string.graphDizzyTitle_40Sessions));
        graphDizzy40.setTitleColor(getResources().getColor(R.color.grey_600));
       // graphDizzy40.setTitleTextSize(25f);
        graphDizzy40.getViewport().setScalable(true);
        graphDizzy40.getViewport().setScalableY(true);
        seriesDizzy1.setThickness(5);
        seriesDizzy1.setColor(getResources().getColor(R.color.themeGradientPrimary));
        seriesDizzy1.setAnimated(true);
        seriesDizzy1.setDrawDataPoints(true);
        seriesDizzy1.setDataPointsRadius(10);

        Viewport viewportDizzy1 = graphDizzy40.getViewport();
        viewportDizzy1.setYAxisBoundsManual(true);
        viewportDizzy1.setXAxisBoundsManual(true);
        viewportDizzy1.setMinY(0);
        viewportDizzy1.setMaxY(10);
        if (sessions.size() >40){
            viewportDizzy1.setMinX((sessions.size()-40));
            viewportDizzy1.setMaxX(sessions.size()+1);
            Log.d("Vestibio", "min x " +(sessions.size()-40)+ "   maxx "+ sessions.size());
        }else{
            viewportDizzy1.setMinX(0);
            viewportDizzy1.setMaxX(40);
        }

        graphDizzy40.getViewport().setScalable(true);
        graphDizzy40.getViewport().setScalableY(true);
        graphDizzy40.getViewport().setScrollable(true);
        graphDizzy40.getViewport().setScrollableY(true);


        GridLabelRenderer gridLabelDizzy1 = graphDizzy40.getGridLabelRenderer();
        gridLabelDizzy1.setVerticalAxisTitle(getResources().getString(R.string.graph_dizzyLevel));
        gridLabelDizzy1.setHorizontalAxisTitle(getResources().getString(R.string.graph_sessionNumber));
        gridLabelDizzy1.setHorizontalAxisTitleColor(getResources().getColor(R.color.grey_600));
        gridLabelDizzy1.setVerticalAxisTitleColor(getResources().getColor(R.color.grey_600));
        gridLabelDizzy1.setVerticalLabelsColor(getResources().getColor(R.color.themeGradientPrimary));
        gridLabelDizzy1.setHorizontalLabelsColor(getResources().getColor(R.color.themeGradientPrimary));
        gridLabelDizzy1.setGridColor(getResources().getColor(R.color.themeGradientLight));



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
        seriesDizzy1.setOnDataPointTapListener(new OnDataPointTapListener() {
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

        series1.setOnDataPointTapListener(new OnDataPointTapListener() {
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
