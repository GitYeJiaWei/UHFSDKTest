package com.rfid.uhfsdktest;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ioter.rfid.IReceiveTag;
import com.ioter.rfid.RfidBuilder;
import com.ioter.rfid.RfidHelper;
import com.rfid.common.ACache;
import com.rfid.common.ScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestActivity extends BaseActivity {

    private RfidHelper helper;
    private Button bt_set, bt_start, bt_reset;
    private TextView tv_total,tv_tian;
    private ListView lv_list;
    private ACache aCache = null;
    private ConcurrentHashMap<String, EPC> map = new ConcurrentHashMap<>();
    private Myadapter myadapter;
    private String set1, set2, set3, set4 = null;
    private static final String TAG = "TESTANS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        aCache = ACache.get(this);

        bt_set = (Button) findViewById(R.id.bt_set);
        bt_start = (Button) findViewById(R.id.bt_start);
        bt_reset = (Button) findViewById(R.id.bt_reset);
        tv_total = (TextView) findViewById(R.id.tv_total);
        tv_tian = (TextView) findViewById(R.id.tv_tian);
        lv_list = (ListView) findViewById(R.id.lv_list);

        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //读标签
                if (bt_start.getText().toString().trim().equals("开始测试")) {
                    //开始读标签
                    helper.startInventroy();
                    bt_start.setText("停止测试");
                } else {
                    //停止读标签和停止感应模块
                    helper.stopInventroyAndGpio();
                    bt_start.setText("开始测试");
                }
            }
        });


        bt_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.clear();
                tv_total.setText("0");
                myadapter.clearData();
            }
        });


        bt_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        myadapter = new Myadapter(this);
        lv_list.setAdapter(myadapter);
    }

    //初始化并弹出对话框方法
    private void showDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.cw_location_dialog_dong, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        Button btn_agree = (Button) view.findViewById(R.id.btn_agree);
        final CheckBox cb1 = (CheckBox) view.findViewById(R.id.cb_1);
        final CheckBox cb2 = (CheckBox) view.findViewById(R.id.cb_2);
        final CheckBox cb3 = (CheckBox) view.findViewById(R.id.cb_3);
        final CheckBox cb4 = (CheckBox) view.findViewById(R.id.cb_4);
        final EditText et1 = (EditText) view.findViewById(R.id.et_1);
        final EditText et2 = (EditText) view.findViewById(R.id.et_2);
        final EditText et3 = (EditText) view.findViewById(R.id.et_3);
        final EditText et4 = (EditText) view.findViewById(R.id.et_4);

        cb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb1.isChecked()) {
                    et1.setEnabled(true);
                } else {
                    et1.setEnabled(false);
                }
            }
        });

        cb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb2.isChecked()) {
                    et2.setEnabled(true);
                } else {
                    et2.setEnabled(false);
                }
            }
        });

        cb3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb3.isChecked()) {
                    et3.setEnabled(true);
                } else {
                    et3.setEnabled(false);
                }
            }
        });

        cb4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb4.isChecked()) {
                    et4.setEnabled(true);
                } else {
                    et4.setEnabled(false);
                }
            }
        });


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set1 = et1.getText().toString().trim();
                set2 = et2.getText().toString().trim();
                set3 = et3.getText().toString().trim();
                set4 = et4.getText().toString().trim();
                aCache.put("key1", set1);
                aCache.put("key2", set2);
                aCache.put("key3", set3);
                aCache.put("key4", set4);
                reHelp();
                dialog.dismiss();
            }
        });

        dialog.show();

        //此处设置位置窗体大小，我这里设置为了手机屏幕宽度的3/4
        dialog.getWindow().setLayout((ScreenUtils.getScreenWidth(this) / 4 * 3), LinearLayout.LayoutParams.WRAP_CONTENT);
    }


    @Override
    protected void onStart() {
        super.onStart();
        reHelp();
    }

    private void reHelp() {
        if (helper != null) {
            helper.destroy();
        }
        set1 = aCache.getAsString("key1");
        set2 = aCache.getAsString("key2");
        set3 = aCache.getAsString("key3");
        set4 = aCache.getAsString("key4");

        Map<Integer, Byte> map1 = new HashMap<>();
        if (set1 != null && !set1.equals("")) {
            map1.put(0, Byte.valueOf(set1));
        }
        if (set2 != null && !set2.equals("")) {
            map1.put(1, Byte.valueOf(set2));
        }
        if (set3 != null && !set3.equals("")) {
            map1.put(2, Byte.valueOf(set3));
        }
        if (set4 != null && !set4.equals("")) {
            map1.put(3, Byte.valueOf(set4));
        }
        if (map1.size() == 0) {
            map1.put(0, (byte) 15);
            map1.put(1, (byte) 15);
        }

        byte[] setAns = new byte[map1.size()];
        byte[] setPowers = new byte[map1.size()];
        StringBuffer sb = new StringBuffer();
        int a = 0;
        for (Integer key : map1.keySet()) {
            setPowers[a] = map1.get(key);
            setAns[a] = Byte.valueOf(key+"");
            sb.append(key+1+"  ");
            a++;
        }

        tv_tian.setText(sb.toString());
        helper = new RfidBuilder().setConnectType(RfidBuilder.COM_TYPE).setPort("dev/ttyS0").setBaudRate(115200).setWorkAnts(setAns).setWorkAntPowers(setPowers).setReceiveTagListener(new IReceiveTag()
                //mHelper =  new RfidBuilder().setConnectType(RfidBuilder.IP_TYPE).setHost("192.168.31.188").setIpPort(8086).setWorkAnts(new byte[]{0}).setWorkAntPowers(new byte[]{25}).setReceiveTagListener(new IReceiveTag()
        {
            /**
             * 数据回调
             * @param strEPC EPC标签值
             * @param strRSSI rssi值
             * @param btAntId 天线id
             */
            @Override
            public void onReceiveTags(String strEPC, String strRSSI, byte btAntId) {
                Log.d("TAG", "epc:" + strEPC + "-----rssi:" + strRSSI + "----AntId:" + btAntId);
                if (!map.containsKey(strEPC)) {
                    EPC epc = new EPC();
                    epc.setEpc(strEPC);
                    map.put(strEPC, epc);
                } else {
                    return;
                }

                List<EPC> epcList = new ArrayList<>();
                Iterator iterator = map.keySet().iterator();
                int a = 0;
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    EPC epc = map.get(key);
                    epcList.add(epc);
                    a++;
                }
                myadapter.updateDatas(epcList);
                tv_total.setText(a + "");
            }

            /**
             *
             * @param btGpio1Value 传感gpio值
             * @param btGpio2Value
             * @param btGpio3Value
             * @param btGpio4Value
             */
            @Override
            public void onGpioValue(int btGpio1Value, int btGpio2Value, int btGpio3Value, int btGpio4Value) {
                Log.d("TAG", btGpio1Value + "," + btGpio2Value + "," + btGpio3Value + "," + btGpio4Value);
            }

            @Override
            public void onStatus(boolean isWork, int statusValue) {
                if (isWork) {
                    //开启盘点
                }
                Log.d("onStatus", "onStatus:" + isWork + "-----" + statusValue);
            }

            @Override
            public void onTagLostConnect() {
                Log.d("TAG", "onTagConnect");
            }

            @Override
            public void onGpioLostConnect() {
                Log.d("TAG", "onGpioConnect");
            }
        }).build();
        helper.isRfiderWork(1);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //记得destory
        if (helper != null) {
            helper.destroy();
        }
    }

    /**
     * 点击空白区域隐藏键盘.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent me) {
        if (me.getAction() == MotionEvent.ACTION_DOWN) {  //把操作放在用户点击的时候
            View v = getCurrentFocus();      //得到当前页面的焦点,ps:有输入框的页面焦点一般会被输入框占据
            if (isShouldHideKeyboard(v, me)) { //判断用户点击的是否是输入框以外的区域
                hideKeyboard(v.getWindowToken());   //收起键盘
            }
        }
        return super.dispatchTouchEvent(me);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {  //判断得到的焦点控件是否包含EditText
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],    //得到输入框在屏幕中上下左右的位置
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击位置如果是EditText的区域，忽略它，不收起键盘。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略
        return false;
    }

    /**
     * 获取InputMethodManager，隐藏软键盘
     *
     * @param token
     */
    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //记得destory
        if (helper != null) {
            helper.destroy();
        }
    }
}