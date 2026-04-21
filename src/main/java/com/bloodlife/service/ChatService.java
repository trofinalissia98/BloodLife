package com.bloodlife.service;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
public class ChatService {
    private static final Dotenv dotenv = Dotenv.load();
    private final OkHttpClient client;

    public ChatService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public String getAiResponse(String userMessage) throws IOException {
        // Luăm cheia chiar în momentul apelului, nu la începutul programului
        String apiKey = dotenv.get("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("Cheia API lipsește din fișierul .env!");
        }

        // URL-ul stabil pentru Europa/România (v1beta uneori dă 404 pe modele pro)
        // Încearcă varianta asta cu "gemini-1.5-flash" (e cel mai disponibil acum)
        // Am schimbat gemini-1.5-flash cu gemini-pro
        // Trecem pe v1 (stabil) și modelul gemini-pro (fără numere de versiune în nume)
        // Folosim modelul pe care browserul tocmai ni l-a confirmat ca fiind disponibil
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        String context = "Ești Bloodie, asistentul virtual al aplicației BloodLife. " +
                "Ești prietenos și ajuți donatorii cu informații despre donarea de sânge, sau alte informatii. " +
                "Răspunde prietenos. Întrebare: ";

        JSONObject jsonBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject contentObj = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject textPart = new JSONObject();

        textPart.put("text", context + userMessage);
        parts.put(textPart);
        contentObj.put("parts", parts);
        contents.put(contentObj);
        jsonBody.put("contents", contents);

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url) // Folosim URL-ul construit local aici
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // IMPORTANT: Printează în consolă ce zice Google (mesajul de eroare JSON)
                String errorBody = response.body() != null ? response.body().string() : "no error body";
                System.err.println("Detalii eroare Google: " + errorBody);
                throw new IOException("Eroare API: " + response.code());
            }

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
