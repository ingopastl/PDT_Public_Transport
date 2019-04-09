package services;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

public class HttpManager {
    private static HttpManager instance;

    private CloseableHttpClient client;

    private HttpManager() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();

        HttpClientBuilder builder = HttpClients.custom();
        builder.setConnectionManager(cm);
        this.client = builder.build();
    }

    public static HttpManager getInstance() {
        if (instance == null) {
            instance = new HttpManager();
        }
        return instance;
    }

    private String handleResponse(HttpResponse response) throws Exception {
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity, "UTF-8");
        EntityUtils.consume(entity);

        return json;
    }

    public String requestGet(String url) throws Exception {
        HttpGet getRequest = new HttpGet(url);
        return handleResponse(client.execute(getRequest));
    }

    public String requestPost(String url) throws Exception {
        HttpPost postRequest = new HttpPost(url);
        return handleResponse(client.execute(postRequest));
    }
}
