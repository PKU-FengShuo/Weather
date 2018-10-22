package com.example.fengshuo.weather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fengshuo.bean.TodayWeather;
import com.example.fengshuo.util.NetUtil;

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

/**
 * Created by fengshuo on 2018/10/4.
 */

public class MainActivity extends Activity implements View.OnClickListener{

    private static final int UPDATE_TODAY_WEATHER=1;

    private ImageView mUpdateBtn;

    private ImageView mCitySelect;

    private TextView cityTv,timeTv,humidityTv,weekTv,pmDataTv,pmQualityTv,
            temperatureTv,climateTv,windTv,city_name_Tv,nowTv;
    private ImageView weatherImg,pmImg;

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

    @Override
    public void onClick(View view){

        if(view.getId()==R.id.title_city_manager){
            Intent i=new Intent(this,SelectCity.class);
            //startActivity(i);
            startActivityForResult(i,1);
        }

        if (view.getId()==R.id.title_update_btn){
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
    private void queryWeatherCode(String cityCode){
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
                                if (fengxiangCount==0){
                                    eventType=xmlPullParser.next();
                                    todayWeather.setFengxiang(xmlPullParser.getText());
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
                                if (dateCount==0){
                                    eventType=xmlPullParser.next();
                                    todayWeather.setDate(xmlPullParser.getText());
                                }
                                dateCount++;
                                break;
                            case "high":
                                if (highCount==0){
                                    eventType=xmlPullParser.next();
                                    todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                }
                                highCount++;
                                break;
                            case "low":
                                if (lowCount==0){
                                    eventType=xmlPullParser.next();
                                    todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                }
                                lowCount++;
                                break;
                            case "type":
                                if (typeCount==0){
                                    eventType=xmlPullParser.next();
                                    todayWeather.setType(xmlPullParser.getText());
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
        editor.commit();
    }

}