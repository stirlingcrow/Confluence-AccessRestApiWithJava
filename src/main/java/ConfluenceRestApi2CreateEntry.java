import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Creates a Confluence wiki page via the RESTul API
 * using an HTTP Post command.
 */
public class ConfluenceRestApi2CreateEntry {

    //private static final String BASE_URL = "http://localhost:1990/confluence";
    private static final String BASE_URL = "https://<context>.atlassian.net/wiki";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String ENCODING = "utf-8";

    public static String createContentRestUrl()throws UnsupportedEncodingException
    {
        return String.format("%s/rest/api/content/?&os_authType=basic&os_username=%s&os_password=%s", BASE_URL, URLEncoder.encode(USERNAME, ENCODING), URLEncoder.encode(PASSWORD, ENCODING));

    }

    public static void main(final String[] args) throws Exception
    {
        String wikiPageTitle = "My Awesome Page";
        String wikiPage = "<h1>Things That Are Awesome</h1><ul><li>Birds</li><li>Mammals</li><li>Decapods</li></ul>";
        String wikiSpace = "JOUR";
        String labelToAdd = "awesome_stuff";
        int parentPageId = 9994250;


        JSONObject newPage = defineConfluencePage(wikiPageTitle,
                wikiPage,
                wikiSpace,
                labelToAdd,
                parentPageId);

        createConfluencePageViaPost(newPage);

    }

    public static void createConfluencePageViaPost(JSONObject newPage) throws Exception
    {
        HttpClient client = new DefaultHttpClient();

        // Send update request
        HttpEntity pageEntity = null;

        try
        {
            //2016-12-18 - StirlingCrow: Left off here.  Was finally able to get the post command to work
            //I can begin testing adding more data to the value stuff (see above)
            HttpPost postPageRequest = new HttpPost(createContentRestUrl());

            StringEntity entity = new StringEntity(newPage.toString(), ContentType.APPLICATION_JSON);
            postPageRequest.setEntity(entity);

            HttpResponse postPageResponse = client.execute(postPageRequest);
            pageEntity = postPageResponse.getEntity();

            System.out.println("Push Page Request returned " + postPageResponse.getStatusLine().toString());
            System.out.println("");
            System.out.println(IOUtils.toString(pageEntity.getContent()));
        }
        finally
        {
            EntityUtils.consume(pageEntity);
        }
    }

    public static JSONObject defineConfluencePage(String pageTitle,
                                                  String wikiEntryText,
                                                  String pageSpace,
                                                  String label,
                                                  int parentPageId) throws JSONException
    {
        //This would be the command in Python (similar to the example
        //in the Confluence example:
        //
        //curl -u <username>:<password> -X POST -H 'Content-Type: application/json' -d'{
        // "type":"page",
        // "title":"My Awesome Page",
        // "ancestors":[{"id":9994246}],
        // "space":{"key":"JOUR"},
        // "body":
        //        {"storage":
        //                   {"value":"<h1>Things That Are Awesome</h1><ul><li>Birds</li><li>Mammals</li><li>Decapods</li></ul>",
        //                    "representation":"storage"}
        //        },
        // "metadata":
        //             {"labels":[
        //                        {"prefix":"global",
        //                        "name":"journal"},
        //                        {"prefix":"global",
        //                        "name":"awesome_stuff"}
        //                       ]
        //             }
        // }'
        // http://localhost:8080/confluence/rest/api/content/ | python -mjson.tool

        JSONObject newPage = new JSONObject();

        // "type":"page",
        // "title":"My Awesome Page"
        newPage.put("type","page");
        newPage.put("title", pageTitle);

        // "ancestors":[{"id":9994246}],
        JSONObject parentPage = new JSONObject();
        parentPage.put("id",parentPageId);

        JSONArray parentPageArray = new JSONArray();
        parentPageArray.put(parentPage);

        newPage.put("ancestors", parentPageArray);

        // "space":{"key":"JOUR"},
        JSONObject spaceOb = new JSONObject();
        spaceOb.put("key",pageSpace);
        newPage.put("space", spaceOb);

        // "body":
        //        {"storage":
        //                   {"value":"<p><h1>Things That Are Awesome</h1><ul><li>Birds</li><li>Mammals</li><li>Decapods</li></ul></p>",
        //                    "representation":"storage"}
        //        },
        JSONObject jsonObjects = new JSONObject();

        jsonObjects.put("value", wikiEntryText);
        jsonObjects.put("representation","storage");

        JSONObject storageObject = new JSONObject();
        storageObject.put("storage", jsonObjects);

        newPage.put("body", storageObject);


        //LABELS
        // "metadata":
        //             {"labels":[
        //                        {"prefix":"global",
        //                        "name":"journal"},
        //                        {"prefix":"global",
        //                        "name":"awesome_stuff"}
        //                       ]
        //             }
        JSONObject prefixJsonObject1 = new JSONObject();
        prefixJsonObject1.put("prefix","global");
        prefixJsonObject1.put("name","journal");
        JSONObject prefixJsonObject2 = new JSONObject();
        prefixJsonObject2.put("prefix","global");
        prefixJsonObject2.put("name",label);

        JSONArray prefixArray = new JSONArray();
        prefixArray.put(prefixJsonObject1);
        prefixArray.put(prefixJsonObject2);

        JSONObject labelsObject = new JSONObject();
        labelsObject.put("labels", prefixArray);

        newPage.put("metadata",labelsObject);

        return newPage;
    }
}
