
package projector

import collection.mutable.LinkedHashMap

object Projectors extends Projectors {

  val tests = (1 to 10).map { case id =>
    val p = new TestProjector()
    p.name = s"Test-$id"
    p
  }

  addGroup("Tests", tests)
  addGroup("5", tests.slice(8,10))
  addGroup("4", tests.slice(6,8))
  addGroup("3", tests.slice(4,6))
  addGroup("2", tests.slice(2,4))
  addGroup("1", tests.take(2))

  all.zipWithIndex.foreach { case (p,id) => p.id = id }
}

trait Projectors {
  
  val groups = LinkedHashMap[String,Seq[Projector]]()
  def all = groups.values.flatten.toList.distinct

  def addGroup(group:String, ps:Seq[Projector]) = {
    groups(group) = ps
  }
}