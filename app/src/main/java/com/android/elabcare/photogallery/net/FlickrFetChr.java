package com.android.elabcare.photogallery.net;

import android.net.Uri;
import android.util.Log;

import com.android.elabcare.photogallery.model.GalleryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by runningwater on 2016/12/6.
 */

public class FlickrFetChr {
    private static final String TAG = "FlickrFetChr";
    private static final String API_KEY = "90b626dadf8e36eee3b7499d8411844e";

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + " : with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchItems() {
        List<GalleryItem> items = new ArrayList<GalleryItem>();
//        String url = Uri.parse("https://api.flickr.com/services/rest/")
//                .buildUpon()
//                .appendQueryParameter("method", "flickr.photos.getRecent")
//                .appendQueryParameter("api_key", API_KEY)
//                .appendQueryParameter("extras", "url_s")
//                .appendQueryParameter("format", "json")
//                .appendQueryParameter("nojsoncallback", "1")
//                .build().toString();
        String url = Uri.parse("http://image.baidu.com/channel/listjson")
                .buildUpon()
                .appendQueryParameter("pn", "0")
                .appendQueryParameter("rn", "30")
                .appendQueryParameter("tag1", "美女")
                .appendQueryParameter("tag2", "全部")
                .appendQueryParameter("ie", "utf8")
                .build().toString();
        try {
            Log.i(TAG, "Url: " + url);
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);

        } catch (IOException e) {
            //e.printStackTrace();
            Log.e(TAG, "Failed to fetch items", e);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }
        return items;
    }

    /**
     * 解析 Json 数据
     *
     * @param items
     * @param jsonBody 数据，期格式如下
     *                 {
     *                 "photos": {
     *                 "page": 1,
     *                 "pages": 10,
     *                 "perpage": 100,
     *                 "total": 1000,
     *                 "photo": [{
     *                 "id": "30633471944",
     *                 "owner": "21022123@N04",
     *                 "secret": "586b2d9c32",
     *                 "server": "5544",
     *                 "farm": 6,
     *                 "title": "Baltimore Museum of Art",
     *                 "ispublic": 1,
     *                 "isfriend": 0,
     *                 "isfamily": 0
     *                 },
     *                 {
     *                 "id": "31474846625",
     *                 "owner": "145532591@N02",
     *                 "secret": "eda462a3c7",
     *                 "server": "5524",
     *                 "farm": 6,
     *                 "title": "20161107_095207",
     *                 "ispublic": 1,
     *                 "isfriend": 0,
     *                 "isfamily": 0
     *                 }]
     *                 },
     *                 "stat": "ok"
     *                 }
     */
    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws JSONException {
        //JSONObject photosJsonObject = jsonBody.getJSONObject("");
        JSONArray photoJsonArray = jsonBody.getJSONArray("data");

        for (int i = 0; i < photoJsonArray.length() - 1; i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("abs"));

            if (!photoJsonObject.has("image_url")) {
                continue;
            }
            item.setUrl(photoJsonObject.getString("image_url"));
            items.add(item);
        }
    }
}
