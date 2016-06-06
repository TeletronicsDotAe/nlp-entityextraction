package ae.teletronics.nlp.entityextraction.types.sender

import java.io.InputStream

import scala.xml.{Node, XML}

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

object TrainMessage {
  def readMessages(inputStream: InputStream): Seq[TrainMessage] = {
    val xml = XML.load(inputStream)
    (xml \\ "messages" \\ "message")
      .map(fromNode)
  }

  private def fromNode(node: Node) = {
    val content = (node \\ "content").text
    val (sender, pos) = extract(node, "sender")
    val (receiver, receiverPos) = extract(node, "receiver")

    TrainMessage(content, receiver, receiverPos, sender, pos)
  }

  private def extract(node: Node, tagName: String) = {
    val name = (node \\ tagName \ "@name").text
    val pos = (node \\ tagName \ "@position").text

    val nameValue = if (name.isEmpty) None else Some(name)
    val posValue = if (pos.isEmpty) -1 else pos.toInt

    (nameValue, posValue)
  }
}