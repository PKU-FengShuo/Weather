package com.example.fengshuo.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
/**
 * Created by fengshuo on 2019/1/18.
 */

public class pageAdapter extends PagerAdapter{

    private Context context;
    private List<View> list;

    @Override
    public void notifyDataSetChanged(){
        super.notifyDataSetChanged();
    }

    public pageAdapter(Context context,List<View> list){
        this.context=context;
        this.list=list;
    }

    @Override
    public int getCount(){
        return list.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o){
        return view==o;
    }

    @Override
    public View instantiateItem(ViewGroup container, int position){
        View view=list.get(position);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object){
        View view=(View)object;
        container.removeView(view);
    }
}
