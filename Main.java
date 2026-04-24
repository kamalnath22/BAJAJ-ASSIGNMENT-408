import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class Main {

    public static void main(String[] args) throws Exception {

        
        String regNo = "Ra2311003020408";

        String BASE = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";

        HttpClient client = HttpClient.newHttpClient();

        
        Set<String> seen = new HashSet<>();

        // total score per participant
        Map<String, Integer> scoreMap = new HashMap<>();

        for (int poll = 0; poll <= 9; poll++) {

            String url = BASE + "/quiz/messages?regNo=" + regNo + "&poll=" + poll;

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> res =
                    client.send(req, HttpResponse.BodyHandlers.ofString());

            String raw = res.body();

            // handle server errors gracefully
            if (!raw.trim().startsWith("{")) {
                System.out.println("Poll " + poll + " skipped (server issue)");
                if (poll < 9) Thread.sleep(5000);
                continue;
            }

            JSONObject obj = new JSONObject(raw);
            JSONArray events = obj.getJSONArray("events");

            int newEvents = 0;
            int dupEvents = 0;

            for (int j = 0; j < events.length(); j++) {

                JSONObject e = events.getJSONObject(j);

                //  CORRECT DEDUP: roundId + participant (as per assignment spec)
                String roundId = e.getString("roundId");
                String participant = e.getString("participant");
                String key = roundId + "|" + participant;

                if (seen.add(key)) {
                    int score = e.getInt("score");
                    scoreMap.merge(participant, score, Integer::sum);
                    newEvents++;
                } else {
                    dupEvents++;
                }
            }

            System.out.printf("Poll %d completed — %d new, %d duplicates%n",
                    poll, newEvents, dupEvents);

            if (poll < 9) Thread.sleep(5000);
        }

        // print results
        System.out.println("\n--- Final Scores ---");

        int total = 0;
        for (Map.Entry<String, Integer> entry : scoreMap.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
            total += entry.getValue();
        }

        System.out.println("Total Score = " + total);
        System.out.println("Unique events = " + seen.size());

        // sort leaderboard
        List<Map.Entry<String, Integer>> list =
                new ArrayList<>(scoreMap.entrySet());

        list.sort((a, b) -> b.getValue() - a.getValue());

        JSONArray leaderboard = new JSONArray();

        for (Map.Entry<String, Integer> entry : list) {
            JSONObject p = new JSONObject();
            p.put("participant", entry.getKey());
            p.put("totalScore", entry.getValue());
            leaderboard.put(p);
        }

        // prepare payload
        JSONObject payload = new JSONObject();
        payload.put("regNo", regNo);
        payload.put("leaderboard", leaderboard);

        // submit
        HttpRequest postReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/quiz/submit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> postRes =
                client.send(postReq, HttpResponse.BodyHandlers.ofString());

        System.out.println("\n--- Server Response ---");
        System.out.println(postRes.body());
    }
}
