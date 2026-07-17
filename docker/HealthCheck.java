import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class HealthCheck {
    private HealthCheck() {
    }

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:8080/api/health"))
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();
        HttpResponse<Void> response = client.send(
                request,
                HttpResponse.BodyHandlers.discarding());

        if (response.statusCode() != 200) {
            System.exit(1);
        }
    }
}
