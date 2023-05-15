package com.mystnihon.wakeonlan;

import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static com.mystnihon.wakeonlan.utils.MacUtils.getMacBytes;

@Slf4j
public class WakeOnLan {
    public boolean sendPacket(String ipAddress, String macAddress, int port) {
        try {
            byte[] macBytes = getMacBytes(macAddress);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            InetAddress address = InetAddress.getByName(ipAddress);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.send(packet);
            }
            log.info("Wake-on-LAN packet sent to host/ip={} MAC={} on port {}", ipAddress, macAddress, port);
            return true;
        } catch (Exception e) {
            log.error("Failed to send Wake-on-LAN packet", e);
            return false;
        }
    }
}
