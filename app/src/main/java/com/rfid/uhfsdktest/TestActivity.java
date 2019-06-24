package com.rfid.uhfsdktest;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import com.ioter.rfid.IReceiveTag;
import com.ioter.rfid.RfidBuilder;
import com.ioter.rfid.RfidHelper;
import com.rfid.common.ACache;
import com.rfid.common.EPC;
import com.rfid.common.ScreenUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestActivity extends AppCompatActivity {

    private RfidHelper helper;
    private Button bt_set, bt_start, bt_reset,bt_sure,bt_clear;
    private TextView tv_total_ready,tv_rb,tv_total_real,tv_tian;
    private EditText edt_num;
    private ListView lv_list;
    private ACache aCache = null;
    private ConcurrentHashMap<String, EPC> hashMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
    private Myadapter myadapter;
    private ArrayList<Integer> scannedCodes = new ArrayList<Integer>();

    private static final String TAG = "TESTANS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        aCache = ACache.get(this);

        tv_rb = (TextView) findViewById(R.id.tv_rb);
        bt_set = (Button) findViewById(R.id.bt_set);
        bt_start = (Button) findViewById(R.id.bt_start);
        bt_reset = (Button) findViewById(R.id.bt_reset);
        tv_total_ready = (TextView) findViewById(R.id.tv_total_ready);
        tv_total_real = (TextView) findViewById(R.id.tv_total_real);
        tv_tian = (TextView) findViewById(R.id.tv_tian);
        lv_list = (ListView) findViewById(R.id.lv_list);
        edt_num = (EditText) findViewById(R.id.edt_num);
        bt_sure = (Button) findViewById(R.id.bt_sure);
        bt_clear = (Button) findViewById(R.id.bt_clear);

        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initview();
            }
        });



        //提交
        bt_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bt_start.getText().toString().trim().equals("停止")) {
                    //停止读标签和停止感应模块
                    helper.stopInventroyAndGpio();
                    bt_start.setText("开始");
                }
                map.clear();
                hashMap.clear();
                edt_num.setText("");
                tv_total_real.setTextColor(Color.GREEN);
                tv_total_real.setText("0");
                tv_total_ready.setText("0");
                myadapter.clearData();

            }
        });

        bt_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始读标签
                bt_sure.setFocusableInTouchMode(true);
                bt_sure.setFocusable(true);
                bt_sure.requestFocus();
                if (edt_num.getText().toString().toUpperCase().trim().equals("IOT123456")){
                    SetNum();
                }
            }
        });

        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bt_start.getText().toString().trim().equals("停止")) {
                    //停止读标签和停止感应模块
                    helper.stopInventroyAndGpio();
                    bt_start.setText("开始");
                }
                map.clear();
                hashMap.clear();
                edt_num.setText("");
                tv_total_real.setTextColor(Color.GREEN);
                tv_total_real.setText("0");
                tv_total_ready.setText("0");
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

    private void initview(){
        //读标签
        if (bt_start.getText().toString().trim().equals("开始")) {
            //开始读标签
            helper.startInventroy();
            bt_start.setText("停止");
        } else {
            //停止读标签和停止感应模块
            helper.stopInventroyAndGpio();
            bt_start.setText("开始");
        }
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
        String set1 = aCache.getAsString("key1");
        String set2 = aCache.getAsString("key2");
        String set3 = aCache.getAsString("key3");
        String set4 = aCache.getAsString("key4");
        String rb = aCache.getAsString("rb");

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
        Object[] key_arr = map1.keySet().toArray();
        Arrays.sort(key_arr);
        for (Object key2 : key_arr) {
            int kk = (int) key2;
            setPowers[a] = map1.get(kk);
            setAns[a] = Byte.valueOf(kk + "");
            sb.append(kk + 1 + "("+setPowers[a]+")  ");
            a++;
        }

        if (rb != null && !rb.equals("")){
            tv_rb.setText(rb);
        }else{
            tv_rb.setText("出库");
        }

        tv_tian.setText(sb.toString());
        helper = new RfidBuilder().setConnectType(RfidBuilder.COM_TYPE).setPort("dev/ttyS1").setBaudRate(115200).setWorkAnts(setAns).setWorkAntPowers(setPowers).setReceiveTagListener(new IReceiveTag()
                //helper =  new RfidBuilder().setConnectType(RfidBuilder.IP_TYPE).setHost("192.168.31.188").setIpPort(8086).setWorkAnts(new byte[]{0,1,2,3}).setWorkAntPowers(new byte[]{25,25,25,25}).setReceiveTagListener(new IReceiveTag()
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
                    if (strEPC.length()==8){
                        if (strEPC.startsWith("A00000") || strEPC.startsWith("B00000") || strEPC.startsWith("C00000")
                                || strEPC.startsWith("D00000")|| strEPC.startsWith("E00000")){
                            String key = strEPC.substring(0,1);
                            if (hashMap.containsKey(key)){
                                if (hashMap.get(key).list.contains(strEPC)){
                                    int num =hashMap.get(key).getNum()+1;
                                    hashMap.get(key).setNum(num);
                                }else {
                                    EPC epc = new EPC();
                                    epc.setEpc(strEPC);
                                    epc.setNum(1);
                                    hashMap.put(key,epc);
                                }
                            }else {
                                EPC epc = new EPC();
                                epc.setEpc(strEPC);
                                epc.setNum(1);
                                hashMap.put(strEPC,epc);
                            }
                        }else{
                            /*EPC epc = new EPC();
                            epc.setEpc(strEPC);
                            epc.setNum(1);
                            hashMap.put(strEPC,epc);*/
                        }
                    }else {
                        /*EPC epc = new EPC();
                        epc.setEpc(strEPC);
                        epc.setNum(1);
                        hashMap.put(strEPC,epc);*/
                    }
                    map.put(strEPC, strEPC);
                } else {
                    return;
                }
                uplist();
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

    //设置功率
    public void showDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.cw_location_dialog_dong, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        Button btn_agree = (Button) view.findViewById(R.id.btn_agree);
        final RadioButton  rb_in= (RadioButton) view.findViewById(R.id.rb_in);
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
                    et1.setFocusable(true);
                    et1.setFocusableInTouchMode(true);
                    et1.requestFocus();
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
                    et2.setFocusable(true);
                    et2.setFocusableInTouchMode(true);
                    et2.requestFocus();
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
                    et3.setFocusable(true);
                    et3.setFocusableInTouchMode(true);
                    et3.requestFocus();
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
                    et4.setFocusable(true);
                    et4.setFocusableInTouchMode(true);
                    et4.requestFocus();
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
                String set1 = et1.getText().toString().trim();
                String set2 = et2.getText().toString().trim();
                String set3 = et3.getText().toString().trim();
                String set4 = et4.getText().toString().trim();
                aCache.put("key1", set1);
                aCache.put("key2", set2);
                aCache.put("key3", set3);
                aCache.put("key4", set4);
                if (rb_in.isChecked()){
                    aCache.put("rb","入库");
                }else{
                    aCache.put("rb","出库");
                }
                reHelp();
                dialog.dismiss();
            }
        });

        dialog.show();

        //此处设置位置窗体大小，我这里设置为了手机屏幕宽度的3/4
        dialog.getWindow().setLayout((ScreenUtils.getScreenWidth(this) * 3 / 4), LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    public void SetNum(){
        EPC epc1 = new EPC();
        epc1.setEpc("A");
        epc1.setNum(0);
        EPC epc2 = new EPC();
        epc2.setEpc("B");
        epc2.setNum(0);
        EPC epc3 = new EPC();
        epc3.setEpc("C");
        epc3.setNum(0);
        EPC epc4 = new EPC();
        epc4.setEpc("D");
        epc4.setNum(0);
        EPC epc5 = new EPC();
        epc5.setEpc("E");
        epc5.setNum(0);
        for (int i = 1; i < 16; i++) {
            if (i==3){
                continue;
            }if (i<10){
                epc1.list.add("A000000"+i);
            }else{
                epc1.list.add("A00000"+i);
            }
        }
        for (int i = 1; i < 11; i++) {
            if (i<10){
                epc2.list.add("B000000"+i);
            }else{
                epc2.list.add("B00000"+i);
            }
        }
        for (int i = 1; i < 11; i++) {
            if (i==3){
                continue;
            }if (i<10){
                epc3.list.add("C000000"+i);
            }else{
                epc3.list.add("C00000"+i);
            }

        }
        for (int i = 1; i < 51; i++) {
            if (i<10){
                epc4.list.add("D000000"+i);
            }else{
                epc4.list.add("D00000"+i);
            }

        }
        for (int i = 1; i < 51; i++) {
            if (i<10){
                epc5.list.add("E000000"+i);
            }else{
                epc5.list.add("E00000"+i);
            }

        }
        hashMap.put("A",epc1);
        hashMap.put("B",epc2);
        hashMap.put("C",epc3);
        hashMap.put("D",epc4);
        hashMap.put("E",epc5);
        uplist();
    }

    private void uplist(){
        List<EPC> epcList = new ArrayList<>();
        Iterator iterator = hashMap.keySet().iterator();
        int a = 0,b = 0;
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            EPC epc = hashMap.get(key);
            if (epc.getEpc().length()>1){
                tv_total_real.setTextColor(Color.RED);
            }
            a+= epc.list.size();
            b+= epc.getNum();
            epcList.add(epc);
        }
        myadapter.updateDatas(epcList);
        tv_total_real.setText(b + "");
        tv_total_ready.setText(a + "");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                View view = getCurrentFocus();
                ScreenUtils.hideKeyboard(ev, view, TestActivity.this);//调用方法判断是否需要隐藏键盘
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    //获取扫描枪的扫描数据
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode!= KeyEvent.KEYCODE_ENTER){ //扫码枪以回车为结束
            scannedCodes.add(keyCode);
        }else{ //结束
            handleKeyCodes();
        }
        return super.onKeyDown(keyCode, event);
    }


    private void handleKeyCodes(){
        map.clear();
        hashMap.clear();
        edt_num.setText("");
        tv_total_real.setTextColor(Color.GREEN);
        tv_total_real.setText("0");
        tv_total_ready.setText("0");
        myadapter.clearData();

        ScreenUtils fnString = new ScreenUtils();
        String result = "";
        boolean hasShift = false;
        for(int keyCode : scannedCodes){
            result += fnString.keyCodeToChar(keyCode, hasShift);
            hasShift = (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT);
        }
        edt_num.setText(result);
        if (edt_num.getText().toString().toUpperCase().trim().equals("IOT123456")){
            SetNum();
        }
        scannedCodes.clear();
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
