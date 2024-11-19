import org.apache.spark.{SparkConf, SparkContext}

object RddDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("RddDemo").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val value = sc.parallelize(Array(1, 2, 3, 4, 5, 6, 7, 8, 9))
    //map,将原来rdd里面的每个元素做映射
    val maprdd=value.map(_*2)
    maprdd.collect().foreach(println)
    //
  }
}
