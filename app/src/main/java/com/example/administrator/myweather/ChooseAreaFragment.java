package com.example.administrator.myweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.myweather.db.City;
import com.example.administrator.myweather.db.County;
import com.example.administrator.myweather.db.Province;
import com.example.administrator.myweather.util.HttpUtil;
import com.example.administrator.myweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY =2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;
    //选中的省份
    private Province selectedProvince;
    //选中的城市
    private City selectedCity;
    //选中的县
    private County selectedCounty;
    //当前选中的级别
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView)view.findViewById(R.id.title_text);
        backButton = (Button)view.findViewById(R.id.back_btn);
        listView = (ListView)view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (currentLevel){
                    case LEVEL_PROVINCE:
                        selectedProvince = provinceList.get(position);
                        queryCities();
                        break;
                    case LEVEL_CITY:
                        selectedCity = cityList.get(position);
                        queryCounties();
                        break;
                    case LEVEL_COUNTY:
                        selectedCounty = countyList.get(position);
                        if (getActivity() instanceof MainActivity){
                            Toast.makeText(getContext(),selectedCounty.getCountryName(),Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(),WeatherActivity.class);
                            intent.putExtra("County_data",selectedCounty);
                            startActivity(intent);
                            getActivity().finish();
                        }else if (getActivity() instanceof WeatherActivity){
                         WeatherActivity  activity = (WeatherActivity) getActivity();
                         activity.drawerLayout.closeDrawers();
                         activity.swipeRefreshLayout.setRefreshing(true);
                         activity.requestWeather(selectedCounty.getWeatherId());
                        }
                    default:
                        break;
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    queryProvince();
                }
            }
        });
        queryProvince();
    }
/**
 * 全国的省份数据，优先从数据库获取
* */
    private void queryProvince() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);

        if (DataSupport.findAll(Province.class)!= null){
            provinceList = DataSupport.findAll(Province.class);
            if (provinceList.size()>0) {
                dataList.clear();
                for (Province province : provinceList) {
                    dataList.add(province.getProvinceName());
                }
                adapter.notifyDataSetChanged();
//                listView.setSelection(0);
                currentLevel = LEVEL_PROVINCE;
            }else {
                String address = "http://guolin.tech/api/china";
                queryFromService(address,"province");
            }
        }else {
            String address = "http://guolin.tech/api/china";
            queryFromService(address,"province");
        }
    }

    /**
     * 市里的县数据，优先从数据库获取
     * */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        if (DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class)!= null){
            countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
            if (countyList.size()>0){
                dataList.clear();
                for (County county: countyList){
//                    if (!county.getCountryName().equals(selectedCity.getCityName())){
                        dataList.add(county.getCountryName());
//                    }
                }
                adapter.notifyDataSetChanged();
//                listView.setSelection(0);
                currentLevel = LEVEL_COUNTY;
            }else {
                int provinceCode = selectedProvince.getProvinceCode();
                int cityCode = selectedCity.getCityCode();
                String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
                queryFromService(address,"county");
            }
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromService(address,"county");
        }

    }
    /**
     * 全省的城市数据，优先从数据库获取
     * */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        if (DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class) != null){
            cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
            if (cityList.size()>0){
                dataList.clear();
                for (City city: cityList){
//                    if (!city.getCityName().equals(selectedProvince.getProvinceName())){
                        dataList.add(city.getCityName());
//                    }
                }
                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                currentLevel = LEVEL_CITY;
            }else {
                int provinceCode = selectedProvince.getProvinceCode();
                String address = "http://guolin.tech/api/china/"+provinceCode;
                queryFromService(address,"city");
            }
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromService(address,"city");
        }

    }
    /**
     * 根据传入的地址和类型查询
     * */
    private void queryFromService(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvince();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("city".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"load failed",Toast.LENGTH_LONG).show();
                    }
                });
            }


        });
    }

    private void closeProgressDialog() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("loading...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
    }
}
