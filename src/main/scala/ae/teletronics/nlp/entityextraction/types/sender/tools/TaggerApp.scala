package ae.teletronics.nlp.entityextraction.types.sender.tools

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}

import ae.teletronics.nlp.entityextraction.types.sender.TrainMessage
import opennlp.tools.tokenize.SimpleTokenizer

import scala.io.{Source, StdIn}

/**
  * Created by trym on 20-05-2016.
  */
object TaggerApp extends App {

  private val tokenizer = SimpleTokenizer.INSTANCE
  private val outputFileName = args.head + ".xml"
  private val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName)))

  // 1. Read input file or print help message
  // 2. For each line
  // 2.1 Display tokens with index
  // 2.2 Request receiver name and token position
  // 2.3 Request sender name and token position
  // 2.4 store as xml in output file
  // 3. Display thanks message

  writer.write("<messages>")
  writer.newLine()
  getInputFileLines()
    .foreach(m => {

      for (_  <- 1 to 100){
        println()
      }

      println(m)
      println
      println

      val tokens: Array[(String, Int)] = tokenizer.tokenize(m).zipWithIndex.map(s => {
        print(s._1 + ":" + s._2 + " \n")
        s
      })

      println()
      val receiverPos = StdIn.readLine("Provide receiver token position (empty if none): ")
      val senderPos = StdIn.readLine("Provide sender token position (empty if none): ")
      val receiverPosInt = if (receiverPos.isEmpty) -1 else receiverPos.toInt
      val senderPosInt = if (senderPos.isEmpty) -1 else senderPos.toInt

      writer.write(
        TrainMessage(m,
          token(tokens, receiverPosInt), receiverPosInt,
          token(tokens, senderPosInt), senderPosInt)
          .toXml)
      writer.newLine()
      writer.flush()
    })
  writer.write("</messages>")
  writer.newLine()
  writer.flush()
  writer.close()

  println()
  println(s"Thank you for your input. The xml output file can be found at ${outputFileName}")

  private def token(tokens: Array[(String, Int)], receiverPos: Int) =
    if (receiverPos < 0) None else Some(tokens(receiverPos)._1)

  private def getInputFileLines() = {
    if (args.isEmpty) {
      println("Please provide the file name as input to this application \n" +
        "java -jar entityextraction-1.0-SNAPSHOT-jar-with-dependencies.jar myFile.txt")
      List()
    } else {
      Source.fromFile(args.head).getLines
    }
  }
}
