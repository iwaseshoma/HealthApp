import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class RegisterServer {

    public static void main(String[] args) {

        try {
            ServerSocket server = new ServerSocket(5000);

            System.out.println("サーバ起動中...");

            while (true) {

                Socket socket = server.accept();

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        socket.getInputStream()));

                String nickname = reader.readLine();
                String height = reader.readLine();
                String weight = reader.readLine();

                System.out.println("===== 初回登録 =====");
                System.out.println("ニックネーム : " + nickname);
                System.out.println("身長 : " + height + " cm");
                System.out.println("体重 : " + weight + " kg");

                reader.close();
                socket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
