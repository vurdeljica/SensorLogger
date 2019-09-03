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

class ServerInfoListAdapter extends ArrayAdapter<String> {
    private List<ServerInfo> serverInfoList;
    private NetworkManager networkManager;

    private long updateInterval = 1500L;
    private Handler updateHandler = new Handler();

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateServerInfoList();
            updateHandler.postDelayed(this, updateInterval);
        }
    };

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

    private void startUpdates() {
        updateHandler.postDelayed(updateRunnable, updateInterval);
    }
}
