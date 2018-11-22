package com.rfid.uhfsdktest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ioter.rfid.IReceiveTag;
import com.ioter.rfid.RfidBuilder;
import com.ioter.rfid.RfidHelper;
import com.rfid.permission.PermissionsActivity;
import com.rfid.permission.PermissionsChecker;


public class MainActivity extends BaseActivity {

    private RfidHelper mHelper;

    private Button start_bt,stop_bt,start_gpio_bt,work_bt,test_tb;
    private TextView epc;
    private TextView gpios;
    private PermissionsChecker mPermissionsChecker; // 权限检测器
    private static final int REQUEST_CODE = 0; // 请求码


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        work_bt = (Button) findViewById(R.id.work_bt);
        start_bt = (Button) findViewById(R.id.start_bt);
        start_gpio_bt = (Button) findViewById(R.id.start_gpio_bt);
        stop_bt = (Button) findViewById(R.id.stop_bt);
        epc = (TextView) findViewById(R.id.epc);
        gpios = (TextView) findViewById(R.id.gpio);
        test_tb = (Button) findViewById(R.id.test_bt);


        work_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mHelper.isRfiderWork(1);
            }
        });
        start_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //读标签
                mHelper.startInventroy();
            }
        });
        start_gpio_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //开启感应模块
                mHelper.startGpio();
            }
        });
        stop_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //停止读标签和停止感应模块
                mHelper.stopInventroyAndGpio();
            }
        });

        test_tb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //记得destory
                if(mHelper!=null)
                {
                    mHelper.destroy();
                }
                Intent intent = new Intent(MainActivity.this,TestActivity.class);
                startActivity(intent);
            }
        });

        //动态添加权限
        mPermissionsChecker = new PermissionsChecker(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish();
            System.exit(0);
        }
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE);
    }


    @Override
    protected void onStart() {
        super.onStart();
        /**
         * 构建者构建helper工具类
         */
        mHelper =  new RfidBuilder().setConnectType(RfidBuilder.COM_TYPE).setPort("dev/ttyS0").setBaudRate(115200).setWorkAnts(new byte[]{0,1}).setWorkAntPowers(new byte[]{15,15}).setReceiveTagListener(new IReceiveTag()
                //mHelper =  new RfidBuilder().setConnectType(RfidBuilder.IP_TYPE).setHost("192.168.31.188").setIpPort(8086).setWorkAnts(new byte[]{0}).setWorkAntPowers(new byte[]{25}).setReceiveTagListener(new IReceiveTag()
        {
            /**
             * 数据回调
             * @param strEPC EPC标签值
             * @param strRSSI rssi值
             * @param btAntId 天线id
             */
            @Override
            public void onReceiveTags(String strEPC, String strRSSI, byte btAntId)
            {
                Log.d("TAG","epc:"+strEPC + "-----rssi:"+strRSSI+"----AntId:"+btAntId);
                epc.setText(strEPC);
            }

            /**
             *
             * @param btGpio1Value 传感gpio值
             * @param btGpio2Value
             * @param btGpio3Value
             * @param btGpio4Value
             */
            @Override
            public void onGpioValue(int btGpio1Value, int btGpio2Value, int btGpio3Value, int btGpio4Value)
            {
                Log.d("TAG",btGpio1Value+","+btGpio2Value+","+btGpio3Value+","+btGpio4Value);
                gpios.setText(btGpio1Value+","+btGpio2Value+","+btGpio3Value+","+btGpio4Value);
            }

            @Override
            public void onStatus(boolean isWork,int statusValue)
            {
                if(isWork)
                {
                    //开启盘点
                }
                Log.d("onStatus","onStatus:"+isWork+"-----"+statusValue);
            }

            @Override
            public void onTagLostConnect()
            {
                Log.d("TAG","onTagConnect");
            }

            @Override
            public void onGpioLostConnect()
            {
                Log.d("TAG","onGpioConnect");
            }
        }).build();
    }

    @Override
    protected void onResume()
    {
        mHelper.isRfiderWork(1);
        // 缺少权限时, 进入权限配置页面
        if (mPermissionsChecker.lacksPermissions()) {
            startPermissionsActivity();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //记得destory
        if(mHelper!=null)
        {
            mHelper.destroy();
        }
    }
}
