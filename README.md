# nlp-entityextraction
A Maven Scala project wrapping entity extraction (NER) frameworks providing an easy to use API.

####Master branch build status:
[![](https://api.travis-ci.org/repositories/TeletronicsDotAe/nlp-entityextraction.svg?branch=master)](https://travis-ci.org/TeletronicsDotAe/nlp-entityextraction)

####Develop branch build status:
[![](https://api.travis-ci.org/repositories/TeletronicsDotAe/nlp-entityextraction.svg?branch=develop)](https://travis-ci.org/TeletronicsDotAe/nlp-entityextraction)


This project uses the GATE arabic NER packages. GATE does not offer these packages as a maven dependency, so they have been downloaded from the [gate download page](https://gate.ac.uk/download/) and incorporated into the project as explained [here](https://github.com/charlieg/Entity-Extraction-Demos)

The GATE documentation can be found [here](https://gate.ac.uk/sale/tao/split.html)

The structure of the Arabic entity recognizer in this project follows the one shown in [this demo project](https://github.com/charlieg/Entity-Extraction-Demos/blob/master/src/main/java/demo/gate/ArabicExtractor.java)

The file ANERCorp.txt, which lies in ./test/resources and which contains a corpus of marked-up Arabic sentences has been downloaded from [here](http://users.dsic.upv.es/grupos/nle/?file=kop4.php)

ANERCorp.txt is marked up using the mark for the MUC-6 conference, shown [here](https://books.google.dk/books?id=uo0BKcI-qm0C&pg=PA148&lpg=PA148&dq=B-PERS+I-PERS+NER&source=bl&ots=6uTNJOW-kt&sig=74q_GAxgdlHSt_-2XgfVCAWHHM8&hl=da&sa=X&ved=0ahUKEwjV2Ny9la7MAhWEEiwKHfkHAG0Q6AEIJDAA#v=onepage&q=B-PERS%20I-PERS%20NER&f=false)