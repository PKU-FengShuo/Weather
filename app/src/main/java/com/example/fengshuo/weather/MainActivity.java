package com.example.fengshuo.weather;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fengshuo.app.MyApplication;
import com.example.fengshuo.bean.City;
import com.example.fengshuo.bean.TodayWeather;
import com.example.fengshuo.util.NetUtil;
import com.example.fengshuo.weather.MyLocationListerner;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.Manifest;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;

import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import com.example.fengshuo.util.pageAdapter;


import static java.lang.System.exit;

/**
 * Created by fengshuo on 2018/10/4.
 */

public class MainActivity extends Activity implements View.OnClickListener,ViewPager.OnPageChangeListener{

    private static final int UPDATE_TODAY_WEATHER=1;
    private static final int DB=2;

    private ImageView mUpdateBtn,mTitleShare;

    private ImageView mTitleLocation;

    private ImageView mCitySelect;

    private TextView cityTv,timeTv,humidityTv,weekTv,pmDataTv,pmQualityTv,
            temperatureTv,climateTv,windTv,city_name_Tv,nowTv;
    private ImageView weatherImg,pmImg;

    private ProgressBar mUpdateProgress;

    private LocationManager locationManager;

    public LocationClient mLocationClient=null;
    private MyLocationListerner myListener=new MyLocationListerner();

    private TextView date1,temperature1,fengxiang1,climate1,
            date2,temperature2,fengxiang2,climate2,
            date3,temperature3,fengxiang3,climate3,
            date4,temperature4,fengxiang4,climate4;

    private ImageView weatherImg1,weatherImg2,weatherImg3,weatherImg4;
    private ImageView page1Img,page2Img;

    private ViewPager viewPager;
    private List<View> views;
    private pageAdapter pageadapter;

