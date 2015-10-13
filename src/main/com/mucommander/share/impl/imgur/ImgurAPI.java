package com.mucommander.share.impl.imgur;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.File;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Mathias
 */
public class ImgurAPI {

    public final static String API_ENDPOINT = "https://api.imgur.com/3/";
    public final static String API_IMAGE = API_ENDPOINT + "image";
    private String API_KEY;

    public ImgurAPI(String API_KEY) {
        this.API_KEY = API_KEY;
    }

    public Future uploadAsync(File file, final Callback cb) {
        Logger.getLogger(ImgurJob.class.getName()).log(Level.INFO, "Uploading file {0}", file.getAbsoluteFile());

        Future<HttpResponse<JsonNode>> future = Unirest.post(API_IMAGE)
                .header("Authorization", "Client-ID " + API_KEY)
                .header("Content-Type", "multipart/form-data")
                .field("image", file).asJsonAsync(new com.mashape.unirest.http.async.Callback<JsonNode>() {

                    @Override
                    public void completed(HttpResponse<JsonNode> response) {

                        JSONArray array = response.getBody().getArray();
                        JSONObject jsonResponse = array.getJSONObject(0);

                        Boolean success = (Boolean) jsonResponse.get("success");
                        Integer status = (Integer) jsonResponse.getInt("status");

                        if (success && status == 200) {
                            Logger.getLogger(ImgurProvider.class.getName()).log(Level.INFO, "File upload to imgur completed");
                            JSONObject jsonData = jsonResponse.getJSONObject("data");
                            if (jsonData != null) {
                                String url = (String) jsonData.get("link");
                                cb.completed(url);
                            }
                        } else {
                            Logger.getLogger(ImgurProvider.class.getName()).log(Level.INFO, "success returned {0}", success);
                            Logger.getLogger(ImgurProvider.class.getName()).log(Level.INFO, "Status returned {0}", status);
                        }
                    }

                    @Override
                    public void failed(UnirestException e) {
                        Logger.getLogger(ImgurProvider.class.getName()).log(Level.INFO, "File upload to imgur failed");
                    }

                    @Override
                    public void cancelled() {
                        Logger.getLogger(ImgurProvider.class.getName()).log(Level.INFO, "File upload to imgur canceled");
                    }
                });
        return future;

    }

    public interface Callback {

        public void completed(String url);

    }

}
