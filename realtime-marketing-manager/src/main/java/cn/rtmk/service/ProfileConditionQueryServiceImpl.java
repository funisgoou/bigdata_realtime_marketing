package cn.rtmk.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ProfileConditionQueryServiceImpl implements ProfileConditionQueryService {
    private RestHighLevelClient client;
    SearchRequest request;

    public ProfileConditionQueryServiceImpl() {
        client = new RestHighLevelClient(RestClient.builder(new HttpHost("doitedu", 9200, "http")));
        request = new SearchRequest("doeusers");
    }

    //接口文档：[{"tagId":"tg01","compareType":"eq","compareValue":"3"},{"tagId":"tg04","compareType":"match","compareValue":"运动"}]
    @Override
    public RoaringBitmap queryProfileUsers(JSONArray jsonArray) throws IOException {
        /**
         * 组合条件查询
         */
        // 定义一个 条件组装器
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject paramObject = jsonArray.getJSONObject(i);
            String tagId = paramObject.getString("tagId");
            String compareType = paramObject.getString("compareType");
            String compareValue = paramObject.getString("compareValue");
            switch (compareType) {
                case "lt":
                    RangeQueryBuilder lt = QueryBuilders.rangeQuery(tagId).lt(compareValue);

                    if (compareValue.matches("\\d+(.\\d+)?")) {
                        lt = QueryBuilders.rangeQuery(tagId).lt(Float.parseFloat(compareValue));
                    }
                    boolQueryBuilder.must(lt);
                    break;
                case "gt":
                    RangeQueryBuilder gt = QueryBuilders.rangeQuery(tagId).gt(compareValue);
                    if (compareValue.matches("\\d+(.\\d+)?")) {
                        gt = QueryBuilders.rangeQuery(tagId).gt(Float.parseFloat(compareValue));
                    }
                    boolQueryBuilder.must(gt);
                    break;
                case "ge":
                    RangeQueryBuilder gte = QueryBuilders.rangeQuery(tagId).gte(compareValue);
                    if (compareValue.matches("\\d+(.\\d+)?")) {
                        gte = QueryBuilders.rangeQuery(tagId).gte(Float.parseFloat(compareValue));
                    }
                    boolQueryBuilder.must(gte);
                    break;
                case "le":
                    RangeQueryBuilder lte = QueryBuilders.rangeQuery(tagId).lte(compareValue);
                    if (compareValue.matches("\\d+(.\\d+)?")) {
                        lte = QueryBuilders.rangeQuery(tagId).lte(Float.parseFloat(compareValue));
                    }
                    boolQueryBuilder.must(lte);
                    break;
                case "between":
                    String[] fromTo = compareValue.split(",");
                    RangeQueryBuilder between = QueryBuilders.rangeQuery(tagId).from(fromTo[0], true).to(fromTo[1], true);
                    if (fromTo[0].matches("\\d+(.\\d+)?") && fromTo[1].matches("\\d+(.\\d+)?")) {
                        between = QueryBuilders.rangeQuery(tagId).from(Float.parseFloat(fromTo[0]), true).to(Float.parseFloat(fromTo[1]), true);
                    }
                    boolQueryBuilder.must(between);
                    break;
                default:
                    MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(tagId, compareValue);
                    boolQueryBuilder.must(matchQueryBuilder);
            }
//            if("lt".equals(compareType)){
//                RangeQueryBuilder lt = QueryBuilders.rangeQuery(tagId).lt(compareValue);
//                boolQueryBuilder.must(lt);
//            }else if("gt".equals(compareType))
//            {
//                RangeQueryBuilder gt = QueryBuilders.rangeQuery(tagId).gt(compareValue);
//                boolQueryBuilder.must(gt);            }
//            else
//            {
//                MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(tagId, compareValue);
//                boolQueryBuilder.must(matchQueryBuilder);
//            }

        }

        // 将查询条件参数，封装成查询请求
        request.source(new SearchSourceBuilder().query(boolQueryBuilder));

        // 用客户端发送请求
        SearchResponse response2 = client.search(request, RequestOptions.DEFAULT);
        RoaringBitmap bitmap = RoaringBitmap.bitmapOf();
        response2.getHits().forEach(ht -> bitmap.add(Integer.parseInt(ht.getId())));
        return bitmap;
    }
}
