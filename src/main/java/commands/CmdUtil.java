package commands;


import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;

public class CmdUtil
{
    /**
     * This method sends a HTTP request to the specified URL.
     * @param URL
     * @return
     * String - The data returned from the request
     * null - Error occured.
     */
    public static String sendHTTPRequest(String URL)
    {
        return sendHTTPRequestWithHeader(URL, null);
    }

    /**
     * This method sends a HTTP request to the specified URL, including the specified headers.<br>
     * The HashMap's key will be used as the header name, with the data as the header data.
     * @param URL
     * @param headers
     * @return
     * String - The data returned from the request
     * null - Error occured.
     */
    public static String sendHTTPRequestWithHeader(String URL, HashMap<String, String> headers)
    {
        try
        {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(URL);
            if (headers != null)
            {
                headers.forEach((name, value) -> httpGet.addHeader(name, value));
            }
            ResponseHandler<String> responseHandler = response ->
            {
                int status = response.getStatusLine().getStatusCode();
                if (status == 200)
                {
                    HttpEntity httpEntity = response.getEntity();
                    if (httpEntity != null)
                    {
                        return EntityUtils.toString(httpEntity);
                    }
                    else
                    {
                        return null;
                    }
                }
                else
                {
                    throw new ClientProtocolException("HTML returned code: " + status + ".");
                }
            };
            String pageData = httpClient.execute(httpGet, responseHandler);
            httpClient.close();
            return StringEscapeUtils.unescapeHtml4(pageData);
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
            return "";
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "";
        }
    }
}
