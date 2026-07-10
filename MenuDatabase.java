import java.util.*;

// 店の種類ごとの「メニュー候補とカロリー」を提供するクラス
// 本来は店ごとに異なるメニューをAPIやDBから取得するのが理想だが、
// Places APIは実際のメニュー情報までは提供していないため、
// ここでは一般的な選択肢をカテゴリごとに用意している
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

    // 飲食店で選べる一般的なメニュー例
    public static List<MenuItem> getRestaurantMenu() {
        List<MenuItem> list = new ArrayList<>();
        list.add(new MenuItem("小エビのサラダ", 198));
        list.add(new MenuItem("チキンのサラダ", 237));
        list.add(new MenuItem("ガーリックフォッカチオ", 297));
        list.add(new MenuItem("ライス", 303));
        list.add(new MenuItem("ムール貝のガーリック焼き", 339));
        list.add(new MenuItem("ソーセージピザ", 643));
        list.add(new MenuItem("焼チーズ ミラノ風ドリア", 666));
        list.add(new MenuItem("その他(自由入力)", -1)); // -1は自由入力を意味する特殊値
        return list;
    }

    // ジムで選べる一般的なトレーニング例（有酸素は時間・距離から別途計算）
    public static List<String> getGymMenu() {
        List<String> list = new ArrayList<>();
        list.add("ランニング（有酸素）");
        list.add("ウォーキング（有酸素）");
        list.add("筋力トレーニング（無酸素）");
        list.add("ヨガ・ストレッチ");
        list.add("その他(自由入力)");
        return list;
    }
}