import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class CountryJudge {

    public static void judge(double averageCalorie) {

        ArrayList<CountryData> list = new ArrayList<>();

        try {

            BufferedReader br = new BufferedReader(new FileReader("country.csv"));

            // 見出しを読み飛ばす
            br.readLine();

            String line;

            while ((line = br.readLine()) != null) {

                String[] data = line.split(",");

                String country = data[0];
                String age = data[1];
                String gender = data[2];
                int calorie = Integer.parseInt(data[3]);

                list.add(new CountryData(country, age, gender, calorie));
            }

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
            return;
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

        System.out.println("1日平均摂取カロリー");
        System.out.printf("%.0f kcal%n", averageCalorie);

        System.out.println();

        System.out.println("あなたに最も近いのは");

        System.out.println("国：" + nearest.getCountry());
        System.out.println("年代：" + nearest.getAge());
        System.out.println("性別：" + nearest.getGender());
        System.out.println("平均との差：" + (int)min + " kcal");
    }
}