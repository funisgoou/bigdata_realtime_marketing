package cn.rtmk.test.bitmap_inject;


import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.roaringbitmap.RoaringBitmap;

import java.nio.ByteBuffer;
import java.util.Map;


/**
 * 从外部注入规则，对输入的行为事件进行处理
 */
@Slf4j
public class FlinkInjectBitmapRuleProcessEvent {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
        environment.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);
        environment.getCheckpointConfig().setCheckpointStorage("file:/d/checkpoint/");
        StreamTableEnvironment tenv = StreamTableEnvironment.create(environment);
        
        //获取用户实时行为事件流
        DataStreamSource<String> eventStr = environment.socketTextStream("doitedu", 4444);
        SingleOutputStreamOperator<Tuple2<Integer, String>> events = eventStr.map(new MapFunction<String, Tuple2<Integer, String>>() {
            @Override
            public Tuple2<Integer, String> map(String s) throws Exception {
                String[] split = s.split(",");
                return Tuple2.of(Integer.parseInt(split[0]), split[1]);
            }
        });
        
        //获取规则系统中的规则定义流
        tenv.executeSql("CREATE TABLE rtmk_rule_def (\n" +
                "                        rule_id STRING,\n" +
                "                        profile_users_bitmap BINARY,\n" +
                "                        PRIMARY KEY(rule_id) NOT ENFORCED\n" +
                ") WITH (\n" +
                "    'connector' = 'mysql-cdc',\n" +
                "    'hostname' = 'doitedu',\n" +
                "    'port' = '3306',\n" +
                "    'username' = 'root',\n" +
                "    'password' = 'root',\n" +
                "    'database-name' = 'rtmk',\n" +
                "    'table-name' = 'rtmk_rule_def')");
        Table table = tenv.sqlQuery("select rule_id,profile_users_bitmap from rtmk_rule_def");
        DataStream<Row> rowDataStream = tenv.toChangelogStream(table);
        SingleOutputStreamOperator<Tuple2<String, RoaringBitmap>> ruleDefineStream = rowDataStream.map(new MapFunction<Row, Tuple2<String, RoaringBitmap>>() {
            @Override
            public Tuple2<String, RoaringBitmap> map(Row row) throws Exception {
                String ruleId = row.getFieldAs("rule_id");
                byte[] profileUsersBitmap = row.getFieldAs("profile_users_bitmap");
                //反序列化本次拿到的bitmap
                RoaringBitmap roaringBitmap = RoaringBitmap.bitmapOf();
                roaringBitmap.deserialize(ByteBuffer.wrap(profileUsersBitmap));
                return Tuple2.of(ruleId, roaringBitmap);
            }
        });
      
        //将规则定义流广播
        MapStateDescriptor<String, RoaringBitmap> broadcastStateDesc = new MapStateDescriptor<>("rule_info", String.class, RoaringBitmap.class);
        BroadcastStream<Tuple2<String, RoaringBitmap>> broadcastStream = ruleDefineStream.broadcast(broadcastStateDesc);

        //将广播流与行为事件流进行连接
        events.keyBy(tp->tp.f0)
                .connect(broadcastStream)
                .process(new KeyedBroadcastProcessFunction<Object, Tuple2<Integer, String>, Tuple2<String, RoaringBitmap>, Object>() {
                    @Override 
                    public void processElement(Tuple2<Integer, String> event, KeyedBroadcastProcessFunction<Object, Tuple2<Integer, String>, Tuple2<String, RoaringBitmap>, Object>.ReadOnlyContext readOnlyContext, Collector<Object> collector) throws Exception {
                        ReadOnlyBroadcastState<String, RoaringBitmap> broadcastState = readOnlyContext.getBroadcastState(broadcastStateDesc);
                        for (Map.Entry<String, RoaringBitmap> entry : broadcastState.immutableEntries()) {
                            String ruleId = entry.getKey();
                            RoaringBitmap  usersBitmap = entry.getValue();
                            collector.collect(String.format("规则id为:%s,用户id为:%s,目标人群是否包含此人:%s", ruleId, event.f0, usersBitmap.contains(event.f0)));
                        }
                    }

                    @Override
                    public void processBroadcastElement(Tuple2<String, RoaringBitmap> ruleInfo, KeyedBroadcastProcessFunction<Object, Tuple2<Integer, String>, Tuple2<String, RoaringBitmap>, Object>.Context context, Collector<Object> collector) throws Exception {
                        log.error("接受到一个新的规则定义信息,规则id为:{}", ruleInfo.f0);
                        BroadcastState<String, RoaringBitmap> broadcastState = context.getBroadcastState(broadcastStateDesc);
                        broadcastState.put(ruleInfo.f0,ruleInfo.f1);
                    }
                }).print();
        environment.execute();
    }
}
