package io.enfuse.plugin

import groovy.json.JsonSlurper
import groovy.json.StringEscapeUtils

class ChatGPTService {

    public static String OPEN_AI_COMPLETIONS_ENDPOINT = "https://api.openai.com/v1/completions"

    static String preparePrompt(String testCode, String sourceCode, String error) {
        return """I am a Test-Driven-Development coding assistant. I will be sent some test code, source code and a failure and will do my best to explain a possible solutions to fix the test failure (or exception).
TEST CODE
------
$testCode
------
SOURCE CODE
------
$sourceCode
------
ERROR
------
$error
------
SOLUTION:"""
    }

    static MessageResponse callChatGPTCompletion(String prompt, ChatGPTPluginConfiguration configuration) {
        prompt = sanitizePrompt(prompt)
        String body = prepareJSONRequest(prompt, configuration)

        HttpURLConnection request = prepareRequest(body, configuration)

        return processResponseAndReturn(request)
    }
    static String sanitizePrompt(String prompt) {
        return StringEscapeUtils.escapeJava(prompt)
    }

    static String prepareJSONRequest(String prompt, ChatGPTPluginConfiguration configuration) {
        return """{"model":"${configuration.getModel()}","prompt":"$prompt","temperature":${configuration.getTemperature()},"max_tokens":2000}"""
        // TODO: make max tokens (max minus prompt tokens count)
    }

    static HttpURLConnection prepareRequest(String body, ChatGPTPluginConfiguration configuration) {
        Map<String, String> headers = new HashMap<>()
        headers.put("Content-Type", "application/json")
        headers.put("Authorization", "Bearer ${configuration.getOpenAIKey()}")
        if (configuration.getOpenAIOrganization() != null) {
            headers.put("OpenAI-Organization", configuration.getOpenAIOrganization())
        }

        return WebRequestService.preparePostRequest(OPEN_AI_COMPLETIONS_ENDPOINT, body, headers)
    }

    static MessageResponse processResponseAndReturn(HttpURLConnection request) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        int responseCode = request.getResponseCode()
        def response = responseCode == 200 ? jsonSlurper.parseText(request.getInputStream().getText()) : jsonSlurper.parseText(request.getErrorStream().getText())

        if (responseCode != 200) {
            println "Something went terribly, terribly, wrong"
            println(response)
            return new MessageResponse(false, String.format("Failed to call chatGPT. Error [%s]", response.error.message))
        }
        if (response.choices[0].finish_reason == 'length') {
            println('Oh no! The length was too short!')
            return new MessageResponse(false, String.format("ChatGPT response was interrupted due to message length. Here was interrupted response\n %s", response.choices[0].text.trim()))
        }
        return new MessageResponse(true, response.choices[0].text.trim())
    }

}
