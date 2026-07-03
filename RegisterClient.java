import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.border.LineBorder;

public class RegisterClient {

    public static void main(String[] args) {

        // 色設定
        Color backgroundColor = new Color(220, 255, 220); // 外側の背景（薄い黄緑）
        Color cardColor = new Color(245, 255, 245); // 枠の中（さらに薄い黄緑）
        Color buttonColor = new Color(34, 139, 34); // 濃い緑

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
        Font appTitleFont = new Font("HGS創英角ポップ体", Font.BOLD, 50);
        Font titleFont = new Font("HGS創英角ポップ体", Font.BOLD, 70);
        Font labelFont = new Font("HGS創英角ポップ体", Font.PLAIN, 30);
        Font fieldFont = new Font("HGS創英角ポップ体", Font.PLAIN, 28);

        // アプリ名
        JLabel appTitleLabel = new JLabel("健康管理アプリ");
        appTitleLabel.setFont(appTitleFont);
        appTitleLabel.setForeground(buttonColor);
        appTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        appTitleLabel.setBackground(backgroundColor);
        appTitleLabel.setOpaque(true);
        appTitleLabel.setBorder(
                BorderFactory.createEmptyBorder(80, 0, 20, 0) // 「健康管理アプリ」の文字の高さ調整
        );

        // 中央に配置するパネル
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(cardColor);

        // タイトル
        JLabel titleLabel = new JLabel("初回登録");
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(buttonColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBackground(cardColor);
        titleLabel.setOpaque(true);

        // ニックネーム
        JLabel nameLabel = new JLabel("ニックネーム");
        nameLabel.setFont(labelFont);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setBackground(cardColor);
        nameLabel.setOpaque(true);

        JTextField nameField = new JTextField();
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(180, 220, 180),
                        2,
                        true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        nameField.setFont(fieldFont);
        nameField.setMaximumSize(new Dimension(500, 50));
        nameField.setBackground(Color.WHITE);

        // 身長
        JLabel heightLabel = new JLabel("身長(cm)");
        heightLabel.setFont(labelFont);
        heightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        heightLabel.setBackground(cardColor);
        heightLabel.setOpaque(true);

        JTextField heightField = new JTextField();
        heightField.setFont(fieldFont);
        heightField.setMaximumSize(new Dimension(500, 50));
        heightField.setBackground(Color.WHITE);

        heightField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(180, 220, 180),
                        2,
                        true),
                BorderFactory.createEmptyBorder(
                        10, 15, 10, 15)));

        // 体重
        JLabel weightLabel = new JLabel("体重(kg)");
        weightLabel.setFont(labelFont);
        weightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        weightLabel.setBackground(cardColor);
        weightLabel.setOpaque(true);

        JTextField weightField = new JTextField();
        weightField.setFont(fieldFont);
        weightField.setMaximumSize(new Dimension(500, 50));
        weightField.setBackground(Color.WHITE);

        weightField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(180, 220, 180),
                        2,
                        true),
                BorderFactory.createEmptyBorder(
                        10, 15, 10, 15)));

        // 登録ボタン
        JButton registerButton = new JButton("登録") {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // 影
                g2.setColor(new Color(0, 0, 0, 30));

                g2.fillRoundRect(
                        10,
                        10,
                        getWidth() - 1,
                        getHeight() - 1,
                        40,
                        40);
                g2.setColor(getBackground());

                g2.fillRoundRect(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        40,
                        40);

                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(getBackground());

                g2.drawRoundRect(
                        0,
                        0,
                        getWidth() - 1,
                        getHeight() - 1,
                        40,
                        40);

                g2.dispose();
            }
        };
        registerButton.setFont(labelFont);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setBackground(buttonColor);
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setMaximumSize(new Dimension(250, 60));

        // コンポーネント追加
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(40));

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

        // 枠を追加
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(cardColor);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(buttonColor, 4, true),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)));
        cardPanel.add(centerPanel, BorderLayout.CENTER);

        // フォームを少し下げるためのパネル
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(backgroundColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(00, 0, 0, 0); // 枠の高さ調整

        wrapper.add(cardPanel, gbc);

        // メインパネル
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(backgroundColor);

        mainPanel.add(appTitleLabel, BorderLayout.NORTH);
        mainPanel.add(wrapper, BorderLayout.CENTER);

        frame.add(mainPanel);

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
                        "すべて入力してください。");
                return;
            }

            try {
                // TCP接続
                Socket socket = new Socket("localhost", 5000);

                PrintWriter writer = new PrintWriter(
                        socket.getOutputStream(),
                        true);

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
                        "登録が完了しました。");

                String[] options = {
                        "近くのジムを探す",
                        "近くの飲食チェーン店を探す"
                };

                int result = JOptionPane.showOptionDialog(
                        frame,
                        "表示する場所を選択してください。",
                        "周辺検索",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[0]);

                /*
                 * if (result == 0) {
                 * PlacesFetcher.main(new String[]{"gym"});
                 * } else if (result == 1) {
                 * PlacesFetcher.main(new String[]{"restaurant"});
                 * }
                 */

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "サーバに接続できません。");

                ex.printStackTrace();
            }
        });

        frame.setVisible(true);
    }
}