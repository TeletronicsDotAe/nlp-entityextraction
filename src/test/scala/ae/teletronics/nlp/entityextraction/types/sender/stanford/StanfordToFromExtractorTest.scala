package ae.teletronics.nlp.entityextraction.types.sender.stanford

import org.junit.Assert.assertEquals
import org.junit.{Ignore, Test}

import scala.io.Source
import scala.xml.XML

/**
  * Created by trym on 09-06-2016.
  */
class StanfordToFromExtractorTest {
  private val extractor = new StanfordToFromExtractor()

  @Test
  def testSenderExtraction() = {
    val message = "Got it -Bibob"
    val res = extractor.process(message)
    assertEquals(1, res.from.size)
    assertEquals("Bibob", res.from(0))
  }

  @Test
  def testReceiverExtraction() = {
    val message = "Peter, you have 1 message and 4 friend requests on Facebook"
    val res = extractor.process(message)
    assertEquals(1, res.to.size)
    assertEquals("Peter", res.to(0))
  }

  @Test
  def testEmptyExtraction() = {
    val message = "Joe popped my balloon today"
    val res = extractor.process(message)
    assertEquals(0, res.from.size)
    assertEquals(0, res.to.size)
  }

  @Ignore
  @Test
  def testExtraction2() = {
    for (c: String <- Source.fromFile("src/test/resources/").getLines()) {
      val res = extractor.process(c)
      if (!res.from.isEmpty) {
        println("Found a sender: " + res.from(0) + " in message: " + c)
      }
    }

  }

  @Ignore
  @Test
  def testExtractionAll() = {
    var noSender = 0
    var totalCount = 0
    var falseSender = 0
    var senderTP = 0
    for (m <- loadMessages()) {
      totalCount = totalCount + 1
      val res = extractor.process(m.content)
      if (!m.from.isEmpty && res.from.size < 1) {
//        println("Didn't find the sender: " + m)
        noSender = noSender + 1
      } else if (!m.from.isEmpty && !m.from.equals(res.from(0))) {
        println("Wrong sender found: " +res.from(0) + " in: " + m)
      } else if (m.from.isEmpty && res.from.size > 0) {
        println("Found a sender where no one is tagged: " + res.from(0) + " in: " + m)
        falseSender = falseSender + 1
      } else if (res.from.size > 0) {
//        println("Found sender: " + res(0))
        senderTP = senderTP + 1
      } else if (!m.from.isEmpty) {
        println("Expected sender: " + m.from)
      }
    }
    println("Summary:\n\tTotal count: " + totalCount +
      "\n\tMissed sender: " + noSender +
      "\n\tFound sender: " + senderTP +
      "\n\tFalse sender (FP): " + falseSender)
  }

  private def loadMessages() = {
    val f = getClass.getClassLoader.getResourceAsStream("eng-per.tagged.xml")
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
