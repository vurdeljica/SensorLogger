package rs.ac.bg.etf.rti.sensorlogger.presentation.devices;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.android.gms.wearable.Node;

public class DeviceListItemViewModel extends BaseObservable {
    private Node node;

    DeviceListItemViewModel(Node node) {
        this.node = node;
    }

    @Bindable
    public String getNodeName() {
        return node.getDisplayName();
    }

    @Bindable
    public String getNodeId() {
        return node.getId();
    }
}
