package ae.teletronics.nlp.entityextraction.model

/**
  * Created by trym on 16-06-2016.
  */
case class ToFrom(to: List[String], from: List[String]) {
  def from2String(): Option[String] = asOption(from)
  def to2String(): Option[String] = asOption(to)

  private def asOption(strings: List[String]): Option[String] = {
    if (strings.size > 0) Some(strings.mkString(" ")) else None
  }

}
