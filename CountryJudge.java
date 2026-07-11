import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class CountryJudge {

    public static String judge(int[] weekCalories) {


        // 1週間の合計
        int total = 0;

        for (int calorie : weekCalories) {
            total += calorie;
        }

        // 1日平均
        double averageCalorie = total / 7.0;

        ArrayList<CountryData> list = new ArrayList<>();

        try {

            BufferedReader br = new BufferedReader(new FileReader("country.csv"));

            // 見出しを読み飛ばす
            br.readLine();

            String line;

            while ((line = br.readLine()) != null) {

                String[] data = line.split(",");

                list.add(new CountryData(
        data[0],
        data[1],
        data[2],
        Double.parseDouble(data[3])));
            }

            br.close();

        } catch (Exception e) {
    e.printStackTrace();
    return "country.csv を読み込めませんでした。";
}

        CountryData nearest = null;
        double min = Double.MAX_VALUE;

        for (CountryData c : list) {

            double diff = Math.abs(averageCalorie - c.getCalorie());

            if (diff < min) {
                min = diff;
                nearest = c;
            }
        }

        if (nearest == null) {
            return "比較できるデータがありません。";
        }

        return "===== 判定結果 =====\n"
                + "1週間の合計：" + total + " kcal\n"
                + String.format("1日平均：%.1f kcal\n\n", averageCalorie)
                + "あなたに最も近い食生活\n"
                + "国：" + nearest.getCountry() + "\n"
                + "年代：" + nearest.getAge() + "\n"
                + "性別：" + nearest.getGender() + "\n"
                + String.format("平均との差：%.1f kcal", min);
    }
}