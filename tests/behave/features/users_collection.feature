Feature: The resource “a collection of users”

  Background:
     Given the URI for the resource “a collection of users”
       And a valid JSON representation of a new user
       And a valid set of request headers to create a new user

  Scenario: The resource should support OPTIONS
      When we send a OPTIONS request to the resource
      Then the response status code should be 204
       And the response header "Allow" should be "OPTIONS, POST"
       And the response should contain no body

  Scenario: The resource should not support HEAD
      When we send a HEAD request to the resource
      Then the response status code should be 405
       And the response header "Content-Type" should contain "text/plain"
       And the response should contain no body

  Scenario: The resource should not support GET
      When we send a GET request to the resource
      Then the response status code should be 405
       And the response header "Content-Type" should contain "text/plain"
       And the response body should contain "not allowed"

  Scenario: The resource should not support PUT
      When we send a PUT request to the resource
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

  Scenario: The resource should reject a POST request that’s missing the header API-Key
     Given the request header "API-Key" is removed
      When we send a POST request to the resource
      Then the response status code should be 401
       And the response header "Content-Type" should contain "text/plain"
       And the response body should contain "API-Key is required"

  Scenario: The resource should reject a POST request if the API-Key header has an invalid value
     Given the request header "API-Key" is set to "foo"
      When we send a POST request to the resource
      Then the response status code should be 403
       And the response header "Content-Type" should contain "text/plain"
       And the response body should contain "not allowed to access this resource"

  Scenario: The resource should reject a POST request if the header Content-Type is not sent
     Given the request header "Content-Type" is removed
      When we send a POST request to the resource
      Then the response status code should be 400
       And the response header "Content-Type" should contain "text/plain"
       And the response body should contain "Content-Type"

  Scenario: The resource should reject a POST request if the header Content-Type is not application/json
     Given the request header "Content-Type" is set to "text/plain"
      When we send a POST request to the resource
      Then the response status code should be 415
       And the response header "Content-Type" should contain "text/plain"
       And the response body should contain "must be of type application/json"

  # Skipped because I don’t know a quick and easy way to prevent Requests from sending
  # the request header Content-Length
  # Scenario: The resource should reject a POST request if the header Content-Length is not sent
  #    Given the request header "Content-Length" is removed
  #     When we send a POST request to the resource
  #     Then the response status code should be 411
  #      And the response header "Content-Type" should contain "text/plain"
  #      And the response body should contain "must contain the header Content-Length"

  # Skipped because I don’t know a quick and easy way to prevent Requests from overriding
  # the value of Content-Length
  # Scenario: The resource should reject a POST request if the header Content-Length has an invalid string value
  #    Given the request header "Content-Length" is set to "foo"
  #     When we send a POST request to the resource
  #     Then the response status code should be 400

  # Skipped because I don’t know a quick and easy way to prevent Requests from overriding
  # the value of Content-Length
  # Scenario: The resource should reject a POST request if the header Content-Length has an invalid integer value
  #    Given the request header "Content-Length" is set to "0"
  #     When we send a POST request to the resource
  #     Then the response status code should be 400
  #      And the response header "Content-Type" should contain "text/plain"
  #      And the response body should contain "Content-Length"

  Scenario: The resource should reject a POST request if the request body is not valid JSON
     Given the request body is set to "--foo--"
      When we send a POST request to the resource
      Then the response status code should be 400
       And the response header "Content-Type" should contain "text/plain"
       And the response body should contain "Malformed JSON in request body"

  Scenario: The resource should reject a POST request if the request body is missing a required key
     Given the request body key "name" is removed
      When we send a POST request to the resource
      Then the response status code should be 400
       And the response header "Content-Type" should contain "text/plain"
       And the response body should contain "name"

  Scenario: The resource should reject a POST request if the request body contains an empty required key
     Given the request body key "name" is set to ""
      When we send a POST request to the resource
      Then the response status code should be 400
       And the response header "Content-Type" should contain "text/plain"
       And the response body should contain "failed validation"
       And the response body should contain "name"

  Scenario: The resource should reject a POST request if the request body key 'address' is not an object
     Given the request body key "address" is set to "whatever"
      When we send a POST request to the resource
      Then the response status code should be 400
       And the response header "Content-Type" should contain "text/plain"
       And the response body should contain "failed validation"
       And the response body should contain "address"

  Scenario: The resource should reject a POST request if the request body contains an invalid ZIP code
     Given the request body contains an invalid ZIP code
      When we send a POST request to the resource
      Then the response status code should be 400
       And the response header "Content-Type" should contain "text/plain"
       And the response body should contain "failed validation"
       And the response body should contain "zip"

  Scenario: A valid POST request should result in the creation of a new user resource
      When we send a POST request to the resource
      Then the response status code should be 201
       And the response header "Location" should match "/users/\d"
       And the response header "Content-Type" should contain "application/json"
       And the response body should be valid JSON
       And the response object should be a valid user object
       And a HEAD request to the URI of the new resource should return a 200

  Scenario: Two valid POST requests should result in the creation of a 2 new user resources
      When we send 2 POST requests to the resource
      Then the Locations of the new resources should be different
       And HEAD requests to the Locations of the new resources should return 200s
