# ChatGPT Test Gradle Plugin

This is a companion repository for [Andrew Weeks](https://github.com/andyrewwer) and [Javin Hung](https://github.com/jh-enfuseio) SpringOne Explore talk.
You can find this plugin on [gradle here](https://plugins.gradle.org/plugin/io.enfuse.ChatGPTPlugin).

## Usage

1. Include the plugin in your build.gradle
Using the plugins DSL:
```groovy
plugins {
  id "io.enfuse.ChatGPTPlugin" version "X.Y.Z"
}
```
Using legacy plugin application:
```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "io.enfuse.plugin:chatgpt-test-gradle-plugin:X.Y.Z"
  }
}

apply plugin: "io.enfuse.ChatGPTPlugin"
```
> Update X.Y.Z with the your preferred versions. You can find available versions [here](https://plugins.gradle.org/plugin/io.enfuse.ChatGPTPlugin).

3. Create a properties file (e.g. env.properties) and include your openAIAPIKey. You can find a template here [env.properties.template](./env.properties.template)
```properties
#required
openAIKey=sk-XXXXXXXXX

# optional
openAIOrganization=
temperature=0
model=text-davinci-003
```

4. Update configuration in your build.gradle file. You can change the path to your properties file.
```groovy
chatGPT {
    path = "env.properties" // optional, this is default
    codeDirectory = "./src/main/java/" // optional, this is default
    testDirectory = "./src/test/java"  // optional, this is default
}
```

5. run `./gradlew test` and see the output:
```
 $ ./gradlew test

> Task :test
    GreetingControllerIntegrationTest > post_saves() FAILED
java.lang.AssertionError: JSON path "$.content" expected:<This is my content> but was:<null>
    Calling ChatGPT for test resolution:
The error is indicating that the content value is null when it should be "This is my content". This could be due to an issue with the GreetingInfo constructor, which is currently set to accept a null value for the content parameter. To fix this, the constructor should be updated to require a non-null value for the content parameter. Additionally, the post mapping in the GreetingControllerIntegration class should be updated to ensure that the request body is not null before attempting to save it.
    
    GreetingServiceTest > save_shouldSaveCandidate() FAILED
org.opentest4j.AssertionFailedError: expected: <this is amazing content> but was: <null>
    Calling ChatGPT for test resolution:
The test code is expecting the response from the save() method to have a content value of "this is amazing content", however, the main code is not setting the content value of the GreetingRecord object. To fix this, the content value should be set in the main code before the GreetingRecord object is saved. This can be done by adding the following line of code to the save() method:
record.setContent(request.getContent());

16 tests completed, 2 failed
```

## Contributing
If you find something wrong or suggestion, please free feel to submit a PR and assign to `@andyrewwer` or create an issue specifying branch and as many details as possible.  
