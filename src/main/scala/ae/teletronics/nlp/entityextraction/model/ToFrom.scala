package ae.teletronics.nlp.entityextraction.model

/**
  * Created by trym on 16-06-2016.
  */
case class ToFrom(to: List[String], from: List[String]) {
  def from2String() = from.mkString(" ")
  def to2String() = to.mkString(" ")

}
