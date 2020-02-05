package rs.ac.bg.etf.rti.sensorlogger.persistency;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import rs.ac.bg.etf.rti.sensorlogger.SensorDataProtos;

/**
 * PersistenceManager is Singleton which gives api for
 * storing and deleting data gathered from sensors.
 */
public class PersistenceManager {

    private static final long HISTORY_DELETION_PERIOD = (4 * 60 + 15) * 60;

    private static PersistenceManager instance;

    private File dataDirectory;

    private PersistenceManager() {
        try {
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
     * Stores location data
     * @param _locationData list of objects that represent location data
     * @param timestamp timestamp when sensor data is gathered
     */
    public void saveLocationData(List<SensorDataProtos.LocationData> _locationData, long timestamp) {
    final List<SensorDataProtos.LocationData> locationData = _locationData;
        //                int fileId = mobileFileId.getAndIncrement();
        String timeCreation = getTimeCreated(timestamp);
        String binaryFilePath = dataDirectory.getPath() + "/" + timestamp + "-location" + timeCreation + ".txt";
    String tempFilePath = dataDirectory.getPath() + "/" + timestamp + "-location-temp" + timeCreation + ".txt";
    String compressedFilePath = dataDirectory.getPath() + "/" + timestamp + "-location-compressed" + timeCreation + ".txt";

        try (FileOutputStream output = new FileOutputStream(binaryFilePath, false)) {
        for (SensorDataProtos.LocationData location : locationData) {
            location.writeDelimitedTo(output);
        }

        compressFile(binaryFilePath, tempFilePath);
        renameFile(tempFilePath, compressedFilePath);

    } catch (Exception e) {
        e.printStackTrace();
    }

}

    private String getTimeCreated(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("-yyyy-MM-dd-HH-mm-ss-");
        Date date = new Date(timestamp);
        return formatter.format(date);
    }

    /**
     * Stores data gathered from sensors
     * @param _sensorData list of objects that represent sensor data gathered from sensors
     * @param timestamp timestamp when sensor data is gathered
     */
    public void saveSensorData(List<SensorDataProtos.SensorData> _sensorData, String nodeId, long timestamp) {
        final List<SensorDataProtos.SensorData> sensorData = _sensorData;
//                int fileId = deviceFileId.getAndIncrement();
        String timeCreation = getTimeCreated(timestamp);

        String binaryFilePath = dataDirectory.getPath() + "/" + timestamp + "-device" + nodeId + timeCreation + ".txt";
        String tempFilePath = dataDirectory.getPath() + "/" + timestamp + "-device" + nodeId + timeCreation + "-temp.txt";
        String compressedFilePath = dataDirectory.getPath() + "/" + timestamp + "-device" + nodeId + timeCreation + "-compressed.txt";

        try (FileOutputStream output = new FileOutputStream(binaryFilePath, false)) {
            for (SensorDataProtos.SensorData sensor : sensorData) {
                sensor.writeDelimitedTo(output);
            }

            compressFile(binaryFilePath, tempFilePath);
            renameFile(tempFilePath, compressedFilePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void compressFile(String inputFilePath, String outputFilePath) throws Exception {
        FileInputStream fis = new FileInputStream(inputFilePath);
        FileOutputStream fos = new FileOutputStream(outputFilePath);
        DeflaterOutputStream dos = new DeflaterOutputStream(fos, new Deflater(Deflater.BEST_SPEED));

        copyFiles(fis, dos);

        File file = new File(inputFilePath);
        boolean deleted = file.delete();
    }

    private void renameFile(String originalPath, String renamedPath) {
        File from = new File(originalPath);
        File to = new File(renamedPath);
        from.renameTo(to);
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
               File jsonFileTemp = new File(dataDirectory.getPath() + "/dailyActivity-temp.json");
               String jsonFilePath = dataDirectory.getPath() + "/dailyActivity-json.json";
               dbManager.saveToJson(jsonFileTemp);
               renameFile(jsonFileTemp.getPath(), jsonFilePath);
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
        if (files == null) {
            return;
        }
        for (File currentFile : files) {
            String extension = currentFile.getName().substring(currentFile.getName().lastIndexOf("."));
            if (extension.equals(".json")) {
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
