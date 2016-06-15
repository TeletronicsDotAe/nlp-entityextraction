package ae.teletronics.nlp.entityextraction.types.sender

import ae.teletronics.nlp.entityextraction.types.sender.DoubleUtil.asDouble
import org.apache.spark.mllib.linalg.{Vector, Vectors}

/**
  * Created by trym on 19-05-2016.
  *
  * Features:
  *   alfa only (0 - not, 1 - is), See java.lang.Character#isLetter
  *   isCapitalized (0 - not, 1 - is),
  *   locationInMessage ([1..MessageLength])
  *   previousTerm, (0 - none, 1 - xx string.hashCode)
  *   isPerson (0 - not, 1 - is)
  */
case class SenderFeature(term: String, isCapitalized: Boolean, locationInMessage: Int, previousTerm: Int, isPerson: Boolean) {

  def features(): Vector = {
    Vectors.dense(alfaOnly(term), asDouble(isCapitalized), locationInMessage, previousTerm, asDouble(isPerson))
  }

  private def alfaOnly(s: String): Double = asDouble(s.forall(_.isLetter))

}

