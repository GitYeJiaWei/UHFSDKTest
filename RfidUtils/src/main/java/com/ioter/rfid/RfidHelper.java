package com.ioter.rfid;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.config.CMD;
import com.rfid.config.ERROR;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.ReaderSetting;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class RfidHelper
{
    public static final String TAG = "TAG";
    private ModuleConnector connector;
    private RFIDReaderHelper mReader;
    private RXObserver rxObserver;
    private String mPort = "dev/ttyS0";
    private int mBaudRate = 115200;
    /**
     * ant power range from 0 to 33(0x00 - 0x21)
     */
    private byte[] workAntPowers = new byte[]{(byte)25,(byte)25,(byte)0,(byte)0};
    private byte[] workAnts = new byte[]{0,1};
    private int mAntPosition;
    private int connectType;
    private String host;
    private int ipPort;
    private IReceiveTag mReceiveTagListener;
    private Handler mUiHandler;
    private Runnable mLoopRunnable;
    private byte[] gpioValues = new byte[]{1,1,0,0};
    private volatile boolean bInvetory = false;//是否开始盘点
    private volatile boolean bGpio = false;
    private int mStatusValue;//定义读写器判定标签值
    public final static int MSG_READ_FREE = 0;
    public final static int MSG_READ_EPC = 1;
    public final static int MSG_READ_GPIO = 2;
    private AtomicInteger mReadStatus = new AtomicInteger(MSG_READ_FREE);//当前读写器读写的状态,更新状态后希望立马被其他线程看到
    private ExecutorService executor;

    public RfidHelper(RfidBuilder builder) {
        executor = Executors.newFixedThreadPool(4);
        connector = new ReaderConnector();
        rxObserver = new RXObserver(){
            @Override
            protected void onExeCMDStatus(byte cmd, byte status)
            {
                Log.d(TAG,"CMD ========"+cmd+",STATUS ======"+status);
                if(cmd == CMD.SET_OUTPUT_POWER)
                {
                    if(mReceiveTagListener != null)
                    {
                        mUiHandler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                //正常工作
                                mReceiveTagListener.onStatus(true,mStatusValue);
                            }
                        });
                    }
                }
                else if(cmd == CMD.SET_WORK_ANTENNA)
                {
                    if(status == ERROR.SUCCESS)
                    {
                        mReader.realTimeInventory((byte) 0xff,(byte)0x01);
                        mAntPosition++;
                    }
                }else if(cmd == CMD.READ_GPIO_VALUE)
                {
                    if(status == ERROR.SUCCESS)
                    {
                        if (mReadStatus.get()== MSG_READ_GPIO)
                        {
                            mReader.readGpioValue((byte) 0xff);
                        } else if (mReadStatus.get() == MSG_READ_EPC)
                        {
                            bInvetory = true;
                            bGpio = false;
                        } else
                        {
                            bInvetory = false;
                            bGpio = false;
                        }
                    }
                }else if(cmd == CMD.REAL_TIME_INVENTORY)
                {
                    if (status == 34)//盘点失败(阈值异常)
                    {
                        mReader.realTimeInventory((byte) 0xff,(byte)0x01);
                        mAntPosition++;
                    }
                }
                mUiHandler.removeCallbacks(mLoopRunnable);
                if(cmd != CMD.SET_OUTPUT_POWER)
                {
                    mUiHandler.postDelayed(mLoopRunnable, 2100);
                }
            }
            @Override
            protected void onInventoryTag(final RXInventoryTag tag) {
                Log.d(TAG,"taging ========");
                if(tag != null&& mReceiveTagListener != null)
                {
                    mUiHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mReceiveTagListener.onReceiveTags(tag.strEPC.replace(" ", ""),tag.strRSSI,tag.btAntId);
                        }
                    });
                }
                mUiHandler.removeCallbacks(mLoopRunnable);
                mUiHandler.postDelayed(mLoopRunnable,2100);
            }
            @Override
            protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd endTag) {
                mUiHandler.removeCallbacks(mLoopRunnable);
                if(mReadStatus.get() == MSG_READ_EPC)
                {
                    mUiHandler.postDelayed(mLoopRunnable,2100);
                    bInvetory = true;
                    bGpio = false;
                    initWorkAnt();
                }else if(mReadStatus.get()== MSG_READ_GPIO)
                {
                    mUiHandler.postDelayed(mLoopRunnable,2100);
                    bInvetory = false;
                    bGpio = true;
                }else
                {
                    bInvetory = false;
                    bGpio = false;
                }
                Log.d(TAG,"InventoryEnd"+bInvetory + ":"+bGpio);
            }

            @Override
            protected void refreshSetting(final ReaderSetting readerSetting)
            {
                if (mReceiveTagListener != null)
                {
                    if(readerSetting.btGpio1Value!=gpioValues[0] || readerSetting.btGpio2Value!=gpioValues[1] || readerSetting.btGpio3Value!=gpioValues[2] || readerSetting.btGpio4Value!=gpioValues[3])
                    {
                        mUiHandler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                mReceiveTagListener.onGpioValue(readerSetting.btGpio1Value,readerSetting.btGpio2Value,readerSetting.btGpio3Value,readerSetting.btGpio4Value);
                            }
                        });
                        gpioValues[0]=readerSetting.btGpio1Value;
                        gpioValues[1]=readerSetting.btGpio2Value;
                        gpioValues[2]=readerSetting.btGpio3Value;
                        gpioValues[3]=readerSetting.btGpio4Value;
                    }
                    Log.d("TAG",gpioValues[0]+","+gpioValues[1]+","+gpioValues[2]+","+gpioValues[3]);

                }
            }
        };
        mUiHandler = new Handler(Looper.getMainLooper());
        mLoopRunnable = new Runnable()
        {
            public void run()
            {

                if(mReadStatus.get() == MSG_READ_EPC)
                {
                    if(mReceiveTagListener!=null)
                    {
                        mReceiveTagListener.onTagLostConnect();
                    }
                }else if(mReadStatus.get() == MSG_READ_GPIO)
                {
                    if(mReceiveTagListener!=null)
                    {
                        mReceiveTagListener.onGpioLostConnect();
                    }
                }
                else if(mReadStatus.get() == MSG_READ_FREE)
                {
                    if(mReceiveTagListener!=null)
                    {
                        mReceiveTagListener.onStatus(false,mStatusValue);
                    }
                }
               /*
               try
                {
                    Thread.sleep(20000); 网络连接方式需要等待10几秒后重新启动
                    startInventroy();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }*/
            }
        };
        if(!TextUtils.isEmpty(builder.getPort()))
        {
            this.mPort = builder.getPort();
        }
        if(builder.getBaudRate()!=0)
        {
            this.mBaudRate = builder.getBaudRate();
        }
        byte[] workAntPowers = builder.getWorkAntPowers();
        if(workAntPowers!=null&&workAntPowers.length <= 4)
        {
            for(int i=0;i<workAntPowers.length;i++)
            {
                this.workAntPowers[i] = workAntPowers[i];
            }
        }
        this.connectType = builder.getConnectType();
        this.host = builder.getHost();
        this.ipPort = builder.getIpPort();
        this.mReceiveTagListener = builder.getReceiveTagListener();
        byte[] workAnts = builder.getWorkAnts();
        if(workAnts!=null&&workAnts.length>0)
        {
            this.workAnts = workAnts;
        }
        initConnected();
    }

    private boolean initConnected()
    {
        Future<Boolean> result = executor.submit(new Callable<Boolean>()
            {
                @Override
                public Boolean call()
                {
                    if (RfidHelper.this.connectType == RfidBuilder.COM_TYPE)
                    {
                        boolean a = initRfidReader(connector.connectCom(mPort, mBaudRate));
                        return a;
                    } else if (RfidHelper.this.connectType == RfidBuilder.IP_TYPE)
                    {
                        return initRfidReader(connector.connect(host, ipPort));
                    }
                    return false;
                }
            });
        try
        {
            return result.get();
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private boolean initRfidReader(boolean isInit)
    {
        if(isInit)
        {
            ModuleManager.newInstance().setUHFStatus(true);
            try
            {
                mReader = RFIDReaderHelper.getDefaultHelper();
                mReader.registerObserver(rxObserver);
                mReadStatus.set(MSG_READ_FREE);
                bInvetory = false;
                bGpio = false;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * set up workAnt
     */
    private void initWorkAnt()
    {
        if(workAnts!=null && workAnts.length>0 && workAnts.length <= 4)
        {
            mAntPosition = mAntPosition % workAnts.length;
            mReader.setWorkAntenna((byte) 0xff, workAnts[mAntPosition]);
        }
    }

    /**
     * 判断读写器是否可用
     */
    public void isRfiderWork(int statusValue)
    {
        boolean result = connector.isConnected();
        if(!result)
        {
            result = initConnected();
        }
        if(!result)
        {
            return;
        }
        this.mStatusValue = statusValue;
        mUiHandler.removeCallbacks(mLoopRunnable);
        mUiHandler.postDelayed(mLoopRunnable, 2100);
        if (mReadStatus.get() == MSG_READ_FREE)
        {
            mReader.setOutputPower((byte)0xff,workAntPowers[0],workAntPowers[1],workAntPowers[2],workAntPowers[3]);
        } else if (mReadStatus.get() == MSG_READ_GPIO)
        {
            mReadStatus.compareAndSet(MSG_READ_GPIO, MSG_READ_FREE);
            executor.execute(new PowerSetRunnable());
        } else if (mReadStatus.get() == MSG_READ_EPC)
        {
            mReadStatus.compareAndSet(MSG_READ_EPC, MSG_READ_FREE);
            executor.execute(new PowerSetRunnable());
        }
    }

    /**
     * 开启盘点
     * 流程：设置工作天线-->确定设置天线成功-->发起盘点指令-->盘存一轮结束，收到盘存结束标识
     */
    public void startInventroy()
    {
        boolean result = connector.isConnected();
        if(!result)
        {
            result = initConnected();
        }
        if(!result)
        {
            return;
        }
        mUiHandler.removeCallbacks(mLoopRunnable);
        mUiHandler.postDelayed(mLoopRunnable, 2100);
        if (mReadStatus.get() == MSG_READ_FREE)
        {
            mReadStatus.compareAndSet(MSG_READ_FREE, MSG_READ_EPC);
            initWorkAnt();
        } else if (mReadStatus.get() == MSG_READ_GPIO)
        {
            mReadStatus.compareAndSet(MSG_READ_GPIO, MSG_READ_EPC);
            executor.execute(new InventroyRunnable());
        } else if (mReadStatus.get() == MSG_READ_EPC)
        {
            initWorkAnt();
        }
    }


    public void startGpio()
    {
        boolean result = connector.isConnected();
        if(!result)
        {
            result = initConnected();
        }
        if(!result)
        {
            return;
        }
        mUiHandler.removeCallbacks(mLoopRunnable);
        mUiHandler.postDelayed(mLoopRunnable, 2100);
        if (mReadStatus.get() == MSG_READ_FREE)
        {
            mReadStatus.compareAndSet(MSG_READ_FREE, MSG_READ_GPIO);
            mReader.readGpioValue((byte) 0xff);
        } else if (mReadStatus.get() == MSG_READ_EPC)
        {
            mReadStatus.compareAndSet(MSG_READ_EPC, MSG_READ_GPIO);
            executor.execute(new GpioRunnable());
        } else if (mReadStatus.get() == MSG_READ_GPIO)
        {
            mReader.readGpioValue((byte) 0xff);
        }
    }


    /**
     * 在切换读写器读取模式(比如说从读取标签，切换到读取gpio)的时候，不用先调用该方法（目前切换模式时，有阻塞处理）
     */
    public void stopInventroyAndGpio()
    {
        mUiHandler.removeCallbacks(mLoopRunnable);
        if(mReadStatus.get() == MSG_READ_EPC)
        {
            mReadStatus.compareAndSet(MSG_READ_EPC,MSG_READ_FREE);
        }else if(mReadStatus.get() == MSG_READ_GPIO)
        {
            mReadStatus.compareAndSet(MSG_READ_GPIO,MSG_READ_FREE);
        }
    }

    /**
     * 通过判断设置功率，查看读写器是否工作
     */
    class PowerSetRunnable implements Runnable
    {
        @Override
        public void run()
        {
            while (true)
            {
                if(!bInvetory&&!bGpio&&mReadStatus.get() == MSG_READ_FREE)
                {
                    Log.d("work","work:");
                    mReader.setOutputPower((byte)0xff,workAntPowers[0],workAntPowers[1],workAntPowers[2],workAntPowers[3]);
                    break;
                }
            }
        }
    }


    class InventroyRunnable implements Runnable
    {
        @Override
        public void run()
        {
            while (true)
            {
                if(bInvetory)
                {
                    initWorkAnt();
                    break;
                }
            }
        }
    }

    class GpioRunnable implements Runnable
    {
        @Override
        public void run()
        {
            while (true)
            {
                if(bGpio)
                {
                    Log.d(TAG,"GPIO =====");
                    mReader.readGpioValue((byte) 0xff);
                    break;
                }
            }
        }
    }


    public void destroy() {
        if (mReader != null) {
            mReader.unRegisterObserver(rxObserver);
        }
        if (connector != null) {
            connector.disConnect();
        }
        if(executor!=null)
        {
            executor.shutdownNow();
        }
        if(mUiHandler!=null)
        {
            mUiHandler.removeCallbacks(mLoopRunnable);
        }
        ModuleManager.newInstance().setUHFStatus(false);
        ModuleManager.newInstance().release();
    }

}
