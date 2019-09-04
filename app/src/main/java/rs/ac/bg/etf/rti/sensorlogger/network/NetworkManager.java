package rs.ac.bg.etf.rti.sensorlogger.network;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NetworkManager {

    private static NetworkManager instance;

    private FileTransferServiceDiscovery serviceDiscovery;
    private NetworkFileTransfer networkFileTransfer;

    private List<ServerInfo> localServersInfo = new ArrayList<>();

    public static NetworkManager getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkManager(context);
        }

        return instance;
    }

    private NetworkManager(Context context) {
        serviceDiscovery = FileTransferServiceDiscovery.getInstance(context);
        networkFileTransfer = NetworkFileTransfer.getInstance();

        serviceDiscovery.discoverServices(new ServiceDiscoveryListener() {
            @Override
            public void onServiceFound(String hostname, String ipAddress, int port, String instanceName) {
                ServerInfo serverInfo = new ServerInfo(hostname, ipAddress, port, instanceName);
                localServersInfo.add(serverInfo);
            }

            @Override
            public void onServiceLost(String ipAddress) {
                removeLocalServerInfo(ipAddress);
            }
        });
    }

    private void removeLocalServerInfo(String instanceName) {
        for (Iterator<ServerInfo> iter = localServersInfo.listIterator(); iter.hasNext(); ) {
            ServerInfo sInfo = iter.next();
            if (sInfo.getInstanceName().equals(instanceName)) {
                iter.remove();
            }
        }
    }

    public List<ServerInfo> getServersInformation() {
        List<ServerInfo> retServerInfo = new ArrayList<>();

        for(ServerInfo sInfo : localServersInfo) {
            ServerInfo serverInfo = new ServerInfo(sInfo.getHostname(), sInfo.getIpAddress(), sInfo.getPort(), sInfo.getInstanceName());
            retServerInfo.add(serverInfo);
        }

        return retServerInfo;
    }

    public void uploadDirectoryContentToServer(ServerInfo serverInfo, File directory) {
//        if(!isServerInfoValid(serverInfo)) return;

        String serverURL = ServerInfo.makeURL(serverInfo);
        networkFileTransfer.sendAllFilesInDirectory(serverURL, directory);
    }

    private boolean isServerInfoValid(ServerInfo sInfo) {
        for(ServerInfo serverInfo : localServersInfo) {
            if (serverInfo.equals(sInfo)) {
                return true;
            }
        }

        return false;
    }

}
