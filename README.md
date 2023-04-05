# ChatGPT Test Gradle Plugin

This is a companion repository for [Andrew Weeks](https://github.com/andyrewwer) and [Javin Hung](https://github.com/jh-enfuseio) SpringOne Explore talk.

## Usage

For now the plugin is not available as a plugin so you will need to:

1. Copy the `src/main/groovy` into your project
2. Update your build.gradle with the following code:
```groovy 
plugins {
   id 'java-gradle-plugin'
}

def plugin =
new GroovyScriptEngine(file('src/main/groovy').absolutePath, this.class.classLoader)
.loadScriptByName('io/enfuse/plugin/ChatGPTPlugin.groovy')

apply plugin: plugin
```

3. Create a properties file (e.g. env.properties) and include your openAIAPIKey. You can find a template here [env.properties.template](./env.properties.template)
```properties
#required
openAIKey=sk-XXXXXXXXX

# optional
openAIOrganization=
temperature=0
model=text-davinci-003
```

4. Update configuration in your build.gradle file
```groovy
chatGPT {
    path = "env.properties"
    //	codeDirectory = "./src/main/java/" // optional, this is default
    //	testDirectory = "./src/test/java"  // optional, this is default thoughts
}

```

5. run `./gradlew test` and see the output:
```
 $ ./gradlew test

> Task :test
~~~~~~~~~~~~~~~~~~START~~~~~~~~~~~~~~
    GreetingControllerIntegrationTest > post_saves() FAILED 
java.lang.AssertionError: JSON path "$.content" expected:<This is my content> but was:<null>
    Found the source code file with path./src/main/java/io/enfuse/democrudapp/greeting/GreetingControllerIntegration.java
The error is indicating that the content value is null when it should be "This is my content". This could be due to an issue with the GreetingInfo constructor, which is currently set to accept a null value for the content parameter. To fix this, the constructor should be updated to require a non-null value for the content parameter. Additionally, the post mapping in the GreetingControllerIntegration class should be updated to ensure that the request body is not null before attempting to save it.
~~~~~~~~~~~~~~~~~~END~~~~~~~~~~~~~~~~~
~~~~~~~~~~~~~~~~~~START~~~~~~~~~~~~~~
    GreetingServiceTest > save_shouldSaveCandidate() FAILED 
org.opentest4j.AssertionFailedError: expected: <this is amazing content> but was: <null>
    Found the source code file with path./src/main/java/io/enfuse/democrudapp/greeting/GreetingService.java
The test code is expecting the response from the save() method to have a content value of "this is amazing content", however, the main code is not setting the content value of the GreetingRecord object. To fix this, the content value should be set in the main code before the GreetingRecord object is saved. This can be done by adding the following line of code to the save() method:
record.setContent(request.getContent());
~~~~~~~~~~~~~~~~~~END~~~~~~~~~~~~~~~~~

16 tests completed, 2 failed
```

## Contributing
If you find something wrong or suggestion, please free feel to submit a PR and assign to `@andyrewwer` or create an issue specifying branch and as many details as possible.  
