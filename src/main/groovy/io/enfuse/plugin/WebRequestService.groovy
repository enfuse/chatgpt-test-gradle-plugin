package io.enfuse.plugin

class WebRequestService {

    static URLConnection preparePostRequest(String url, String body, Map<String, String> headers) {
        URLConnection postRequest = new URL(url).openConnection()

        postRequest.setRequestMethod("POST")
        postRequest.setDoOutput(true)

        for (String header: headers.keySet()) {
            postRequest.setRequestProperty(header, headers.get(header))
        }

        postRequest.getOutputStream().write(body.getBytes("UTF-8"))

        return postRequest
    }
}
