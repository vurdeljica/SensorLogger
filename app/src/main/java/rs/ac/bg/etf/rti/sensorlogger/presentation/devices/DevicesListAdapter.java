package rs.ac.bg.etf.rti.sensorlogger.presentation.devices;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.wearable.Node;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.rti.sensorlogger.R;
import rs.ac.bg.etf.rti.sensorlogger.databinding.DeviceListItemBinding;

public class DevicesListAdapter extends RecyclerView.Adapter<DevicesListAdapter.DeviceViewHolder> {

    private List<Node> nodeList;

    DevicesListAdapter() {
        nodeList = new ArrayList<>();
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DeviceListItemBinding deviceListItemBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.device_list_item, parent, false);
        return new DeviceViewHolder(deviceListItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Node node = nodeList.get(position);
        holder.deviceListItemBinding.setVm(new DeviceListItemViewModel(node));
    }

    void clear() {
        nodeList.clear();
    }

    void addAll(List<Node> list) {
        nodeList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {

        private DeviceListItemBinding deviceListItemBinding;

        DeviceViewHolder(@NonNull DeviceListItemBinding deviceListItemBinding) {
            super(deviceListItemBinding.getRoot());

            this.deviceListItemBinding = deviceListItemBinding;
        }
    }

}