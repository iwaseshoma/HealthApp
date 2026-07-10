public class CountryData {

    private String country;
    private String age;
    private String gender;
    private int calorie;

    public CountryData(String country, String age, String gender, int calorie) {
        this.country = country;
        this.age = age;
        this.gender = gender;
        this.calorie = calorie;
    }

    public String getCountry() {
        return country;
    }

    public String getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public int getCalorie() {
        return calorie;
    }
}