package com.example.coolweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import com.example.coolweather.db.SavedCounty;
import com.example.coolweather.util.OnItemClickListener;
import com.example.coolweather.util.Saved;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class SaveActivity extends AppCompatActivity {


    List<Saved> nameList=new ArrayList<>();
    List<SavedCounty> countryList=new ArrayList<>();
    OnItemClickListener recyclerListener=new OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            String name=nameList.get(position).getCountryName();
            String weatherId=null;
            for(SavedCounty savedCounty:countryList){
                if(name.equals(savedCounty.getCountyName())){
                    weatherId=savedCounty.getWeatherId();
                }
            }
            SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(SaveActivity.this).edit();
            editor.putString("weather",null);
            editor.apply();
            Intent intent=new Intent(SaveActivity.this,WeatherActivity.class);
            intent.putExtra("weather_id",weatherId);
            startActivity(intent);
            ActivityManager manager=(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);//将系统栏设置为透明色
        }

        Button backButton=(Button) findViewById(R.id.back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        initCounty();
        RecyclerView recyclerView=(RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        final MyAdapter myAdapter=new MyAdapter(nameList,recyclerListener);
        recyclerView.setAdapter(myAdapter);

        ItemTouchHelper helper=new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP|ItemTouchHelper.DOWN;//拖拽
                int swipeFlags = ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;//侧滑删除
                return makeMovementFlags(dragFlags,swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                LitePal.deleteAll("SavedCounty","countyName = ?",nameList.get(viewHolder.getAdapterPosition()).getCountryName());
                nameList.remove(viewHolder.getAdapterPosition());
                myAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        });
        helper.attachToRecyclerView(recyclerView);
    }

    private void initCounty(){
        countryList= LitePal.findAll(SavedCounty.class);
        for(SavedCounty savedCounty:countryList){
            Saved saved=new Saved(savedCounty.getCountyName());
            nameList.add(saved);
        }
    }
}