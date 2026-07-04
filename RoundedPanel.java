import javax.swing.*;
import java.awt.*;

class RoundedPanel extends JPanel {

    public RoundedPanel() {
        setOpaque(false); // 超重要
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 40;

        // 影（少し右下にずらす）
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRoundRect(6, 6, getWidth() - 12, getHeight() - 12, arc, arc);

        // 本体
        g2.setColor(new Color(245, 255, 245));
        g2.fillRoundRect(0, 0, getWidth() - 12, getHeight() - 12, arc, arc);

        g2.dispose();

        super.paintComponent(g);
    }
}