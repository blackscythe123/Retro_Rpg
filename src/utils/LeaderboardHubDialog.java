package utils;

import consoles.GamingConsole;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Hub dialog allowing players to browse leaderboards for any game and speed.
 */
public class LeaderboardHubDialog extends JDialog {
    private static final Color BG_TOP = new Color(18, 24, 48);
    private static final Color BG_BOTTOM = new Color(8, 10, 24);
    private static final Color GLASS_TOP = new Color(54, 64, 112, 235);
    private static final Color GLASS_BOTTOM = new Color(32, 38, 70, 225);
    private static final Color GLASS_BORDER = new Color(102, 122, 196, 220);
    private static final Color ACCENT_PRIMARY = new Color(110, 142, 255);
    private static final Color TEXT_PRIMARY = new Color(236, 240, 255);
    private static final Color TEXT_SUBTLE = new Color(185, 195, 230);
    private static final Font TITLE_FONT = new Font("Poppins", Font.BOLD, 26);
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font CELL_FONT = new Font("SansSerif", Font.PLAIN, 13);

    private final JComboBox<GameOption> gameCombo;
    private final JComboBox<GamingConsole.GameSpeedProfile> speedCombo;
    private final LeaderboardTableModel tableModel;
    private final JLabel statusLabel;

    private static final GameOption[] GAME_OPTIONS = {
            new GameOption("Snake Redux", "SnakeConsole"),
            new GameOption("Flappy Bird Neo", "FlappyBirdConsole"),
            new GameOption("Space Shooter Hyperdrive", "SpaceShooterConsole")
    };

    public LeaderboardHubDialog(JFrame owner) {
        super(owner, "Leaderboards", true);
        setResizable(false);

        GradientPanel root = new GradientPanel(BG_TOP, BG_BOTTOM);
        root.setLayout(new BorderLayout(20, 20));
        root.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Leaderboard Nexus");
        title.setForeground(ACCENT_PRIMARY);
        title.setFont(TITLE_FONT);
        header.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Browse best runs across every module and tempo.");
        subtitle.setForeground(TEXT_SUBTLE);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        header.add(subtitle, BorderLayout.SOUTH);

        root.add(header, BorderLayout.NORTH);

        GlassPanel controls = new GlassPanel(new BorderLayout(12, 12), GLASS_TOP, GLASS_BOTTOM, GLASS_BORDER, true);
        controls.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel selectors = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        selectors.setOpaque(false);
        gameCombo = new JComboBox<>(GAME_OPTIONS);
        gameCombo.setFont(new Font("SansSerif", Font.BOLD, 14));
        gameCombo.setForeground(TEXT_PRIMARY);
        gameCombo.setBackground(new Color(28, 36, 78));
        gameCombo.setBorder(BorderFactory.createLineBorder(new Color(70, 88, 142), 1, true));

        speedCombo = new JComboBox<>(GamingConsole.GameSpeedProfile.values());
        speedCombo.setFont(new Font("SansSerif", Font.BOLD, 14));
        speedCombo.setForeground(TEXT_PRIMARY);
        speedCombo.setBackground(new Color(28, 36, 78));
        speedCombo.setBorder(BorderFactory.createLineBorder(new Color(70, 88, 142), 1, true));

        selectors.add(new JLabelStyled("Game"));
        selectors.add(gameCombo);
        selectors.add(new JLabelStyled("Tempo"));
        selectors.add(speedCombo);

        controls.add(selectors, BorderLayout.NORTH);

        tableModel = new LeaderboardTableModel();
        JTable table = new JTable(tableModel);
        table.setFont(CELL_FONT);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(new Color(22, 28, 60, 215));
        table.setRowHeight(32);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setDefaultRenderer(Object.class, new GlowRowRenderer());

        JTableHeader headerComp = table.getTableHeader();
        headerComp.setReorderingAllowed(false);
        headerComp.setFont(HEADER_FONT);
        headerComp.setForeground(TEXT_PRIMARY);
        headerComp.setBackground(new Color(36, 48, 92));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GLASS_BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        scrollPane.getViewport().setBackground(new Color(18, 24, 48));
        controls.add(scrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(TEXT_SUBTLE);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controls.add(statusLabel, BorderLayout.SOUTH);

        root.add(controls, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);
        JButton refreshButton = createGhostButton("Refresh");
        refreshButton.addActionListener(e -> refresh());
        JButton closeButton = createPrimaryButton("Close");
        closeButton.addActionListener(e -> dispose());
        footer.add(refreshButton);
        footer.add(closeButton);
        root.add(footer, BorderLayout.SOUTH);

        gameCombo.addActionListener(e -> refresh());
        speedCombo.addActionListener(e -> refresh());

        setContentPane(root);
        setPreferredSize(new Dimension(600, 520));
        pack();
        setLocationRelativeTo(owner);
        refresh();
    }

    private void refresh() {
        GameOption game = (GameOption) gameCombo.getSelectedItem();
        GamingConsole.GameSpeedProfile speed = (GamingConsole.GameSpeedProfile) speedCombo.getSelectedItem();
        if (game == null || speed == null) {
            tableModel.setEntries(java.util.Collections.emptyList());
            statusLabel.setText("Select a game and tempo.");
            return;
        }
        try {
            Leaderboard lb = new Leaderboard(game.id, speed.toString());
            List<Leaderboard.Entry> entries = lb.readTop(25);
            tableModel.setEntries(entries);
            statusLabel.setText(entries.isEmpty() ? "No recorded runs yet." : String.format("Showing top %d runs", entries.size()));
        } catch (Exception ex) {
            tableModel.setEntries(java.util.Collections.emptyList());
            statusLabel.setText("Failed to load entries: " + ex.getMessage());
        }
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(new Color(18, 22, 36));
        button.setBackground(ACCENT_PRIMARY);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 26, 10, 26));
        return button;
    }

    private JButton createGhostButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(new Color(44, 54, 92, 225));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        return button;
    }

    private static class GameOption {
        final String label;
        final String id;

        GameOption(String label, String id) {
            this.label = label;
            this.id = id;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private class LeaderboardTableModel extends AbstractTableModel {
        private final String[] columns = {"Rank", "Player", "Score", "Time", "Played"};
        private java.util.List<Leaderboard.Entry> data = java.util.Collections.emptyList();

        void setEntries(java.util.List<Leaderboard.Entry> entries) {
            this.data = entries;
            fireTableDataChanged();
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

    private String formatElapsed(long ms) {
        long totalSeconds = ms / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
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

    private static class JLabelStyled extends JLabel {
        JLabelStyled(String text) {
            super(text);
            setForeground(TEXT_SUBTLE);
            setFont(new Font("SansSerif", Font.PLAIN, 14));
        }
    }
}
