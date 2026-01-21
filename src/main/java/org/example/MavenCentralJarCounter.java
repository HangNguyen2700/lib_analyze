package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class MavenCentralJarCounter {

    private static final String MAVEN_SEARCH_API = "https://search.maven.org/solrsearch/select";

    public static void main(String[] args) {
        try {
            System.out.println("Counting Maven Central artifacts...\n");

            // Methode 1: Nur explizit als jar markierte Artifacts
            int explicitJarCount = countExplicitJarArtifacts();
            System.out.println("Artifacts with explicit p:jar: " + explicitJarCount);

            // Methode 2: Alle Artifacts (JAR ist default, also fast alle)
            int totalArtifacts = countAllArtifacts();
            System.out.println("Total artifacts in Maven Central: " + totalArtifacts);

            // Methode 3: Nicht-JAR Artifacts ausschließen
            int nonJarCount = countNonJarArtifacts();
            System.out.println("Non-JAR artifacts (pom, war, ear, etc.): " + nonJarCount);

            int estimatedJars = totalArtifacts - nonJarCount;
            System.out.println("\nEstimated JAR artifacts (total - non-jar): " + estimatedJars);

        } catch (Exception e) {
            System.err.println("Error querying Maven Central: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int countExplicitJarArtifacts() throws Exception {
        // Nur Artifacts die explizit p:jar haben
        String query = "q=p:jar&rows=0&wt=json";
        return executeCountQuery(query);
    }

    private static int countAllArtifacts() throws Exception {
        // Alle Artifacts in Maven Central
        String query = "q=*:*&rows=0&wt=json";
        return executeCountQuery(query);
    }

    private static int countNonJarArtifacts() throws Exception {
        // Zähle alle nicht-JAR packaging types
        // Häufige Typen: pom, war, ear, maven-plugin, aar, bundle, etc.
        String query = "q=(p:pom OR p:war OR p:ear OR p:maven-plugin OR p:aar OR p:bundle OR p:ejb OR p:rar OR p:par)&rows=0&wt=json";
        return executeCountQuery(query);
    }

    private static int executeCountQuery(String query) throws Exception {
        String urlString = MAVEN_SEARCH_API + "?" + query;

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("HTTP GET Request Failed with Error code: " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        conn.disconnect();

        // Parse JSON response
        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONObject responseObj = jsonResponse.getJSONObject("response");
        int numFound = responseObj.getInt("numFound");

        return numFound;
    }
}