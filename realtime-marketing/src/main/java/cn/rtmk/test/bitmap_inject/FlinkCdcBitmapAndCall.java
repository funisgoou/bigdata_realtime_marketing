package cn.rtmk.test.bitmap_inject;

import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;
import org.apache.flink.util.Collector;
import org.roaringbitmap.RoaringBitmap;

import java.nio.ByteBuffer;

public class FlinkCdcBitmapAndCall {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
        environment.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);
        environment.getCheckpointConfig().setCheckpointStorage("file:/d/checkpoint/");
        StreamTableEnvironment tenv = StreamTableEnvironment.create(environment);
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
        rowDataStream.process(new ProcessFunction<Row, String>() {
            @Override
            public void processElement(Row row, ProcessFunction<Row, String>.Context context, Collector<String> collector) throws Exception {
                RowKind kind = row.getKind();
                if(kind == RowKind.INSERT){
                    String ruleId = row.<String>getFieldAs("rule_id");
                    byte[] profileUsersBitmap = row.<byte[]>getFieldAs("profile_users_bitmap");
                    //反序列化本次拿到的bitmap
                    RoaringBitmap roaringBitmap = RoaringBitmap.bitmapOf();
                    roaringBitmap.deserialize(ByteBuffer.wrap(profileUsersBitmap));
                    collector.collect(String.format("100存在与否:%s", roaringBitmap.contains(100)));
                    collector.collect(String.format("200存在与否:%s", roaringBitmap.contains(200)));
                    collector.collect(String.format("965存在与否:%s", roaringBitmap.contains(965)));
                    collector.collect(String.format("966存在与否:%s", roaringBitmap.contains(966)));
                }
            }
        }).print();
        environment.execute();

    }
}
