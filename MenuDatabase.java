import java.util.*;

// 店の種類ごとの「メニュー候補とカロリー」を提供するクラス
// 本来は店ごとに異なるメニューをAPIやDBから取得するのが理想だが、
// Places APIは実際のメニュー情報までは提供していないため、
// ここではチェーン名を判定して、対応する固定メニューを返す形にしている
public class MenuDatabase {

    public static class MenuItem {
        public final String name;
        public final int calorie; // kcal

        public MenuItem(String name, int calorie) {
            this.name = name;
            this.calorie = calorie;
        }

        @Override
        public String toString() {
            return name + "（" + calorie + " kcal）";
        }
    }

    // ===== チェーン店ごとのメニュー辞書 =====
    // key: 店名に含まれるかどうかで判定する文字列(チェーン名)
    // value: そのチェーンのメニュー一覧
    private static final Map<String, List<MenuItem>> CHAIN_MENU_MAP = new LinkedHashMap<>();

    static {
        // サイゼリヤのメニュー(北千住店で確認したものを登録)
        List<MenuItem> saizeriyaMenu = new ArrayList<>();
        saizeriyaMenu.add(new MenuItem("小エビのサラダ", 198));
        saizeriyaMenu.add(new MenuItem("チキンのサラダ", 237));
        saizeriyaMenu.add(new MenuItem("ガーリックフォッカチオ", 297));
        saizeriyaMenu.add(new MenuItem("ライス", 303));
        saizeriyaMenu.add(new MenuItem("ムール貝のガーリック焼き", 339));
        saizeriyaMenu.add(new MenuItem("ソーセージピザ", 643));
        saizeriyaMenu.add(new MenuItem("焼チーズ ミラノ風ドリア", 666));
        CHAIN_MENU_MAP.put("Saizeriya", saizeriyaMenu);

        // 今後、他チェーンのメニューもここに追加していく
        // 例:
        // List<MenuItem> sukiyaMenu = new ArrayList<>();
        // sukiyaMenu.add(new MenuItem("牛丼(並)", 733));
        // CHAIN_MENU_MAP.put("すき家", sukiyaMenu);
    }

    // チェーン未登録の店で使う、汎用メニュー(自由入力のみ)
    private static List<MenuItem> getGenericMenu() {
        List<MenuItem> list = new ArrayList<>();
        list.add(new MenuItem("その他(自由入力)", -1)); // -1は自由入力を意味する特殊値
        return list;
    }

    // 店名を受け取り、チェーン名が含まれていれば専用メニューを、
    // 含まれていなければ汎用メニューを返す
    public static List<MenuItem> getRestaurantMenu(String placeName) {
        if (placeName != null) {
            for (Map.Entry<String, List<MenuItem>> entry : CHAIN_MENU_MAP.entrySet()) {
                if (placeName.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return getGenericMenu();
    }

    // 互換用: 引数なし版(既存コードが呼んでいる場合のフォールバック)
    // 新規実装では上のgetRestaurantMenu(String)を使うこと
    public static List<MenuItem> getRestaurantMenu() {
        return getGenericMenu();
    }

    // ジムのトレーニング項目。種類ごとに入力させる項目が変わるため、
    // 種類(Type)を持たせている
    public static class GymActivity {
        public enum Type {
            AEROBIC,   // 有酸素運動(時間・速度を入力させる)
            STRENGTH,  // 筋力トレーニング(重量・セット数・回数を入力させる)
            STRETCH,   // ストレッチ・ヨガ(時間のみ入力させる)
            FREE       // 自由入力
        }

        public final String name;
        public final Type type;

        public GymActivity(String name, Type type) {
            this.name = name;
            this.type = type;
        }
    }

    // ジムで選べる一般的なトレーニング例
    public static List<GymActivity> getGymMenu() {
        List<GymActivity> list = new ArrayList<>();
        list.add(new GymActivity("ランニング", GymActivity.Type.AEROBIC));
        list.add(new GymActivity("ウォーキング", GymActivity.Type.AEROBIC));
        list.add(new GymActivity("筋力トレーニング", GymActivity.Type.STRENGTH));
        list.add(new GymActivity("ヨガ・ストレッチ", GymActivity.Type.STRETCH));
        list.add(new GymActivity("その他(自由入力)", GymActivity.Type.FREE));
        return list;
    }
}