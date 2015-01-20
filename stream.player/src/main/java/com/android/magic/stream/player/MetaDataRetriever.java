package com.android.magic.stream.player;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Subscriber;

/**
 * Handles retrieving and parsing of the metadata to get the stream title.
 */
/*package*/ class MetaDataRetriever {

    private static final String LOG_TAG = MetaDataRetriever.class.getSimpleName();

    private static final String STREAM_TITLE = "StreamTitle";

    protected URL mStreamUrl;
    private Map<String, String> mMetadata;
    private boolean isError;

    public MetaDataRetriever(URL streamUrl) {
        setStreamUrl(streamUrl);

        isError = false;
    }

    /**
     * Get the mMetadata of the url async, every 5 seconds
     *
     * @return a mapping containing the url and the stream title and possibly artist
     */
    public Observable<String> getMetadataAsync() {

        return Observable.create(
                new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(
                            final Subscriber<? super String> observer) {
                        Log.d(LOG_TAG, "call refresh");
                        try {
                            retreiveMetadata();
                            observer.onNext(mMetadata.get(STREAM_TITLE));
                        } catch (IOException e) {
                        }

                    }
                });
    }

    public String getMetadata() throws IOException {
        retreiveMetadata();

        return mMetadata.get(STREAM_TITLE);
    }

    private void retreiveMetadata() throws IOException {
        URLConnection con = mStreamUrl.openConnection();
        con.setRequestProperty("Icy-MetaData", "1");
        con.setRequestProperty("Connection", "close");
        con.setRequestProperty("Accept", null);
        con.connect();

        int metaDataOffset = 0;
        Map<String, List<String>> headers = con.getHeaderFields();
        InputStream stream = con.getInputStream();

        if (headers.containsKey("icy-metaint")) {
            // Headers are sent via HTTP
            metaDataOffset = Integer.parseInt(headers.get("icy-metaint").get(0));
        } else {
            // Headers are sent within a stream
            StringBuilder strHeaders = new StringBuilder();
            char c;
            while ((c = (char) stream.read()) != -1) {
                strHeaders.append(c);
                if (strHeaders.length() > 5 && (strHeaders.substring(
                        (strHeaders.length() - 4), strHeaders.length()).equals("\r\n\r\n"))) {
                    // end of headers
                    break;
                }
            }

            // Match headers to get mMetadata offset within a stream
            Pattern p = Pattern.compile("\\r\\n(icy-metaint):\\s*(.*)\\r\\n");
            Matcher m = p.matcher(strHeaders.toString());
            if (m.find()) {
                metaDataOffset = Integer.parseInt(m.group(2));
            }
        }

        // In case no data was sent
        if (metaDataOffset == 0) {
            isError = true;
            return;
        }

        // Read mMetadata
        int b;
        int count = 0;
        int metaDataLength = 4080; // 4080 is the max length
        boolean inData = false;
        StringBuilder metaData = new StringBuilder();
        // Stream position should be either at the beginning or right after headers
        while ((b = stream.read()) != -1) {
            count++;

            // Length of the mMetadata
            if (count == metaDataOffset + 1) {
                metaDataLength = b * 16;
            }

            if (count > metaDataOffset + 1 && count < (metaDataOffset + metaDataLength)) {
                inData = true;
            } else {
                inData = false;
            }
            if (inData) {
                if (b != 0) {
                    metaData.append((char) b);
                }
            }
            if (count > (metaDataOffset + metaDataLength)) {
                break;
            }

        }

        // Set the data
        mMetadata = MetaDataRetriever.parseMetadata(metaData.toString());

        // Close
        stream.close();
    }

    public boolean isError() {
        return isError;
    }

    public URL getStreamUrl() {
        return mStreamUrl;
    }

    public void setStreamUrl(URL streamUrl) {
        this.mMetadata = null;
        this.mStreamUrl = streamUrl;
        this.isError = false;
    }

    public static Map<String, String> parseMetadata(String metaString) {
        Map<String, String> metadata = new HashMap();
        String[] metaParts = metaString.split(";");
        Pattern p = Pattern.compile("^([a-zA-Z]+)=\\'([^\\']*)\\'$");
        Matcher m;
        for (int i = 0; i < metaParts.length; i++) {
            m = p.matcher(metaParts[i]);
            if (m.find()) {
                metadata.put((String) m.group(1), (String) m.group(2));
            }
        }

        return metadata;
    }
}