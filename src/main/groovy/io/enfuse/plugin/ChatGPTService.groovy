package io.enfuse.plugin

import groovy.json.JsonSlurper
import groovy.json.StringEscapeUtils

class ChatGPTService {

    static String preparePrompt(String testCode, String mainCode, String error) {
        return """I am a Test-Driven-Development coding assistant. I will be sent some test code, main code and a failure and will do my best to explain a possible solutions to fix the test failure (or exception).
TEST CODE
------
$testCode
------
MAIN CODE
------
$mainCode
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
//        println(body)

        URLConnection request = prepareRequest(body, configuration)

        return processResponseAndReturn(request)
    }

    private static MessageResponse processResponseAndReturn(URLConnection request) {
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

    static String sanitizePrompt(String prompt) {
        return StringEscapeUtils.escapeJava(prompt)
    }

    static String prepareJSONRequest(String prompt, ChatGPTPluginConfiguration configuration) {
        return """{"model":"${configuration.getModel()}","prompt":"$prompt","temperature":${configuration.getTemperature()},"max_tokens":2000}"""
        // TODO: make max tokens (max minus prompt tokens count)
    }

    static URLConnection prepareRequest(String body, ChatGPTPluginConfiguration configuration) {
        URLConnection postRequest = new URL("https://api.openai.com/v1/completions").openConnection()

        postRequest.setRequestMethod("POST")
        postRequest.setDoOutput(true)
        postRequest.setRequestProperty("Content-Type", "application/json")
        postRequest.setRequestProperty("Authorization", "Bearer ${configuration.getOpenAIKey()}")
        if (configuration.getOpenAIOrganization() != null) {
            postRequest.setRequestProperty("OpenAI-Organization", configuration.getOpenAIOrganization())
        }
        //todo intelliJ says better way?
        postRequest.getOutputStream().write(body.getBytes("UTF-8"))

        return postRequest
    }
}
