package com.example.administrator.myweather;

//import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

//public class MainActivity extends ActionBarActivity {
public class MainActivity  extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getIntent().getIntExtra("from",0) !=1 ){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (preferences.getString("weather",null) != null){
                Intent intent = new Intent(this,WeatherActivity.class);
                startActivity(intent);
                finish();
            }
        }

    }
}
