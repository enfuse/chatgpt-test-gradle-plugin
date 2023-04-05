package io.enfuse.plugin

import spock.lang.Specification

class ChatGPTServiceTest extends Specification {

    void testPreparePrompt() {
        String inlineTestCode = "void testMethod() { //some test code }"
        String inlineSourceCode = "package enfuse.io.plugin class SourceClass { void method() {return}}"
        String error = "assertion false expected true"

        when:
        String response = ChatGPTService.preparePrompt(inlineTestCode, inlineSourceCode, error)

        then:
        response.contains(inlineTestCode)
        response.contains(inlineSourceCode)
        response.contains(error)

    }

    void testCallChatGPTCompletion() {
    }

    void testSanitizePrompt() {
        when:
        String response = ChatGPTService.sanitizePrompt(""" new line test
""")
        then:
        response == " new line test\\n"
    }

    void testPrepareJSONRequest() {
        when:
        ChatGPTPluginConfiguration configuration = new ChatGPTPluginConfiguration("sk-XXXX", "org", "text-davinci-003", 0f)
        String response = ChatGPTService.prepareJSONRequest("prompt", configuration)

        then:
        response == '{"model":"text-davinci-003","prompt":"prompt","temperature":0.0,"max_tokens":2000}'
    }

    void testPrepareRequest() {
        given:
        ChatGPTPluginConfiguration configuration = new ChatGPTPluginConfiguration("sk-XXXX", "org", "text-davinci-003", 0f)
        String body = '{"model":"text-davinci-003","prompt":"prompt","temperature":0.0,"max_tokens":2000}'

        URLConnection expectedResponse = Stub(URLConnection)
        GroovySpy(WebRequestService, global: true)

        when:
        URLConnection response = ChatGPTService.prepareRequest(body, configuration)

        then:
        1 * WebRequestService.preparePostRequest(_, _, _) >> {
            assert it[0] == ChatGPTService.OPEN_AI_COMPLETIONS_ENDPOINT
            assert it[1] == body
            assert it[2].size() == 3
            assert it[2]["Authorization"] == "Bearer sk-XXXX"
            assert it[2]["Content-Type"] == "application/json"
            assert it[2]["OpenAI-Organization"] == "org"
            return expectedResponse
        }
        response == expectedResponse
    }
}
