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
    private static int totalCalorieIntake = 0; // 累計摂取カロリー(簡易)

    // 左側パネル(CardLayoutで「未選択」「詳細」を切り替え)
    private static JPanel leftPanel;
    private static CardLayout cardLayout;
    private static JLabel totalCalorieLabel;

    private static final String CARD_EMPTY = "empty";
    private static final String CARD_DETAIL = "detail";

    public static void main(String[] args) throws Exception {

        System.setProperty("http.agent", "HealthApp-Practice/1.0 (student project; contact: 24fi017@ms.dendai.ac.jp)");

        JFrame frame = new JFrame("健康管理アプリ - 地図画面");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ===== 地図の準備 =====
        JXMapViewer mapViewer = new JXMapViewer();
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
        List<PlacesFetcher.Place> restaurants = fetcher.searchNearby(35.748, 139.806, 1000, "restaurant");
        List<PlacesFetcher.Place> gyms = fetcher.searchNearby(35.748, 139.806, 1000, "gym");

        for (PlacesFetcher.Place p : restaurants) {
            waypoints.add(new PlaceWaypoint(p));
        }
        for (PlacesFetcher.Place p : gyms) {
            waypoints.add(new PlaceWaypoint(p));
        }

        WaypointPainter<PlaceWaypoint> painter = new WaypointPainter<>();
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
        totalCalorieLabel = new JLabel("本日の摂取カロリー合計: 0 kcal", SwingConstants.CENTER);
        totalCalorieLabel.setFont(new Font("メイリオ", Font.BOLD, 14));
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

        // ===== 画面分割 (左:情報パネル 右:地図) =====
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, mapViewer);
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
                options[0]
        );

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

    // 飲食店用: メニュー選択リスト + 登録ボタン
    private static JPanel buildRestaurantSelectionPanel(PlacesFetcher.Place place) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel instructionLabel = new JLabel("食べたメニューを選択してください");
        instructionLabel.setFont(new Font("メイリオ", Font.PLAIN, 14));
        panel.add(instructionLabel, BorderLayout.NORTH);

        List<MenuDatabase.MenuItem> menuItems = MenuDatabase.getRestaurantMenu();
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

        registerButton.addActionListener(e -> {
            MenuDatabase.MenuItem selected = menuList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(panel, "メニューを選択してください。");
                return;
            }

            int calorie = selected.calorie;

            // 「その他(自由入力)」が選ばれた場合は手入力させる
            if (calorie == -1) {
                String input = JOptionPane.showInputDialog(panel, "食べたメニュー名とカロリー(kcal)を入力してください\n例: オムライス 650");
                if (input == null || input.isBlank()) return;

                try {
                    String[] parts = input.trim().split("\\s+");
                    calorie = Integer.parseInt(parts[parts.length - 1]);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "カロリーの数値を正しく入力してください。");
                    return;
                }
            }

            totalCalorieIntake += calorie;
            totalCalorieLabel.setText("本日の摂取カロリー合計: " + totalCalorieIntake + " kcal");

            JOptionPane.showMessageDialog(panel,
                    place.name + " で「" + selected.name + "」(" + calorie + " kcal) を記録しました。");

            // TODO: ここでサーバーへ送信し、食事記録として保存する処理を追加する
        });

        panel.add(registerButton, BorderLayout.SOUTH);

        return panel;
    }

    // ジム用: トレーニング種目選択(有酸素は後日、時間・距離入力に対応予定)
    private static JPanel buildGymSelectionPanel(PlacesFetcher.Place place) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel instructionLabel = new JLabel("行ったトレーニングを選択してください");
        instructionLabel.setFont(new Font("メイリオ", Font.PLAIN, 14));
        panel.add(instructionLabel, BorderLayout.NORTH);

        List<String> gymMenu = MenuDatabase.getGymMenu();
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String item : gymMenu) {
            model.addElement(item);
        }

        JList<String> menuList = new JList<>(model);
        menuList.setFont(new Font("メイリオ", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(menuList);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton registerButton = new JButton("このトレーニングを登録する");
        registerButton.setFont(new Font("メイリオ", Font.PLAIN, 14));

        registerButton.addActionListener(e -> {
            String selected = menuList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(panel, "トレーニング内容を選択してください。");
                return;
            }
            // TODO: 有酸素の場合は時間・距離を追加入力し、消費カロリーを計算する処理へ
            JOptionPane.showMessageDialog(panel, place.name + " で「" + selected + "」を記録しました。");
        });

        panel.add(registerButton, BorderLayout.SOUTH);

        return panel;
    }
}