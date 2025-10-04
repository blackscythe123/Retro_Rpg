package utils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Styled dialog prompting the player to save their score.
 */
public class SaveScoreDialog extends JDialog {
    private static final Color BG_TOP = new Color(18, 24, 48);
    private static final Color BG_BOTTOM = new Color(8, 10, 24);
    private static final Color GLASS_TOP = new Color(54, 64, 112, 235);
    private static final Color GLASS_BOTTOM = new Color(32, 38, 70, 225);
    private static final Color GLASS_BORDER = new Color(102, 122, 196, 220);
    private static final Color ACCENT_PRIMARY = new Color(110, 142, 255);
    private static final Color TEXT_PRIMARY = new Color(236, 240, 255);
    private static final Color TEXT_SUBTLE = new Color(185, 195, 230);

    private final JTextField nameField;
    private boolean saveSelected = false;

    public static class Result {
        public final boolean save;
        public final String name;

        Result(boolean save, String name) {
            this.save = save;
            this.name = name;
        }
    }

    public SaveScoreDialog(JFrame owner, String gameLabel, String speedLabel, int score, String timeText) {
        super(owner, "Save Score", true);
        setResizable(false);

        GradientPanel root = new GradientPanel(BG_TOP, BG_BOTTOM);
        root.setLayout(new BorderLayout(18, 18));
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));

        JLabel title = new JLabel("Victory Capsule", JLabel.CENTER);
        title.setFont(new Font("Poppins", Font.BOLD, 24));
        title.setForeground(ACCENT_PRIMARY);
        root.add(title, BorderLayout.NORTH);

        GlassPanel body = new GlassPanel(new BorderLayout(10, 16), GLASS_TOP, GLASS_BOTTOM, GLASS_BORDER, true);
        body.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel summary = new JLabel(String.format("%s — %s", gameLabel, speedLabel));
        summary.setForeground(TEXT_SUBTLE);
        summary.setFont(new Font("SansSerif", Font.PLAIN, 14));
        body.add(summary, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        JLabel scoreLabel = new JLabel(String.format("Final Score: %d", score));
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        scoreLabel.setForeground(TEXT_PRIMARY);
        scoreLabel.setHorizontalAlignment(JLabel.CENTER);
        center.add(scoreLabel, BorderLayout.NORTH);

        JLabel timeLabel = new JLabel(String.format("Time Survived: %s", timeText));
        timeLabel.setHorizontalAlignment(JLabel.CENTER);
        timeLabel.setForeground(TEXT_SUBTLE);
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        center.add(timeLabel, BorderLayout.CENTER);

        JPanel namePanel = new JPanel(new BorderLayout(6, 0));
        namePanel.setOpaque(false);
        JLabel nameLabel = new JLabel("Pilot Tag");
        nameLabel.setForeground(TEXT_SUBTLE);
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        namePanel.add(nameLabel, BorderLayout.NORTH);

        nameField = new JTextField("Player");
        nameField.setForeground(TEXT_PRIMARY);
        nameField.setBackground(new Color(24, 30, 64));
        nameField.setCaretColor(TEXT_PRIMARY);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 88, 142), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        nameField.setFont(new Font("SansSerif", Font.BOLD, 14));
        namePanel.add(nameField, BorderLayout.CENTER);

        center.add(namePanel, BorderLayout.SOUTH);
        body.add(center, BorderLayout.CENTER);

        JLabel hint = new JLabel("Save to immortalize this run.", JLabel.CENTER);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 12));
        hint.setForeground(new Color(160, 170, 210));
        body.add(hint, BorderLayout.SOUTH);

        root.add(body, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        JButton skipButton = createGhostButton("Skip");
        skipButton.addActionListener(e -> {
            saveSelected = false;
            dispose();
        });
        JButton saveButton = createPrimaryButton("Save");
        saveButton.addActionListener(e -> {
            saveSelected = true;
            dispose();
        });
        actions.add(skipButton);
        actions.add(saveButton);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
        setPreferredSize(new Dimension(420, 360));
        pack();
        setLocationRelativeTo(owner);
    }

    public Result showDialogAndGetResult() {
        setVisible(true);
        return new Result(saveSelected, nameField.getText());
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(new Color(18, 22, 36));
        button.setBackground(ACCENT_PRIMARY);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
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
}
