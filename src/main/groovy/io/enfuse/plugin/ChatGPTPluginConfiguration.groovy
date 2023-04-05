package io.enfuse.plugin

class ChatGPTPluginConfiguration {

    private String openAIKey
    private String openAIOrganization
    private String model
    private float temperature

    ChatGPTPluginConfiguration(String openAIKey, String openAIOrganization, String model, float temperature) {
        this.openAIKey = openAIKey
        this.openAIOrganization = openAIOrganization
        this.model = model
        this.temperature = temperature
    }

    String getOpenAIKey() {
        return openAIKey
    }

    String getOpenAIOrganization() {
        return openAIOrganization
    }

    String getModel() {
        return model
    }

    float getTemperature() {
        return temperature
    }
}
