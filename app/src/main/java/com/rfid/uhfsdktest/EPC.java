package com.rfid.uhfsdktest;

import java.io.Serializable;

public class EPC implements Serializable{
    private String epc;

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }
}
