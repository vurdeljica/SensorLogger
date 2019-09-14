package rs.ac.bg.etf.rti.sensorlogger.network;

import android.content.Context;
import android.util.Log;

import com.github.druk.rx2dnssd.BonjourService;
import com.github.druk.rx2dnssd.Rx2Dnssd;
import com.github.druk.rx2dnssd.Rx2DnssdBindable;

import org.reactivestreams.Subscription;

import java.net.Inet4Address;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * FileTransferServiceDiscovery is Singleton which gives api for
 * working with DNS-SD
 */
public class FileTransferServiceDiscovery {
    private static final String REGISTRATION_TYPE = "_hap._tcp";
    private static final String DOMAIN = "local.";
    private static final String SERVICE_NAME = "SensorLoggerFileTransfer";

    private static FileTransferServiceDiscovery instance;

    Context context;

    ServiceDiscoveryListener clientCallbacks;

    Rx2Dnssd rxdnssd;
    Subscription fileTransferSubscription;

    /**
     * Classic Singleton method for getting instance.
     * @return ServiceDiscoveryListener instance
     */
    public static FileTransferServiceDiscovery getInstance(Context context) {
        if (instance == null) {
            instance = new FileTransferServiceDiscovery(context);
        }

        return instance;
    }

    private FileTransferServiceDiscovery(Context context) {
        this.context = context;
        rxdnssd = new Rx2DnssdBindable(context);
    }

    /**
     * Start DNS-SD service discovery
     * @param clientCallbacks callbacks that will be called when service is found or lost
     */
    public void discoverServices(ServiceDiscoveryListener clientCallbacks) {
        this.clientCallbacks = clientCallbacks;

        stopDiscovery();  // Cancel any existing discovery request
        //initializeDiscoveryListeners();
        subscribeToFileTransferService();
    }

    /**
     * Stop discovery. Callbacks will not be triggered anymore.
     */
    public void stopDiscovery() {
        if (fileTransferSubscription != null) {
            try {
                fileTransferSubscription.cancel();
            } catch (Exception e) {
               e.printStackTrace();
            } finally {
                clientCallbacks = null;
            }
        }
    }

    private void serviceFoundCallback(BonjourService bonjourService) {
        Log.d("TAG", bonjourService.toString());

        String serviceName = bonjourService.getServiceName();

        if (clientCallbacks != null && serviceName.contains(SERVICE_NAME)) {
            String hostname = bonjourService.getHostname();
            String ipv4Address = getIpAddress(bonjourService);
            String instanceName = bonjourService.getServiceName();

            int port = bonjourService.getPort();

            clientCallbacks.onServiceFound(hostname, ipv4Address, port, instanceName);
        }
    }

    private void serviceLostCallback(BonjourService bonjourService) {
        Log.d("TAG", "Service lost");

        String instanceName = bonjourService.getServiceName();

        if (clientCallbacks != null) {
            clientCallbacks.onServiceLost(instanceName);
        }
    }

    private String getIpAddress(BonjourService bonjourService) {
        Inet4Address ipv4AddressInetAddress = bonjourService.getInet4Address();
        String ipv4Address = "";
        if (ipv4AddressInetAddress != null) {
            ipv4Address = ipv4AddressInetAddress.getHostAddress();
        }

        boolean containsIpInTXTRecord = bonjourService.getTxtRecords().containsKey("ip");
        if (containsIpInTXTRecord) {
            ipv4Address = bonjourService.getTxtRecords().get("ip");
        }

        return ipv4Address;
    }

    private void subscribeToFileTransferService() {
        fileTransferSubscription = (Subscription) rxdnssd.browse(REGISTRATION_TYPE, DOMAIN)
                .compose(rxdnssd.resolve())
                .compose(rxdnssd.queryRecords())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bonjourService -> {
                    if (bonjourService.isLost()) {
                        serviceLostCallback(bonjourService);
                    } else {
                        serviceFoundCallback(bonjourService);
                    }
                });
    }
}