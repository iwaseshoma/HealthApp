import org.jxmapviewer.*;
import org.jxmapviewer.viewer.*;
import org.jxmapviewer.input.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import javax.swing.event.MouseInputListener;

public class MapClient {

    // 店舗情報を保持できる独自のWaypointクラス
    // DefaultWaypointの代わりにこれを使うことで、描画時にPlace情報を参照できる
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

    // 種類ごとに色・店名を描画する自作レンダラー
    public static class ColoredWaypointRenderer implements WaypointRenderer<PlaceWaypoint> {

        @Override
        public void paintWaypoint(Graphics2D g, JXMapViewer map, PlaceWaypoint wp) {

            Point2D point = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());

            int x = (int) point.getX();
            int y = (int) point.getY();

            // ジムは赤、飲食店は青
            Color color = wp.getPlace().type.equals("gym") ? Color.RED : Color.BLUE;

            g = (Graphics2D) g.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // ピン本体(円)を描画
            int radius = 8;
            g.setColor(color);
            g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            g.setColor(Color.BLACK);
            g.drawOval(x - radius, y - radius, radius * 2, radius * 2);

            // 店名をピンの下に表示
            g.setFont(new Font("メイリオ", Font.PLAIN, 12));
            String name = wp.getPlace().name;
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(name);

            int textX = x - textWidth / 2;
            int textY = y + radius + 14;

            // 読みやすくするため、文字の背景に白帯を敷く
            g.setColor(new Color(255, 255, 255, 200));
            g.fillRect(textX - 2, textY - fm.getAscent(), textWidth + 4, fm.getHeight());

            g.setColor(Color.BLACK);
            g.drawString(name, textX, textY);

            g.dispose();
        }
    }

    private static Map<PlaceWaypoint, PlacesFetcher.Place> waypointPlaceMap = new HashMap<>();

    public static void main(String[] args) throws Exception {

        // OSMタイルサーバーに識別可能なUser-Agentを送るための設定
        System.setProperty("http.agent", "HealthApp-Practice/1.0 (student project; contact: 24fi017@ms.dendai.ac.jp)");

        JFrame frame = new JFrame("地図画面");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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

        // 東京駅を中心とした表示
        GeoPosition center = new GeoPosition(35.748, 139.806);
        mapViewer.setZoom(4);
        mapViewer.setAddressLocation(center);

        // ドラッグ・ズーム操作
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

        // Places APIで周辺の飲食店・ジムを取得
        PlacesFetcher fetcher = new PlacesFetcher();
        List<PlacesFetcher.Place> restaurants = fetcher.searchNearby(35.748, 139.806, 1000, "restaurant");
        List<PlacesFetcher.Place> gyms = fetcher.searchNearby(35.748, 139.806, 1000, "gym");

        Set<PlaceWaypoint> waypoints = new HashSet<>();
        for (PlacesFetcher.Place p : restaurants) {
            PlaceWaypoint wp = new PlaceWaypoint(p);
            waypoints.add(wp);
            waypointPlaceMap.put(wp, p);
        }
        for (PlacesFetcher.Place p : gyms) {
            PlaceWaypoint wp = new PlaceWaypoint(p);
            waypoints.add(wp);
            waypointPlaceMap.put(wp, p);
        }

        // WaypointPainterに自作のレンダラーをセット
        WaypointPainter<PlaceWaypoint> painter = new WaypointPainter<>();
        painter.setWaypoints(waypoints);
        painter.setRenderer(new ColoredWaypointRenderer());
        mapViewer.setOverlayPainter(painter);

        // アイコンクリック検出
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (PlaceWaypoint wp : waypoints) {
                    Point2D point = mapViewer.getTileFactory()
                            .geoToPixel(wp.getPosition(), mapViewer.getZoom());
                    Rectangle rect = mapViewer.getViewportBounds();
                    int x = (int) (point.getX() - rect.getX());
                    int y = (int) (point.getY() - rect.getY());

                    if (e.getPoint().distance(x, y) < 15) {
                        PlacesFetcher.Place place = waypointPlaceMap.get(wp);
                        openInputDialog(frame, place);
                        break;
                    }
                }
            }
        });

        frame.add(mapViewer);
        frame.setVisible(true);
    }

    // アイコンクリック時にメニュー/トレーニング入力ダイアログを開く
    private static void openInputDialog(JFrame frame, PlacesFetcher.Place place) {
        if (place.type.equals("restaurant")) {
            String menu = JOptionPane.showInputDialog(frame,
                    place.name + " で食べたメニューを入力してください");
            // TODO: サーバへ送信・カロリー計算処理へ
        } else {
            String training = JOptionPane.showInputDialog(frame,
                    place.name + " で行ったトレーニング内容を入力してください");
            // TODO: 有酸素の場合は時間・距離の追加入力へ
        }
    }
}