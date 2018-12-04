package com.rfid.uhfsdktest;

import android.content.Context;
import android.graphics.Color;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rfid.common.EPC;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YJW on 2018/4/11.
 */

public class Myadapter extends BaseAdapter {
    //定义需要包装的JSONArray对象
    public List<EPC> mymodelList=new ArrayList<>();
    private Context context=null;
    //视图容器
    private LayoutInflater layoutInflater;

    public Myadapter(Context _context){
        this.context=_context;
        //创建视图容器并设置上下文
        this.layoutInflater= LayoutInflater.from(_context);
    }

    public void updateDatas(List<EPC> datalist){
        if(datalist == null)
        {
            return;
        }else{
            mymodelList.clear();
            mymodelList.addAll(datalist);
            notifyDataSetChanged();
        }

    }

    /**
     * 清空列表的所有数据
     */
    public void clearData()
    {
        mymodelList.clear();
        notifyDataSetChanged();
    }



    @Override
    public int getCount() {
        return this.mymodelList.size();
    }

    @Override
    public Object getItem(int position) {
        if (getCount()>0){
            return this.mymodelList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItemView listItemView =null;
        if (convertView ==null){
            //获取list_item布局文件的视图
            convertView = layoutInflater.inflate(R.layout.item_list,null);
            //获取控件对象
            listItemView =new ListItemView();
            listItemView.num  = (TextView) convertView.findViewById(R.id.tv_epcNum);
            listItemView.total = (TextView) convertView.findViewById(R.id.tv_epc);
            listItemView.plan = (TextView) convertView.findViewById(R.id.tv_plan);
            listItemView.real = (TextView) convertView.findViewById(R.id.tv_real);
            //设置控件集到convertView
            convertView.setTag(listItemView);
        }
        else{
            listItemView = (Myadapter.ListItemView)convertView.getTag();
        }

        final EPC m1=(EPC) this.getItem(position);
        if (!(m1.getEpc().length()==1 &&(m1.getEpc().contains("A") || m1.getEpc().contains("B") || m1.getEpc().contains("C")))){
            listItemView.total.setBackgroundColor(Color.RED);
            listItemView.total.setTextColor(Color.WHITE);
        }else {
            listItemView.total.setBackgroundColor(Color.GREEN);
            listItemView.total.setTextColor(Color.WHITE);
        }
        listItemView.num.setText(position+1+"");
        listItemView.total.setText(m1.getEpc());
        listItemView.plan.setText(m1.list.size()+"");
        listItemView.real.setText(m1.getNum()+"");
        return convertView;
    }

    /**
     * 使用一个类来保存Item中的元素
     * 自定义控件集合
     */
    public final class ListItemView{
        TextView total,num,plan,real;
    }
}
