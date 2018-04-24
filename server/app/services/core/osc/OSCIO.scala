
package flow

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._


class OSCSink extends OSCSend with IO {
  var prefix = ""
  override def sink(name:String) = Some(Sink.foreach(send(prefix + "/" + name, _:Any)).mapMaterializedValue(x => akka.NotUsed))
}