/**
 * Copyright (C) 2010 Grameen Foundation
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy of
 the License at http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 License for the specific language governing permissions and limitations under
 the License.
 */

package org.grameenfoundation.consulteca.utils;

import android.util.Log;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.grameenfoundation.consulteca.ApplicationRegistry;
import org.grameenfoundation.consulteca.GlobalConstants;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Set of helper methods to abstract out network pain
 */
public class HttpHelpers {

    /**
     * Tag for logging purposes.
     */
    private static final String TAG = "HttpHelpers";

    // Network connection and read timeout (in milliseconds)
    public static final int NETWORK_TIMEOUT = 30 * 1000;

    public static final String LOCATION_HEADER = "x-applab-location";

    public HttpHelpers() {
    }

    /**
     * Convenience overload that takes a string
     */
    public static String fetchContent(String remoteAddress) {
        try {
            return fetchContent(new URI(remoteAddress), null);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * Convenience overload that takes a string and custom http headers.
     */
    public static String fetchContent(String remoteAddress, HashMap<String, String> headers) {
        try {
            return fetchContent(new URI(remoteAddress), headers);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * Downloads an HTML string from the specified location.
     *
     * @param remoteAddress The URI to fetch content from
     * @param headers       is a list of http headers that you may want to pass to the server. You can pass null if you do not
     *                      have any headers to send.
     * @return An HTML string; if null is returned, the caller should assume that the destination was unreachable and
     *         trigger the appropriate error behavior.
     */
    public static String fetchContent(URI remoteAddress, HashMap<String, String> headers) {
        try {
            return getUncompressedResponseString(fetchResponseObject(remoteAddress, headers));
        } catch (IOException e) {
            Log.e("HttpHelpers", "Problem fetching content", e);
            return null;
        }
    }

    /**
     * Gets an http response object from the specified location.
     *
     * @param remoteAddress The URI to fetch content from
     * @param headers       is a list of http headers that you may want to pass to the server. You can pass null if you do not
     *                      have any headers to send.
     * @return An http response object; if null is returned, the caller should assume that the destination was
     *         unreachable and trigger the appropriate error behavior.
     */
    public static HttpResponse fetchResponseObject(URI remoteAddress, HashMap<String, String> headers) {
        try {
            HttpParams httpParameters = HttpHelpers.getConnectionParameters();
            HttpClient client = new DefaultHttpClient(httpParameters);
            HttpGet getMethod = new HttpGet(remoteAddress);
            addCommonHeaders(getMethod);
            addHeaders(getMethod, headers);
            return client.execute(getMethod);
        } catch (IOException e) {
            Log.e("HttpHelpers", e.getMessage(), e);
            return null;
        }
    }

    public static InputStream getResource(String imageUrl) throws IOException {
        HttpURLConnection httpUrlConnection = null;

        URL url = new URL(imageUrl);
        httpUrlConnection = (HttpURLConnection) url.openConnection();
        HttpHelpers.addCommonHeaders(httpUrlConnection);
        httpUrlConnection.setConnectTimeout(NETWORK_TIMEOUT);
        httpUrlConnection.setReadTimeout(NETWORK_TIMEOUT);
        httpUrlConnection.connect();
        return getInputStream(httpUrlConnection);

    }

    private static InputStream getInputStream(HttpURLConnection httpUrlConnection) throws IOException {
        InputStream inputStream = httpUrlConnection.getInputStream();
        String contentEncoding = httpUrlConnection.getHeaderField("Content-Encoding");
        if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
            inputStream = new GZIPInputStream(inputStream);
        }
        return inputStream;
    }

    private static InputStream getInputStream(HttpResponse httpResponse) throws IllegalStateException, IOException {
        InputStream inputStream = httpResponse.getEntity().getContent();
        Header contentEncoding = httpResponse.getFirstHeader("Content-Encoding");
        if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
            inputStream = new GZIPInputStream(inputStream);
        }
        return inputStream;
    }

    /**
     * Constructs HTTP POST JSON request entity
     *
     * @param url
     * @param stringEntity
     * @return Response string entity
     * @throws java.io.IOException

    public static InputStream postJsonRequestAndGetStream(String url,
                                                          StringEntity stringEntity) throws IOException {
        return postDataRequestAndGetStream(url, stringEntity, "application/json; charset = UTF-8");
    }*/

    /**
     * Constructs HTTP POST JSON request entity
     *
     * @param url
     * @param stringEntity
     * @param networkTimeout
     * @return Response string entity
     * @throws java.io.IOException
     */
    public static InputStream postJsonRequestAndGetStream(String url, StringEntity stringEntity, int networkTimeout)
            throws IOException {
        return postDataRequestAndGetStream(url, stringEntity, "application/json; charset = UTF-8", networkTimeout);
    }

    public static InputStream postJsonRequestAndGetStream(String url, int networkTimeout,
                                                          List<NameValuePair> params) throws IOException {
        return postDataRequestAndGetStream(url, "application/x-www-form-urlencoded; charset = UTF-8",
                networkTimeout, params);
    }

    public static InputStream postFormRequestAndGetStream(String url, UrlEncodedFormEntity formEntity, int networkTimeout)
            throws IOException {
        return postDataRequestAndGetStream(url, formEntity, "application/x-www-form-urlencoded", networkTimeout);
    }

    /**
     * Constructs HTTP POST XML request entity
     *
     * @param url
     * @param stringEntity
     * @return Response string entity
     * @throws java.io.IOException
     */
    public static InputStream postXmlRequestAndGetStream(String url, StringEntity stringEntity) throws IOException {
        return postDataRequestAndGetStream(url, stringEntity, "text/xml");
    }

    public static InputStream postDataRequestAndGetStream(String url, StringEntity stringEntity, String contentType)
            throws IllegalStateException, ClientProtocolException, IOException {
        HttpParams httpParameters = HttpHelpers.getConnectionParameters();
        HttpClient httpClient = new DefaultHttpClient(httpParameters);
        HttpPost httpPost = new HttpPost(url);
        HttpHelpers.addCommonHeaders(httpPost);
        stringEntity.setContentType(contentType);
        httpPost.setEntity(stringEntity);
        return getInputStream(httpClient.execute(httpPost));
    }

    public static InputStream postDataRequestAndGetStream(String url, StringEntity stringEntity, String contentType,
                                                          int networkTimeout)
            throws IllegalStateException, ClientProtocolException, IOException {
        HttpParams httpParameters = HttpHelpers.getConnectionParameters(networkTimeout);
        HttpClient httpClient = new DefaultHttpClient(httpParameters);
        HttpPost httpPost = new HttpPost(url);
        HttpHelpers.addCommonHeaders(httpPost);
        stringEntity.setContentType(contentType);
        httpPost.setEntity(stringEntity);
        httpPost.addHeader("Accept-Encoding", "gzip");
        return getInputStream(httpClient.execute(httpPost));
    }

    public static InputStream postDataRequestAndGetStream(String url, String contentType, int networkTimeout,
                                                          List<NameValuePair> params)
            throws IllegalStateException, ClientProtocolException, IOException {
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params);
        entity.setContentType(contentType);

        HttpParams httpParameters = HttpHelpers.getConnectionParameters(networkTimeout);
        HttpClient httpClient = new DefaultHttpClient(httpParameters);
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Accept-Encoding", "gzip");

        httpPost.setEntity(entity);
        return getInputStream(httpClient.execute(httpPost));
    }

    public static InputStream postDataRequestAndGetStream(String url, UrlEncodedFormEntity formEntity,
                                                          String contentType, int networkTimeout)
            throws IllegalStateException, ClientProtocolException, IOException {
        HttpParams httpParameters = HttpHelpers.getConnectionParameters(networkTimeout);
        HttpClient httpClient = new DefaultHttpClient(httpParameters);
        HttpPost httpPost = new HttpPost(url);
        HttpHelpers.addCommonHeaders(httpPost);
        formEntity.setContentType(contentType);
        formEntity.setContentEncoding(HTTP.UTF_8);
        httpPost.setEntity(formEntity);
        httpPost.addHeader("HTTP-Version", "HTTP/1.0");
        httpPost.addHeader("Accept-Encoding", "gzip");
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset = UTF-8");

        HttpResponse res = httpClient.execute(httpPost);

        return getInputStream(res);
    }

    public static String postXmlRequest(String url, StringEntity stringEntity) throws IOException {
        HttpParams httpParameters = HttpHelpers.getConnectionParameters();
        HttpClient httpClient = new DefaultHttpClient(httpParameters);
        HttpPost httpPost = new HttpPost(url);
        HttpHelpers.addCommonHeaders(httpPost);
        stringEntity.setContentType("text/xml");
        httpPost.setEntity(stringEntity);
        return getUncompressedResponseString(httpClient.execute(httpPost));
    }

    private static Reader getUncompressedResponseReader(HttpResponse httpResponse) throws IllegalStateException, IOException {
        InputStream inputStream = getInputStream(httpResponse);
        return new BufferedReader(new InputStreamReader(inputStream));
    }

    public static Reader getUncompressedResponseReader(HttpURLConnection con) throws IllegalStateException, IOException {
        InputStream inputStream = getInputStream(con);
        return new BufferedReader(new InputStreamReader(inputStream));
    }

    /**
     * Checks if the data is g-zipped and unzips it first
     *
     * @param httpResponse
     * @return String
     * @throws java.io.IOException
     * @throws IllegalStateException
     */
    public static String getUncompressedResponseString(HttpResponse httpResponse) throws IllegalStateException, IOException {
        BufferedReader reader = (BufferedReader) getUncompressedResponseReader(httpResponse);
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n"); // TODO: Should this be \r\n or Environment.NewLine or something?
        }
        reader.close();
        return stringBuilder.toString();
    }

