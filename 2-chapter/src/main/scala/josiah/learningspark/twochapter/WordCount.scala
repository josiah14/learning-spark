package josiah.learningspark.twochapter

/**
 * Everyone's favourite wordcount example.
 */

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object ChapterTwo {
  def main(args: Array[String]): Unit = {
    WordCount.run
  }
}

/**
  * This assumes the Cloudera 5.7 Quickstart Docker container
  */
object WordCount {
  /**
   * A slightly more complex than normal wordcount example with optional
   * separators and stopWords. Splits on the provided separators, removes
   * the stopwords, and converts everything to lower case.
   */
  def run: Unit = {
    val licenseFile = "file:///usr/lib/spark/LICENSE"
    val conf: SparkConf = new SparkConf().setAppName("Learning Spark: Chapter 2")
    val sc = new SparkContext(conf)
    val licenseData = sc.textFile(licenseFile, 2).cache()
    // Split it up into words.words
    val words = licenseData.flatMap(line => line.split(" "))
    // Transform each item into a pair with a single count (the map step)
    val singles = words.map(w => (w, 1))
    // Reduce the pairs to get the count of each word by matching on the key(word)
    val frequencies = singles.reduceByKey((acc, count) => acc + count)
    frequencies.saveAsTextFile("/user/spark/out/wordcount")
    frequencies.collect.foreach(println(_))
  }
}
