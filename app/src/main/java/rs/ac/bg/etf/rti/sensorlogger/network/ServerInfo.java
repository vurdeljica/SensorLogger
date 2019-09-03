package rs.ac.bg.etf.rti.sensorlogger.network;

import androidx.annotation.NonNull;

public class ServerInfo {
    private String hostname;
    private String ipAddress;
    private String instanceName;
    private int port;

    public ServerInfo(String hostname, String ipAddress, int port, String instanceName) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.instanceName = instanceName;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static String makeURL(ServerInfo serverInfo) {
        return "http://" + serverInfo.getIpAddress() + ":" + serverInfo.getPort();
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof ServerInfo)) {
            return false;
        }

        ServerInfo serverInfo = (ServerInfo) obj;

        return hostname.equals(serverInfo.getHostname()) && ipAddress.equals(serverInfo.getIpAddress()) && port == serverInfo.getPort() && instanceName.equals(serverInfo.getInstanceName());
    }

    @NonNull
    @Override
    public String toString() {
        return hostname + "[" + ipAddress + ":" + port + "]";
    }

}