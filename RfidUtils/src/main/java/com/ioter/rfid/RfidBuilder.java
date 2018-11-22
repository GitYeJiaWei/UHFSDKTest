package com.ioter.rfid;

public class RfidBuilder
{
    public static final int COM_TYPE = 1;
    public static final int IP_TYPE = 2;

    public String getPort()
    {
        return port;
    }
    /**
     * 设置端口号
     * @param port
     * @return
     */
    public RfidBuilder setPort(String port)
    {
        this.port = port;
        return this;
    }

    public int getBaudRate()
    {
        return baudRate;
    }

    /**
     * 设置波特率
     * @param baudRate
     * @return
     */
    public RfidBuilder setBaudRate(int baudRate)
    {
        this.baudRate = baudRate;
        return this;
    }

    public IReceiveTag getReceiveTagListener()
    {
        return receiveTagListener;
    }

    /**
     * 设置数据回调
     * @param receiveTagListener
     * @return
     */
    public RfidBuilder setReceiveTagListener(IReceiveTag receiveTagListener)
    {
        this.receiveTagListener = receiveTagListener;
        return this;
    }
    public int getConnectType()
    {
        return connectType;
    }

    public RfidBuilder setConnectType(int connectType)
    {
        this.connectType = connectType;
        return this;
    }

    public byte[] getWorkAnts()
    {
        return workAnts;
    }


    private String port;
    private int baudRate;
    private int connectType;
    private String host;
    private int ipPort;
    /**
     * 4天线 0，1，2，3
     */
    private byte[] workAnts;

    public byte[] getWorkAntPowers()
    {
        return workAntPowers;
    }

    public RfidBuilder setWorkAntPowers(byte[] workAntPowers)
    {
        this.workAntPowers = workAntPowers;
        return this;
    }

    private byte[] workAntPowers;
    private IReceiveTag receiveTagListener;
    public String getHost()
    {
        return host;
    }

    public RfidBuilder setHost(String host)
    {
        this.host = host;
        return this;
    }

    public int getIpPort()
    {
        return ipPort;
    }

    public RfidBuilder setIpPort(int ipPort)
    {
        this.ipPort = ipPort;
        return this;
    }

    /**
     * 设置工作天线 0，1，2，3
     * @param workAnts
     * @return
     */
    public RfidBuilder setWorkAnts(byte[] workAnts)
    {
        this.workAnts = workAnts;
        return this;
    }
    public RfidHelper build()
    {
        return new RfidHelper(this);
    }
}
