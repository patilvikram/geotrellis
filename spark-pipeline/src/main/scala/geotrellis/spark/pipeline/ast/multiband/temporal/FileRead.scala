package geotrellis.spark.pipeline.ast.multiband.temporal

import geotrellis.raster._
import geotrellis.spark._
import geotrellis.spark.pipeline.ast.Read
import geotrellis.spark.pipeline.json
import geotrellis.vector._

import org.apache.spark.rdd.RDD

case class FileRead(read: json.ReadFile) extends Read[RDD[(TemporalProjectedExtent, MultibandTile)]] {
  def get: RDD[(TemporalProjectedExtent, MultibandTile)] = ???
}