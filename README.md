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

## Usage Examples

### Create a user

    $ curl -i -X POST -H API-Key:13tm31n -H Content-Type:application/json -d '{"name":"Amanda Hugginkiss", "address":{"zip":"10025"}, "phone":"867-5309"}' http://localhost:5000/users
    HTTP/1.1 201 Created
    Date: Sun, 25 Jan 2015 16:26:48 GMT
    Location: http://localhost:5000/users/1
    Content-Type: application/json;charset=UTF-8
    Content-Length: 99
    Server: Jetty(7.6.13.v20130916)

    {
      "name": "Amanda Hugginkiss",
      "address": {
        "zip": "10025"
      },
      "phone": "867-5309",
      "links": {
        "news": "http://localhost:5000/users/1/news"
      }
    }

### Retrieve a user

    $ curl -i -X GET -H API-Key:13tm31n -H Accept:application/json http://localhost:5000/users/1
    HTTP/1.1 200 OK
    Date: Sun, 25 Jan 2015 17:15:27 GMT
    Content-Type: application/json;charset=UTF-8
    Content-Length: 99
    Server: Jetty(7.6.13.v20130916)

    {
      "name": "Amanda Hugginkiss",
      "address": {
        "zip": "10025"
      },
      "phone": "867-5309",
      "links": {
        "news": "http://localhost:5000/users/1/news"
      }
    }
