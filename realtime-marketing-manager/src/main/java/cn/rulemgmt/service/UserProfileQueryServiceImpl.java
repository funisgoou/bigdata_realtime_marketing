package cn.rulemgmt.service;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

public class UserProfileQueryServiceImpl {
    private RestHighLevelClient client;
    SearchRequest request;
    public UserProfileQueryServiceImpl(){
client = new RestHighLevelClient(RestClient.builder(new HttpHost("doitedu", 9200, "http")));
        request=new SearchRequest("doeusers");
    }
    public void queryProfileUsers(){
        request.source().query();
    }
}
