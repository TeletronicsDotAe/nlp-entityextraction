package ae.teletronics.nlp.entityextraction.gate

/**
  * Created by Boris on 2016-04-27.
  */

import scala.io.Source

object TestCaseReader {

  case class AnnotatedWord(word: String, annotation: String)

  val persB = "B-PERS"
  val persI = "I-PERS"
  val perB = "B-PER"
  val perI = "I-PER"
  val locB  = "B-LOC"
  val locI  = "I-LOC"
  val orgB  = "B-ORG"
  val orgI  = "I-ORG"
  val miscB = "B-MISC"
  val miscI = "I-MISC"
  val other = "O"

  val legalAnnotations = List(persB, persI, perB, perI, locB, locI, orgB, orgI, miscB, miscI, other)
  val sentenceSplitter = "."

  private def getParts(line: String): List[String] = {
    line.split("\\s")
        .map(_.trim)
        .filter(!_.isEmpty)
        .toList
  }

  // look at eg. line 77231 in ANERCorp.txt for an example of an unparseable line. We skip those
  private def isParseable(line: String): Boolean = {
    val parts = getParts(line)
    parts.length == 2 //&& legalAnnotations.contains(parts(1))
  }

  // precondition: isParseable(line)
  private def parseAnnotation(line: String): AnnotatedWord = {
    val parts = getParts(line)
    AnnotatedWord(parts(0), parts(1))
  }

  private def spanInclusive(words: List[AnnotatedWord], predicate: (AnnotatedWord) => Boolean) = {
    def spanInclusiveInner(state: Tuple2[List[AnnotatedWord], List[AnnotatedWord]]): Tuple2[List[AnnotatedWord], List[AnnotatedWord]] = {
      state match {
        case (_, Nil) => state
        case (reverseSentence, first :: rest) if predicate(first) => (first :: reverseSentence, rest)
        case (reverseSentence, first :: rest) => spanInclusiveInner((first :: reverseSentence, rest))
      }
    }

    val (sentenceReversed, rest) = spanInclusiveInner(Nil, words)
    (sentenceReversed.reverse, rest)
  }

  private def makeSentences(words: List[AnnotatedWord]): List[List[AnnotatedWord]] = {
    if (words.isEmpty)
      List[List[AnnotatedWord]]()
    else {
      val (sentence, rest) = spanInclusive(words, (aw) => aw.word == sentenceSplitter)
      sentence :: makeSentences(rest)
    }
  }

  private def makeTestCase(sentence: List[AnnotatedWord]): TestCase = {
    def makePredicate: (List[String]) => (AnnotatedWord) => Boolean = (words) => (aw) => words.contains(aw.annotation)
    val persons = contiguousGroups(sentence, makePredicate(List(persB, persI, perB, perI)))
    val locations = contiguousGroups(sentence, makePredicate(List(locB, locI)))
    val organizations = contiguousGroups(sentence, makePredicate(List(orgB, orgI)))
    def mkString(aws: List[AnnotatedWord]): String = {
      aws.map(_.word).foldLeft(new StringBuilder)((acc: StringBuilder, word: String) => {
        word match {
          // backticks to provide a stable identifier for the pattern match, otherwise the pattern will just match anything.
          case `sentenceSplitter` => acc.append(word)
          case _ => acc.append(" ").append(word)
        }
      }).toString.trim
    }
    TestCase(mkString(sentence), persons.map(mkString), locations.map(mkString), organizations.map(mkString))
  }

  private def contiguousGroups(words: List[AnnotatedWord], predicate: (AnnotatedWord) => Boolean): List[List[AnnotatedWord]] = {
    words match {
      case Nil => List[List[AnnotatedWord]]()
      case word :: rest if predicate(word) => {
        val (group, rest) = words.span(predicate)
        group :: contiguousGroups(rest, predicate)
      }
      case word :: rest => contiguousGroups(rest, predicate)
    }
  }

  def readANERCorpTestCases: List[TestCase] = {
    val lines = Source.fromFile("src/test/resources/ANERCorp.txt", "UTF-8").getLines
//    val lines = Source.fromFile("src/test/resources/AQMAR_Arabic_NER_corpus-1.0.txt").getLines
    val annotatedLines = lines.filter(isParseable).map(parseAnnotation).toList
    val sentences: List[List[AnnotatedWord]] = makeSentences(annotatedLines)
    sentences.map(makeTestCase)
  }

  def readAQMARCorpTestCases: List[TestCase] = {
    val lines = Source.fromFile("src/test/resources/AQMAR_Arabic_NER_corpus-1.0.txt", "UTF-8").getLines
    val annotatedLines = lines.filter(isParseable).map(parseAnnotation).toList
    val sentences: List[List[AnnotatedWord]] = makeSentences(annotatedLines)
    sentences.map(makeTestCase)
  }
}
