package be.sv3r.creatorbingo.util;

import be.sv3r.creatorbingo.CreatorBingo;
import be.sv3r.creatorbingo.transcripts.Server;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.leangen.geantyref.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;

public class RestUtils {
    public static Server sendServerPostRequest() throws IOException, InterruptedException {
        Gson gson = new Gson();

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/%s", CreatorBingo.getApiUrl(), "servers")))
                .header("Api-Key", CreatorBingo.getApiKey())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

        try {
            return gson.fromJson(postResponse.body(), Server.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public static List<Server> sendServerGetRequest() throws IOException, InterruptedException {
        Type collectionType = new TypeToken<Collection<Server>>() {
        }.getType();
        Gson gson = new Gson();

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/%s", CreatorBingo.getApiUrl(), "servers")))
                .header("Api-Key", CreatorBingo.getApiKey())
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

        return gson.fromJson(getResponse.body(), collectionType);
    }
}