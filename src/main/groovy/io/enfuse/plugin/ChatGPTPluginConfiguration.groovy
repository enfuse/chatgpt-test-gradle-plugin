package io.enfuse.plugin

class ChatGPTPluginConfiguration {

    private String openAIKey
    private String openAIOrganization
    private String model
    private float temperature

    String getOpenAIKey() {
        return openAIKey
    }

    void setOpenAIKey(String openAIKey) {
        this.openAIKey = openAIKey
    }

    String getOpenAIOrganization() {
        return openAIOrganization
    }

    void setOpenAIOrganization(String openAIOrganization) {
        this.openAIOrganization = openAIOrganization
    }

    String getModel() {
        return model
    }

    void setModel(String model) {
        this.model = model
    }

    float getTemperature() {
        return temperature
    }

    void setTemperature(float temperature) {
        this.temperature = temperature
    }
}
