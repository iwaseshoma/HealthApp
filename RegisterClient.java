import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.net.Socket;

public class RegisterClient {

    public static void main(String[] args) {

        // ウィンドウ作成
        JFrame frame = new JFrame("初回登録");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // フォント設定
        Font labelFont = new Font("メイリオ", Font.PLAIN, 30);
        Font fieldFont = new Font("メイリオ", Font.PLAIN, 28);

        // 中央に配置するパネル
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // ニックネーム
        JLabel nameLabel = new JLabel("ニックネーム");
        nameLabel.setFont(labelFont);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField nameField = new JTextField();
        nameField.setFont(fieldFont);
        nameField.setMaximumSize(new Dimension(500, 50));

        // 身長
        JLabel heightLabel = new JLabel("身長(cm)");
        heightLabel.setFont(labelFont);
        heightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField heightField = new JTextField();
        heightField.setFont(fieldFont);
        heightField.setMaximumSize(new Dimension(500, 50));

        // 体重
        JLabel weightLabel = new JLabel("体重(kg)");
        weightLabel.setFont(labelFont);
        weightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField weightField = new JTextField();
        weightField.setFont(fieldFont);
        weightField.setMaximumSize(new Dimension(500, 50));

        // 登録ボタン
        JButton registerButton = new JButton("登録");
        registerButton.setFont(labelFont);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // コンポーネント追加
        centerPanel.add(nameLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(nameField);

        centerPanel.add(Box.createVerticalStrut(30));

        centerPanel.add(heightLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(heightField);

        centerPanel.add(Box.createVerticalStrut(30));

        centerPanel.add(weightLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(weightField);

        centerPanel.add(Box.createVerticalStrut(50));

        centerPanel.add(registerButton);

        // 画面中央に配置
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.add(centerPanel);

        frame.add(wrapper);

        // 登録ボタン押下時
        registerButton.addActionListener(e -> {

            String nickname = nameField.getText();
            String height = heightField.getText();
            String weight = weightField.getText();

            // 未入力チェック
            if (nickname.isEmpty() ||
                height.isEmpty() ||
                weight.isEmpty()) {

                JOptionPane.showMessageDialog(
                        frame,
                        "すべて入力してください。"
                );
                return;
            }

            try {
                // TCP接続
                Socket socket = new Socket("localhost", 5000);

                PrintWriter writer =
                        new PrintWriter(
                                socket.getOutputStream(),
                                true);

                // サーバへ送信
                writer.println(nickname);
                writer.println(height);
                writer.println(weight);

                writer.close();
                socket.close();

                JOptionPane.showMessageDialog(
                        frame,
                        "登録が完了しました。"
                );

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "サーバに接続できません。"
                );

                ex.printStackTrace();
            }
        });

        frame.setVisible(true);
    }
}
