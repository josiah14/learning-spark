package josiah.learningspark.fourchapter

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.HashPartitioner
import org.apache.spark.Partitioner

import org.apache.spark.rdd._

import cats._
import cats.implicits._
import cats.data.{Kleisli, Reader}
import cats.instances._
import scala.util.{Try, Success, Failure, Either, Left, Right}
import scala.reflect.ClassTag
import scala.language.postfixOps
import scala.collection.JavaConversions._
import scala.language.implicitConversions._

object ChapterFour {
  def main(args: Array[String]): Unit = {
    PageRank.runPageRankApp
  }
}

/**
  * 1. Init each page's rank to 1.0
  * 2. On each iteration, have page `p` send a contribution of `rank(p)/numNeighbors(p)` to each of its neighbors.
  * 3. Set each page's rank to `0.15 + 0.85 * contributionsReceived`.
  *
  * Steps 2 & 3 repeat for several iterations until convergence is obtained.
  **/
object PageRank {
  type PageRankAlg[A] = Kleisli[Try, SparkContext, A]

  // I'll sort-of mimick Haskell's IO type, here.  Writing
  // to a `SparkConf` will return a `SparkContextBuilder[Unit]`,
  // and reading from one will return a `SparkContextBuilder[T]`,
  // where `T` is the type of the value being read from the `SparkConf`.
  type SparkConfRW[A] = Kleisli[Try, SparkConf, A]

  type SparkContextReader[A] = Kleisli[Try, SparkContext, A]

  def setAppName(name: String): SparkConfRW[Unit] =
    Kleisli(conf => Try(conf.setAppName(name)))

  def buildSparkContext: SparkConfRW[SparkContext] =
    Kleisli(conf => Try(new SparkContext(conf)))

  def configureExeParams: Try[SparkContext] =
    setAppName("Learning Spark: Chapter 4: Page Rank") *>
      buildSparkContext run new SparkConf()

  def loadObjectFile(path: String)
      : SparkContextReader[RDD[(String, Seq[String])]] =
    Kleisli(sc => Try(sc.objectFile(path)))

  /**
    * Wrap partitionBy function in `Try` context because it can throw a
    * `SparkException` if it's given a `HashPartitioner` if it cannot partition
    * the array keys.
    * [[https://github.com/apache/spark/blob/v2.2.0/core/src/main/scala/org/apache/spark/rdd/PairRDDFunctions.scala#L534]]
    **/
  def partitionBy[A, B](p: Partitioner)(rdd: RDD[(A, B)])
      (implicit kt: ClassTag[A], vt: ClassTag[B], ord: Ordering[A] = null)
      : Try[RDD[(A, B)]] = Try(rdd.partitionBy(p))

  def persist[A](rdd: RDD[A]): Try[RDD[A]] = Try(rdd.persist())

  def scLinks(path: String): SparkContextReader[RDD[(String, Seq[String])]] =
    loadObjectFile(path)
      .mapF(_ >>= partitionBy(new HashPartitioner(100)) >>= persist)

  def initializeRanks(links: RDD[(String, Seq[String])]): RDD[(String, Double)] =
    links.mapValues(_ => 1.0)

  def pageRank(links: RDD[(String, Seq[String])])(ranks: RDD[(String, Double)])
      : RDD[(String, Double)] =
    links.join(ranks).flatMap {
      case (pageId, (pageLinks, rank)) =>
        pageLinks.par.map(dest => (dest, rank / pageLinks.size)).seq
    }.reduceByKey(_ + _).mapValues(0.15 + 0.85*_)

  def iteratePageRank(
    links: RDD[(String, Seq[String])],
    numIterations: Int
  ): RDD[(String, Double)] = {
    val pageRank1 = pageRank(links)(_)
    Stream.from(1).map(_ => pageRank1).take(numIterations)
      .foldLeft(initializeRanks(links))((ranks, prFunc) => prFunc(ranks))
  }

  val objectFilePath: String = "/learningspark/4-chapter/page-rank/input/links"
  val pageRankApp: SparkContextReader[RDD[(String, Double)]] =
    scLinks(objectFilePath).map(iteratePageRank(_, 10))

  val reportFilePath: String = "/learningspark/4-chapter/page-rank/output/ranks"
  def reportPageRankRunResults(results: Try[RDD[(String, Double)]]): Unit =
    results.map(_.saveAsTextFile("/")).failed.foreach(_.printStackTrace)

  def runPageRankApp: Unit =
    reportPageRankRunResults(configureExeParams >>= pageRankApp.run)
}
