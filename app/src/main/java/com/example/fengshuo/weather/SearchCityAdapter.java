package com.example.fengshuo.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.example.fengshuo.bean.City;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fengshuo on 2018/11/7.
 */

public class SearchCityAdapter extends BaseAdapter {
    private Context mContext;
    private List<City> mCityList;
    private List<City> mSearchCityLists;
    private LayoutInflater mInflater;

    public SearchCityAdapter(Context context,List<City> cityList){
        mContext=context;
        mCityList=cityList;
        mSearchCityLists=new ArrayList<City>();
        mInflater=LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mSearchCityLists.size();
    }

    @Override
    public Object getItem(int position) {
        return mSearchCityLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null){
            convertView=mInflater.inflate(R.layout.default_search_city,null);
        }
        TextView provinceTv=(TextView)convertView.findViewById(R.id.search_province);
        TextView cityTv=(TextView)convertView.findViewById(R.id.search_city);
        provinceTv.setText(mSearchCityLists.get(position).getProvince());
        cityTv.setText(mSearchCityLists.get(position).getCity());
        return convertView;
    }

    public Filter getFilter(){
        final Filter filter=new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String str=constraint.toString().toUpperCase();
                FilterResults results=new FilterResults();
                ArrayList<City> filterList=new ArrayList<City>();

                if (mCityList!=null&&mCityList.size()!=0){
                    for (City city:mCityList){
                        if (city.getAllFristPY().indexOf(str)>-1
                                ||city.getAllPY().indexOf(str)>-1
                                ||city.getCity().indexOf(str)>-1){
                            filterList.add(city);
                        }
                    }
                }
                results.values=filterList;
                results.count=filterList.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mSearchCityLists=(ArrayList<City>)results.values;
                if (results.count>0){
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

}
