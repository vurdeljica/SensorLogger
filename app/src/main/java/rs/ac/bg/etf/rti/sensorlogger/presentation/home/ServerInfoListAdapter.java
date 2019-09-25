package rs.ac.bg.etf.rti.sensorlogger.presentation.home;

import android.content.Context;
import android.os.Handler;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.rti.sensorlogger.network.NetworkManager;
import rs.ac.bg.etf.rti.sensorlogger.network.ServerInfo;

/**
 * List adapter for the server info list in the data transfer dialog
 */
class ServerInfoListAdapter extends ArrayAdapter<String> {
    /**
     * List of potential receiving servers
     */
    private List<ServerInfo> serverInfoList;
    /**
     * Manager used for sending the data to the server
     */
    private NetworkManager networkManager;

    /**
     * Update interval for refreshing the server list
     */
    private long updateInterval = 1500L;
    private Handler updateHandler = new Handler();

    /**
     * Runnable for getting the list of servers
     */
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateServerInfoList();
            updateHandler.postDelayed(this, updateInterval);
        }
    };

    /**
     * Updates the server info list
     */
    private void updateServerInfoList() {
        serverInfoList.clear();
        clear();
        serverInfoList.addAll(networkManager.getServersInformation());
        addAll(Stream.of(serverInfoList).map(ServerInfo::toString).toList());
        notifyDataSetChanged();
    }

    ServerInfoListAdapter(@NonNull Context context, int resource, @NonNull List<ServerInfo> objects) {
        super(context, resource, Stream.of(objects).map(ServerInfo::toString).toList());
        serverInfoList = new ArrayList<>();
        networkManager = NetworkManager.getInstance(context);
        setNotifyOnChange(true);
        updateServerInfoList();
        startUpdates();
    }

    ServerInfo getServerInfoAt(int position) {
        return serverInfoList.get(position);
    }

    /**
     * Starts the server info updates
     */
    private void startUpdates() {
        updateHandler.postDelayed(updateRunnable, updateInterval);
    }
}
