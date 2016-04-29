package ae.teletronics.nlp.entityextraction

/**
  * Created by Boris on 2016-04-27.
  */

import scala.io.Source

object TestCaseReader {

  case class AnnotatedWord(word: String, annotation: String)

  val persB = "B-PERS"
  val persI = "I-PERS"
  val locB  = "B-LOC"
  val locI  = "I-LOC"
  val orgB  = "B-ORG"
  val orgI  = "I-ORG"
  val miscB = "B-MISC"
  val miscI = "I-MISC"
  val other = "O"

  val legalAnnotations = List(persB, persI, locB, locI, orgB, orgI, miscB, miscI, other)

  private def makeSentence(words: List[AnnotatedWord]): String = {
    words.map(_.word)
         .fold("")((acc: String, word: String) => acc + " " + word)
  }

  private def getParts(line: String): List[String] = {
    line.split("\\s")
        .map(_.trim)
        .filter(!_.isEmpty)
        .toList
  }

  // look at eg. line 77231 in ANERCorp.txt for an example of an unparseable line. We skip those
  private def isParseable(line: String): Boolean = {
    val parts = getParts(line)
    parts.length == 2 && legalAnnotations.contains(parts(1))
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
      val (sentence, rest) = spanInclusive(words, (aw) => aw.word == ".")
      sentence :: makeSentences(rest)
    }
  }

  private def makeTestCase(sentence: List[AnnotatedWord]): TestCase = {
    def makePredicate: (List[String]) => (AnnotatedWord) => Boolean = (words) => (aw) => words.contains(aw.annotation)
    val persons = contiguousGroups(sentence, makePredicate(List(persB, persI)))
    val locations = contiguousGroups(sentence, makePredicate(List(locB, locI)))
    val organizations = contiguousGroups(sentence, makePredicate(List(locB, locI)))
    def mkString: List[AnnotatedWord] => String = (aws) => aws.map(_.word).foldLeft(new StringBuilder)((acc: StringBuilder, word: String) => acc.append(" ").append(word)).toString.trim
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

  def readTestCases: List[TestCase] = {
    val lines = Source.fromFile("src/test/resources/ANERCorp.txt").getLines
    val annotatedLines = lines.filter(isParseable).map(parseAnnotation).toList
    val sentences: List[List[AnnotatedWord]] = makeSentences(annotatedLines)
    sentences.map(makeTestCase)
  }
}
