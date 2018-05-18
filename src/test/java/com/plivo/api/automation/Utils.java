package com.plivo.api.automation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class Utils {

    public String readJsonDataFromFile(String arg1) throws Throwable {
        String json;
        FileInputStream fin = new FileInputStream(new File(System.getProperty("user.dir")+"//src//test//resources//testData//"+arg1));
        InputStreamReader in = new InputStreamReader(fin);
        BufferedReader bufferedReader = new BufferedReader(in);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        json = sb.toString();
        System.out.println(json);
        return json;
    }


    public static JSONObject getupdatedJsonData(JSONObject Object, String jsonPath, Object value) throws IOException, JSONException {
        String data= Object.toString();
        final Configuration jacksonJsonNode = Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider()).mappingProvider(new JacksonMappingProvider()).build();
        final DocumentContext doc;

        if (value .equals("null")) {
            doc = JsonPath.using(jacksonJsonNode).parse(data).set("$."+jsonPath, null);
        }else{
            doc = JsonPath.using(jacksonJsonNode).parse(data).set("$."+jsonPath, value);
        }


        String json1=doc.read("$").toString();
        JsonObject obj = new JsonParser().parse(json1).getAsJsonObject();
        return (new JSONObject (obj.toString()));
        //return obj;

    }
}
