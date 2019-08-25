package rs.ac.bg.etf.rti.sensorlogger.persistency;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import rs.ac.bg.etf.rti.sensorlogger.SensorDataProtos;

public class PersitencyManager {

    private static final long HISTORY_DELETION_PERIOD = 4 * 60 * 60;

    private static PersitencyManager instance;
    private AtomicInteger mobileFileId = new AtomicInteger(0);
    private AtomicInteger deviceFileId = new AtomicInteger(0);

    File dataDirectory;

    private PersitencyManager() {
        try {
            //Todo: Namestiti gde se cuvaju podaci, samo treba izmeniti dataPath
            //dataDirectory = new File(dataPath);
            dataDirectory = new File(Environment.getExternalStorageDirectory() + "/testDirectory");
            dataDirectory.mkdir();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PersitencyManager getInstance() {
        if (instance == null) {
            instance = new PersitencyManager();
        }

        return instance;
    }

    public void saveMobileData(List<SensorDataProtos.MobileData> mobileData) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                int fileId = mobileFileId.getAndIncrement();
                String binaryFilePath = dataDirectory.getPath() + "/mobile" + fileId + ".bin";
                String compressedFilePath = dataDirectory.getPath() + "/mobile" + fileId + "-compressed.bin";

                try(FileOutputStream output = new FileOutputStream(binaryFilePath, false))
                {
                    for (SensorDataProtos.MobileData mobileSensorData : mobileData) {
                        mobileSensorData.writeDelimitedTo(output);
                    }

                    compressFile(binaryFilePath, compressedFilePath);

                } catch(Exception e) {
                    e.printStackTrace();
                }

            }
        };

        thread.start();
    }

    public void saveDeviceData(List<SensorDataProtos.DeviceData> deviceData) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                int fileId = deviceFileId.getAndIncrement();
                String binaryFilePath = dataDirectory.getPath() + "/device" + fileId + ".bin";
                String compressedFilePath = dataDirectory.getPath() + "/device" + fileId + "-compressed.bin";

                try(FileOutputStream output = new FileOutputStream(binaryFilePath, false)) {
                    for (SensorDataProtos.DeviceData mobileDeviceData: deviceData) {
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

    public void deleteLastForHoursOfSensorData() {
        File[] files = dataDirectory.listFiles();
        for(int i = 0; i < files.length; i++) {
            File currentFile = files[i];
            String extension = currentFile.getPath().substring(currentFile.getPath().lastIndexOf("."));
            long fileLifetimeMin = (System.currentTimeMillis() - currentFile.lastModified()) / (1000 * 60);

            if (extension.equals("bin") && fileLifetimeMin <= HISTORY_DELETION_PERIOD) {
                currentFile.delete();
            }
        }

    }

}
