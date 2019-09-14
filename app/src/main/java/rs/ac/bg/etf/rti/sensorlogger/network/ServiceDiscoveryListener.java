package rs.ac.bg.etf.rti.sensorlogger.network;

/**
 * Represent interface of callbacks that will be triggered when service is found/lost
 */
interface ServiceDiscoveryListener {
    void onServiceFound(String hostname, String ipAddress, int port, String instanceName);
    void onServiceLost(String instanceName);
}
