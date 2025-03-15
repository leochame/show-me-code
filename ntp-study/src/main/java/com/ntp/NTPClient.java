package com.ntp;

import com.ntp.core.ClockCompensator;
import com.ntp.core.NtpServer;
import com.ntp.exception.NtpException;
import com.ntp.exception.ServerUnreachableException;
import com.ntp.exception.StratumViolationException;
import com.ntp.routing.NtpRouter;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

// 文件5: NTPClient.java
public class NTPClient {
    private static final NTPUDPClient client = new NTPUDPClient();
    private static final ClockCompensator compensator = new ClockCompensator();
    private static final NtpRouter router = new NtpRouter(Arrays.asList(
            new NtpServer("ntp1.aliyun.com", 1),
            new NtpServer("ntp2.tencent.com", 2),
            new NtpServer("pool.ntp.org", 3)
    ));

    static {
        client.setDefaultTimeout(5000);
        client.setVersion(4);
    }

    public static long getNetworkTime() {
        try {
            NtpServer server = router.selectBestServer();
            InetAddress address = router.resolve(server);

            TimeInfo info = client.getTime(address);
            info.computeDetails();

            validateResponse(info.getMessage());

            return compensator.compensate(
                    info.getMessage().getTransmitTimeStamp().getTime(),
                    info.getDelay()
            );
        } catch (IOException e) {
            throw new ServerUnreachableException("Network failure");
        }
    }

    private static void validateResponse(NtpV3Packet packet) {
        if(packet.getStratum() > 3) {
            throw new StratumViolationException(packet.getStratum());
        }

        if(packet.getReferenceIdString().startsWith("RATE")) {
            throw new NtpException("Server rate limited", null);
        }
    }

    public static void shutdown() {
        client.close();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 800; i++) {
            System.out.println("Current NTP time: " + NTPClient.getNetworkTime());
            NTPClient.getNetworkTime();
        }
        System.out.println(1);
        NTPClient.shutdown();
    }
}