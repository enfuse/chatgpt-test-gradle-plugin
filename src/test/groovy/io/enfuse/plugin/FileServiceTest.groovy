package io.enfuse.plugin

import org.gradle.api.tasks.testing.TestDescriptor
import spock.lang.Specification

class FileServiceTest extends Specification {

    void testGetTestCode_success() {
        given:
        TestDescriptor testDescriptor = Mock(TestDescriptor)
        2 * testDescriptor.getClassName() >> "GreetingControllerIntegrationTest"
        1 * testDescriptor.getDisplayName() >> "getOne_retrievesGreeting"

        when:
        MessageResponse response = FileService.getTestCode(testDescriptor, './src/test/resources/')

        then:
        response.isSuccess()
        response.getMessage().trim() == 'void getOne_retrievesGreeting() throws Exception {\n        mockMvc.perform(get("/greetings/1"))\n                .andExpect(status().isOk());\n    }'
    }

    void testGetTestCode_notFound() {
        given:
        TestDescriptor testDescriptor = Mock(TestDescriptor)
        2 * testDescriptor.getClassName() >> "GreetingControllerIntegrationTest"

        when:
        MessageResponse response = FileService.getTestCode(testDescriptor, 'not-a-directory')

        then:
        !response.isSuccess()
        response.getMessage().trim() == 'Failed to find test code class matching [GreetingControllerIntegrationTest] at path [not-a-directory].'
    }

    void testGetClassCode_success() {
        given:
        TestDescriptor testDescriptor = Mock(TestDescriptor)
        2 * testDescriptor.getClassName() >> "GreetingController"

        when:
        MessageResponse response = FileService.getClassCode(testDescriptor, './src/test/resources/')

        then:
        response.isSuccess()
        response.getMessage().trim() == 'class GreetingController {\n    String helloWorld() {\n        return "Hello World. I am an example java class."\n    }\n}'
    }

    void testGetClassCode_fileNotFound() {
        given:
        TestDescriptor testDescriptor = Mock(TestDescriptor)
        2 * testDescriptor.getClassName() >> "GreetingController"

        when:
        MessageResponse response = FileService.getClassCode(testDescriptor, './not-a-directory')

        then:
        !response.isSuccess()
        response.getMessage() == 'Failed to find source code class matching [GreetingController] at path [./not-a-directory].'
    }

    void testGetClassCode_readerException() {
        //todo
    }

    void testGetTestClassName() {
        given:
        TestDescriptor testDescriptor = Mock(TestDescriptor)
        2 * testDescriptor.getClassName() >> "io.enfuse.GreetingController"

        when:
        String response = FileService.getTestClassName(testDescriptor)

        then:
        response == "GreetingController"
    }

    void testGetAllFilePaths() {
        when:
        List<FileService.FoundFile> response = FileService.getAllFilePaths('./src/test/resources/test')

        then:
        response.size() == 3
        response[0].getPath() == "./src/test/resources/test/file1.txt"
        response[0].getName() == "file1.txt"
        response[1].getPath() == "./src/test/resources/test/subdirectory/GreetingControllerIntegrationTest.java"
        response[1].getName() == "GreetingControllerIntegrationTest.java"
        response[2].getPath() == "./src/test/resources/test/subdirectory/GreetingController.java"
        response[2].getName() == "GreetingController.java"

    }

    void testFindClosestMatch() {
        given:
        FileService.FoundFile file1 = new FileService.FoundFile("./src/main/java/io/enfuse/GreetingController.java", "GreetingController.java")
        FileService.FoundFile file2 = new FileService.FoundFile("./src/main/java/io/enfuse/GreetingService.java", "GreetingService.java")
        FileService.FoundFile file3 = new FileService.FoundFile("./src/main/java/io/enfuse/GreetingRepository.java", "GreetingRepository.java")
        FileService.FoundFile file4 = new FileService.FoundFile("./src/main/java/io/enfuse/GreetingModel.java", "GreetingModel.java")
        List<FileService.FoundFile> files = Arrays.asList(file1, file2, file3, file4)

        when:
        FileService.FoundFile controller = FileService.findClosestMatch(files, "GreetingControllerTest.java")
        FileService.FoundFile controllerIntegration = FileService.findClosestMatch(files, "GreetingControllerIntegrationTest.java")
        FileService.FoundFile service = FileService.findClosestMatch(files, "GreetingServiceTest.java")
        FileService.FoundFile repository = FileService.findClosestMatch(files, "GreetingRepositoryTest.java")
        FileService.FoundFile random = FileService.findClosestMatch(files, "file-that-wont-be-found")

        then:
        controller == file1
        controllerIntegration == file1
        service == file2
        repository == file3
        random == file1
    }
}
