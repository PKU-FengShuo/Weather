package com.example.fengshuo.weather;

import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.example.fengshuo.app.MyApplication;
import com.example.fengshuo.bean.City;
import com.example.fengshuo.util.NetUtil;

import java.util.List;

/**
 * Created by fengshuo on 2018/11/2.
 */

public class MyLocationListerner extends BDAbstractLocationListener {
    public String recity;
    public String cityCode;
    @Override
    public void onReceiveLocation(BDLocation location){
        //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
        //以下只列举部分获取地址相关的结果信息
        //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

        String addr = location.getAddrStr();    //获取详细地址信息
        String country = location.getCountry();    //获取国家
        String province = location.getProvince();    //获取省份
        String city = location.getCity();    //获取城市
        String district = location.getDistrict();    //获取区县
        String street = location.getStreet();    //获取街道信息
        Log.d("location_city",city);
        recity=city.replace("市","");

        List<City> mCityList;
        MyApplication myApplication;
        myApplication= MyApplication.getInstance();

        mCityList=myApplication.getCityList();

        for (City cityl:mCityList){
            if (cityl.getCity().equals(recity)){
                cityCode=cityl.getNumber();
                Log.d("location_code",cityCode);
            }
        }
    }
}
