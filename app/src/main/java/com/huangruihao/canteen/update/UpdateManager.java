package com.huangruihao.canteen.update;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

/**
 * Created by alexwang on 6/6/16.
 * UpdateManager
 */
public class UpdateManager {
    public interface CheckUpdateCallback {
        void onCheckUpdateDone(boolean hasNewVersion, String latestVersion, String releaseDate);
        void onCheckUpdateFailed(String reason);
    }

    private static final int GET_BUFFER_SIZE = 5 * 1024; // 5KiB
    private static final int DOWNLOAD_BUFFER_SIZE = 5 * 1024 * 1024; // 5MiB
    private static final String URL_QUERY_LATEST_VERSION = "http://canteen9.a1ex.wang/apk/latest";
    private static final String URL_APK_RELEASE = "http://canteen9.a1ex.wang/apk/app-release.apk";
    private static final String URL_APK_RELEASE_MD5 = "http://canteen9.a1ex.wang/apk/md5sum";

    private static final String RELEASE_TIME_TAG = "2016-06-06 16:57:55 +0800";

    /** Send GET url, returns data string
     *
     * @param urlString: url
     * @return string content
     * @throws IOException
     */
    private static String get(String urlString) throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream());
            byte[] contents = new byte[GET_BUFFER_SIZE];

            int bytesRead;
            String strFileContents = "";
            while((bytesRead = in.read(contents)) != -1) {
                strFileContents += new String(contents, 0, bytesRead);
            }
            System.out.print(strFileContents);
            return strFileContents;
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    /**
     * Send GET url request, save result to file
     * @param urlString url
     * @param savePath where you want to save the result
     * @throws IOException
     */
    private static void download(String urlString, String savePath) throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream());
            byte[] contents = new byte[DOWNLOAD_BUFFER_SIZE];

            FileOutputStream fos = new FileOutputStream(savePath);

            int bytesRead;
            int totalBytes = 0;
            while((bytesRead = in.read(contents)) != -1) {
                fos.write(contents, 0, bytesRead);
                totalBytes += bytesRead;
                System.out.print("downloading, downloaded bytes" + totalBytes);
            }
            System.out.print("download done, total bytes" + totalBytes);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    /**
     * Calculate MD5 hash of a binary/text file in hexadecimal representation.
     * @param filePath file path, null if Android system does not support the MD5 algorithm
     * @return hexadecimal string of MD5 hash
     */
    public static String calculateMD5(String filePath) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e("calculateMD5", "Exception while getting Digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            Log.e("calculateMD5", "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e("calculateMD5", "Exception on closing MD5 input stream", e);
            }
        }
    }

    public void checkUpdate(final CheckUpdateCallback cb) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String latestVersion = get(URL_QUERY_LATEST_VERSION);
                    String[] versionStrings = latestVersion.split("\n");
                    if (versionStrings.length == 2) {
                        String version = versionStrings[0];
                        String dateString = versionStrings[1];

                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
                        Date latestReleaseDate = format.parse(dateString);
                        Date currentReleaseDate = format.parse(RELEASE_TIME_TAG);
                        cb.onCheckUpdateDone(
                                latestReleaseDate.getTime() > currentReleaseDate.getTime(),
                                version,
                                dateString);
                    }
                } catch (IOException|ParseException e) {
                    e.printStackTrace();
                    cb.onCheckUpdateFailed("网络错误");
                }
            }
        }.start();
    }

    public void doUpdate(final Context context) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String savePath = Environment.getExternalStorageDirectory() +"/canteen9-release.apk";
                    download(URL_APK_RELEASE, savePath);

                    String md5 = calculateMD5(savePath);
                    String correctMd5 = get(URL_APK_RELEASE_MD5);
                    if (md5 != null && correctMd5 != null && md5.equals(correctMd5)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(
                                Uri.fromFile(new File(savePath)),
                                "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                    else {
                        Log.e("doUpdate", String.format("%s != %s", md5, correctMd5));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
