import org.jxmapviewer.*;
import org.jxmapviewer.viewer.*;
import org.jxmapviewer.input.*;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

public class MapClient {

    // ===== 店舗情報を保持できる独自のWaypointクラス =====
    public static class PlaceWaypoint implements Waypoint {
        private final GeoPosition position;
        private final PlacesFetcher.Place place;

        public PlaceWaypoint(PlacesFetcher.Place place) {
            this.position = new GeoPosition(place.lat, place.lng);
            this.place = place;
        }

        @Override
        public GeoPosition getPosition() {
            return position;
        }

        public PlacesFetcher.Place getPlace() {
            return place;
        }
    }

    // ===== 種類ごとに色・店名を描画する自作レンダラー =====
    public static class ColoredWaypointRenderer implements WaypointRenderer<PlaceWaypoint> {
        @Override
        public void paintWaypoint(Graphics2D g, JXMapViewer map, PlaceWaypoint wp) {
            Point2D point = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());
            int x = (int) point.getX();
            int y = (int) point.getY();

            Color color = wp.getPlace().type.equals("gym") ? Color.RED : Color.BLUE;

            g = (Graphics2D) g.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int radius = 8;
            g.setColor(color);
            g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            g.setColor(Color.BLACK);
            g.drawOval(x - radius, y - radius, radius * 2, radius * 2);

            g.setFont(new Font("メイリオ", Font.PLAIN, 12));
            String name = wp.getPlace().name;
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(name);
            int textX = x - textWidth / 2;
            int textY = y + radius + 14;

            g.setColor(new Color(255, 255, 255, 200));
            g.fillRect(textX - 2, textY - fm.getAscent(), textWidth + 4, fm.getHeight());
            g.setColor(Color.BLACK);
            g.drawString(name, textX, textY);