    private Handler mHandler=new Handler(){
      public void handleMessage(Message msg){
          switch (msg.what){
              case UPDATE_TODAY_WEATHER:
                  updateTodayWeather((TodayWeather) msg.obj);
                  break;
              default:
                  break;
          }
      }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        mUpdateBtn=(ImageView)findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        mTitleLocation=(ImageView)findViewById(R.id.title_location);
        mTitleLocation.setOnClickListener(this);

        if(NetUtil.getNetworkState(this)!=NetUtil.NETWORK_NONE){
            Log.d("myWeather","网络ok");
            //  Toast.makeText(MainActivity.this,"网络ok！",Toast.LENGTH_LONG).show();
        }else
        {
            Log.d("myWeather","网络挂了");
            Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
        }

        mCitySelect=(ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

        initView();
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,},1);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE,},1);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,},1);

        mLocationClient=new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数
        initLocation();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(0);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);
        //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，
        // 可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，
        // 设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    void initView(){
        city_name_Tv=(TextView)findViewById(R.id.title_city_name);
        cityTv=(TextView)findViewById(R.id.city);
        timeTv=(TextView)findViewById(R.id.time);
        humidityTv=(TextView)findViewById(R.id.humidity);
        weekTv=(TextView)findViewById(R.id.week_today);
        pmDataTv=(TextView)findViewById(R.id.pm_data);
        pmQualityTv=(TextView)findViewById(R.id.pm2_5_quality);
        pmImg=(ImageView)findViewById(R.id.pm2_5_img);
        temperatureTv=(TextView)findViewById(R.id.temperature);
        climateTv=(TextView)findViewById(R.id.climate);
        windTv=(TextView)findViewById(R.id.wind);
        weatherImg=(ImageView)findViewById(R.id.weather_img);
        nowTv=(TextView)findViewById(R.id.now_temperature);

        /**********************设置ViewPager页面*************************/
        viewPager=(ViewPager)findViewById(R.id.pages);
        View page1=LayoutInflater.from(this).inflate(R.layout.page1,null);
        View page2=LayoutInflater.from(this).inflate(R.layout.page2,null);
        views=new ArrayList<>();
        date1 = (TextView) page1.findViewById(R.id.date1);
        temperature1 = (TextView)page1.findViewById(R.id.temperature1);
        fengxiang1 = (TextView)page1.findViewById(R.id.fengxiang1);
        climate1 = (TextView)page1.findViewById(R.id.climate1);
        weatherImg1 = (ImageView)page1.findViewById(R.id.weather_img1);
        date2 = (TextView) page1.findViewById(R.id.date2);
        temperature2 = (TextView)page1.findViewById(R.id.temperature2);
        fengxiang2 = (TextView)page1.findViewById(R.id.fengxiang2);
        climate2 = (TextView)page1.findViewById(R.id.climate2);
        weatherImg2 = (ImageView)page1.findViewById(R.id.weather_img2);
        date3 = (TextView) page2.findViewById(R.id.date3);
        temperature3 = (TextView)page2.findViewById(R.id.temperature3);
        fengxiang3 = (TextView)page2.findViewById(R.id.fengxiang3);
        climate3 = (TextView)page2.findViewById(R.id.climate3);
        weatherImg3 = (ImageView)page2.findViewById(R.id.weather_img3);
        date4 = (TextView) page2.findViewById(R.id.date4);
        temperature4 = (TextView)page2.findViewById(R.id.temperature4);
        fengxiang4 = (TextView)page2.findViewById(R.id.fengxiang4);
        climate4 = (TextView)page2.findViewById(R.id.climate4);
        weatherImg4 = (ImageView)page2.findViewById(R.id.weather_img4);

        //设置适配器
        views.add(page1);
        views.add(page2);
        pageadapter=new pageAdapter(this,views);
        viewPager.setAdapter(pageadapter);
        page1Img=(ImageView)findViewById(R.id.page1Img);
        page2Img=(ImageView)findViewById(R.id.page2Img);
        viewPager.setOnPageChangeListener(this);

        /*****************************完********************************/

        SharedPreferences sharedPreferences=getSharedPreferences("config",MODE_PRIVATE);
        String city=sharedPreferences.getString("city","N/A");
        String updatetime=sharedPreferences.getString("updatetime","N/A");
        String wendu=sharedPreferences.getString("wendu","N/A");
        String shidu=sharedPreferences.getString("shidu","N/A");
        String pm25=sharedPreferences.getString("pm25","N/A");
        String quality=sharedPreferences.getString("quality","N/A");
        String fengxiang=sharedPreferences.getString("fengxiang","N/A");
        String fengli=sharedPreferences.getString("fengli","N/A");
        String date=sharedPreferences.getString("date","N/A");
        String high=sharedPreferences.getString("high","N/A");
        String low=sharedPreferences.getString("low","N/A");
        String type=sharedPreferences.getString("type","N/A");
        String date_1=sharedPreferences.getString("date1","N/A");
        String temperature_1=sharedPreferences.getString("temperature1","N/A");
        String fengxiang_1=sharedPreferences.getString("fengxiang1","N/A");
        String climate_1=sharedPreferences.getString("climate1","N/A");
        String date_2=sharedPreferences.getString("date2","N/A");
        String temperature_2=sharedPreferences.getString("temperature2","N/A");
        String fengxiang_2=sharedPreferences.getString("fengxiang2","N/A");
        String climate_2=sharedPreferences.getString("climate2","N/A");
        String date_3=sharedPreferences.getString("date3","N/A");
        String temperature_3=sharedPreferences.getString("temperature3","N/A");
        String fengxiang_3=sharedPreferences.getString("fengxiang3","N/A");
        String climate_3=sharedPreferences.getString("climate3","N/A");
        String date_4=sharedPreferences.getString("date4","N/A");
        String temperature_4=sharedPreferences.getString("temperature4","N/A");
        String fengxiang_4=sharedPreferences.getString("fengxiang4","N/A");
        String climate_4=sharedPreferences.getString("climate4","N/A");

        //设置N/A
        date1.setText(date_1);
        temperature1.setText(temperature_1);
        fengxiang1.setText(fengxiang_1);
        climate1.setText(climate_1);
        date2.setText(date_2);
        temperature2.setText(temperature_2);
        fengxiang2.setText(fengxiang_2);
        climate2.setText(climate_2);
        date3.setText(date_3);
        temperature3.setText(temperature_3);
        fengxiang3.setText(fengxiang_3);
        climate3.setText(climate_3);
        date4.setText(date_4);
        temperature4.setText(temperature_4);
        fengxiang4.setText(fengxiang_4);
        climate4.setText(climate_4);

        city_name_Tv.setText(city+"天气");
        cityTv.setText(city);
        timeTv.setText(updatetime+"发布");
        humidityTv.setText("湿度："+shidu);
        pmDataTv.setText(pm25);
        pmQualityTv.setText(quality);
        weekTv.setText(date);
        temperatureTv.setText(high+"~"+low);
        climateTv.setText(type);
        windTv.setText("风力："+fengli);
        nowTv.setText("温度："+wendu);
    }

    public void setUpdateProgress(){
        mUpdateBtn=(ImageView) findViewById(R.id.title_update_btn);
        mUpdateProgress=(ProgressBar)findViewById(R.id.title_update_progress);
        mTitleShare=(ImageView)findViewById(R.id.title_share);

        mUpdateBtn.setVisibility(View.GONE);

        RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)mTitleShare.getLayoutParams();
        params.addRule(RelativeLayout.LEFT_OF,R.id.title_update_progress);
        mTitleShare.setLayoutParams(params);

        mUpdateProgress.setVisibility(View.VISIBLE);
    }

    public void setUpdateBtn(){
        mUpdateBtn=(ImageView) findViewById(R.id.title_update_btn);
        mUpdateProgress=(ProgressBar)findViewById(R.id.title_update_progress);
        mUpdateBtn.setVisibility(View.VISIBLE);

        mTitleShare=(ImageView)findViewById(R.id.title_share);

        RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)mTitleShare.getLayoutParams();
        // params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.LEFT_OF,R.id.title_update_btn);
        mTitleShare.setLayoutParams(params);

        mUpdateProgress.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view){

        if(view.getId()==R.id.title_city_manager){
            Intent i=new Intent(this,SelectCity.class);
            //startActivity(i);
            startActivityForResult(i,1);
        }

        if (view.getId()==R.id.title_update_btn){

            setUpdateProgress();

            SharedPreferences sharedPreferences=getSharedPreferences("config",MODE_PRIVATE);
            String cityCode=sharedPreferences.getString("main_city_code","101010100");
            Log.d("myWeather",cityCode);


            if(NetUtil.getNetworkState(this)!=NetUtil.NETWORK_NONE){
                Log.d("myWeather","网络ok");
                queryWeatherCode(cityCode);
                //  Toast.makeText(MainActivity.this,"网络ok！",Toast.LENGTH_LONG).show();
            }else
            {
                Log.d("myWeather","网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }

        }
        if (view.getId()==R.id.title_location){
            setUpdateProgress();
            if (mLocationClient.isStarted()){
                mLocationClient.stop();
            }
            mLocationClient.start();

            final Handler BDHandler=new Handler(){
                public void handleMessage(Message msg){
                    switch (msg.what){
                        case DB:
                            if (msg.obj != null) {
                                if (NetUtil.getNetworkState(MainActivity.this) != NetUtil.NETWORK_NONE) {
                                    Log.d("myWeather", "网络ok");
                                    queryWeatherCode(myListener.cityCode);
                                    //  Toast.makeText(MainActivity.this,"网络ok！",Toast.LENGTH_LONG).show();
                                } else {
                                    Log.d("myWeather", "网络挂了");
                                    Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
                                }

                            }
                            myListener.cityCode=null;
                            break;
                        default:
                            break;
                    }
                }
            };

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (myListener.cityCode==null){
                            Thread.sleep(2000);
                        }
                        Message msg=new Message();
                        msg.what=DB;
                        msg.obj=myListener.cityCode;
                        BDHandler.sendMessage(msg);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();



           // getLocation();
            /***
             *
             *
             * 获取位置
             *
             *
             * ****/
        }
    }

    void getLocation(){
        try{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,},1);
            Log.d("locationcheck",Integer.toString(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)));
            Log.d("locationcheck",Integer.toString(PackageManager.PERMISSION_GRANTED));
            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                String serviceString = Context.LOCATION_SERVICE;
                LocationManager locationManager = (LocationManager) getSystemService(serviceString);

                String provider;

                List<String> providerList = locationManager.getProviders(true);
                if (providerList.contains(LocationManager.GPS_PROVIDER)) {
                    provider = LocationManager.GPS_PROVIDER;
                } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
                    provider = LocationManager.NETWORK_PROVIDER;
                } else {
                    Toast.makeText(this, "No Location provider to use", Toast.LENGTH_SHORT).show();
                    return;
                }

                Location location = locationManager.getLastKnownLocation(provider);

                Double lat;
                Double lng;

                Log.d("temp_location", Double.toString(location.getLatitude()));
                lat = location.getLatitude();//纬度
                lng = location.getLongitude();//经度


                String addressStr = "no address \n";
                getLocationCity(lat,lng);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void getLocationCity(final Double lat,final Double lng){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                    StringBuilder sb = new StringBuilder();
                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            sb.append(address.getAddressLine(i)).append(" ");
                        }
                        sb.append(address.getCountryName());

                        Log.i("location", "国家" + address.getCountryName());
                        Log.i("location", "address.getAddressLine(0) :" + address.getAddressLine(0));
                        Log.i("location", "address.getAddressLine(1) :" + address.getAddressLine(1));
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1);
                    }
                } catch (Exception e) {
                    Log.d("location", "geocoder is wrong");
                    e.printStackTrace();
                }
            }
        }).start();
    }


    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if (requestCode==1&&resultCode==RESULT_OK){
            String newCityCode=data.getStringExtra("cityCode");
            Log.d("myWeather","选择的城市代码为"+newCityCode);
            if (NetUtil.getNetworkState(this)!=NetUtil.NETWORK_NONE){
                Log.d("myWeather","网络OK");
                queryWeatherCode(newCityCode);
            }else {
                Log.d("myWeather","网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * @param cityCode
     */
    public void queryWeatherCode(String cityCode){
        final String address="http://wthrcdn.etouch.cn/WeatherApi?citykey="+cityCode;
        Log.d("myWeather",address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con=null;
                TodayWeather todayWeather=null;
                try{
                    URL url=new URL(address);
                    con=(HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in=con.getInputStream();
                    BufferedReader reader=new BufferedReader(new InputStreamReader(in));
                    StringBuilder response=new StringBuilder();
                    String str;
                    while ((str=reader.readLine())!=null){
                        response.append(str);
                        Log.d("myWeather",str);
                    }
                    String responseStr=response.toString();
                    Log.d("myWeather",responseStr);
                    todayWeather=parseXML(responseStr);
                    if (todayWeather!=null){
                        Log.d("myWeather",todayWeather.toString());

                        Message msg=new Message();
                        msg.what=UPDATE_TODAY_WEATHER;
                        msg.obj=todayWeather;
                        mHandler.sendMessage(msg);

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if (con!=null){
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather=null;
        int fengxiangCount=0;
        int fengliCount =0;
        int dateCount=0;
        int highCount =0;
        int lowCount=0;
        int typeCount =0;
        try {
            XmlPullParserFactory fac=XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser=fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType=xmlPullParser.getEventType();
            Log.d("myWeather","parseXML");
            int j=0;
            while (eventType!=XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        switch (xmlPullParser.getName()){
                            case "resp":
                                todayWeather=new TodayWeather();
                                break;
                            case "city":
                                eventType=xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                                break;
                            case "updatetime":
                                eventType=xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                                break;
                            case "wendu":
                                eventType=xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                                break;
                            case "fengli":
                                if (fengliCount==0){
                                    eventType=xmlPullParser.next();
                                    todayWeather.setFengli(xmlPullParser.getText());
                                }
                                fengliCount++;
                                break;
                            case "shidu":
                                eventType=xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                                break;
                            case "fengxiang":
                                eventType=xmlPullParser.next();
                                if (fengxiangCount==0){
                                    todayWeather.setFengxiang(xmlPullParser.getText());
                                }
                                else if(fengxiangCount==1){
                                    todayWeather.setTomorrow1_fengxiang(xmlPullParser.getText());
                                }
                                else if(fengxiangCount==2){
                                    todayWeather.setTomorrow2_fengxiang(xmlPullParser.getText());
                                }
                                else if(fengxiangCount==3){
                                    todayWeather.setTomorrow3_fengxiang(xmlPullParser.getText());
                                }
                                else if(fengxiangCount==4){
                                    todayWeather.setTomorrow4_fengxiang(xmlPullParser.getText());
                                }
                                fengxiangCount++;
                                break;
                            case "pm25":
                                eventType=xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                                break;
                            case "quality":
                                eventType=xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                                break;
                            case "date":
                                eventType=xmlPullParser.next();
                                if (dateCount==0){
                                    todayWeather.setDate(xmlPullParser.getText());
                                }
                                else if(dateCount==1){
                                    todayWeather.setTomorrow1_date(xmlPullParser.getText());
                                }
                                else if(dateCount==2){
                                    todayWeather.setTomorrow2_date(xmlPullParser.getText());
                                }
                                else if(dateCount==3){
                                    todayWeather.setTomorrow3_date(xmlPullParser.getText());
                                }
                                else if(dateCount==4){
                                    todayWeather.setTomorrow4_date(xmlPullParser.getText());
                                }
                                dateCount++;
                                break;
                            case "high":
                                eventType=xmlPullParser.next();
                                if (highCount==0){
                                    todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                }
                                else if(highCount==1){
                                    todayWeather.setTomorrow1_high(xmlPullParser.getText().substring(2).trim());
                                }
                                else if(highCount==2){
                                    todayWeather.setTomorrow2_high(xmlPullParser.getText().substring(2).trim());
                                }
                                else if(highCount==3){
                                    todayWeather.setTomorrow3_high(xmlPullParser.getText().substring(2).trim());
                                }
                                else if(highCount==4){
                                    todayWeather.setTomorrow4_high(xmlPullParser.getText().substring(2).trim());
                                }
                                highCount++;
                                break;
                            case "low":
                                eventType=xmlPullParser.next();
                                if (lowCount==0){
                                    todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                }
                                else if(lowCount==1){
                                    todayWeather.setTomorrow1_low(xmlPullParser.getText().substring(2).trim());
                                }
                                else if(lowCount==2){
                                    todayWeather.setTomorrow2_low(xmlPullParser.getText().substring(2).trim());
                                }
                                else if(lowCount==3){
                                    todayWeather.setTomorrow3_low(xmlPullParser.getText().substring(2).trim());
                                }
                                else if(lowCount==4){
                                    todayWeather.setTomorrow4_low(xmlPullParser.getText().substring(2).trim());
                                }
                                lowCount++;
                                break;
                            case "type":
                                eventType=xmlPullParser.next();
                                if (typeCount==0){
                                    todayWeather.setType(xmlPullParser.getText());
                                }
                                else if(typeCount==1){
                                    todayWeather.setTomorrow1_type(xmlPullParser.getText());
                                }
                                else if(typeCount==2){
                                    todayWeather.setTomorrow2_type(xmlPullParser.getText());
                                }
                                else if(typeCount==3){
                                    todayWeather.setTomorrow3_type(xmlPullParser.getText());
                                }
                                else if(typeCount==4){
                                    todayWeather.setTomorrow4_type(xmlPullParser.getText());
                                }
                                typeCount++;
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType=xmlPullParser.next();
            }
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return todayWeather;
    }

    void weatherImage(String climate,ImageView weatherImg){
        if(climate.equals("暴雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
        if(climate.equals("暴雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
        if(climate.equals("大暴雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
        if(climate.equals("大雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
        if(climate.equals("大雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
        if(climate.equals("多云"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
        if(climate.equals("雷阵雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
        if(climate.equals("雷阵雨冰雹"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
        if(climate.equals("晴"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
        if(climate.equals("沙尘暴"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
        if(climate.equals("特大暴雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
        if(climate.equals("雾"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
        if(climate.equals("小雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
        if(climate.equals("小雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
        if(climate.equals("阴"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
        if(climate.equals("雨夹雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
        if(climate.equals("阵雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
        if(climate.equals("阵雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
        if(climate.equals("中雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
        if(climate.equals("中雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
    }

    void updateTodayWeather(TodayWeather todayWeather){
        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+"发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        nowTv.setText("温度："+todayWeather.getWendu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力："+todayWeather.getFengli());

        if(todayWeather.getPm25()!=null){
            int pm2_5=Integer.parseInt(todayWeather.getPm25());
            if(pm2_5<=50) pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
            if(pm2_5>50&&pm2_5<=100) pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
            if(pm2_5>100&&pm2_5<=150) pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
            if(pm2_5>150&&pm2_5<=200) pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
            if(pm2_5>200&&pm2_5<=300) pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
            if(pm2_5>300) pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
        }

        weatherImage(todayWeather.getType(),weatherImg);
        weatherImage(todayWeather.getTomorrow1_type(),weatherImg1);
        weatherImage(todayWeather.getTomorrow2_type(),weatherImg2);
        weatherImage(todayWeather.getTomorrow3_type(),weatherImg3);
        weatherImage(todayWeather.getTomorrow4_type(),weatherImg4);

        //--------------------更新ViewPager--------------------

        date1.setText(todayWeather.getTomorrow1_date());
        temperature1.setText(todayWeather.getTomorrow1_high()+"~"+todayWeather.getTomorrow1_low());
        fengxiang1.setText(todayWeather.getTomorrow1_fengxiang());
        climate1.setText(todayWeather.getTomorrow1_type());

        date2.setText(todayWeather.getTomorrow2_date());
        temperature2.setText(todayWeather.getTomorrow2_high()+"~"+todayWeather.getTomorrow2_low());
        fengxiang2.setText(todayWeather.getTomorrow2_fengxiang());
        climate2.setText(todayWeather.getTomorrow2_type());

        date3.setText(todayWeather.getTomorrow3_date());
        temperature3.setText(todayWeather.getTomorrow3_high()+"~"+todayWeather.getTomorrow3_low());
        fengxiang3.setText(todayWeather.getTomorrow3_fengxiang());
        climate3.setText(todayWeather.getTomorrow3_type());

        date4.setText(todayWeather.getTomorrow4_date());
        temperature4.setText(todayWeather.getTomorrow4_high()+"~"+todayWeather.getTomorrow4_low());
        fengxiang4.setText(todayWeather.getTomorrow4_fengxiang());
        climate4.setText(todayWeather.getTomorrow4_type());
        //--------------------更新ViewPager--------------------

        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();

        SharedPreferences settings
                = (SharedPreferences)getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("city",todayWeather.getCity());
        editor.putString("updatetime",todayWeather.getUpdatetime());
        editor.putString("wendu",todayWeather.getWendu());
        editor.putString("shidu",todayWeather.getShidu());
        editor.putString("pm25",todayWeather.getPm25());
        editor.putString("quality",todayWeather.getQuality());
        editor.putString("fengxiang",todayWeather.getFengxiang());
        editor.putString("fengli",todayWeather.getFengli());
        editor.putString("date",todayWeather.getDate());
        editor.putString("high",todayWeather.getHigh());
        editor.putString("low",todayWeather.getLow());
        editor.putString("type",todayWeather.getType());

        editor.putString("date1",todayWeather.getTomorrow1_date());
        editor.putString("temperature1",todayWeather.getTomorrow1_high()+"~"+todayWeather.getTomorrow1_low());
        editor.putString("fengxiang1",todayWeather.getTomorrow1_fengxiang());
        editor.putString("climate1",todayWeather.getTomorrow1_type());

        editor.putString("date2",todayWeather.getTomorrow2_date());
        editor.putString("temperature2",todayWeather.getTomorrow2_high()+"~"+todayWeather.getTomorrow2_low());
        editor.putString("fengxiang2",todayWeather.getTomorrow2_fengxiang());
        editor.putString("climate2",todayWeather.getTomorrow2_type());

        editor.putString("date3",todayWeather.getTomorrow3_date());
        editor.putString("temperature3",todayWeather.getTomorrow3_high()+"~"+todayWeather.getTomorrow3_low());
        editor.putString("fengxiang3",todayWeather.getTomorrow3_fengxiang());
        editor.putString("climate3",todayWeather.getTomorrow3_type());

        editor.putString("date4",todayWeather.getTomorrow4_date());
        editor.putString("temperature4",todayWeather.getTomorrow4_high()+"~"+todayWeather.getTomorrow4_low());
        editor.putString("fengxiang4",todayWeather.getTomorrow4_fengxiang());
        editor.putString("climate4",todayWeather.getTomorrow4_type());

        editor.commit();

        setUpdateBtn();
    }


    @Override
    public void onPageScrolled(int i,float v,int i1){

    }

    @Override
    public void onPageSelected(int i){
        if(i==0){
            page1Img.setImageResource(R.drawable.point_enable);
            page2Img.setImageResource(R.drawable.point_disable);
        }else {
            page1Img.setImageResource(R.drawable.point_disable);
            page2Img.setImageResource(R.drawable.point_enable);
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

}