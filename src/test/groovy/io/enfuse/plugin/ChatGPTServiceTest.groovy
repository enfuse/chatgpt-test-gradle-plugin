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

        HttpURLConnection expectedResponse = Stub(HttpURLConnection)
        GroovySpy(WebRequestService, global: true)

        when:
        HttpURLConnection response = ChatGPTService.prepareRequest(body, configuration)

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

    void testProcessResponseAndReturn_happyPath() {
        given:
        String content = '{"choices":[{"finish_reason":"stop","text":"This is a completion"}]}'

        HttpURLConnection mockConnection = Mock(HttpURLConnection)
        1 * mockConnection.getResponseCode() >> 200
        1 * mockConnection.getInputStream() >> new ByteArrayInputStream(content.getBytes())

        when:
        MessageResponse response = ChatGPTService.processResponseAndReturn(mockConnection)

        then:
        response.isSuccess()
        response.getMessage() == "This is a completion"
    }

    void testProcessResponseAndReturn_lengthStop() {
        given:
        String content = '{"choices":[{"finish_reason":"length","text":"This is a completion"}]}'

        HttpURLConnection mockConnection = Mock(HttpURLConnection)
        1 * mockConnection.getResponseCode() >> 200
        1 * mockConnection.getInputStream() >> new ByteArrayInputStream(content.getBytes())

        when:
        MessageResponse response = ChatGPTService.processResponseAndReturn(mockConnection)

        then:
        !response.isSuccess()
        response.getMessage() == "ChatGPT response was interrupted due to message length. Here was interrupted response\n This is a completion"
    }

    void testProcessResponseAndReturn_error() {
        given:
        String content = '{"error":{"message":"I am a generic authentication exception"}}'

        HttpURLConnection mockConnection = Mock(HttpURLConnection)
        1 * mockConnection.getResponseCode() >> 401
        1 * mockConnection.getErrorStream() >> new ByteArrayInputStream(content.getBytes())

        when:
        MessageResponse response = ChatGPTService.processResponseAndReturn(mockConnection)

        then:
        !response.isSuccess()
        response.getMessage() == "Failed to call chatGPT. Error [I am a generic authentication exception]"
    }

    void testCallChatGPTCompletion() {
        given:
        ChatGPTPluginConfiguration configuration = new ChatGPTPluginConfiguration("sk-XXXX", "org", "text-davinci-003", 0f)
        String prompt = 'Some complicated prompt'

        String content = '{"choices":[{"finish_reason":"stop","text":"This is a completion"}]}'

        HttpURLConnection mockConnection = Mock(HttpURLConnection)
        mockConnection.getResponseCode() >> 200
        mockConnection.getInputStream() >> new ByteArrayInputStream(content.getBytes())
        GroovySpy(WebRequestService, global: true)
        1 * WebRequestService.preparePostRequest(_,_,_) >> { return mockConnection }

        when:
        MessageResponse response = ChatGPTService.callChatGPTCompletion(prompt, configuration)

        then:
        response.isSuccess()
        response.getMessage() == "This is a completion"
    }
}
