package com.pgore.uploader;


import com.pgore.options.CommandLineOptions;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;

import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpParams;

import java.io.File;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FileUploader {

    public static void main(String[] args) throws IOException, OptionException {

        System.out.println(FileUploader.class.getCanonicalName());
        CommandLineOptions options = new CommandLineOptions(args);

        URL uploadPath = options.getHttpURL();
        String filePath = options.getFilePath();
        File file = new File(filePath);

        HttpClient client = new HttpClient();
        PutMethod put = new PutMethod(uploadPath.toString());
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/octet-stream");


        Part[] part = {
                new FilePart(file.getName(), file)
        };

        //Set headers
        for(Map.Entry<String, String> entry: headers.entrySet())
        {
            put.setRequestHeader(new Header(entry.getKey(), entry.getValue()));
        }

        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
        put.setRequestEntity( new MultipartRequestEntity(part,put.getParams()));

        int status = client.executeMethod(put);
        if(status >= 300){
            System.out.println(new String(put.getResponseBody()));
            throw new IOException(String.format("Error uploading file to %s. Error Code: %s", uploadPath, status));
        }else{
            System.out.println(String.format("Uploaded file:%s successfully", filePath));
        }
    }
}
