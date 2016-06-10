package ae.teletronics.nlp.entityextraction.types.sender.stanford

import org.junit.Assert.assertEquals
import org.junit.{Ignore, Test}

import scala.xml.XML

/**
  * Created by trym on 09-06-2016.
  */
class StanfordToFromExtractorTest {
  private val extractor = new StanfordToFromExtractor()

  @Test
  def testExtraction() = {
    val message = "Got it -Bibob"
    val res = extractor.process(message)
    assertEquals(1, res.size)
    assertEquals("Bibob", res(0))
  }

  @Test
  def testExtractionNoSender() = {
    val message = "Joe popped my balloon today"
    val res = extractor.process(message)
    assertEquals(0, res.size)
  }

  @Ignore
  @Test
  def testExtractionAll() = {
    var noSender = 0
    var totalCount = 0
    var falseSender = 0
    for (m <- loadMessages()) {
      totalCount = totalCount + 1
      val res = extractor.process(m.content)
      if (!m.from.isEmpty && res.size < 1) {
//        println("Didn't find the sender: " + m)
        noSender = noSender + 1
      } else if (!m.from.isEmpty && !m.from.equals(res(0))) {
        println("Wrong sender found: " +res(0) + " in: " + m)
      } else if (m.from.isEmpty && res.size > 0) {
        println("Found a sender where no one is tagged: " + res(0) + " in: " + m)
        falseSender = falseSender + 1
      } else if (res.size > 0) {
        println("Found sender: " + res(0))
      } else if (!m.from.isEmpty) {
        println("Expected sender: " + m.from)
      }
    }
    println("Summary:\n\tTotal count: " + totalCount +
      "\n\tMissed sender: " + noSender +
      "\n\tFalse sender (FP): " + falseSender)
  }

  private def loadMessages() = {
    val f = getClass.getClassLoader.getResourceAsStream("prod-sms/prod-sms-eng.xml")
//    val f = getClass.getClassLoader.getResourceAsStream("prod-sms/prod-sms-eng-per.tagged.xml")
//    val f = getClass.getClassLoader.getResourceAsStream("prod-sms/prod-sms-eng-per.tagged.only-sender.xml")
    val xml = XML.load(f)
    val xMessages = xml \\ "messages" \\ "message"
    val msgs = xMessages
      .map(sms => new Message(
        (sms \\ "sender" \@ "name"),
        asInt(sms \\ "sender" \@ "position"),
        (sms \\ "receiver" \@ "name"),
        asInt(sms \\ "receiver" \@ "position"),
        (sms \\ "content").text))
      .toList
    f.close()

    msgs
  }

  private def asInt(s: String): Int = if (s.isEmpty) -1 else s.toInt
}
