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

### Retrieve a user as XML

    $ curl -i -H API-Key:13tm31n -H Accept:application/xml http://localhost:5000/users/1
    HTTP/1.1 200 OK
    Date: Mon, 26 Jan 2015 18:14:36 GMT
    Content-Type: application/xml;charset=UTF-8
    Content-Length: 201
    Server: Jetty(7.6.13.v20130916)

    <?xml version="1.0" encoding="UTF-8"?><user><name>Amanda Hugginkiss</name><phone>867-5309</phone><address><zip>10025</zip></address><links><news>http://localhost:5000/users/1/news</news></links></user>

### Retrieve the news for a user

    $ curl -i -X GET -H API-Key:13tm31n -H Accept:application/json http://localhost:5000/users/1/news
    HTTP/1.1 200 OK
    Date: Sun, 25 Jan 2015 17:15:27 GMT
    Content-Type: application/json;charset=UTF-8
    Transfer-Encoding: chunked
    Server: Jetty(7.6.13.v20130916)

    {
      "weather": {
        "forecast": {
            ...lots of keys here...
        }
      },
      "news": {
        "headlines": [
            "Chinese Officials Vow To Fix Nation’s Crumbling Reeducation System",
            "Medical Breakthrough Provides Elderly Woman With 2 Extra Years Of Inconveniencing Family",
            "Nation’s Historians Warn The Past Is Expanding At Alarming Rate",
            "Man With Serious Mental Illness Committed To City Bus"
        ]
      }
    }

## Cache Testing

The resource “news for a user” contains data from external services, and can therefore be quite slow. Its implementation therefore uses caching to ensure that it isn’t slow. (This also reduces usage of the external services, which can save money.)

Here’s a primitive approach to confirming that the resource is caching the external data and not retrieving it for every request.

(This assumes a bash shell.)

1. Create a new user resource as above. The below commands assume the user has the ID "1"
2. Warm the cache: `time curl -H API-Key:13tm31n -H Accept:application/json http://localhost:5000/users/1/news`
3. Note the time required for the initial call; it’s generally 700–1200 ms
4. send a bunch of concurrent requests: `ab -c 40 -n 2000 -H API-Key:13tm31n -H Accept:application/json http://localhost:5000/users/1/news`
5. note the median time for all requests; it should be less than 100 ms
