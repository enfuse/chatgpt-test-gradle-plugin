package io.enfuse.plugin


import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult

class ChatGPTPlugin implements Plugin<Project> {

    static final String ANSI_RESET = "\u001B[0m"
    static final String ANSI_BLACK = "\u001B[30m"
    static final String ANSI_RED = "\u001B[31m"
    static final String ANSI_GREEN = "\u001B[32m"
    static final String ANSI_YELLOW = "\u001B[33m"
    static final String ANSI_BLUE = "\u001B[34m"
    static final String ANSI_PURPLE = "\u001B[35m"
    static final String ANSI_CYAN = "\u001B[36m"
    static final String ANSI_WHITE = "\u001B[37m"

    static final String ANSI_BLACK_BACKGROUND = "\u001B[40m"
    static final String ANSI_RED_BACKGROUND = "\u001B[41m"
    static final String ANSI_GREEN_BACKGROUND = "\u001B[42m"
    static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m"
    static final String ANSI_BLUE_BACKGROUND = "\u001B[44m"
    static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m"
    static final String ANSI_CYAN_BACKGROUND = "\u001B[46m"
    static final String ANSI_WHITE_BACKGROUND = "\u001B[47m"

    void apply(Project project) {
        ChatGPTPluginExtension extension = project.getExtensions()
                .create("chatGPT", ChatGPTPluginExtension.class)

        project.tasks.withType(Test).configureEach({ Test test ->
            ChatGPTPluginConfiguration configuration = validatePropsAndCreateConfiguration(extension) //todo throws exception, graceful failure?
            def listener = { TestDescriptor testDescriptor, TestResult testResult ->
                if (testResult.getResultType() != TestResult.ResultType.FAILURE) {
                    return
                }
                println()
                print('    ')
                System.out.println("${FileService.getTestClassName(testDescriptor)} > ${testDescriptor.getDisplayName()}$ANSI_RED FAILED $ANSI_RESET")
                String error = getParsedErrorFromException(testResult.getException())
                println(error)
                print('    ')

                MessageResponse classCodeResponse = FileService.getClassCode(testDescriptor, extension.getCodeDirectory())
                if (!classCodeResponse.isSuccess()) {
                    println(ANSI_RED + classCodeResponse.getMessage() + ANSI_RESET)
                    return
                }
                MessageResponse testCodeResponse = FileService.getTestCode(testDescriptor, extension.getTestDirectory())
                if (!testCodeResponse.isSuccess()) {
                    println(ANSI_RED + classCodeResponse.getMessage() + ANSI_RESET)
                    return
                }
                String classCode = classCodeResponse.getMessage()
                String testCode = testCodeResponse.getMessage()

                String prompt = ChatGPTService.preparePrompt(testCode, classCode, error)
                println("Calling ${ANSI_GREEN}ChatGPT${ANSI_RESET} for test resolution:")
                MessageResponse response = ChatGPTService.callChatGPTCompletion(prompt, configuration)

                println((response.isSuccess() ? ANSI_GREEN : ANSI_RED) + response.getMessage() + ANSI_RESET)
            }
            test.testLogging.lifecycle.events = []
            test.afterTest(listener)
        })
    }

    private static String getParsedErrorFromException(Throwable exception) {
        if (exception.getCause() == null) {
            return exception.toString()
        }
        int STACK_TRACE_DEPTH = 5

        StackTraceElement[] stackTrace = exception.getStackTrace()
        StringBuilder stackTraceString = new StringBuilder()
        for (int i = 0; i < STACK_TRACE_DEPTH; i++) {
            if (i > stackTrace.length) {
                break
            }
            stackTraceString.append(stackTrace[i]).append("\n")
        }
        return stackTraceString
    }

    private static ChatGPTPluginConfiguration validatePropsAndCreateConfiguration(ChatGPTPluginExtension extension) {
        Properties props = new Properties()
        props.load(new File(extension.getPath()).newReader())
        String openAIKey = props.getProperty("openAIKey")
        if (openAIKey == null) {
            throw new GradleException("The [openAIKey] property is required. Please include it in your ${extension.getPath()} file. File path can be specified in your build.gradle under the chatGPT block.")
        }

        String openAIOrg = props.getProperty("openAIOrganization", null)
        String model = props.getProperty("model", "text-davinci-003")
        float temperature = Float.parseFloat(props.getProperty("temperature", "0"))
        return new ChatGPTPluginConfiguration(openAIKey, openAIOrg, model, temperature)
    }
}
