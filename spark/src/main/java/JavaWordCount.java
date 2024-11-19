import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;

public class JavaWordCount {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("JavaLambdaWordCount").setMaster("local[*]");
        //创建SparkContext
        JavaSparkContext jsc = new JavaSparkContext(conf);
        //创建RDD
        JavaRDD<String> lines = jsc.textFile("D:\\ProgramFiles\\Java\\Bigdata\\spark\\src\\main\\resources\\word.txt");
        //切分压平
        JavaRDD<String> words = lines.flatMap(line -> Arrays.asList(line.split(" ")).iterator());
        //将单词和一组合
        JavaPairRDD<String, Integer> wordAndOne = words.mapToPair(word -> Tuple2.apply(word, 1));
        //分组聚合
        JavaPairRDD<String, Integer> reduced = wordAndOne.reduceByKey((a, b) -> a + b);
        //调换顺序
        JavaPairRDD<Integer, String> swapped = reduced.mapToPair(tp -> tp.swap());
        //排序
        JavaPairRDD<Integer, String> sorted = swapped.sortByKey(false);
        //调换顺序
        JavaPairRDD<String, Integer> result = sorted.mapToPair(tp -> tp.swap());
        //将数据保存到HDFS
        result.saveAsTextFile(args[1]);
        //释放资源
        jsc.stop();
}
}