package io.enfuse.plugin

class ChatGPTPluginExtension {

    String path = "env.properties"
    String codeDirectory = "./src/main/java/"
    String testDirectory = "./src/test/java/"

    String getPath() {
        return path
    }

    void setPath(String path) {
        this.path = path
    }

    String getCodeDirectory() {
        return codeDirectory
    }

    void setCodeDirectory(String codeDirectory) {
        this.codeDirectory = codeDirectory
    }

    String getTestDirectory() {
        return testDirectory
    }

    void setTestDirectory(String testDirectory) {
        this.testDirectory = testDirectory
    }
}
