
package flow

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._


class OSCSendIO extends OSCSend with IO {
  var prefix = ""
  override def sink(name:String) = Some(Sink.foreach(send(prefix + "/" + name, _:Float)).mapMaterializedValue(x => akka.NotUsed))
}