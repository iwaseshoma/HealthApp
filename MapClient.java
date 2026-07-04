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

    private static Map<Waypoint, PlacesFetcher.Place> waypointPlaceMap = new HashMap<>();

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
        "x", "y", "z"
) {
    @Override
    public String getTileUrl(int x, int y, int zoom) {
        int z = getTotalMapZoom() - zoom;
        return "https://tile.openstreetmap.org/" + z + "/" + x + "/" + y + ".png";
    }
};

mapViewer.setTileFactory(new DefaultTileFactory(info));

        //東京駅を中心とした
        GeoPosition center = new GeoPosition(35.681236, 139.767125); 
        mapViewer.setZoom(4);
        mapViewer.setAddressLocation(center);

        // ドラッグ・ズーム操作
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

        // Places APIで周辺の飲食店・ジムを取得
        PlacesFetcher fetcher = new PlacesFetcher();
        List<PlacesFetcher.Place> restaurants = fetcher.searchNearby(35.681236, 139.767125, 1000, "restaurant");
        List<PlacesFetcher.Place> gyms = fetcher.searchNearby(35.681236, 139.767125, 1000, "gym");

        Set<Waypoint> waypoints = new HashSet<>();
        for (PlacesFetcher.Place p : restaurants) {
            Waypoint wp = new DefaultWaypoint(new GeoPosition(p.lat, p.lng));
            waypoints.add(wp);
            waypointPlaceMap.put(wp, p);
        }
        for (PlacesFetcher.Place p : gyms) {
            Waypoint wp = new DefaultWaypoint(new GeoPosition(p.lat, p.lng));
            waypoints.add(wp);
            waypointPlaceMap.put(wp, p);
        }

        WaypointPainter<Waypoint> painter = new WaypointPainter<>();
        painter.setWaypoints(waypoints);
        mapViewer.setOverlayPainter(painter);

        // アイコンクリック検出
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (Waypoint wp : waypoints) {
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
