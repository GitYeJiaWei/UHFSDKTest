package com.rfid.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    public List<String> list = new ArrayList<>();
}
