import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.net.Socket;

public class RegisterClient {

    public static void main(String[] args) {

        // 色設定
        Color backgroundColor = new Color(220, 255, 220); // 薄い黄緑
        Color buttonColor = new Color(34, 139, 34);       // 濃い緑

        // ウィンドウ作成
        JFrame frame = new JFrame("初回登録");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 画面サイズ取得
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // 横幅を画面の半分、高さは全体
        frame.setSize(screenSize.width / 2, screenSize.height);

        // 画面中央に配置
        frame.setLocationRelativeTo(null);

        frame.getContentPane().setBackground(backgroundColor);

        // フォント設定
        Font labelFont = new Font("メイリオ", Font.PLAIN, 30);
        Font fieldFont = new Font("メイリオ", Font.PLAIN, 28);

        // 中央に配置するパネル
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(backgroundColor);

        // ニックネーム
        JLabel nameLabel = new JLabel("ニックネーム");
        nameLabel.setFont(labelFont);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setBackground(backgroundColor);
        nameLabel.setOpaque(true);

        JTextField nameField = new JTextField();
        nameField.setFont(fieldFont);
        nameField.setMaximumSize(new Dimension(500, 50));
        nameField.setBackground(Color.WHITE);

        // 身長
        JLabel heightLabel = new JLabel("身長(cm)");
        heightLabel.setFont(labelFont);
        heightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        heightLabel.setBackground(backgroundColor);
        heightLabel.setOpaque(true);

        JTextField heightField = new JTextField();
        heightField.setFont(fieldFont);
        heightField.setMaximumSize(new Dimension(500, 50));
        heightField.setBackground(Color.WHITE);

        // 体重
        JLabel weightLabel = new JLabel("体重(kg)");
        weightLabel.setFont(labelFont);
        weightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        weightLabel.setBackground(backgroundColor);
        weightLabel.setOpaque(true);

        JTextField weightField = new JTextField();
        weightField.setFont(fieldFont);
        weightField.setMaximumSize(new Dimension(500, 50));
        weightField.setBackground(Color.WHITE);

        // 登録ボタン
        JButton registerButton = new JButton("登録");
        registerButton.setFont(labelFont);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setBackground(buttonColor);
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setMaximumSize(new Dimension(250, 60));

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
        wrapper.setBackground(backgroundColor);
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

                PrintWriter writer = new PrintWriter(
                        socket.getOutputStream(),
                        true
                );

                // サーバへ送信
                writer.println(nickname);
                writer.println(height);
                writer.println(weight);

                writer.close();
                socket.close();

                // 入力欄を空にする
                nameField.setText("");
                heightField.setText("");
                weightField.setText("");

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