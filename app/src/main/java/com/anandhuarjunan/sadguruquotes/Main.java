package com.anandhuarjunan.sadguruquotes;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog;
import com.github.dhaval2404.colorpicker.listener.ColorListener;
import com.github.dhaval2404.colorpicker.model.ColorShape;
import com.github.dhaval2404.colorpicker.model.ColorSwatch;

import org.jetbrains.annotations.NotNull;

public class Main extends AppCompatActivity {

    Button button,button2,battery = null;
    EditText editText = null;
    public static final String MyPREFERENCES = "MyPrefs" ;
    ProgressBar progressBar = null;
    RadioGroup radioGroup = null;
    RadioButton everyDayRd = null;
    RadioButton oneMinRd = null;
    RadioButton oneHourRd = null;

    RadioGroup actionWidget = null;
    RadioButton widgetConfig = null;
    RadioButton changeQuote = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        editText = findViewById(R.id.editTextTextPersonName);
        progressBar = findViewById(R.id.progressBar);
        radioGroup = findViewById(R.id.radioGroup);
        everyDayRd = findViewById(R.id.everyDayRd);
        oneHourRd = findViewById(R.id.oneHourRd);
        oneMinRd = findViewById(R.id.oneMinRd);
        actionWidget = findViewById(R.id.widgetAction);
        widgetConfig = findViewById(R.id.widgetConfig);
        changeQuote = findViewById(R.id.changeQuotes);
        battery = findViewById(R.id.battery);

        editText.setText(sharedpreferences.getString("sync_url","https://sadguruquotes.000webhostapp.com/quotes.json"));
        button.setOnClickListener(view -> {
               SharedPreferences.Editor editor = sharedpreferences.edit();
               editor.putString("sync_url", editText.getText().toString());
               editor.apply();
                QuoteRetriever quoteRetriever = new QuoteRetriever(progressBar,getApplicationContext(),editText.getText().toString());
                quoteRetriever.execute();
        });


        updateFrequency(sharedpreferences);
        updateWidgetAction(sharedpreferences);


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                RadioButton checkedRadioButton = (RadioButton)group.findViewById(checkedId);
                boolean isChecked = checkedRadioButton.isChecked();
                if (isChecked)
                {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putLong("frequency", getPeriodInMilisecond(checkedRadioButton.getId()));
                    editor.apply();
                    Toast.makeText(Main.this, "Add the widget as fresh to take effect.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        actionWidget.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                RadioButton checkedRadioButton = (RadioButton)group.findViewById(checkedId);
                boolean isChecked = checkedRadioButton.isChecked();
                if (isChecked)
                {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt("widgetAction", checkedRadioButton.getId());
                    editor.apply();
                    Toast.makeText(Main.this, "Add the widget as fresh to take effect.", Toast.LENGTH_SHORT).show();


                }
            }
        });


        button2.setOnClickListener(view -> {
            new MaterialColorPickerDialog
                    .Builder(this)
                    .setTitle("Pick Theme")
                    .setColorShape(ColorShape.SQAURE)
                    .setColorSwatch(ColorSwatch._300)
                    .setColorListener(new ColorListener() {
                        @Override
                        public void onColorSelected(int color, @NotNull String colorHex) {
                            updateWidgetColour(color);
                        }
                    })
                    .show();
        });

        battery.setOnClickListener(view->{
             Intent intent = new Intent();
             intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
             startActivity(intent);
        });


    }

    private void updateWidgetAction(SharedPreferences sharedpreferences) {
        int widgetAction = getApplicationContext().getSharedPreferences("MyPrefs",Context.MODE_PRIVATE).getInt("widgetAction",R.id.widgetConfig);
        RadioButton radioButton = actionWidget.findViewById(widgetAction);
        radioButton.setChecked(true);
    }


    private long getPeriodInMilisecond(int id) {
        if(R.id.oneMinRd == id){
            return 60000;
        }
       else if(R.id.everyDayRd == id){
            return 86400000;
        }
       else if(R.id.oneHourRd == id){
            return 3600000;
        }
       return 60000;
    }

private void updateFrequency(SharedPreferences sharedPreferences){
    long currentFreq  = sharedPreferences.getLong("frequency",60000);
    if(60000 == currentFreq){
        oneMinRd.setChecked(true);

    }
    else if(86400000 == currentFreq){
        everyDayRd.setChecked(true);

    }
    else if(3600000 == currentFreq){
        oneHourRd.setChecked(true);
    }

}
    private void updateWidgetColour(int color){
        Context context = getApplicationContext();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.quotes);
        ComponentName thisWidget = new ComponentName(context, QuotesWidget.class);
        remoteViews.setTextColor(R.id.appwidget_text,color);
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }
}