package com.mystnihon.wakeonlan;

public interface IWakeOnLan {
    boolean sendPacket(String ipAddress, String macAddress, int port);
}
