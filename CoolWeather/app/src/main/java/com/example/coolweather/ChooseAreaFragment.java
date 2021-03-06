package com.example.coolweather;

import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;
    private ImageView whereButton;
    public FrameLayout.LayoutParams layoutParams;
    public EditText editText;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        layoutParams=(FrameLayout.LayoutParams) view.findViewById(R.id.choose_layout).getLayoutParams();
        titleText=(TextView) view.findViewById(R.id.title_text);
        backButton=(Button) view.findViewById(R.id.back_button);
        listView=(ListView) view.findViewById(R.id.list_view);
        whereButton=(ImageView) view.findViewById(R.id.where_button);
        editText=(EditText) view.findViewById(R.id.where_name);
        adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);

        if(getActivity() instanceof WeatherActivity){
            //?????????????????????margin??????
            layoutParams.topMargin=60;
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(currentLevel==LEVEL_PROVINCE){
                    provinceList=LitePal.findAll(Province.class);
                    dataList.clear();
                    for(Province province:provinceList){
                        dataList.add(province.getProvinceName());
                    }
                    adapter.notifyDataSetChanged();
                    final String str=editText.getText().toString();
                    for(int i=0;i<dataList.size();i++){
                        if(dataList.get(i).indexOf(str)==-1){
                            provinceList.remove(i);
                            dataList.remove(i);
                            i--;
                        }
                    }
                    adapter.notifyDataSetChanged();
                }else if(currentLevel==LEVEL_CITY){
                    cityList= LitePal.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
                    dataList.clear();
                    for(City city:cityList){
                        dataList.add(city.getCityName());
                    }
                    adapter.notifyDataSetChanged();
                    final String str=editText.getText().toString();
                    for(int i=0;i<dataList.size();i++){
                        if(dataList.get(i).indexOf(str)==-1){
                            cityList.remove(i);
                            dataList.remove(i);
                            i--;
                        }
                    }
                    adapter.notifyDataSetChanged();
                }else if(currentLevel==LEVEL_COUNTY){
                    countyList=LitePal.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
                    dataList.clear();
                    for(County county:countyList){
                        dataList.add(county.getCountyName());
                    }
                    adapter.notifyDataSetChanged();
                    final String str=editText.getText().toString();
                    for(int i=0;i<dataList.size();i++){
                        if(dataList.get(i).indexOf(str)==-1){
                            countyList.remove(i);
                            dataList.remove(i);
                            i--;
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(i);
                    editText.setText("");
                    editText.setHint("??????????????????");
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(i);
                    editText.setText("");
                    editText.setHint("??????????????????");
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    editText.setText("");
                    String weatherId=countyList.get(i).getWeatherId();
                    if(getActivity() instanceof MainActivity){
                        Intent intent=new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof  WeatherActivity){
                        WeatherActivity activity=(WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                        FragmentManager manager=getActivity().getSupportFragmentManager();
                        manager.popBackStack();
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    queryProvinces();
                }else if(getActivity() instanceof WeatherActivity&&currentLevel==LEVEL_PROVINCE){
                    //???????????????????????????????????????
                    FragmentManager manager=getActivity().getSupportFragmentManager();
                    manager.popBackStack();
                }else if(getActivity() instanceof MainActivity&&currentLevel==LEVEL_PROVINCE){
                    getActivity().finish();
                }
            }
        });
        queryProvinces();
        whereButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(),LocationActivity.class);
                startActivity(intent);
            }
        });
    }

    private void queryProvinces(){
        titleText.setText("??????");
        if(getActivity() instanceof WeatherActivity){
            backButton.setVisibility(View.VISIBLE);
        }else if(getActivity() instanceof  MainActivity){
            backButton.setVisibility(View.VISIBLE);
        }
        provinceList= LitePal.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);//??????ListView
            currentLevel=LEVEL_PROVINCE;
        }else {
            String address="http://guolin.tech/api/china";
            queryFromService(address,"province");
        }
    }

    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList= LitePal.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/" + provinceCode;
            queryFromService(address,"city");
        }
    }

    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=LitePal.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromService(address,"county");
        }
    }

    private void queryFromService(String address,final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {//???????????????????????????
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"????????????",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {//???????????????????????????
                String responseText=response.body().string();
                boolean result=false;
                if("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("county".equals(type)){
                    result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("????????????...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
