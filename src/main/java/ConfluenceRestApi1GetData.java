import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * This class will fetch content data from
 * a Confluence entry (using HTTP GET) given
 * the Page ID and return the results in JSON.
 */
public class ConfluenceRestApi1GetData {

    //private static final String BASE_URL = "http://localhost:1990/confluence";
    private static final String BASE_URL = "https://<context>.atlassian.net/wiki";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String ENCODING = "utf-8";

    private static String getContentRestUrl(final Long contentId, final String[] expansions) throws UnsupportedEncodingException
    {
        final String expand = URLEncoder.encode(StringUtils.join(expansions, ","), ENCODING);

        return String.format("%s/rest/api/content/%s?expand=%s&os_authType=basic&os_username=%s&os_password=%s", BASE_URL, contentId, expand, URLEncoder.encode(USERNAME, ENCODING), URLEncoder.encode(PASSWORD, ENCODING));
    }

    public static void main(final String[] args) throws Exception
    {
        //This is the Page ID that can be found
        //if you go to the "Page Information" section
        //within Confluence
        final long pageId = 9994252;

        HttpClient client = new DefaultHttpClient();

        // Get current page version
        String pageObj = null;
        HttpEntity pageEntity = null;
        try
        {
            HttpGet getPageRequest = new HttpGet(getContentRestUrl(pageId, new String[] {"body.storage", "version", "ancestors"}));
            HttpResponse getPageResponse = client.execute(getPageRequest);
            pageEntity = getPageResponse.getEntity();

            pageObj = IOUtils.toString(pageEntity.getContent());

            System.out.println("Get Page Request returned " + getPageResponse.getStatusLine().toString());
            System.out.println("");
            System.out.println(pageObj);
        }
        finally
        {
            if (pageEntity != null)
            {
                EntityUtils.consume(pageEntity);
            }
        }


    }
}
