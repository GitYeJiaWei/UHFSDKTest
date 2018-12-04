package com.rfid.common;

import java.io.Serializable;

public class EPC implements Serializable{
    private String epc;

    private int num;

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
