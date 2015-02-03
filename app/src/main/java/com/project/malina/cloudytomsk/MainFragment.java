package com.project.malina.cloudytomsk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;


public class MainFragment extends Fragment {

   private TextView mTempText;
   private TextView mWarningText;
   private TextView mDegreeText;
   private ProgressWheel mProgressWheel;
        public MainFragment() {
        }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void getData(){
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWeather().execute();
        } else {
            mWarningText.setText(getResources().getText(R.string.no_connection));
        }
    }

    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            mTempText = (TextView) rootView.findViewById(R.id.temp_text);
            mWarningText = (TextView) rootView.findViewById(R.id.warning_text);
            mDegreeText = (TextView) rootView.findViewById(R.id.degree);

            // Making cool progressbar - thanks to github.com/Todd-Davies
            mProgressWheel = (ProgressWheel) rootView.findViewById(R.id.progressBar);
            ShapeDrawable bg = new ShapeDrawable(new RectShape());
            int[] pixels = new int[] { 0xFF2E9121, 0xFF2E9121, 0xFF2E9121,
                0xFF2E9121, 0xFF2E9121, 0xFF2E9121, 0xFFFFFFFF, 0xFFFFFFFF};
            Bitmap bm = Bitmap.createBitmap(pixels, 8, 1, Bitmap.Config.ARGB_8888);
            Shader shader = new BitmapShader(bm,
                Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            mProgressWheel.setRimShader(shader);

            // Making nice button - thanks to github.com/hoang8f
            FButton mRefreshButton = (FButton) rootView.findViewById(R.id.refresh_button);
            mRefreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getData();
                }
            });
            getData();
            return rootView;
        }

    private class DownloadWeather extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressWheel.setVisibility(View.VISIBLE);
            mDegreeText.setVisibility(View.INVISIBLE);
            mTempText.setText(null);
            mWarningText.setText(null);
            mProgressWheel.spin();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect("http://m.pogodavtomske.ru/").get();
                Element temperature = doc.select("div.flr.yellow44").get(1);
                return temperature.text();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressWheel.setVisibility(View.INVISIBLE);
            mTempText.setText(result);
            if (result == null){
                mWarningText.setText(getResources().getText(R.string.push_refresh));
            } else {
                mDegreeText.setVisibility(View.VISIBLE);
                mWarningText.setText(null);
            }
        }
    }

}
