import java.net.URI;
import java.net.http.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class PlacesFetcher {

    // 環境変数 GOOGLE_PLACES_API_KEY からAPIキーを読み込む
    private static final String API_KEY = System.getenv("GOOGLE_PLACES_API_KEY");
    private static final String ENDPOINT = "https://places.googleapis.com/v1/places:searchNearby";

    // 検索結果を格納するクラス
    public static class Place {
        String name;
        double lat;
        double lng;
        String type; // "restaurant" or "gym"

        Place(String name, double lat, double lng, String type) {
            this.name = name;
            this.lat = lat;
            this.lng = lng;
            this.type = type;
        }

        @Override
        public String toString() {
            return name + " (" + type + ") [" + lat + ", " + lng + "]";
        }
    }

    // includedType: "restaurant" または "gym" を指定
    public List<Place> searchNearby(double lat, double lng, int radiusMeters, String includedType) throws Exception {

        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new IllegalStateException(
                    "環境変数 GOOGLE_PLACES_API_KEY が設定されていません。setx で設定後、ターミナルを開き直してください。");
        }

        JSONObject locationRestriction = new JSONObject()
                .put("circle", new JSONObject()
                        .put("center", new JSONObject()
                                .put("latitude", lat)
                                .put("longitude", lng))
                        .put("radius", radiusMeters));

        JSONObject requestBody = new JSONObject()
                .put("includedTypes", new JSONArray().put(includedType))
                .put("maxResultCount", 10)
                .put("locationRestriction", locationRestriction);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .header("Content-Type", "application/json")
                .header("X-Goog-Api-Key", API_KEY)

                .header("X-Goog-FieldMask", "places.displayName,places.location")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<Place> results = new ArrayList<>();

        if (response.statusCode() != 200) {
            System.err.println("APIエラー: " + response.statusCode());
            System.err.println(response.body());
            return results;
        }

        JSONObject json = new JSONObject(response.body());

        if (!json.has("places")) {
            return results; // 該当なしの場合
        }

        JSONArray places = json.getJSONArray("places");
        for (int i = 0; i < places.length(); i++) {
            JSONObject p = places.getJSONObject(i);
            String name = p.getJSONObject("displayName").getString("text");
            double placeLat = p.getJSONObject("location").getDouble("latitude");
            double placeLng = p.getJSONObject("location").getDouble("longitude");
            results.add(new Place(name, placeLat, placeLng, includedType));
        }

        return results;
    }

    // 動作確認用（単体で実行してテストできる）
    public static void main(String[] args) throws Exception {
        // 東京駅を中心に半径1000m以内の飲食店、ジムをAPIに問合せしrestaurantsに格納
        PlacesFetcher fetcher = new PlacesFetcher();

        List<Place> restaurants = fetcher.searchNearby(35.748, 139.806, 10000, "restaurant");
        System.out.println("=== 飲食店 ===");
        restaurants.forEach(System.out::println);

        List<Place> gyms = fetcher.searchNearby(35.748, 139.806, 1000, "gym");
        System.out.println("=== ジム ===");
        gyms.forEach(System.out::println);
    }
}