
package flow

import org.apache.pekko._
import org.apache.pekko.actor._
import org.apache.pekko.stream._
import org.apache.pekko.stream.scaladsl._


class TrackpadIO extends IO {

  def state = Trackpad.source
  def pos = state.map(_.pos)
  def vel = state.map(_.vel)
  def size = state.map(_.size)
  def angle = state.map(_.angle)
  def fingers = state.map(_.fingers)
  def count = fingers.map(_.length)
  def finger(i:Int) = fingers.filter(_.length > i).map(_(i))
  def pos(i:Int) = finger(i).map(_.pos)
  def vel(i:Int) = finger(i).map(_.vel)
  def size(i:Int) = finger(i).map(_.size)
  def angle(i:Int) = finger(i).map(_.angle)

  override def sources:Map[String,Source[Any,NotUsed]] = Map(
    "state" -> state,
    "pos" -> pos,
    "vel" -> vel,
    "size" -> size,
    "angle" -> angle,
    "fingers" -> fingers,
    "count" -> count,
    "finger0" -> finger(0),
    "finger1" -> finger(1),
    "finger2" -> finger(2),
    "finger3" -> finger(3),
    "finger4" -> finger(4),
    "finger5" -> finger(5),
    "pos0" -> pos(0),
    "pos1" -> pos(1),
    "pos2" -> pos(2),
    "pos3" -> pos(3),
    "pos4" -> pos(4),
    "pos5" -> pos(5),
    "vel0" -> vel(0),
    "vel1" -> vel(1),
    "vel2" -> vel(2),
    "vel3" -> vel(3),
    "vel4" -> vel(4),
    "vel5" -> vel(5),
    "size0" -> size(0),
    "size1" -> size(1),
    "size2" -> size(2),
    "size3" -> size(3),
    "size4" -> size(4),
    "size5" -> size(5),
    "angle0" -> angle(0),
    "angle1" -> angle(1),
    "angle2" -> angle(2),
    "angle3" -> angle(3),
    "angle4" -> angle(4),
    "angle5" -> angle(5)
  )


}