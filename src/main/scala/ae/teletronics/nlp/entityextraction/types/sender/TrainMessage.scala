package ae.teletronics.nlp.entityextraction.types.sender

/**
  * Created by trym on 20-05-2016.
  */
case class TrainMessage(content: String, r: Option[String], rPos: Int, s: Option[String], sPos: Int) {

  def toXml() =
    s"<message><content>${content}</content>${f("receiver", r, rPos)}${f("sender", s, sPos)}</message>"

  private def f(tag: String, name: Option[String], p: Int) =
    name
      .map(n => s"""<${tag} name=\"${n}\" position=\"${p}\" />""")
      .getOrElse("")
}
