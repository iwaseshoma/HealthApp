public class CountryData {

    private String country;
    private String age;
    private String gender;
    private double calorie;

    public CountryData(String country, String age, String gender, double calorie) {
        this.country = country;
        this.age = age;
        this.gender = gender;
        this.calorie = calorie;
    }

    public double getCalorie() {
        return calorie;
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

}