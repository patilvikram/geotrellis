/*
 * Copyright 2016 Azavea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package geotrellis.spark.buffer

import geotrellis.spark._
import geotrellis.raster._
import geotrellis.raster.crop._
import geotrellis.raster.stitch._
import geotrellis.util._

import org.apache.spark.rdd._
import org.apache.spark.storage.StorageLevel

import scala.reflect.ClassTag
import scala.collection.mutable.ArrayBuffer

object CollectNeighbors {

  /** Collects tile neighbors by slicing the neighboring tiles to the given
    * buffer size
    */
  def apply[K: SpatialComponent: ClassTag, V](rdd: RDD[(K, V)]): RDD[(K, Map[Direction, (K, V)])] = {
    val neighbored: RDD[(K, (Direction, (K, V)))] =
      rdd
        .flatMap { case (key, value) =>
          val SpatialKey(col, row) = key

          Seq(
            (key, (CenterDirection, (key, value))),

            (key.setComponent(SpatialKey(col-1, row)), (RightDirection, (key, value))),
            (key.setComponent(SpatialKey(col+1, row)), (LeftDirection, (key, value))),
            (key.setComponent(SpatialKey(col, row-1)), (BottomDirection, (key, value))),
            (key.setComponent(SpatialKey(col, row+1)), (TopDirection, (key, value))),

            (key.setComponent(SpatialKey(col-1, row-1)), (BottomRightDirection, (key, value))),
            (key.setComponent(SpatialKey(col+1, row-1)), (BottomLeftDirection, (key, value))),
            (key.setComponent(SpatialKey(col-1, row+1)), (TopRightDirection, (key, value))),
            (key.setComponent(SpatialKey(col+1, row+1)), (TopLeftDirection, (key, value)))
          )
        }

    val grouped: RDD[(K, Iterable[(Direction, (K, V))])] =
      rdd.partitioner match {
        case Some(partitioner) => neighbored.groupByKey(partitioner)
        case None => neighbored.groupByKey
      }

    grouped
      .filter { case (_, values) =>
        values.find {
          case (CenterDirection, _) => true
          case _ => false
        }.isDefined
      }
      .mapValues(_.toMap)
  }
}
