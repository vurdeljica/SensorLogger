package rs.ac.bg.etf.rti.sensorlogger.persistency;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.DeflaterOutputStream;

import rs.ac.bg.etf.rti.sensorlogger.SensorDataProtos;

/**
 * PersistenceManager is Singleton which gives api for
 * storing and deleting data gathered from sensors.
 */
public class PersistenceManager {

    private static final long HISTORY_DELETION_PERIOD = (4 * 60 + 15) * 60;

    private static PersistenceManager instance;
//    private AtomicInteger mobileFileId = new AtomicInteger(0);
//    private AtomicInteger deviceFileId = new AtomicInteger(0);

    private File dataDirectory;

    private PersistenceManager() {
        try {
            //Todo: Namestiti gde se cuvaju podaci, samo treba izmeniti dataPath
            //dataDirectory = new File(dataPath);
            dataDirectory = new File(Environment.getExternalStorageDirectory() + "/testDirectory");
            dataDirectory.mkdir();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Classic Singleton method for getting instance.
     * @return PersistenceManager instance
     */
    public static PersistenceManager getInstance() {
        if (instance == null) {
            instance = new PersistenceManager();
        }

        return instance;
    }

    /**
     * Create and run separate thread for storing data gathered from mobile device
     * @param _mobileData list of objects that represent sensor data gathered from mobile device
     * @param timestamp timestamp when sensor data is gathered
     */
    public void saveMobileData(List<SensorDataProtos.MobileData> _mobileData, long timestamp) {
        final List<SensorDataProtos.MobileData> mobileData = _mobileData;
        Thread thread = new Thread() {
            @Override
            public void run() {
//                int fileId = mobileFileId.getAndIncrement();
                String binaryFilePath = dataDirectory.getPath() + "/" + timestamp + "-mobile.txt";
                String compressedFilePath = dataDirectory.getPath() + "/" + timestamp + "-mobile-compressed.txt";

                try (FileOutputStream output = new FileOutputStream(binaryFilePath, false)) {
                    for (SensorDataProtos.MobileData mobileSensorData : mobileData) {
                        mobileSensorData.writeDelimitedTo(output);
                    }

                    compressFile(binaryFilePath, compressedFilePath);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        thread.start();
    }

    /**
     * Create and run separate thread for storing data gathered from smart watch
     * @param _deviceData list of objects that represent sensor data gathered from smart watch
     * @param timestamp timestamp when sensor data is gathered
     */
    public void saveDeviceData(List<SensorDataProtos.DeviceData> _deviceData, String nodeId, long timestamp) {
        final List<SensorDataProtos.DeviceData> deviceData = _deviceData;
        Thread thread = new Thread() {
            @Override
            public void run() {
//                int fileId = deviceFileId.getAndIncrement();
                String binaryFilePath = dataDirectory.getPath() + "/" + timestamp + "-device" + nodeId + ".txt";
                String compressedFilePath = dataDirectory.getPath() + "/" + timestamp + "-device" + nodeId + "-compressed.txt";

                try (FileOutputStream output = new FileOutputStream(binaryFilePath, false)) {
                    for (SensorDataProtos.DeviceData mobileDeviceData : deviceData) {
                        mobileDeviceData.writeDelimitedTo(output);
                    }

                    compressFile(binaryFilePath, compressedFilePath);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        thread.start();
    }

    private void compressFile(String inputFilePath, String outputFilePath) throws Exception {
        FileInputStream fis = new FileInputStream(inputFilePath);
        FileOutputStream fos = new FileOutputStream(outputFilePath);
        DeflaterOutputStream dos = new DeflaterOutputStream(fos);

        copyFiles(fis, dos);

        File file = new File(inputFilePath);
        boolean deleted = file.delete();
    }

    private void copyFiles(InputStream is, OutputStream os) throws Exception {
        int oneByte;
        while ((oneByte = is.read()) != -1) {
            os.write(oneByte);
        }
        os.close();
        is.close();
    }

    /**
     * Create and run separate thread for saving daily activities in json file
     */
    public void saveDailyActivity() {
        Thread thread = new Thread() {
            @Override
            public void run() {
               DatabaseManager dbManager = DatabaseManager.getInstance();
               File jsonFile = new File(dataDirectory.getPath() + "/dailyActivity-json.json");
               dbManager.saveToJson(jsonFile);
            }
        };

        thread.start();
    }

    /**
     * Delete data gathered from sensors in interval [timestamp - 4 hours, timestamp]
     * @param timestamp represents current timestamp
     */
    public void deleteLastFourHoursOfSensorData(long timestamp) {
        File[] files = dataDirectory.listFiles();
        for(int i = 0; i < files.length; i++) {
            File currentFile = files[i];
            String extension = currentFile.getName().substring(currentFile.getName().lastIndexOf("."));
            if (!extension.equals(".bin")) {
                continue;
            }
            long fileTimestamp = Long.parseLong(currentFile.getName().substring(0, currentFile.getName().indexOf("-")));
            long fileLifetimeMin = (timestamp - fileTimestamp) / 1000;

            if (fileLifetimeMin <= HISTORY_DELETION_PERIOD) {
                currentFile.delete();
            }
        }

    }

}
