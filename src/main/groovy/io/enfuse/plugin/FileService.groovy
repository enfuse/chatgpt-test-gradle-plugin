package io.enfuse.plugin


import org.gradle.api.tasks.testing.TestDescriptor

class FileService {

    static MessageResponse getTestCode(TestDescriptor testDescriptor, String path) {
        String testClassName = getTestClassName(testDescriptor)
        List<FoundFile> files = getAllFilePaths(path)
        FoundFile testFile = findClosestMatch(files, testClassName)

        if (testFile == null) {
            return new MessageResponse(false, "Failed to find test code class matching [" + testClassName + "] at path [" + path + "].")
        }
        File foundFile = new File(testFile.getPath())
        foundFile.newReader()
        try (BufferedReader reader = foundFile.newReader()) {
            return getMethodCode(reader, testDescriptor.getDisplayName())
        } catch (Exception e) {
            return new MessageResponse(false, "Something went wrong, try again later. \n" + e.toString())
        }
    }

    static MessageResponse getClassCode(TestDescriptor testDescriptor, String path) {
        String testClassName = getTestClassName(testDescriptor)
        List<FoundFile> files = getAllFilePaths(path)
        FoundFile sourceFile = findClosestMatch(files, testClassName)

        if (sourceFile == null) {
            return new MessageResponse(false, "Failed to find source code class matching [" + testClassName + "] at path [" + path + "].")
        }
        File foundFile = new File(sourceFile.getPath())

        foundFile.newReader()
        try (BufferedReader reader = foundFile.newReader()) {
            return new MessageResponse(true, getFileCode(reader))
        } catch (Exception e) {
            println(e)
            return new MessageResponse(false, "Something went wrong reading the file.")
        }
    }

    static String getTestClassName(TestDescriptor testDescriptor) {
        return testDescriptor.getClassName().substring(testDescriptor.getClassName().lastIndexOf('.') + 1)
    }

    private static MessageResponse getMethodCode(BufferedReader reader, String targetMethod) throws IOException {
        StringBuilder content = new StringBuilder()
        String line

        int balancedBrace = 0

        while ((line = reader.readLine()) != null) {
            if (line.contains(targetMethod)) {
                balancedBrace++
                content.append(line)
                content.append(System.lineSeparator())
                break
            }
        }
        if (balancedBrace == 0) {
            return new MessageResponse(false, String.format("Test with name [%s] not found.", targetMethod))
        }

        while (balancedBrace > 0 && (line = reader.readLine()) != null) {
            if (line.contains("{")) {
                balancedBrace++
            }
            if (line.contains("}")) {
                balancedBrace--
            }
            content.append(line)
            content.append(System.lineSeparator())
        }

        return new MessageResponse(true, content.toString())
    }

    private static String getFileCode(BufferedReader reader) throws IOException {
        StringBuilder content = new StringBuilder()
        String line

        while ((line = reader.readLine()) != null) {
            content.append(line)
            content.append(System.lineSeparator())
        }

        return content.toString()
    }

    static List<FoundFile> getAllFilePaths(String directoryPath) {
        List<FoundFile> filePaths = new ArrayList<>()
        File directory = new File(directoryPath)

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles()
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        filePaths.add(new FoundFile(String.format("%s/%s", directory.getPath(), file.getName()), file.getName()))
                    } else if (file.isDirectory()) {
                        List<FoundFile> subDirectoryFilePaths = getAllFilePaths(String.format("%s/%s", directory.getPath(), file.getName()))
                        filePaths.addAll(subDirectoryFilePaths)
                    }
                }
            }
        }
        return filePaths
    }

    static FoundFile findClosestMatch(List<FoundFile> files, String testClassName) {
        FoundFile closestMatch = null
        int shortestDistance = Integer.MAX_VALUE

        for (FoundFile file: files) {
            String className = file.getName()
            int distance = calculateLevenshteinDistance(className, testClassName)
            if (distance < shortestDistance) {
                shortestDistance = distance
                closestMatch = file
            }
        }

        return closestMatch
    }

    //thanks ChatGPT. Hope this works.
    private static int calculateLevenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            throw new IllegalArgumentException("Strings must not be null")
        }

        int[][] distance = new int[s1.length() + 1][s2.length() + 1]

        for (int i = 0; i <= s1.length(); i++) {
            distance[i][0] = i
        }

        for (int j = 0; j <= s2.length(); j++) {
            distance[0][j] = j
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1
                distance[i][j] = Math.min(Math.min(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1),
                        distance[i - 1][j - 1] + cost)
            }
        }

        return distance[s1.length()][s2.length()]
    }

    static class FoundFile {

        private String path
        private String name

        FoundFile(String path, String name) {
            this.path = path
            this.name = name
        }

        String getPath() {
            return path
        }

        String getName() {
            return name
        }
    }
}

