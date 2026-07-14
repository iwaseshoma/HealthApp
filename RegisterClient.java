import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.border.LineBorder;

public class RegisterClient {

        public static void main(String[] args) {

                // 色設定
                Color backgroundColor = new Color(220, 255, 220); // 外側の背景
                Color cardColor = new Color(245, 255, 245); // 枠の中
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
                Font appTitleFont = new Font("HGS創英角ポップ体", Font.BOLD, 60);
                Font titleFont = new Font("HGS創英角ポップ体", Font.BOLD, 50);
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

                JPanel formPanel = new JPanel(new GridBagLayout());
                formPanel.setBackground(cardColor);

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(15, 10, 15, 10);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.anchor = GridBagConstraints.WEST;

                // 共通サイズ
                Dimension fieldSize = new Dimension(500, 45);

                // --- ニックネーム ---
                JLabel nameLabel = new JLabel("ニックネーム");
                nameLabel.setFont(labelFont);

                RoundedTextField nameField = new RoundedTextField(20);
                nameField.setFont(fieldFont);
                nameField.setPreferredSize(fieldSize);

                gbc.gridx = 0;
                gbc.gridy = 0;
                formPanel.add(nameLabel, gbc);

                gbc.gridx = 1;
                formPanel.add(nameField, gbc);

                // --- 身長 ---
                JLabel heightLabel = new JLabel("身長(cm)");
                heightLabel.setFont(labelFont);

                RoundedTextField heightField = new RoundedTextField(20);
                heightField.setFont(fieldFont);
                heightField.setPreferredSize(fieldSize);

                gbc.gridx = 0;
                gbc.gridy = 1;
                formPanel.add(heightLabel, gbc);

                gbc.gridx = 1;
                formPanel.add(heightField, gbc);

                // --- 体重 ---
                JLabel weightLabel = new JLabel("体重(kg)");
                weightLabel.setFont(labelFont);

                RoundedTextField weightField = new RoundedTextField(20);
                weightField.setFont(fieldFont);
                weightField.setPreferredSize(fieldSize);

                gbc.gridx = 0;
                gbc.gridy = 2;
                formPanel.add(weightLabel, gbc);

                gbc.gridx = 1;
                formPanel.add(weightField, gbc);

                JPanel centerPanel = new JPanel();
                centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
                centerPanel.setBackground(cardColor);

                // 登録ボタン
                JButton registerButton = new JButton("登録") {

                        @Override
                        protected void paintComponent(Graphics g) {

                                Graphics2D g2 = (Graphics2D) g.create();

                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                                RenderingHints.VALUE_ANTIALIAS_ON);

                                int arc = 40;

                                // 影
                                g2.setColor(new Color(0, 0, 0, 30));
                                g2.fillRoundRect(4, 4, getWidth() - 4, getHeight() - 4, arc, arc);

                                // 本体
                                g2.setColor(getBackground());
                                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, arc, arc);

                                g2.dispose();

                                super.paintComponent(g);
                        }

                        @Override
                        protected void paintBorder(Graphics g) {
                                Graphics2D g2 = (Graphics2D) g.create();

                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                                RenderingHints.VALUE_ANTIALIAS_ON);

                                g2.setColor(getBackground());

                                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 40, 40);

                                g2.dispose();
                        }
                };
                registerButton.setFont(labelFont);
                registerButton.setBackground(buttonColor);
                registerButton.setForeground(Color.WHITE);
                registerButton.setFocusPainted(false);
                registerButton.setContentAreaFilled(false);
                registerButton.setOpaque(false);
                registerButton.setMaximumSize(new Dimension(250, 60));

                // コンポーネント追加
                JLabel titleLabel = new JLabel("");

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

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttonPanel.setBackground(cardColor);
                buttonPanel.add(registerButton);

                centerPanel.add(buttonPanel);
                centerPanel.add(titleLabel);
                centerPanel.add(Box.createVerticalStrut(40));
                centerPanel.add(formPanel);
                centerPanel.add(Box.createVerticalStrut(50));

                // 枠を追加
                RoundedPanel cardPanel = new RoundedPanel();
                cardPanel.setLayout(new BorderLayout());
                cardPanel.setOpaque(false);
                cardPanel.setBackground(cardColor);
                cardPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
                cardPanel.add(centerPanel, BorderLayout.CENTER);

                // フォームを少し下げるためのパネル
                JPanel wrapper = new JPanel(new GridBagLayout());
                wrapper.setBackground(backgroundColor);
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
                                Socket socket = new Socket("192.168.0.202", 5000);

                                PrintWriter writer = new PrintWriter(
                                                socket.getOutputStream(),
                                                true);

                                // サーバへ送信
                                writer.println(nickname);
                                writer.println(height);
                                writer.println(weight);
                                MapClient.height = Double.parseDouble(height);
                                MapClient.weight = Double.parseDouble(weight);

                                System.out.println("RegisterClient");
                                System.out.println(MapClient.height);
                                System.out.println(MapClient.weight);

                                writer.close();
                                socket.close();

                                // 入力欄を空にする
                                nameField.setText("");
                                heightField.setText("");
                                weightField.setText("");

                                JOptionPane.showMessageDialog(
                                                frame,
                                                "登録が完了しました。");
                        } catch (Exception ex) {

                                JOptionPane.showMessageDialog(
                                                frame,
                                                "サーバに接続できません。");

                                ex.printStackTrace();
                        }

                        try {
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
                                if (result == 0) {
                                        MapClient.main(new String[] { "gym", weight });
                                } else if (result == 1) {
                                        MapClient.main(new String[] { "restaurant", weight });
                                }
                        } catch (Exception ex) {
                                JOptionPane.showMessageDialog(
                                                frame,
                                                "Google Places API の設定ができていません。");
                        }
                });

                frame.setVisible(true);
        }
}