package io.enfuse.plugin

import groovy.json.JsonSlurper
import spock.lang.Specification

class WebRequestServiceTest extends Specification {
    void testPreparePostRequest() {
        when:
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json")
        headers.put("Authorization", "Bearer sk-XXXXX")

        URLConnection response = WebRequestService.preparePostRequest("https://postman-echo.com/post", '{"key":"value"}', headers)

        then:
        response.getOutputStream().toString() == '{"key":"value"}'
        JsonSlurper jsonSlurper = new JsonSlurper()
        def bodyResponse = jsonSlurper.parseText(response.getInputStream().getText())
        bodyResponse.json.toString() == '{key=value}'
        bodyResponse.headers.authorization == "Bearer sk-XXXXX"
        bodyResponse.headers.get("content-type") == "application/json"
    }
}
