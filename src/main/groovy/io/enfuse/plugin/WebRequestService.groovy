package io.enfuse.plugin

class WebRequestService {

    static HttpURLConnection preparePostRequest(String url, String body, Map<String, String> headers) {
        HttpURLConnection postRequest = (HttpURLConnection)new URL(url).openConnection()

        postRequest.setRequestMethod("POST")
        postRequest.setDoOutput(true)

        for (String header: headers.keySet()) {
            postRequest.setRequestProperty(header, headers.get(header))
        }

        postRequest.getOutputStream().write(body.getBytes("UTF-8"))

        return postRequest
    }
}
