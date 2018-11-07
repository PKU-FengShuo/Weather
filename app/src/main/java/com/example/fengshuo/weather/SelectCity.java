package com.example.fengshuo.weather;

import android.app.Activity;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fengshuo.app.MyApplication;
import com.example.fengshuo.bean.City;
import com.example.fengshuo.db.CityDB;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fengshuo on 2018/10/20.
 */

public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;
    private ListView mlistView;
    private TextView mTitleName;
    private CityDB mCityDB;
    private List<City> mCityList;
    private String[] data={"第1组","第2组","第3组","第4组","第5组","第6组", "第7组","第8组","第9组","第10组","第11组","第12组","第13组", "第14组","第15组","第16组","第17组","第18组","第19组","第20组",
            "第21组","第22组"};
    private int result=-1;
    private City ResultCity;
    private String code;

    private EditText mSearchEditText;
    private SearchCityAdapter mSearchCityAdapter;

    final List<String> CityName=new ArrayList<String>();
    final List<String> CityCode=new ArrayList<String>();

    private MyApplication myApplication;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        mBackBtn=(ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        myApplication=MyApplication.getInstance();
        mCityList=myApplication.getCityList();
        //mCityList=mCityDB.getAllCity();

        for (City city:mCityList){

            //Log.d("showcity",city.getCity());
            CityName.add(city.getCity());
            //city.getCity();
            CityCode.add(city.getNumber());
        }

        mlistView=(ListView)findViewById(R.id.list_view);

        final ArrayAdapter adapter=new ArrayAdapter(
                this,android.R.layout.simple_list_item_1,CityName);
        mlistView.setAdapter(adapter);
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView,View view,int i,long l){
                City city;
                if(mSearchCityAdapter!=null){
                    city=(City) mSearchCityAdapter.getItem(i);
                }
                else
                    city=mCityList.get(i);
                result=i;
                ResultCity=city;
                mTitleName=(TextView)findViewById(R.id.title_name);
                mTitleName.setText("当前城市："+city.getCity());
                Toast.makeText(SelectCity.this,"你单击了："+i+"citycode:"+city.getNumber(),Toast.LENGTH_SHORT).show();
            }
        });

        TextWatcher mTextWatcher=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSearchCityAdapter=new SearchCityAdapter(SelectCity.this,mCityList);

                mlistView.setTextFilterEnabled(true);
                if (mCityList.size()<1|| TextUtils.isEmpty(s)){
                    mlistView.setAdapter(adapter);
                }else {
                    mlistView.setAdapter(mSearchCityAdapter);
                    mSearchCityAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        mSearchEditText=(EditText)findViewById(R.id.search_edit);
        mSearchEditText.addTextChangedListener(mTextWatcher);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back:
                Intent i=new Intent();
                if (result>=0){
                    code=ResultCity.getNumber();
                    i.putExtra("cityCode",code);
                }
                else {
                    i.putExtra("cityCode","101160101");
                }
                setResult(RESULT_OK,i);
                finish();
            default:
                break;
        }
    }
}
