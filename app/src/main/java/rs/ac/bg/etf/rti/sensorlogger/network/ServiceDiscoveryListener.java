package rs.ac.bg.etf.rti.sensorlogger.network;

interface ServiceDiscoveryListener {
    void onServiceFound(String hostname, String ipAddress, int port, String instanceName);
    void onServiceLost(String instanceName);
}
