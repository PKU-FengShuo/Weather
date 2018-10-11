package com.example.fengshuo.weather;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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

    private ImageView mUpdateBtn;

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

    }

    @Override
    public void onClick(View view){
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
                    parseXML(responseStr);
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

    private void parseXML(String xmldata){
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
                            case "city":
                                eventType=xmlPullParser.next();
                                Log.d("myWeather888","1 city:    "+xmlPullParser.getText());
                                break;
                            case "updatetime":
                                eventType=xmlPullParser.next();
                                Log.d("myWeather888","2 updatetime:    "+xmlPullParser.getText());
                                break;
                            case "wendu":
                                eventType=xmlPullParser.next();
                                Log.d("myWeather888","3 wendu:    "+xmlPullParser.getText());
                                break;
                            case "fengli":
                                if (fengliCount==0){
                                    eventType=xmlPullParser.next();
                                    Log.d("myWeather888","4 fengli:    "+xmlPullParser.getText());
                                }
                                fengliCount++;
                                break;
                            case "shidu":
                                eventType=xmlPullParser.next();
                                Log.d("myWeather888","5 shidu:    "+xmlPullParser.getText());
                                break;
                            case "fengxiang":
                                if (fengxiangCount==0){
                                    eventType=xmlPullParser.next();
                                    Log.d("myWeather888","6 fengxiang:    "+xmlPullParser.getText());
                                }
                                fengxiangCount++;
                                break;
                            case "pm25":
                                eventType=xmlPullParser.next();
                                Log.d("myWeather888","7 pm25:    "+xmlPullParser.getText());
                                break;
                            case "quality":
                                eventType=xmlPullParser.next();
                                Log.d("myWeather888","8 quality:    "+xmlPullParser.getText());
                                break;
                            case "date":
                                if (dateCount==0){
                                    eventType=xmlPullParser.next();
                                    Log.d("myWeather888","9 date:    "+xmlPullParser.getText());
                                }
                                dateCount++;
                                break;
                            case "high":
                                if (highCount==0){
                                    eventType=xmlPullParser.next();
                                    Log.d("myWeather888","10 high:    "+xmlPullParser.getText());
                                }
                                highCount++;
                                break;
                            case "low":
                                if (lowCount==0){
                                    eventType=xmlPullParser.next();
                                    Log.d("myWeather888","11 low:    "+xmlPullParser.getText());
                                }
                                lowCount++;
                                break;
                            case "type":
                                if (typeCount==0){
                                    eventType=xmlPullParser.next();
                                    Log.d("myWeather888","12 tpye:    "+xmlPullParser.getText());
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
    }

}