package utils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Neon-styled leaderboard dialog matching the Retro console aesthetic.
 */
public class LeaderboardDialog extends JDialog {
    private static final Color BG_TOP = new Color(18, 24, 48);
    private static final Color BG_BOTTOM = new Color(8, 10, 24);
    private static final Color GLASS_TOP = new Color(54, 64, 112, 235);
    private static final Color GLASS_BOTTOM = new Color(32, 38, 70, 225);
    private static final Color GLASS_BORDER = new Color(102, 122, 196, 220);
    private static final Color ACCENT_PRIMARY = new Color(110, 142, 255);
    private static final Color TEXT_PRIMARY = new Color(236, 240, 255);
    private static final Color TEXT_SUBTLE = new Color(185, 195, 230);
    private static final Font TITLE_FONT = new Font("Poppins", Font.BOLD, 24);
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font CELL_FONT = new Font("SansSerif", Font.PLAIN, 13);

    public LeaderboardDialog(JFrame owner, String gameLabel, String speedLabel, List<Leaderboard.Entry> entries) {
        super(owner, "Leaderboard", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(false);
        setResizable(false);

        GradientPanel root = new GradientPanel(BG_TOP, BG_BOTTOM);
        root.setLayout(new BorderLayout(18, 18));
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));

        JLabelPanel header = new JLabelPanel(gameLabel, speedLabel);
        root.add(header, BorderLayout.NORTH);

        LeaderboardTableModel model = new LeaderboardTableModel(entries);
        JTable table = new JTable(model);
        table.setFont(CELL_FONT);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(new Color(22, 28, 60, 215));
        table.setGridColor(new Color(64, 74, 120));
        table.setRowHeight(32);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setOpaque(false);
        table.setDefaultRenderer(Object.class, new GlowRowRenderer());

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setReorderingAllowed(false);
        tableHeader.setFont(HEADER_FONT);
        tableHeader.setForeground(TEXT_PRIMARY);
        tableHeader.setBackground(new Color(36, 48, 92));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GLASS_BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        scrollPane.getViewport().setBackground(new Color(18, 24, 48));

        GlassPanel body = new GlassPanel(new BorderLayout(), GLASS_TOP, GLASS_BOTTOM, GLASS_BORDER, true);
        body.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        if (entries == null || entries.isEmpty()) {
            javax.swing.JLabel empty = new javax.swing.JLabel("No records yet — be the first!");
            empty.setFont(new Font("SansSerif", Font.BOLD, 16));
            empty.setForeground(TEXT_SUBTLE);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            body.add(empty, BorderLayout.CENTER);
        } else {
            body.add(scrollPane, BorderLayout.CENTER);
        }

        root.add(body, BorderLayout.CENTER);

        JButton closeButton = createPrimaryButton("Return");
        closeButton.addActionListener(e -> dispose());

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.add(closeButton);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
        setPreferredSize(new Dimension(520, 460));
        pack();
        setLocationRelativeTo(owner);
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(new Color(18, 22, 36));
        btn.setBackground(ACCENT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 28, 10, 28));
        btn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }

    private String formatElapsed(long ms) {
        long totalSeconds = ms / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private class LeaderboardTableModel extends AbstractTableModel {
        private final String[] columns = {"Rank", "Player", "Score", "Time", "Played"};
        private final List<Leaderboard.Entry> data;

        LeaderboardTableModel(List<Leaderboard.Entry> entries) {
            this.data = entries;
        }

        @Override
        public int getRowCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Leaderboard.Entry entry = data.get(rowIndex);
            switch (columnIndex) {
                case 0: return rowIndex + 1;
                case 1: return entry.name;
                case 2: return entry.score;
                case 3: return formatElapsed(entry.elapsedMillis);
                case 4: return entry.timestamp;
                default: return "";
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }

    private class GlowRowRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setFont(CELL_FONT);
            setHorizontalAlignment(column == 0 || column == 2 ? SwingConstants.CENTER : SwingConstants.LEFT);
            Color base = new Color(28, 36, 78, 220);
            if (row == 0) {
                base = new Color(48, 60, 120, 230);
            } else if (row == 1) {
                base = new Color(40, 52, 108, 225);
            } else if (row == 2) {
                base = new Color(34, 46, 98, 220);
            }
            if (isSelected) {
                base = base.brighter();
            }
            c.setBackground(base);
            setForeground(TEXT_PRIMARY);
            return c;
        }
    }

    private static class GradientPanel extends JPanel {
        private final Color top;
        private final Color bottom;

        GradientPanel(Color top, Color bottom) {
            this.top = top;
            this.bottom = bottom;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class GlassPanel extends JPanel {
        private final Color top;
        private final Color bottom;
        private final Color border;
        private final boolean translucent;

        GlassPanel(BorderLayout layout, Color top, Color bottom, Color border, boolean translucent) {
            super(layout);
            this.top = top;
            this.bottom = bottom;
            this.border = border;
            this.translucent = translucent;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Color gradientTop = translucent ? top : new Color(top.getRed(), top.getGreen(), top.getBlue());
            Color gradientBottom = translucent ? bottom : new Color(bottom.getRed(), bottom.getGreen(), bottom.getBlue());
            g2.setPaint(new GradientPaint(0, 0, gradientTop, 0, getHeight(), gradientBottom));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            Color outline = translucent ? border : new Color(border.getRed(), border.getGreen(), border.getBlue());
            g2.setColor(outline);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class JLabelPanel extends JPanel {
        JLabelPanel(String game, String speed) {
            super(new BorderLayout());
            setOpaque(false);
            javax.swing.JLabel title = new javax.swing.JLabel(String.format("%s Leaderboard", game));
            title.setFont(TITLE_FONT);
            title.setForeground(TEXT_PRIMARY);
            javax.swing.JLabel subtitle = new javax.swing.JLabel(String.format("Speed: %s", speed));
            subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
            subtitle.setForeground(TEXT_SUBTLE);
            add(title, BorderLayout.NORTH);
            add(subtitle, BorderLayout.SOUTH);
        }
    }
}