            g.dispose();
        }
    }

    // ===== 状態管理 =====
    private static Set<PlaceWaypoint> waypoints = new HashSet<>();
    public static double height;
    public static double weight;
    private static int totalCalorieBurn = 0; // 累計消費カロリー(簡易)
    private static double userWeightKg = -1; // カロリー計算用の体重(未設定なら-1)
    // 摂取カロリー
    private static double totalCalorieIntake = 0;
    private static double[] weekCalories = new double[7];
    private static int currentDay = 0;

    // 消費カロリー
    private static double totalBurnCalorie = 0;
    private static double[] weekBurnCalories = new double[7];
    private static int currentGymDay = 0;
    // 飲食店・ジム一覧
    private static List<PlacesFetcher.Place> restaurantList;
    private static List<PlacesFetcher.Place> gymList;

    // 地図
    private static JXMapViewer mapViewer;
    private static WaypointPainter<PlaceWaypoint> painter;

    // 左側パネル(CardLayoutで「未選択」「詳細」を切り替え)
    private static JPanel leftPanel;
    private static CardLayout cardLayout;
    private static JLabel totalCalorieLabel;

    private static final String CARD_EMPTY = "empty";
    private static final String CARD_DETAIL = "detail";

    public static void main(String[] args) throws Exception {

        System.out.println("=== MapClient開始 ===");
        System.out.println("height = " + height);
        System.out.println("weight = " + weight);

        System.setProperty("http.agent", "HealthApp-Practice/1.0 (student project; contact: 24fi017@ms.dendai.ac.jp)");

        // RegisterClientから体重が渡されていれば、それを使う(再入力を求めない)
        // args[0]: 表示モード("gym" / "restaurant" / それ以外は全件)
        // args[1]: 体重(kg)
        if (args != null && args.length >= 2) {
            try {
                userWeightKg = Double.parseDouble(args[1].trim());
            } catch (NumberFormatException ex) {
                System.err.println("体重の値が不正なため未設定として扱います: " + args[1]);
            }
        }

        JFrame frame = new JFrame("健康管理アプリ - 地図画面");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ===== 地図の準備 =====
        mapViewer = new JXMapViewer();
        TileFactoryInfo info = new TileFactoryInfo(
                1, 17, 17,
                256, true, true,
                "https://tile.openstreetmap.org",
                "x", "y", "z") {
            @Override
            public String getTileUrl(int x, int y, int zoom) {
                int z = getTotalMapZoom() - zoom;
                return "https://tile.openstreetmap.org/" + z + "/" + x + "/" + y + ".png";
            }
        };
        mapViewer.setTileFactory(new DefaultTileFactory(info));

        GeoPosition center = new GeoPosition(35.748, 139.806);
        mapViewer.setZoom(4);
        mapViewer.setAddressLocation(center);

        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

        // ===== 店舗データ取得 =====
        PlacesFetcher fetcher = new PlacesFetcher();

        restaurantList = fetcher.searchNearby(
                35.748,
                139.806,
                1000,
                "restaurant");

        gymList = fetcher.searchNearby(
                35.748,
                139.806,
                1000,
                "gym");

        // 表示モード
        String mode = "all";

        if (args != null && args.length > 0) {
            mode = args[0];
        }
        waypoints.clear();

        if (mode.equals("gym")) {

            for (PlacesFetcher.Place p : gymList) {
                waypoints.add(new PlaceWaypoint(p));
            }

        } else if (mode.equals("restaurant")) {

            for (PlacesFetcher.Place p : restaurantList) {
                waypoints.add(new PlaceWaypoint(p));
            }

        } else {

            for (PlacesFetcher.Place p : restaurantList) {
                waypoints.add(new PlaceWaypoint(p));
            }

            for (PlacesFetcher.Place p : gymList) {
                waypoints.add(new PlaceWaypoint(p));
            }
        }

        painter = new WaypointPainter<>();
        painter.setWaypoints(waypoints);
        painter.setRenderer(new ColoredWaypointRenderer());
        mapViewer.setOverlayPainter(painter);

        // ===== 左側パネル(情報 & メニュー選択) =====
        cardLayout = new CardLayout();
        leftPanel = new JPanel(cardLayout);
        leftPanel.setPreferredSize(new Dimension(380, 0));

        // 何も選ばれていない時の表示
        JPanel emptyPanel = new JPanel(new BorderLayout());
        JLabel emptyLabel = new JLabel("地図上のアイコンを選択してください", SwingConstants.CENTER);
        emptyLabel.setFont(new Font("メイリオ", Font.PLAIN, 16));
        emptyPanel.add(emptyLabel, BorderLayout.CENTER);

        // 累計カロリー表示(画面下部に常時表示)
        totalCalorieLabel = new JLabel("", SwingConstants.CENTER);
        totalCalorieLabel.setFont(new Font("メイリオ", Font.BOLD, 14));
        updateCalorieLabel();
        emptyPanel.add(totalCalorieLabel, BorderLayout.SOUTH);

        leftPanel.add(emptyPanel, CARD_EMPTY);

        // 最初は「未選択」状態を表示
        cardLayout.show(leftPanel, CARD_EMPTY);

        // ===== アイコンクリック検出 =====
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                // クリック位置に近いピンを「全部」集める(最初の1件で確定しない)
                List<PlaceWaypoint> candidates = new ArrayList<>();

                for (PlaceWaypoint wp : waypoints) {
                    Point2D point = mapViewer.getTileFactory()
                            .geoToPixel(wp.getPosition(), mapViewer.getZoom());
                    Rectangle rect = mapViewer.getViewportBounds();
                    int x = (int) (point.getX() - rect.getX());
                    int y = (int) (point.getY() - rect.getY());

                    if (e.getPoint().distance(x, y) < 15) {
                        candidates.add(wp);
                    }
                }

                if (candidates.isEmpty()) {
                    return; // 何もヒットしなければ無視
                }

                if (candidates.size() == 1) {
                    // 候補が1件だけなら、そのまま詳細表示
                    showPlaceDetail(candidates.get(0).getPlace());
                } else {
                    // 候補が複数あれば、選択リストを表示してから詳細表示
                    PlacesFetcher.Place selected = choosePlaceFromCandidates(candidates, mapViewer);
                    if (selected != null) {
                        showPlaceDetail(selected);
                    }
                }
            }
        });

        // ===== 地図右上の切り替えボタン =====
        JPanel toggleButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));

        JButton restaurantToggleButton = new JButton("🍴 飲食店");
        JButton gymToggleButton = new JButton("🏋 ジム");
        restaurantToggleButton.setFont(new Font("メイリオ", Font.PLAIN, 13));
        gymToggleButton.setFont(new Font("メイリオ", Font.PLAIN, 13));

        restaurantToggleButton.addActionListener(e -> {
            showRestaurants();
            // 表示が切り替わったら、選択中の詳細パネルは一旦閉じる
            cardLayout.show(leftPanel, CARD_EMPTY);
        });

        gymToggleButton.addActionListener(e -> {
            showGyms();
            cardLayout.show(leftPanel, CARD_EMPTY);
        });

        toggleButtonPanel.add(restaurantToggleButton);
        toggleButtonPanel.add(gymToggleButton);

        // 地図を包むパネル(上部に切り替えボタン、中央に地図本体)
        JPanel mapWrapperPanel = new JPanel(new BorderLayout());
        mapWrapperPanel.add(toggleButtonPanel, BorderLayout.NORTH);
        mapWrapperPanel.add(mapViewer, BorderLayout.CENTER);

        // ===== 画面分割 (左:情報パネル 右:地図) =====
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, mapWrapperPanel);
        splitPane.setDividerLocation(380);
        splitPane.setOneTouchExpandable(true);

        frame.add(splitPane);
        frame.setVisible(true);
    }

    // クリック位置に複数のピンが重なっている場合、選択リストを表示して1件に絞る
    private static PlacesFetcher.Place choosePlaceFromCandidates(List<PlaceWaypoint> candidates, Component parent) {

        // 表示名にアイコン(絵文字)を付けて種類がひと目でわかるようにする
        String[] options = new String[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            PlacesFetcher.Place p = candidates.get(i).getPlace();
            String icon = p.type.equals("gym") ? "🏋" : "🍴";
            options[i] = icon + " " + p.name;
        }

        String selectedOption = (String) JOptionPane.showInputDialog(
                parent,
                "複数の場所が近くにあります。選択してください:",
                "場所の選択",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (selectedOption == null) {
            return null; // キャンセルされた場合
        }

        int selectedIndex = Arrays.asList(options).indexOf(selectedOption);
        return candidates.get(selectedIndex).getPlace();
    }

    // 店の詳細パネルを組み立てて表示する
    private static void showPlaceDetail(PlacesFetcher.Place place) {

        JPanel detailPanel = new JPanel(new BorderLayout(10, 10));
        detailPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 上部: 店名・種類
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(place.name);
        nameLabel.setFont(new Font("メイリオ", Font.BOLD, 20));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String typeText = place.type.equals("gym") ? "🏋 ジム" : "🍴 飲食店";
        JLabel typeLabel = new JLabel(typeText);
        typeLabel.setFont(new Font("メイリオ", Font.PLAIN, 14));
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(nameLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(typeLabel);

        detailPanel.add(headerPanel, BorderLayout.NORTH);

        if (place.type.equals("restaurant")) {
            detailPanel.add(buildRestaurantSelectionPanel(place), BorderLayout.CENTER);
        } else {
            detailPanel.add(buildGymSelectionPanel(place), BorderLayout.CENTER);
        }

        // 累計カロリー表示は下部に据え置き
        detailPanel.add(totalCalorieLabel, BorderLayout.SOUTH);

        // 既存のdetailカードがあれば置き換える
        leftPanel.add(detailPanel, CARD_DETAIL);
        cardLayout.show(leftPanel, CARD_DETAIL);
        leftPanel.revalidate();
        leftPanel.repaint();
    }

    // 飲食店用: メニュー選択リスト + 登録ボタン + 登録完了(週次記録)ボタン
    private static JPanel buildRestaurantSelectionPanel(PlacesFetcher.Place place) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel instructionLabel = new JLabel("食べたメニューを選択してください");
        instructionLabel.setFont(new Font("メイリオ", Font.PLAIN, 14));
        panel.add(instructionLabel, BorderLayout.NORTH);

        // 店名(チェーン名)に応じたメニューを取得する
        // (例: 「サイゼリヤ」が店名に含まれる場合のみサイゼリヤメニューを表示し、
        //  それ以外の店では汎用メニュー(自由入力)を表示する)
        List<MenuDatabase.MenuItem> menuItems = MenuDatabase.getRestaurantMenu(place.name);
        DefaultListModel<MenuDatabase.MenuItem> model = new DefaultListModel<>();
        for (MenuDatabase.MenuItem item : menuItems) {
            model.addElement(item);
        }

        JList<MenuDatabase.MenuItem> menuList = new JList<>(model);
        menuList.setFont(new Font("メイリオ", Font.PLAIN, 14));
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(menuList);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton registerButton = new JButton("このメニューを登録する");
        registerButton.setFont(new Font("メイリオ", Font.PLAIN, 14));

        JButton completeButton = new JButton("登録完了");
        completeButton.setFont(new Font("メイリオ", Font.PLAIN, 14));
        completeButton.setEnabled(false); // 最初は押せない

        registerButton.addActionListener(e -> {

            MenuDatabase.MenuItem selected = menuList.getSelectedValue();

            if (selected == null) {
                JOptionPane.showMessageDialog(panel, "メニューを選択してください。");
                return;
            }

            int calorie = selected.calorie;

            if (calorie == -1) {

                String input = JOptionPane.showInputDialog(
                        panel,
                        "食べたメニュー名とカロリー(kcal)を入力してください\n例: オムライス 650");

                if (input == null || input.isBlank())
                    return;

                try {
                    String[] parts = input.trim().split("\\s+");
                    calorie = Integer.parseInt(parts[parts.length - 1]);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel,
                            "カロリーの数値を正しく入力してください。");
                    return;
                }
            }

            // 合計カロリーを更新
            totalCalorieIntake += calorie;
            updateCalorieLabel();

            // メニュー名を保存
            registerButton.putClientProperty("menuName", selected.name);

            // 登録完了ボタンを押せるようにする
            completeButton.setEnabled(true);

            JOptionPane.showMessageDialog(
                    panel,
                    selected.name + "（" + calorie + " kcal）を登録しました。\n"
                            + "食事が終わったら「登録完了」を押してください。");
        });

        completeButton.addActionListener(e -> {

            // 今日の摂取カロリーを保存
            weekCalories[currentDay] = totalCalorieIntake;

            JOptionPane.showMessageDialog(
                    panel,
                    (currentDay + 1) + "日目の記録を保存しました。");

            // 次の日へ
            currentDay++;

            // 今日のカロリーをリセット(摂取・消費とも1日単位でリセット)
            totalCalorieIntake = 0;
            totalCalorieBurn = 0;
            updateCalorieLabel();

            // 7日分そろったら比較
            if (currentDay == 7) {

                double intake = 0;
                double burn = 0;

                // まず1週間分を合計
                for (int i = 0; i < 7; i++) {
                    intake += weekCalories[i];
                    burn += weekBurnCalories[i];
                }

                // その後に計算
                double bmr = calculateBMR();
                double totalBurn = burn + bmr * 7;
                double balance = intake - totalBurn;
                double fatChange = balance / 7200.0;

                String result = CountryJudge.judge(weekCalories);

                result += "\n\n";
                result += "===== 1週間の結果 =====\n";
                result += String.format("摂取カロリー：%.0f kcal\n", intake);
                result += String.format("運動消費：%.0f kcal\n", burn);
                result += String.format("基礎代謝(7日分)：%.0f kcal\n", bmr * 7);
                result += String.format("総消費：%.0f kcal\n", totalBurn);
                result += String.format("収支：%.0f kcal\n", balance);

                if (fatChange > 0) {

                    result += String.format(
                            "体脂肪は約%.2f kg増える計算です。",
                            fatChange);

                } else {

                    result += String.format(
                            "体脂肪は約%.2f kg減る計算です。",
                            Math.abs(fatChange));
                }

                JTextArea textArea = new JTextArea(result);
                textArea.setEditable(false);
                textArea.setFont(new Font("メイリオ", Font.BOLD, 24));
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);

                // 背景をダイアログと同じ色にする
                textArea.setBackground(UIManager.getColor("OptionPane.background"));
                textArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                // スクロール付きにしてサイズを指定
                JScrollPane resultScrollPane = new JScrollPane(textArea);
                resultScrollPane.setPreferredSize(new Dimension(700, 500));

                JOptionPane.showMessageDialog(
                        panel,
                        resultScrollPane,
                        "1週間の判定結果",
                        JOptionPane.INFORMATION_MESSAGE);

                // 次の週のためにリセット
                currentDay = 0;
                Arrays.fill(weekCalories, 0);
            }

            completeButton.setEnabled(false);
        });

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        buttonPanel.add(registerButton);
        buttonPanel.add(completeButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ジム用: トレーニング種目ごとにボタンを配置し、押すと種類に応じた入力ダイアログを開く
    private static JPanel buildGymSelectionPanel(PlacesFetcher.Place place) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel instructionLabel = new JLabel("行ったトレーニングを選択してください");
        instructionLabel.setFont(new Font("メイリオ", Font.PLAIN, 14));
        panel.add(instructionLabel, BorderLayout.NORTH);

        JPanel buttonListPanel = new JPanel();
        buttonListPanel.setLayout(new BoxLayout(buttonListPanel, BoxLayout.Y_AXIS));

        List<MenuDatabase.GymActivity> activities = MenuDatabase.getGymMenu();
        for (MenuDatabase.GymActivity activity : activities) {
            JButton button = new JButton(activity.name);
            button.setFont(new Font("メイリオ", Font.PLAIN, 14));
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

            button.addActionListener(e -> {
                switch (activity.type) {
                    case AEROBIC -> openAerobicDialog(panel, place, activity.name);
                    case STRENGTH -> openStrengthDialog(panel, place, activity.name);
                    case STRETCH -> openStretchDialog(panel, place, activity.name);
                    case FREE -> openFreeGymDialog(panel, place);
                }
            });

            buttonListPanel.add(button);
            buttonListPanel.add(Box.createVerticalStrut(8));
        }

        JScrollPane scrollPane = new JScrollPane(buttonListPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 今日を終了ボタン
        JButton finishButton = new JButton("今日を終了");
        finishButton.setFont(new Font("メイリオ", Font.PLAIN, 14));

        panel.add(finishButton, BorderLayout.SOUTH);

        finishButton.addActionListener(e -> {

            // 今日の消費カロリーを保存
            weekBurnCalories[currentGymDay] = totalBurnCalorie;

            currentGymDay++;

            totalBurnCalorie = 0;

            if (currentDay < 7) {
                JOptionPane.showMessageDialog(
                        panel,
                        currentGymDay + "日目を保存しました。");
            }

            if (currentGymDay == 7) {

                double total = 0;

                for (double c : weekBurnCalories) {
                    total += c;
                }

                JOptionPane.showMessageDialog(
                        panel,
                        "===== 1週間の運動結果 =====\n\n"
                                + "1週間の合計消費カロリー\n"
                                + total + " kcal");

            }
        });

        return panel;
    }

    // 有酸素運動: 時間(分)・速度(km/h)を入力させ、簡易的にMET法で消費カロリーを計算する
    private static void openAerobicDialog(Component parent, PlacesFetcher.Place place, String activityName) {

        JTextField timeField = new JTextField();
        JTextField speedField = new JTextField();

        JPanel inputPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        inputPanel.add(new JLabel("時間(分):"));
        inputPanel.add(timeField);
        inputPanel.add(new JLabel("速度(km/h):"));
        inputPanel.add(speedField);

        int result = JOptionPane.showConfirmDialog(parent, inputPanel,
                activityName + " の記録", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION)
            return;

        try {
            double minutes = Double.parseDouble(timeField.getText().trim());
            double speedKmh = Double.parseDouble(speedField.getText().trim());

            double weightKg = getOrAskUserWeight(parent);
            if (weightKg <= 0)
                return;

            // 簡易MET推定: 速度(km/h)にほぼ比例するとして概算(厳密な運動生理学的数値ではない)
            double met = speedKmh * 1.0;
            double hours = minutes / 60.0;
            int burnedCalorie = (int) Math.round(met * weightKg * hours);

            recordBurnedCalorie(parent, place, activityName + "(" + minutes + "分, " + speedKmh + "km/h)",
                    burnedCalorie);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(parent, "時間・速度は数値で入力してください。");
        }
    }

    // 筋力トレーニング: 重量(kg)・セット数・回数を入力させる
    private static void openStrengthDialog(Component parent, PlacesFetcher.Place place, String activityName) {

        JTextField weightField = new JTextField();
        JTextField setsField = new JTextField();
        JTextField repsField = new JTextField();

        JPanel inputPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        inputPanel.add(new JLabel("重量(kg):"));
        inputPanel.add(weightField);
        inputPanel.add(new JLabel("セット数:"));
        inputPanel.add(setsField);
        inputPanel.add(new JLabel("回数(1セットあたり):"));
        inputPanel.add(repsField);

        int result = JOptionPane.showConfirmDialog(parent, inputPanel,
                activityName + " の記録", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION)
            return;

        try {
            double weightLifted = Double.parseDouble(weightField.getText().trim());
            int sets = Integer.parseInt(setsField.getText().trim());
            int reps = Integer.parseInt(repsField.getText().trim());

            // 簡易的な消費カロリー概算(挙上重量×総回数に係数を掛けただけの簡易推定値)
            int burnedCalorie = (int) Math.round(weightLifted * sets * reps * 0.1);

            String detail = activityName + "(" + weightLifted + "kg × " + sets + "セット × " + reps + "回)";
            recordBurnedCalorie(parent, place, detail, burnedCalorie);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(parent, "重量・セット数・回数は数値で入力してください。");
        }
    }

    // ストレッチ・ヨガ: 時間のみ入力させる
    private static void openStretchDialog(Component parent, PlacesFetcher.Place place, String activityName) {

        String input = JOptionPane.showInputDialog(parent, activityName + " の時間(分)を入力してください");
        if (input == null || input.isBlank())
            return;

        try {
            double minutes = Double.parseDouble(input.trim());
            double weightKg = getOrAskUserWeight(parent);
            if (weightKg <= 0)
                return;

            double met = 2.5; // ヨガ・ストレッチの一般的なMET値
            double hours = minutes / 60.0;
            int burnedCalorie = (int) Math.round(met * weightKg * hours);

            recordBurnedCalorie(parent, place, activityName + "(" + minutes + "分)", burnedCalorie);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(parent, "時間は数値で入力してください。");
        }
    }

    // 自由入力: 内容と消費カロリーを手入力
    private static void openFreeGymDialog(Component parent, PlacesFetcher.Place place) {
        String input = JOptionPane.showInputDialog(parent, "トレーニング内容と消費カロリー(kcal)を入力してください\n例: 縄跳び 200");
        if (input == null || input.isBlank())
            return;

        try {
            String[] parts = input.trim().split("\\s+");
            int burnedCalorie = Integer.parseInt(parts[parts.length - 1]);
            String activityName = input.substring(0, input.lastIndexOf(parts[parts.length - 1])).trim();

            recordBurnedCalorie(parent, place, activityName, burnedCalorie);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "カロリーの数値を正しく入力してください。");
        }
    }

    // 消費カロリーの記録を共通化(累計への加算と表示更新)
    private static void recordBurnedCalorie(Component parent, PlacesFetcher.Place place, String detail,
            int burnedCalorie) {
        totalCalorieBurn += burnedCalorie;
        updateCalorieLabel();

        totalBurnCalorie += burnedCalorie;

        JOptionPane.showMessageDialog(parent,
                place.name + " で「" + detail + "」を記録しました。(推定消費: " + burnedCalorie + " kcal)");

        // TODO: ここでサーバーへ送信し、運動記録として保存する処理を追加する
    }

    // 体重が未設定なら入力を求め、以後はキャッシュして使い回す
    // (RegisterClientから渡されていれば、ここで聞かれることはない)
    private static double getOrAskUserWeight(Component parent) {
        if (userWeightKg > 0)
            return userWeightKg;

        String input = JOptionPane.showInputDialog(parent, "カロリー計算のため、体重(kg)を入力してください");
        if (input == null || input.isBlank())
            return -1;

        try {
            userWeightKg = Double.parseDouble(input.trim());
            return userWeightKg;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(parent, "体重は数値で入力してください。");
            return -1;
        }
    }

    private static void updateCalorieLabel() {
        totalCalorieLabel.setText(
                "摂取: " + totalCalorieIntake + " kcal　/　消費: " + totalCalorieBurn + " kcal");
    }

    private static void showRestaurants() {

        waypoints.clear();

        for (PlacesFetcher.Place p : restaurantList) {
            waypoints.add(new PlaceWaypoint(p));
        }

        painter.setWaypoints(waypoints);
        mapViewer.repaint();
    }

    private static void showGyms() {

        waypoints.clear();

        for (PlacesFetcher.Place p : gymList) {
            waypoints.add(new PlaceWaypoint(p));
        }

        painter.setWaypoints(waypoints);
        mapViewer.repaint();
    }

    private static double calculateBMR() {

        System.out.println("=== calculateBMR ===");
        System.out.println("height = " + height);
        System.out.println("weight = " + weight);

        double bmr = 66.47
                + 13.75 * weight
                + 5.0 * height
                - 6.76 * 20;

        System.out.println("height = " + height);
        System.out.println("weight = " + weight);
        System.out.println("BMR = " + bmr);

        return bmr;
    }

}