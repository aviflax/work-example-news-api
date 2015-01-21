# coding=utf-8

import json

import requests

from behave import given, when, then


@given(u'the URI for the resource “a collection of users”')  # noqa
def step_impl(context):
    context.uri = 'http://localhost:5000/users'


@given(u'a valid JSON representation of a new user')  # noqa
def step_impl(context):
    context.req_headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'API-Key': '13tm31n'
    }

    context.req_body = json.dumps({
        'name': 'Amanda Hugginkiss',
        'address': {
            'zip': '10605'
        },
        'phone': '867-5309'
    })


@given(u'the request header "{name}" is set to "{value}"')  # noqa
def step_impl(context, name, value):
    context.req_headers[name] = value


@given(u'the request header "{name}" is removed')  # noqa
def step_impl(context, name):
    del context.req_headers[name]


@when(u'we send a {method} request to the resource')  # noqa
def step_impl(context, method):
    fn = getattr(requests, method.lower())
    context.response = fn(context.uri, headers=context.req_headers, data=context.req_body)


@then(u'the response status code should be {code:d}')  # noqa
def step_impl(context, code):
    assert context.response.status_code == code


@then(u'the response header "{name}" should be "{value}"')  # noqa
def step_impl(context, name, value):
    assert context.response.headers[name] == value
