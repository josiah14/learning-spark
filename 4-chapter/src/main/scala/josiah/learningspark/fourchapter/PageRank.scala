package josiah.learningspark.fourchapter

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.HashPartitioner

import org.apache.spark.rdd._

import cats._
import cats.implicits._
import cats.data.{Kleisli, Reader}
import cats.instances._
import scala.util.{Try, Success, Failure, Either, Left, Right}
import scala.language.postfixOps
import scala.collection.JavaConversions._
import scala.language.implicitConversions._

object ChapterFour {

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

  def setAppName(name: String): SparkConfRW[Unit] =
    Kleisli(conf => Try(conf.setAppName(name)))

  def buildSparkContext: SparkConfRW[SparkContext] =
    Kleisli(conf => Try(new SparkContext(conf)))

  val tsc: Try[SparkContext] =
    setAppName("Learning Spark: Chapter 4: Page Rank") *>
      buildSparkContext run new SparkConf()

  val tlinks: Try[RDD[(String, Seq[String])]] = tsc.map(sc =>
    sc.objectFile[(String, Seq[String])]("/learningspark/4-chapter/page-rank/input/links")
      .partitionBy(new HashPartitioner(100))
      .persist()
  )

  // val links = sc.
}