    public static StringBuilder getUncompressedResponseString(BufferedReader reader) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n"); // TODO: Should this be \r\n or Environment.NewLine or something?
        }
        reader.close();
        return stringBuilder;
    }

    /**
     * Creates HTTP client connection parameters
     *
     * @return collection of HTTP socket and connection timeout parameters
     */
    private static HttpParams getConnectionParameters() {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setSoTimeout(httpParameters, NETWORK_TIMEOUT);
        HttpConnectionParams.setConnectionTimeout(httpParameters, NETWORK_TIMEOUT);
        return httpParameters;
    }

    /**
     * Creates HTTP client connection parameters
     *
     * @return collection of HTTP socket and connection timeout parameters
     */
    private static HttpParams getConnectionParameters(int networkTimeout) {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setSoTimeout(httpParameters, networkTimeout);
        HttpConnectionParams.setConnectionTimeout(httpParameters, networkTimeout);
        return httpParameters;
    }

    /**
     * Adds our common headers
     */
    public static void addCommonHeaders(HttpPost httpPost) {
        HashMap<String, String> commonHeaders = getCommonHeaders();
        Set<String> keys = commonHeaders.keySet();
        for (String key : keys) {
            httpPost.addHeader(key, commonHeaders.get(key));
        }
    }

    public static void addCommonHeaders(URLConnection connection) {
        HashMap<String, String> commonHeaders = getCommonHeaders();
        Set<String> keys = commonHeaders.keySet();
        for (String key : keys) {
            connection.setRequestProperty(key, commonHeaders.get(key));
        }
    }

    private static void addCommonHeaders(HttpGet httpGet) {
        addHeaders(httpGet, getCommonHeaders());
    }

    private static void addHeaders(HttpGet httpGet, HashMap<String, String> headers) {
        if (headers == null) {
            return;
        }

        Set<String> keys = headers.keySet();
        for (String key : keys) {
            httpGet.addHeader(key, headers.get(key));
        }
    }

    private static void addHeaders(HttpPost httpPost, HashMap<String, String> headers) {
        if (headers == null) {
            return;
        }

        Set<String> keys = headers.keySet();
        for (String key : keys) {
            httpPost.addHeader(key, headers.get(key));
        }
    }

    /**
     * Gets a common headers url parameter string starting with ?
     *
     * @return the url parameter string with common headers.
     */
    public static String getCommonParameters() {
        // Add common Headers to the parameter string
        HashMap<String, String> commonHeaders = HttpHelpers.getCommonHeaders();
        String commonParameters = "";
        Set<String> keys = commonHeaders.keySet();
        Boolean isFirst = true;
        for (String key : keys) {
            if (isFirst) {
                commonParameters += "?";
                isFirst = false;
            } else {
                commonParameters += "&";
            }
            commonParameters += key + "=" + URLEncoder.encode(commonHeaders.get(key));
        }

        return commonParameters;
    }

    public static HashMap<String, String> getCommonHeaders() {
        HashMap<String, String> commonHeaders = new HashMap<String, String>();

        // always include the handset ID
        commonHeaders.put("x-Imei", (String) ApplicationRegistry.retrieve(GlobalConstants.KEY_CACHED_DEVICE_IMEI));

        commonHeaders.put("x-applab-appVersion",
                (String) ApplicationRegistry.retrieve(GlobalConstants.KEY_CACHED_APPLICATION_VERSION));

        //commonHeaders.put("Accept-Encoding", "gzip");

        //TODO append location headers in the http header
        //commonHeaders.put(LOCATION_HEADER, GpsManager.getInstance().getLocationAsString());

        return commonHeaders;
    }

    /**
     * Does an HTTP post for a given form data string.
     *
     * @param data is the form data string.
     * @param url  is the url to post to.
     * @return the return string from the server.
     * @throws java.io.IOException
     */
    public static String postData(String data, URL url) throws IOException {

        String result = null;

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            HttpHelpers.addCommonHeaders(conn);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setConnectTimeout(HttpHelpers.NETWORK_TIMEOUT);
            conn.setReadTimeout(HttpHelpers.NETWORK_TIMEOUT);
            conn.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

            writer.write(data);
            writer.flush();
            writer.close();

            String line;
            BufferedReader reader = (BufferedReader) getUncompressedResponseReader(conn);
            while ((line = reader.readLine()) != null) {
                if (result == null)
                    result = line;
                else
                    result += line;
            }

            reader.close();
        } catch (IOException ex) {
            Log.e(TAG, "Failed to read stream data", ex);

            String error = null;

            // TODO Am not yet sure if the section below should make it in the production release.
            // I mainly use it to get details of a failed http request. I get a FileNotFoundException
            // when actually the url is correct but an exception was thrown at the server and i use this
            // to get the server call stack for debugging purposes.
            try {
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    if (error == null)
                        error = line;
                    else
                        error += line;
                }

                reader.close();
            } catch (Exception e) {
                Log.e(TAG, "Problem encountered while trying to get error information:" + error, ex);
            }
        }

        return result;
    }
}