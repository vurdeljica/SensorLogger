package rs.ac.bg.etf.rti.sensorlogger.model;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class DeviceSensorData extends RealmObject {

    private float accX;
    private float accY;
    private float accZ;

    private float gyrX;
    private float gyrY;
    private float gyrZ;

    private float magX;
    private float magY;
    private float magZ;

    private int heartRate;
    private int stepCount;

    @Index
    private String nodeId;

    @PrimaryKey
    private long timestamp;

    public DeviceSensorData() {
    }

    public DeviceSensorData(float accX, float accY, float accZ, float gyrX, float gyrY, float gyrZ,
                            float magX, float magY, float magZ, int heartRate, int stepCount,
                            String nodeId, long timestamp) {
        this.accX = accX;
        this.accY = accY;
        this.accZ = accZ;
        this.gyrX = gyrX;
        this.gyrY = gyrY;
        this.gyrZ = gyrZ;
        this.magX = magX;
        this.magY = magY;
        this.magZ = magZ;
        this.heartRate = heartRate;
        this.stepCount = stepCount;
        this.nodeId = nodeId;
        this.timestamp = timestamp;
    }

    public DeviceSensorData(DeviceSensorData deviceSensorData) {
        if (deviceSensorData == null) return;

        accX = deviceSensorData.getAccX();
        accY = deviceSensorData.getAccY();
        accZ = deviceSensorData.getAccZ();
        gyrX = deviceSensorData.getGyrX();
        gyrY = deviceSensorData.getGyrY();
        gyrZ = deviceSensorData.getGyrZ();
        magX = deviceSensorData.getMagX();
        magY = deviceSensorData.getMagY();
        magZ = deviceSensorData.getMagZ();
        heartRate = deviceSensorData.getHeartRate();
        stepCount = deviceSensorData.getStepCount();
        nodeId = deviceSensorData.getNodeId();
        timestamp = deviceSensorData.getTimestamp();
    }

    public DeviceSensorData(String nodeId, long timestamp) {
        this.nodeId = nodeId;
        this.timestamp = timestamp;
    }

    public float getAccX() {
        return accX;
    }

    public void setAccX(float accX) {
        this.accX = accX;
    }

    public float getAccY() {
        return accY;
    }

    public void setAccY(float accY) {
        this.accY = accY;
    }

    public float getAccZ() {
        return accZ;
    }

    public void setAccZ(float accZ) {
        this.accZ = accZ;
    }

    public float getGyrX() {
        return gyrX;
    }

    public void setGyrX(float gyrX) {
        this.gyrX = gyrX;
    }

    public float getGyrY() {
        return gyrY;
    }

    public void setGyrY(float gyrY) {
        this.gyrY = gyrY;
    }

    public float getGyrZ() {
        return gyrZ;
    }

    public void setGyrZ(float gyrZ) {
        this.gyrZ = gyrZ;
    }

    public float getMagX() {
        return magX;
    }

    public void setMagX(float magX) {
        this.magX = magX;
    }

    public float getMagY() {
        return magY;
    }

    public void setMagY(float magY) {
        this.magY = magY;
    }

    public float getMagZ() {
        return magZ;
    }

    public void setMagZ(float magZ) {
        this.magZ = magZ;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
