package com.example.coolweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ViewStubCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.coolweather.service.AutoUpdateService;
import com.suke.widget.SwitchButton;

public class SettingActivity extends AppCompatActivity {

    private SwitchButton switchButton;

    private ImageView moreImage;

    private LinearLayout linearLayout;

    private Button backButton;

    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);//将系统栏设置为透明色
        }

        switchButton=(SwitchButton) findViewById(R.id.switch_button);
        moreImage=(ImageView) findViewById(R.id.more_image);
        linearLayout=(LinearLayout) findViewById(R.id.more_button);
        backButton=(Button) findViewById(R.id.back_button);
        radioGroup=(RadioGroup) findViewById(R.id.radio_group);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(SettingActivity.this).edit();
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);
        boolean isStatus=preferences.getBoolean("isStatus",false);
        switchButton.setChecked(isStatus);
        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if(isChecked==true){
                    Intent intent=new Intent(SettingActivity.this,AutoUpdateService.class);
                    startService(intent);
                    editor.putBoolean("isStatus",true);
                    editor.apply();
                    Toast.makeText(SettingActivity.this,"已开启",Toast.LENGTH_SHORT).show();
                }else if(isChecked==false){
                    Intent intent=new Intent(SettingActivity.this,AutoUpdateService.class);
                    stopService(intent);
                    editor.putBoolean("isStatus",false);
                    editor.apply();
                    Toast.makeText(SettingActivity.this,"已关闭",Toast.LENGTH_SHORT).show();
                }
            }
        });

        moreImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(moreImage.getDrawable().getCurrent().getConstantState().equals(ContextCompat.getDrawable(SettingActivity.this,R.drawable.many).getConstantState())){
                    moreImage.setImageResource(R.drawable.less);
                    linearLayout.setVisibility(View.VISIBLE);
                }else {
                    moreImage.setImageResource(R.drawable.many);
                    linearLayout.setVisibility(View.GONE);
                }
            }
        });

        int item=preferences.getInt("time",8*60*60*1000);
        switch (item){
            case 28800000:
                radioGroup.check(R.id.eight_time);
                break;
            case 14400000:
                radioGroup.check(R.id.four_time);
                break;
            case 7200000:
                radioGroup.check(R.id.two_time);
                break;
            case 3600000:
                radioGroup.check(R.id.one_time);
                break;
            default:
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.eight_time:
                        editor.putInt("time",8*60*60*1000);
                        editor.apply();
                        AutoUpdateService.anHour=8*60*60*1000;
                        break;
                    case R.id.four_time:
                        editor.putInt("time",4*60*60*1000);
                        editor.apply();
                        AutoUpdateService.anHour=4*60*60*1000;
                        break;
                    case R.id.two_time:
                        editor.putInt("time",2*60*60*1000);
                        editor.apply();
                        AutoUpdateService.anHour=2*60*60*1000;
                        break;
                    case R.id.one_time:
                        editor.putInt("time",60*60*1000);
                        editor.apply();
                        AutoUpdateService.anHour=60*60*1000;
                        break;
                    default:
                }
            }
        });

    }
}