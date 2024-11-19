import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;

public class WordCount {
    public static void main(String[] args) {
        SparkConf sc = new SparkConf().setMaster("local[*]").setAppName("HelloWorld");
        JavaSparkContext jsc = new JavaSparkContext(sc);
        JavaRDD<String> rdd = jsc.textFile("D:\\ProgramFiles\\Java\\Bigdata\\spark\\src\\main\\resources\\word.txt");
        JavaRDD<String> stringJavaRDD = rdd.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                Iterator<String> it = Arrays.asList(s.split(" ")).iterator();
                return it;
            }
        });
        JavaPairRDD<String, Integer> javaPairRDD = stringJavaRDD.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return Tuple2.apply(s, 1);
            }
        });
        JavaPairRDD<String, Integer> reduce = javaPairRDD.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer integer, Integer integer2) throws Exception {
                return integer + integer2;
            }
        });
        // 输出结果到控制台
        reduce.foreach(new VoidFunction<Tuple2<String, Integer>>() {
            @Override
            public void call(Tuple2<String, Integer> pair) throws Exception {
                System.out.println(pair._1() + ": " + pair._2());
            }
        });

        // 关闭 Spark 上下文
        jsc.close();

    }
}
