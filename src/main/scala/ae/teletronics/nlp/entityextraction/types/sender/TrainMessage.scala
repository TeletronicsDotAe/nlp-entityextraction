package ae.teletronics.nlp.entityextraction.types.sender

/**
  * Created by trym on 20-05-2016.
  */
case class TrainMessage(content: String, tagName: Option[String], tagPos: Int = -1)
