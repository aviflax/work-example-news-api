Feature: The resource “the news for a user”

  Background:
     Given the URI for the resource “a collection of users”
       And a valid JSON representation of a new user
       And a valid set of request headers to create a new user
      When we send a POST request to the resource
      Then the response status code should be 201
       And extract the URI for the new “user news” resource
       And create a valid set of request headers to retrieve a resource

  Scenario: The resource should support OPTIONS
      When we send a OPTIONS request to the resource
      Then the response status code should be 204
       And the response header "Allow" should be "OPTIONS, HEAD, GET"
       And the response should contain no body

  Scenario: The resource should support HEAD
      When we send a HEAD request to the resource
      Then the response status code should be 200
       And the response header "Content-Type" should contain "application/json"
       And the response should contain no body

  Scenario: The resource should not support PUT
      When we send a PUT request to the resource
      Then the response status code should be 405
       And the response header "Content-Type" should contain "text/plain"
       And the response body should contain "not allowed"

  Scenario: The resource should not support POST
      When we send a POST request to the resource
      Then the response status code should be 405
       And the response header "Content-Type" should contain "text/plain"
       And the response body should contain "not allowed"

  Scenario: The resource should not support DELETE
      When we send a DELETE request to the resource
      Then the response status code should be 405
       And the response header "Content-Type" should contain "text/plain"
       And the response body should contain "not allowed"

  Scenario: The resource should not support PATCH
      When we send a PATCH request to the resource
      Then the response status code should be 405
       And the response header "Content-Type" should contain "text/plain"
       And the response body should contain "not allowed"

  Scenario: The resource should reject a GET request that’s missing the header API-Key
     Given the request header "API-Key" is removed
      When we send a GET request to the resource
      Then the response status code should be 401
       And the response header "Content-Type" should contain "text/plain"
       And the response body should contain "API-Key is required"

  Scenario: The resource should reject a GET request if the API-Key header has an invalid value
     Given the request header "API-Key" is set to "foo"
      When we send a GET request to the resource
      Then the response status code should be 403
       And the response header "Content-Type" should contain "text/plain"
       And the response body should contain "not allowed to access this resource"

  Scenario: The resource should reject a GET request if the header Accept is unacceptable
     Given the request header "Accept" is set to "text/plain"
      When we send a GET request to the resource
      Then the response status code should be 406
       And the response header "Content-Type" should contain "text/plain"
       And the response body should contain "supports only application/json"

  Scenario: The resource should response to a valid GET request with a success response
      When we send a GET request to the resource
      Then the response status code should be 200
       And the response header "Content-Type" should contain "application/json"
       And the response body should be valid JSON
       And the response object should be a valid user news object
