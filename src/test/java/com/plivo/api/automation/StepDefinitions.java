package com.plivo.api.automation;

import cucumber.api.DataTable;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.response.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.json.JSONObject;
import java.util.*;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class StepDefinitions {

    Properties properties = new Properties();
    private RequestSpecification request = RestAssured.with();
    private Response response;
    private String respValue;
    Utils util = new Utils();
    JSONObject base = null;
    String storedValue;

    @Before(order = 1)
    public void setProperties(){
        try {
            File file = new File("src/test/resources/config.properties");
            FileInputStream fileInput = new FileInputStream(file);
            properties.load(fileInput);
            fileInput.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    @Given("Basic auth$")
    public void auth_token() {
        try {
            String userName = properties.getProperty("auth.id");
            String password = properties.getProperty("auth.token");
            String baseUrl = properties.getProperty("baseUrl") + "/"+userName;
            request.given().baseUri(baseUrl);
            request.given().auth().basic(userName, password);
        }catch (Throwable exception) {
            exception.printStackTrace();
        }
    }


    @And("^Request params$")
    public void request_params_are(DataTable queryParams){
//        String brandId;
        Map<String,Object> map = new HashMap<String,Object>();
        List<Map<String,String>> data = queryParams.asMaps(String.class,String.class);
        for(int i=0; i<data.size();i++) {
            String keyMain = data.get(i).get("key");
            Object keyValue = data.get(i).get("value");
            String tempValue=keyValue.toString();
            if (tempValue.contains("response")) {
                if (tempValue.contains(":")) {
                    String jsonKey = tempValue.split(":")[1];
                    Object tempValue2 = get_the_value_from_response(jsonKey);
                    keyValue = tempValue2.toString();
                } else {
                    keyValue = respValue;
                }
            }
            map.put(keyMain, keyValue);
        }
        request.given()
                .queryParams(map).log().all();
    }


    @Then("^the status code is (\\d+) else \"(.*?)\"$")
    public void verify_status_code(int statusCode, String message){
        int expStatus = response.getStatusCode();
        if(expStatus!=statusCode)
        {
            System.out.println(message);
            fail("Expected code:"+statusCode+" Actual code:"+expStatus+" "+message);
        }
    }

    @And("^get the (.*?) from the response$")
    public String get_the_value_from_response(String key) {

        if (key.equals("response")) {
            respValue = response.body().prettyPrint();

        } else {
            Object valueFromResp = response.then().extract().body().jsonPath().get(key);

            if (valueFromResp instanceof ArrayList) {
                ArrayList arrayId = ((ArrayList) valueFromResp);
                respValue = arrayId.get(0).toString();
            } else if (valueFromResp instanceof Integer) {
                int temp = ((Integer) valueFromResp).intValue();
                respValue = String.valueOf(temp);
            } else {
                respValue = valueFromResp.toString();
            }
        }
        return respValue;
    }


    @When("^user hits \"(.*?)\" (.*?) api$")
    public void api_request_is_made(String uri, String method) {
        try {
            if (method.equalsIgnoreCase("get")) {
                response = request.when().get(uri);
            } else if ((method.equalsIgnoreCase("post")))
                response = request.when().post(uri);
            response.prettyPrint();
        } catch (Exception e) {
            e.printStackTrace();
            fail("unable to hit the request");
        } finally {
            request = RestAssured.with();
        }
    }


    @When("^user hits \"(.*?)\" (.*?) api with id \"(.*?)\"$")
    public void api_request_is_made_with_id(String uri, String method, String id) {
        try {
            if(id.contains("response")) {
                id = respValue;
            }
            String apiUrl = uri + "/" + id;
            if ((method.equalsIgnoreCase("get")))
                response = request.when().get(apiUrl);
            else if ((method.equalsIgnoreCase("post")))
                response = request.when().post(apiUrl);
            response.prettyPrint();
        } catch (Exception e) {
            e.printStackTrace();
            fail("unable to hit the request");
        } finally {
            request = RestAssured.with();
        }
    }


    @And("^Request body as json$")
    public void request_body_as_json(DataTable jsonParams) {
        String jsonFileName;
        String jsonData;
        base = null;

        try {
            List<Map<String, String>> data = jsonParams.asMaps(String.class, String.class);
            jsonFileName = data.get(0).get("value");
            //System.out.println("Json file:" + jsonFileName);
            if (jsonFileName.contains("response")) {
                jsonData = response.body().prettyPrint();
            } else {
                jsonData = util.readJsonDataFromFile(data.get(0).get("value"));
            }
            base = new JSONObject(jsonData);
            for (int i = 1; i < data.size(); i++) {
                String keyMain = data.get(i).get("key");
                String keyValue = data.get(i).get("value");
                Object value = new Object();

                if (keyValue.contains("response")) {
                    if (keyValue.contains(":")) {
                        String jsonKey = keyValue.split(":")[1];
                        Object tempValue = get_the_value_from_response(jsonKey);
                        value = tempValue.toString();
                    } else {
                        value = respValue;
                    }
                } else {
                    value=keyValue;
                }
                base = util.getupdatedJsonData(base, keyMain, value);
            }

            request.given().contentType(ContentType.JSON)
                    .body(base.toString())
                    .log()
                    .all();
        } catch (Exception e) {
            e.printStackTrace();
            fail("unable to update the json file");
        } catch (Throwable t) {
            t.getStackTrace();
            fail("unable to update the json file");
        }
    }

    @And("(.*?) response length should be (\\d+)")
    public void response_length_should_be(String key, int length) {
        if (key.equalsIgnoreCase("json"))
            response.then().body("result.size()", is(length));
        else
            response.then().body(key + ".size()", is(length));

    }

    @And("^Returned json schema is \"(.*?)\"$")
    public void schema_is(String fileName) {
        String basePath = properties.getProperty("schema.path");
        File file = new File(basePath + fileName);
        response.then()
                .body(matchesJsonSchema(file));
    }

    @And("^Response should contain$")
    public void response_should_contain_data(DataTable responseParams) {
        List<Map<String, String>> data = responseParams.asMaps(String.class, String.class);
        for (int j = 0; j < data.size(); j++) {
            String keyMain = data.get(j).get("key");
            String keyValue = data.get(j).get("value");
            if (keyValue.contains("resp")) {
                keyValue = respValue;
            }
            if (!keyValue.equals("null")) {
                assertThat(response.path(keyMain).toString(), containsString(keyValue));
            } else {
                assertNull(response.path(keyMain));
            }
        }
    }

    @And("^store the resp value$")
    public void storeThisValue() {
        storedValue = respValue;
    }

    @And("^verify the cash deducted is equal to \"(.*?)\"$")
    public void verify_the_cash_deducted(String actual) {
        Float expectedValue = Float.valueOf(storedValue)-Float.valueOf(respValue);
        String actVal = get_the_value_from_response(actual);
        String expVal = String.valueOf(expectedValue);
        assertEquals(expVal ,actVal);
    }


}
