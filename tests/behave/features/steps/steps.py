# coding=utf-8

import json
import re

import requests
from behave import given, when, then


@given(u'the URI for the resource “a collection of users”')  # noqa
def step_impl(context):
    context.uri = 'http://localhost:5000/users'


@given(u'a valid JSON representation of a new user')  # noqa
def step_impl(context):
    context.req_obj = {
        'name': 'Amanda Hugginkiss',
        'address': {
            'zip': '10605'
        },
        'phone': '867-5309'
    }

    context.req_body = json.dumps(context.req_obj)


@given(u'a valid set of request headers to create a new user')  # noqa
def step_impl(context):
    context.req_headers = {
        'Content-Type': 'application/json',
        'Content-Length': len(context.req_body),
        'Accept': 'application/json',
        'API-Key': '13tm31n',
    }


@given(u'the request header "{name}" is set to "{value}"')  # noqa
def step_impl(context, name, value):
    context.req_headers[name] = value


@given(u'the request header "{name}" is removed')  # noqa
def step_impl(context, name):
    del context.req_headers[name]


@given(u'the request body is set to "{text}"')  # noqa
def step_impl(context, text):
    context.req_body = text


@given(u'the request body key "{key}" is removed')  # noqa
def step_impl(context, key):
    del context.req_obj[key]
    context.req_body = json.dumps(context.req_obj)


@given(u'the request body key "{key}" is set to "{value}"')  # noqa
def step_impl(context, key, value):
    context.req_obj[key] = value
    context.req_body = json.dumps(context.req_obj)


@given(u'the request body key "{key}" is set to ""')  # noqa
def step_impl(context, key):
    context.req_obj[key] = ""
    context.req_body = json.dumps(context.req_obj)


@given(u'the request body contains an invalid ZIP code')  # noqa
def step_impl(context):
    context.req_obj['address']['zip'] = 'foo'
    context.req_body = json.dumps(context.req_obj)


@when(u'we send a {method} request to the resource')  # noqa
def step_impl(context, method):
    fn = getattr(requests, method.lower())
    context.response = fn(context.uri, headers=context.req_headers, data=context.req_body)


@when(u'we send {num} {method} requests to the resource')  # noqa
def step_impl(context, num, method):
    fn = getattr(requests, method.lower())
    context.responses = [fn(context.uri, headers=context.req_headers, data=context.req_body)
                         for _ in range(int(num))]


@then(u'the response status code should be {code:d}')  # noqa
def step_impl(context, code):
    assert context.response.status_code == code, "expected {}, got {}".format(code, context.response.status_code)


@then(u'the response header "{name}" should be "{value}"')  # noqa
def step_impl(context, name, value):
    assert context.response.headers[name] == value


@then(u'the response header "{name}" should contain "{value}"')  # noqa
def step_impl(context, name, value):
    assert value in context.response.headers[name]


@then(u'the response should contain no body')  # noqa
def step_impl(context):
    assert context.response.content == ''


@then(u'the response body should contain "{text}"')  # noqa
def step_impl(context, text):
    assert text.lower() in context.response.text.lower()


@then(u'the response header "{header}" should match "{pattern}"')  # noqa
def step_impl(context, header, pattern):
    assert re.search(pattern, context.response.headers[header])


@then(u'the response body should be valid JSON')  # noqa
def step_impl(context):
    # this will raise an exception if the response text is not valid JSON
    json.loads(context.response.text)


@then(u'the response object should be a valid user object')  # noqa
def step_impl(context):
    obj = json.loads(context.response.text)
    assert len(obj['name'])
    assert len(obj['phone'])
    assert len(obj['address']['zip'])
    assert re.match('\d{5}', obj['address']['zip'])
    assert 'http' in obj['links']['news']
    assert 'news' in obj['links']['news']


@then(u'the response object should be a valid user news object')  # noqa
def step_impl(context):
    obj = json.loads(context.response.text)
    assert len(obj['weather']['forecast'].keys())
    assert len(obj['news']['headlines'][0])


def _head(uri):
    headers = {
        'Accept': 'application/json',
        'API-Key': '13tm31n',
    }
    return requests.head(uri, headers=headers)


@then(u'a HEAD request to the URI of the new resource should return a 200')  # noqa
def step_impl(context):
    response = _head(context.response.headers['Location'])
    assert response.status_code == 200


@then(u'the Locations of the new resources should be different')  # noqa
def step_impl(context):
    s = set([r.headers['Location'] for r in context.responses])
    assert len(s) == len(context.responses)


@then(u'HEAD requests to the Locations of the new resources should return 200s')  # noqa
def step_impl(context):
    for post_response in context.responses:
        head_response = _head(post_response.headers['Location'])
        assert head_response.status_code == 200


@then(u'extract the URI for the new “user” resource')  # noqa
def step_impl(context):
    context.uri = context.response.headers['Location']


@then(u'extract the URI for the new “user news” resource')  # noqa
def step_impl(context):
    obj = json.loads(context.response.text)
    context.uri = obj['links']['news']


@then(u'create a valid set of request headers to retrieve a resource')  # noqa
def step_impl(context):
    context.req_headers = {
        'Accept': 'application/json',
        'API-Key': '13tm31n',
    }
