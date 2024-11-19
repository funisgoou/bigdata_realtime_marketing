import org.apache.spark.{SparkConf, SparkContext}

object ScalaWordCount {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("WordCount").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val value = sc.textFile("D:\\ProgramFiles\\Java\\Bigdata\\spark\\src\\main\\resources\\word.txt")
    val value1 = value.flatMap(line => line.split(" "))
    val value2 = value1.map(word => (word, 1))
    val value3 = value2.reduceByKey(_ + _)
    value3.collect().foreach(println)
  }
}
