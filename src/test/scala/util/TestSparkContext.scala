package util

import org.apache.spark.sql.SparkSession
import org.scalatest.BeforeAndAfterEach

trait TestSparkContext {

  self: BeforeAndAfterEach =>

  implicit var sparkSession: SparkSession = _

  override protected def beforeEach(): Unit = sparkSession = createSparkSession

  override protected def afterEach(): Unit = sparkSession.stop()

  def createSparkSession: SparkSession =
    SparkSession
      .builder
      .appName("test")
      .master("local")
      .config("spark.driver.allowMultipleContexts", "true")
      .getOrCreate()

}
