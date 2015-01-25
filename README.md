# News API

## Requirements

* Runtime: [a recent JRE (Java Runtime Environment)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* Testing: [Python 2.7](https://www.python.org)

## Install Runtime Dependencies

* [Leiningen](http://leiningen.org)

## Install Development/Testing Dependencies

* Run `pip install -r tests/behave/requirements.txt`

## Running the Server

    lein run

## Running the Tests

First run the server as above.

Then:

    cd tests/behave
    behave
