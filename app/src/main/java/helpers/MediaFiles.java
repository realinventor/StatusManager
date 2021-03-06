package helpers;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by JIMMY on 14-Feb-18.
 */

public class MediaFiles {
    public static ArrayList<String> allFiles = new ArrayList<String>();
    public static ArrayList<String> imageFiles = new ArrayList<String>();
    public static ArrayList<String> videoFiles = new ArrayList<String>();
    public static ArrayList<String> savedVideoFiles = new ArrayList<>();
    public static ArrayList<String> savedImageFiles = new ArrayList<>();
    public static ArrayList<String> savedFiles = new ArrayList<>();
    private static String APP_FOLDER_NAME = "StatusManager";
    public static String WHATSAPP_STATUS_FOLDER_PATH = "";
    public static String WHATSAPP_STATUS_FOLDER_PATH_OLD = Environment.getExternalStorageDirectory()+"/Whatsapp/Media/.Statuses/";
    public static String WHATSAPP_STATUS_FOLDER_PATH_NEW = Environment.getExternalStorageDirectory() + "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses/";
    public static final String DOWNLOADED_IMAGE_PATH = Environment.getExternalStorageDirectory()+"/"+APP_FOLDER_NAME+"/Saved/";


    //Copy to App folder operations
    public static void copyToDownload(String path){
        File src = new File(path);
        File des = new File(DOWNLOADED_IMAGE_PATH, src.getName());
//        Log.d("source file",src.getPath());
//        Log.d("destination file",src.getPath());
        try{
            copyFile(src,des);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }


    public static void initSavedFiles(){
        File directory = new File(DOWNLOADED_IMAGE_PATH);
        savedFiles.clear();
        savedVideoFiles.clear();
        savedImageFiles.clear();

        File[] files = directory.listFiles();
//        Log.d("Files", "Size: "+ files.length);
        if (files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().endsWith(".mp4") || files[i].getName().endsWith(".jpg")) {
                    savedFiles.add(files[i].getName());
                }
                if (files[i].getName().endsWith(".mp4")) {
                    savedVideoFiles.add(files[i].getName());
                }
                if (files[i].getName().endsWith(".jpg")) {
                    savedImageFiles.add(files[i].getName());
                }
            }
        }

    }

    public static ArrayList<String> getSavedFiles(){
        return savedFiles;
    }

    public static ArrayList<String> getSavedVideoFiles(){
        return savedVideoFiles;
    }

    public static ArrayList<String> getSavedImageFiles() {
        return savedImageFiles;
    }

    public static boolean doesWhatsappDirExist(){
        File dir = new File(WHATSAPP_STATUS_FOLDER_PATH);
        if (!dir.exists() || !dir.isDirectory()) {
            return false;
        }
        return true;
    }

    public static boolean checkIfDirExists(String path){
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            return false;
        }
        return true;
    }

    public static void initAppDirectories(){
        File dir = new File(Environment.getExternalStorageDirectory() + "/" +APP_FOLDER_NAME);
        if(dir.exists() && dir.isDirectory()) {
            dir = new File(Environment.getExternalStorageDirectory() + "/" +APP_FOLDER_NAME + "/Saved");
            if(!(dir.exists() && dir.isDirectory())) {
                dir.mkdir();
            }
        }
        else{
            dir.mkdir();
            dir = new File(Environment.getExternalStorageDirectory() + "/" +APP_FOLDER_NAME + "/Saved");
            dir.mkdir();
        }
    }

    public static void initWhatsAppDirPath(){
        if(checkIfDirExists(WHATSAPP_STATUS_FOLDER_PATH_NEW)){
            WHATSAPP_STATUS_FOLDER_PATH = WHATSAPP_STATUS_FOLDER_PATH_NEW;
        }
        else {
            WHATSAPP_STATUS_FOLDER_PATH = WHATSAPP_STATUS_FOLDER_PATH_OLD;
        }
    }

    //Method to initialise allFiles, imageFiles, videoFiles
    public static void initMediaFiles(){
        imageFiles.clear();
        videoFiles.clear();
        allFiles.clear();

        File directory = new File(WHATSAPP_STATUS_FOLDER_PATH);
//        Log.d("Directory exists:",""+doesWhatsappDirExist());
        File[] files = directory.listFiles();

        if(files != null) {   //Handling crash NullPointerException
//        Log.d("Files", "Size: "+ files.length);
            for (int i = 0; i < files.length; i++) {
                allFiles.add(files[i].getName());
                if (files[i].getName().endsWith(".jpg") || files[i].getName().endsWith(".gif")) {
                    imageFiles.add(files[i].getName());
                } else if (files[i].getName().endsWith(".mp4")) {
                    videoFiles.add(files[i].getName());
                }
            }
        }
//        for (String s:imageFiles){
//            Log.d("Image file: ",s);
//        }
//
//        for (String t:videoFiles){
//            Log.d("Video file: ",t);
//        }
    }

    public static ArrayList<String> getAllFiles(){
        return allFiles;
    }

    public static ArrayList<String> getImageFiles() {
        return imageFiles;
    }

    public static ArrayList<String> getVideoFiles() {
        return videoFiles;
    }


    public static float removeExpired(){

        File directory = new File(WHATSAPP_STATUS_FOLDER_PATH);
//        Log.d("Directory exists:",""+doesWhatsappDirExist());
        File[] files = directory.listFiles();
        Date currentTime = new Date();

        float file_size_total = 0;

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentTime);
        cal.add(Calendar.DATE, -1);
        Date dateBeforeOneDay = cal.getTime();
        Log.d("Limit date",dateBeforeOneDay.toString());

        if(files != null){
            int file_count = 0;

            for(int i=0;i<files.length;i++){
                if(new Date(files[i].lastModified()).before(dateBeforeOneDay)){
                    file_count++;
                    file_size_total += files[i].length();
                    files[i].delete();
                }
            }
            Log.d("File count ","Expired files "+file_count);
            Log.d("File size ","Total size of files freed "+file_size_total);

            file_size_total = (file_size_total /1024)/1024;
        }

        return file_size_total;
    }
}
