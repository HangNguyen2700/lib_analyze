package org.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenCentralJarCount {
    public static void main(String[] args) throws Exception {
        String url = "https://search.maven.org/solrsearch/select?q=p:jar&rows=0&wt=json";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "JarCount/1.0")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("HTTP error: " + response.statusCode());
            System.err.println(response.body());
            System.exit(1);
        }

        // Minimal parsing without external JSON libs
        Matcher m = Pattern.compile("\"numFound\"\\s*:\\s*(\\d+)").matcher(response.body());
        if (m.find()) {
            long numFound = Long.parseLong(m.group(1));
            System.out.println("Total artifacts with packaging=jar: " + numFound);
        } else {
            System.err.println("Couldn't find numFound in response.");
        }
    }
}
