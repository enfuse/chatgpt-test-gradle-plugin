package io.enfuse.plugin

import spock.lang.Specification

class ChatGPTPluginConfigurationTest extends Specification {

    void "basic test"() {
        when:
        ChatGPTPluginConfiguration configuration = new ChatGPTPluginConfiguration("sk-XXXX", "org", "text-davinci-003", 0f)

        then:
        configuration.getOpenAIKey() == "sk-XXXX"
        configuration.getOpenAIOrganization() == "org"
        configuration.getModel() == "text-davinci-003"
        configuration.getTemperature() == 0f
    }
}
