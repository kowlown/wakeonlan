package com.mystnihon.wakeonlan;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Slf4j
@Getter
@Setter
@Command(name = "wakeonlan", mixinStandardHelpOptions = true, version = "wakeonlan 1.0",
    description = "Send a wakeonlan magic-packet to a machine targeted with its MAC address.", helpCommand = true)
@SpringBootApplication
public class WakeOnLanConsole implements Callable<Integer>, CommandLineRunner {
    public static final int DEFAULT_PORT = 9;
    private final WakeOnLan wakeOnLan;
    @CommandLine.Parameters(index = "0", description = "The Host/IP address where to send the magic packet.")
    private String ipAddress;
    @CommandLine.Parameters(index = "1", description = "The MAC address of the machine to wake-up.")
    private String macAddress;
    @CommandLine.Option(names = {"-p", "--port"}, description = "The port where to send the magic packet.")
    private int port = DEFAULT_PORT;
    @CommandLine.Option(names = "--help", usageHelp = true,
        description = "Usage: java WakeOnLan <broadcast-ip> <mac-address>\nExample: java WakeOnLan 192.168.0.255 00:0D:61:08:22:4A\nExample: java WakeOnLan 192.168.0.255 00-0D-61-08-22-4A")
    private boolean help;

    public WakeOnLanConsole() {
        wakeOnLan = new WakeOnLan();
    }

    public static void main(String[] args) {
        SpringApplication.run(WakeOnLanConsole.class, args);
    }

    @Override
    public Integer call() {
        if (ipAddress != null && macAddress != null) {
            return wakeOnLan.sendPacket(ipAddress, macAddress, port) ? 0 : 1;
        }
        return 0;
    }


    @Override
    public void run(String... args) {
        int exitCode = new CommandLine(new WakeOnLanConsole()).execute(args);
        System.exit(exitCode);
    }
}
