package com.anandhuarjunan.sadguruquotes;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class QuoteRetriever extends AsyncTask<Void, Void, Boolean>  {
    @SuppressLint("StaticFieldLeak")
    private Context context = null;
    private String quoteUrl = null;
    @SuppressLint("StaticFieldLeak")
    private ProgressBar progressBar = null;
    public QuoteRetriever(ProgressBar progressBar,Context context,String url){
        this.context = context;
        this.quoteUrl = url;
        this.progressBar = progressBar;
    }

   public boolean sync(){
      QuoteLocalDatabase quoteLocalDatabase = new QuoteLocalDatabase(context);
       try{
           StringBuilder resultString = new StringBuilder();
       URL url = new URL(quoteUrl);
       HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
       myConnection.setRequestMethod("GET");
       if (myConnection.getResponseCode()==200) {
           String line;
           BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));
           while ((line = bufferedReader.readLine()) != null) {
               resultString.append(line).append(System.getProperty("line.separator"));
           }
           try {
               JSONObject jsonObject = new JSONObject(resultString.toString());
               JSONArray jsonArray = (JSONArray) jsonObject.get("quotes");

               for(int i=0;i<jsonArray.length();i++){
                   JSONObject quote = jsonArray.getJSONObject(i);
                   quoteLocalDatabase.insertQuoteInDb(Integer.parseInt(quote.getString("id")),quote.getString("quote"));
               }
               return true;
           } catch (JSONException e) {
               e.printStackTrace();
           }

       }
   } catch (IOException e) {
          return false;
    }
       return false;
   }

    @Override
    protected Boolean doInBackground(Void... voids) {
      return  sync();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(result){
            super.onPostExecute(result);
            Toast.makeText(context,"Syncing Completed.",Toast.LENGTH_SHORT).show();
            QuoteLocalDatabase quoteLocalDatabase = new QuoteLocalDatabase(context);
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, QuotesWidget.class);
            QuotesWidget.updateAppWidget(context,mgr,mgr.getAppWidgetIds(thisWidget),quoteLocalDatabase.fetchQuote());
            progressBar.setVisibility(View.GONE);
        }else{
            Toast.makeText(context,"Failed to Sync.",Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }


    }
}
