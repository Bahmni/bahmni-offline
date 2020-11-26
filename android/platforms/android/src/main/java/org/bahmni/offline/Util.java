package org.bahmni.offline;

import org.joda.time.DateTime;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;

public class Util {
    //    TODO: Hemanth/Abishek/Ranganathan - this calss will be deleted once we build eventlog for person_attributes.
    static HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public String getData(URL url) throws IOException {
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setHostnameVerifier(hostnameVerifier);              //TODO : Have to fix this       RE: Hemanth - Don't have to. This method will go away.
        InputStream inputStream = con.getInputStream();
        return convertStreamToString(inputStream);
    }

    private String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static DateTime addMinutesToDate(int minutes, Date beforeTime){
        final long ONE_MINUTE_IN_MILLIS = 60000;

        long curTimeInMs = beforeTime.getTime();
        return new DateTime(curTimeInMs + (minutes * ONE_MINUTE_IN_MILLIS));
    }
}
