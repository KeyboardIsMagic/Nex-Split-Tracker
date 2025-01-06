package com.Utils;

import okhttp3.*;
import okio.BufferedSink;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;


public class HttpUtil
{
    private static final MediaType JSON = MediaType.parse("application/json");

    public static String postRequest(OkHttpClient client, String urlString, String jsonInput) throws IOException
    {
        RequestBody body = RequestBody.create(JSON, jsonInput);

        Request request = new Request.Builder()
                .url(urlString)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute())
        {
            if (!response.isSuccessful())
            {
                throw new IOException("Unexpected response code: " + response.code());
            }

            ResponseBody responseBody = response.body();
            return (responseBody != null) ? responseBody.string() : "";
        }
    }


    public static String getRequest(OkHttpClient client, String urlString) throws IOException
    {
        Request request = new Request.Builder()
                .url(urlString)
                .get()
                .build();

        try (Response response = client.newCall(request).execute())
        {
            if (!response.isSuccessful())
            {
                throw new IOException("Unexpected response code: " + response.code());
            }

            ResponseBody responseBody = response.body();
            return (responseBody != null) ? responseBody.string() : "";
        }
    }


    public static void sendUniqueDiscord(
            OkHttpClient client,
            String urlString,
            List<String> partyList,
            String leader,
            String itemName,
            File imageFile
    ) throws IOException
    {
        String mimeType = Files.probeContentType(imageFile.toPath());
        if (mimeType == null)
        {
            mimeType = "application/octet-stream";
        }

        RequestBody fileBody = RequestBody.create(MediaType.parse(mimeType), imageFile);

        MultipartBody.Builder formBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("player_names", String.join(",", partyList))
                .addFormDataPart("leader", leader)
                .addFormDataPart("item_name", itemName)
                .addFormDataPart("screenshot", imageFile.getName(), fileBody);

        Request request = new Request.Builder()
                .url(urlString)
                .post(formBuilder.build())
                .build();


        try (Response response = client.newCall(request).execute())
        {
            if (!response.isSuccessful())
            {
                throw new IOException("Unexpected response code: " + response.code());
            }

        }
    }
}
