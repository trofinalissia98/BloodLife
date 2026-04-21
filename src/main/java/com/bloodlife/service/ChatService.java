package com.bloodlife.service;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ChatService {
    // Aici vei pune cheia ta API obținută de pe Google AI Studio
    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("GEMINI_API_KEY");

    // URL-ul tău va folosi acum variabila din .env
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    private final OkHttpClient client;

    public ChatService() {
        // Configurăm clientul cu un timeout mai mare (AI-ul poate dura câteva secunde)
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public String getAiResponse(String userMessage) throws IOException {
        // Construim contextul (System Prompt) - îi spunem AI-ului cine este
        String context = "Ești Bloodie, asistentul virtual al aplicației BloodLife. " +
                "Ești prietenos și ajuți donatorii cu informații despre donarea de sânge. " +
                "Răspunde scurt și profesional. Întrebare: ";

        // Structura JSON cerută de Gemini API
        JSONObject jsonBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject parts = new JSONObject();
        parts.put("text", context + userMessage);
        contents.put(new JSONObject().put("parts", new JSONArray().put(parts)));
        jsonBody.put("contents", contents);

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Eroare API: " + response);

            // Extragem textul din răspunsul complex al lui Gemini
            String responseData = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseData);
            return jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");
        }
    }
}