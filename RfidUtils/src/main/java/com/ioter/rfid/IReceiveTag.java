package com.ioter.rfid;

public interface IReceiveTag
{
    /**
     *
     * @param strEPC epc值
     * @param strRSSI rssi值
     * @param btAntId 读取天线id
     */
    public void onReceiveTags(String strEPC, String strRSSI, byte btAntId);

    /**
     * gpio口的各个值
     * @param btGpio1Value
     * @param btGpio2Value
     * @param btGpio3Value
     * @param btGpio4Value
     */
    public void onGpioValue(int btGpio1Value,int btGpio2Value,int btGpio3Value,int btGpio4Value);

    /**
     * 读写器是否工作
     */
    public void onStatus(boolean isWork,int statusValue);

    /**
     * 读标签失去连接
     */
    public void onTagLostConnect();
    /**
     * 读GPIO失去连接
     */
    public void onGpioLostConnect();
}
