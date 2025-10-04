package utils;

import consoles.*;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import utils.AppDirectories;

/**
 * Main application class for the Retro Gaming Console
 */
public class RetroConsole extends JFrame {
    private static final Color BG_TOP = new Color(18, 24, 48);
    private static final Color BG_BOTTOM = new Color(8, 10, 24);
    private static final Color GLASS_TOP = new Color(54, 64, 112, 235);
    private static final Color GLASS_BOTTOM = new Color(32, 38, 70, 225);
    private static final Color GLASS_BORDER = new Color(102, 122, 196, 200);
    private static final Color CONTROL_TOP = new Color(36, 44, 78);
    private static final Color CONTROL_BOTTOM = new Color(30, 36, 66);
    private static final Color CONTROL_BORDER = new Color(82, 102, 168);
    private static final Color ACCENT_PRIMARY = new Color(110, 142, 255);
    private static final Color ACCENT_SECONDARY = new Color(255, 106, 196);
    private static final Color TEXT_PRIMARY = new Color(236, 240, 255);
    private static final Color TEXT_SUBTLE = new Color(185, 195, 230);
    private static final Font HEADLINE_FONT = new Font("Poppins", Font.BOLD, 32);
    private static final Font SUBTITLE_FONT = new Font("SansSerif", Font.PLAIN, 16);
    private static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final DateTimeFormatter LOG_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private GamePanel gamePanel;
    private GamingConsole currentConsole;
    private JPanel controlPanel;
    private JButton pauseButton;
    private JButton restartButton;
    private JButton menuButton;
    private JComboBox<GamingConsole.GameSpeedProfile> speedSelect;
    private JLabel statusLabel;
    private Thread gameLoopThread;
    private volatile boolean loopRunning;
    private GamingConsole.GameSpeedProfile activeSpeedProfile = GamingConsole.GameSpeedProfile.RELAXED;

    public RetroConsole() {
        setTitle("Retro Gaming Console");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 730);
        setLocationRelativeTo(null);
        setResizable(false);

        setLayout(new BorderLayout());
        createMenu();

        setVisible(true);
    }

    private void createMenu() {
        stopGameLoop();
        currentConsole = null;

        GradientPanel root = new GradientPanel(BG_TOP, BG_BOTTOM);
        root.setLayout(new BorderLayout(28, 28));
        root.setBorder(BorderFactory.createEmptyBorder(30, 34, 34, 34));

    GlassPanel hero = new GlassPanel(new BorderLayout(16, 8),
                new Color(64, 78, 134, 235),
                new Color(44, 54, 98, 225),
        GLASS_BORDER,
        true);
        hero.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JLabel title = new JLabel("Retro Gaming Console", SwingConstants.LEFT);
        title.setFont(HEADLINE_FONT);
        title.setForeground(TEXT_PRIMARY);

        JLabel tagline = new JLabel("Three arcade remasters. One neon hub.", SwingConstants.LEFT);
        tagline.setFont(SUBTITLE_FONT);
        tagline.setForeground(TEXT_SUBTLE);

        JPanel heroHeader = new JPanel(new BorderLayout());
        heroHeader.setOpaque(false);
        heroHeader.add(title, BorderLayout.WEST);

        JLabel badge = new JLabel("Arcade Suite v2.0");
        badge.setFont(new Font("SansSerif", Font.BOLD, 12));
        badge.setForeground(ACCENT_SECONDARY);
        heroHeader.add(badge, BorderLayout.EAST);

        hero.add(heroHeader, BorderLayout.NORTH);
        hero.add(tagline, BorderLayout.CENTER);

        JLabel sub = new JLabel("Select a module below and press Launch to dive in.");
        sub.setFont(BODY_FONT);
        sub.setForeground(TEXT_SUBTLE);
        hero.add(sub, BorderLayout.SOUTH);

        root.add(hero, BorderLayout.NORTH);

    GlassPanel catalog = new GlassPanel(new GridLayout(3, 1, 18, 18), GLASS_TOP, GLASS_BOTTOM, GLASS_BORDER, true);
        catalog.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        catalog.add(createGameCard("Snake Redux", "Test your reflexes with reactive hazards, power-ups, and style combos.", () -> startGame(new SnakeConsole())));
        catalog.add(createGameCard("Flappy Bird Neo", "Surf shifting pillars, chain score boosts, and harness dynamic wind tunnels.", () -> startGame(new FlappyBirdConsole())));
        catalog.add(createGameCard("Space Shooter Hyperdrive", "Dogfight saucers, harvest power cores, and survive adaptive enemy tactics.", () -> startGame(new SpaceShooterConsole())));
        root.add(catalog, BorderLayout.CENTER);

        JLabel tip = new JLabel("Tip: SPACE fires or flaps. Use the control bar to pause, restart, or swap speeds instantly.", SwingConstants.LEFT);
        tip.setFont(BODY_FONT);
        tip.setForeground(TEXT_SUBTLE);

    JButton leaderboardButton = createGhostButton("Leaderboards");
    leaderboardButton.addActionListener(e -> openLeaderboardHub());

    JButton exitButton = createGhostButton("Exit");
    exitButton.addActionListener(e -> System.exit(0));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(tip, BorderLayout.CENTER);
    JPanel exitWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        exitWrapper.setOpaque(false);
    exitWrapper.add(leaderboardButton);
        exitWrapper.add(exitButton);
        footer.add(exitWrapper, BorderLayout.EAST);

        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
        revalidate();
        repaint();
    }

    private JPanel createGameCard(String title, String description, Runnable action) {
    GlassPanel card = new GlassPanel(new BorderLayout(12, 10),
                new Color(62, 74, 126, 235),
                new Color(42, 52, 98, 228),
        new Color(112, 132, 202, 180),
        true);
        card.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel name = new JLabel(title);
        name.setFont(new Font("SansSerif", Font.BOLD, 22));
        name.setForeground(TEXT_PRIMARY);
        header.add(name, BorderLayout.WEST);

        JLabel accent = new JLabel("✶");
        accent.setFont(new Font("SansSerif", Font.BOLD, 18));
        accent.setForeground(ACCENT_SECONDARY);
        header.add(accent, BorderLayout.EAST);

        JLabel desc = new JLabel("<html><body style='width:320px'>" + description + "</body></html>");
        desc.setFont(BODY_FONT);
        desc.setForeground(TEXT_SUBTLE);

        JButton playButton = createPrimaryButton("Launch");
        playButton.addActionListener(e -> action.run());
        JPanel buttonBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonBox.setOpaque(false);
        buttonBox.add(playButton);

        card.add(header, BorderLayout.NORTH);
        card.add(desc, BorderLayout.CENTER);
        card.add(buttonBox, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createControlPanel() {
    GlassPanel panel = new GlassPanel(new BorderLayout(20, 0), CONTROL_TOP, CONTROL_BOTTOM, CONTROL_BORDER, false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 12, 20));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonRow.setOpaque(false);

        pauseButton = createGhostButton("Pause");
        pauseButton.addActionListener(e -> togglePause());

        restartButton = createGhostButton("Restart");
        restartButton.addActionListener(e -> restartCurrentGame());

        menuButton = createPrimaryButton("Main Menu");
        menuButton.addActionListener(e -> returnToMenu());

        buttonRow.add(pauseButton);
        buttonRow.add(restartButton);
        buttonRow.add(menuButton);

        JPanel centerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        centerRow.setOpaque(false);
        JLabel speedLabel = new JLabel("Speed");
        speedLabel.setForeground(TEXT_SUBTLE);
        speedLabel.setFont(BODY_FONT);

        speedSelect = new JComboBox<>(GamingConsole.GameSpeedProfile.values());
        speedSelect.setSelectedItem(activeSpeedProfile);
        speedSelect.setFont(BODY_FONT);
        speedSelect.setForeground(TEXT_PRIMARY);
        speedSelect.setBackground(new Color(30, 38, 68));
        speedSelect.setOpaque(true);
        speedSelect.setFocusable(false);
        speedSelect.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 88, 142), 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        speedSelect.addActionListener(e -> {
            GamingConsole.GameSpeedProfile selected = (GamingConsole.GameSpeedProfile) speedSelect.getSelectedItem();
            if (selected != null) {
                activeSpeedProfile = selected;
                if (currentConsole != null) {
                    currentConsole.setSpeedProfile(activeSpeedProfile);
                }
                updateStatusDisplay();
                SwingUtilities.invokeLater(() -> {
                    if (gamePanel != null) {
                        gamePanel.requestFocusInWindow();
                    }
                });
            }
        });
        centerRow.add(speedLabel);
        centerRow.add(speedSelect);

        statusLabel = new JLabel("Status: Ready");
        statusLabel.setForeground(TEXT_PRIMARY);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        panel.add(buttonRow, BorderLayout.WEST);
        panel.add(centerRow, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.EAST);
        return panel;
    }

    private void startGame(GamingConsole console) {
        stopGameLoop();
        currentConsole = console;
        currentConsole.setSpeedProfile(activeSpeedProfile);
        currentConsole.startTimer();
        gamePanel = new GamePanel(console);
    gamePanel.setPreferredSize(new Dimension(800, 600));

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.BLACK);
        container.add(gamePanel, BorderLayout.CENTER);
        controlPanel = createControlPanel();
    JPanel hudWrapper = new JPanel(new BorderLayout());
    hudWrapper.setBackground(new Color(10, 12, 28));
    hudWrapper.setBorder(BorderFactory.createEmptyBorder(12, 20, 20, 20));
    hudWrapper.add(controlPanel, BorderLayout.CENTER);
    container.add(hudWrapper, BorderLayout.SOUTH);

        if (speedSelect != null) {
            speedSelect.setSelectedItem(activeSpeedProfile);
        }

        setContentPane(container);
        revalidate();
        repaint();

        startGameLoop(console);
        SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
    }

    private void startGameLoop(GamingConsole console) {
        loopRunning = true;
        gameLoopThread = new Thread(() -> {
            final long frameDelay = 16L;
            double updateAccumulator = 0.0;
            while (loopRunning && currentConsole == console) {
                updateAccumulator += console.getTimeScale();

                while (updateAccumulator >= 1.0 && loopRunning && currentConsole == console) {
                    console.update();
                    updateAccumulator -= 1.0;
                    if (console.shouldExitToMenu()) {
                        SwingUtilities.invokeLater(this::returnToMenu);
                        return;
                    }
                }

                gamePanel.repaint();
                updateStatusDisplay();

                if (console.shouldExitToMenu()) {
                    SwingUtilities.invokeLater(this::returnToMenu);
                    break;
                }
                if (console.isGameOver()) {
                    // prompt to save score once
                    SwingUtilities.invokeLater(() -> handlePostGame(console));
                    // stop loop
                    loopRunning = false;
                    break;
                }

                try {
                    Thread.sleep(frameDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "RetroConsole-Loop");
        gameLoopThread.setUncaughtExceptionHandler((thread, throwable) ->
                handleFatalException("Game loop", throwable));
        gameLoopThread.setDaemon(true);
        gameLoopThread.start();
    }

    private void stopGameLoop() {
        loopRunning = false;
        if (gameLoopThread != null) {
            gameLoopThread.interrupt();
            try {
                gameLoopThread.join(200);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            gameLoopThread = null;
        }
    }

    private void togglePause() {
        if (currentConsole == null) return;
        currentConsole.togglePause();
        updateStatusDisplay();
        SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
    }

    private void restartCurrentGame() {
        if (currentConsole == null) return;
        currentConsole.reset();
        currentConsole.setSpeedProfile(activeSpeedProfile);
        currentConsole.startTimer();
        updateStatusDisplay();
        SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
    }

    private void updateStatusDisplay() {
        if (currentConsole == null || statusLabel == null) return;
        SwingUtilities.invokeLater(() -> {
            if (currentConsole == null || statusLabel == null) return;
            String state;
            if (currentConsole.isGameOver()) {
                state = "Game Over";
            } else if (currentConsole.isPaused()) {
                state = "Paused";
            } else {
                state = "Running";
            }
            statusLabel.setText(String.format("Status: %s | Speed: %s", state, activeSpeedProfile));
            if (pauseButton != null) {
                pauseButton.setText(currentConsole.isPaused() ? "Resume" : "Pause");
            }
        });
    }

    private void handlePostGame(GamingConsole console) {
        try {
            int finalScore = console.getScore();
            long elapsed = console.getElapsedMillis();
            String speedLabel = console.getSpeedProfile().toString();
            String gameId = console.getClass().getSimpleName();
            String gameLabel = readableGameName(gameId);

            SaveScoreDialog saveDialog = new SaveScoreDialog(this, gameLabel, speedLabel, finalScore, formatElapsed(elapsed));
            SaveScoreDialog.Result result = saveDialog.showDialogAndGetResult();

            utils.Leaderboard lb = new utils.Leaderboard(gameId, speedLabel);
            if (result.save) {
                String playerName = result.name == null ? "" : result.name.trim();
                if (playerName.isEmpty()) {
                    playerName = "Player";
                }
                try {
                    lb.addEntry(playerName, finalScore, elapsed);
                } catch (java.io.IOException e) {
                    JOptionPane.showMessageDialog(this, "Failed to save score: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            showLeaderboardDialog(gameId, speedLabel);
        } finally {
            returnToMenu();
        }
    }

    private void showLeaderboardDialog(String gameId, String speedLabel) {
        utils.Leaderboard lb = new utils.Leaderboard(gameId, speedLabel);
        java.util.List<utils.Leaderboard.Entry> top;
        try {
            top = lb.readTop(10);
        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to read leaderboard: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        LeaderboardDialog dialog = new LeaderboardDialog(this, readableGameName(gameId), speedLabel, top);
        dialog.setVisible(true);
    }

    private String formatElapsed(long ms) {
        long s = ms / 1000;
        long mins = s / 60;
        long secs = s % 60;
        return String.format("%d:%02d", mins, secs);
    }

    private String readableGameName(String classSimpleName) {
        switch (classSimpleName) {
            case "SnakeConsole": return "Snake Redux";
            case "FlappyBirdConsole": return "Flappy Bird Neo";
            case "SpaceShooterConsole": return "Space Shooter Hyperdrive";
            default: return classSimpleName;
        }
    }

    private void openLeaderboardHub() {
        LeaderboardHubDialog dialog = new LeaderboardHubDialog(this);
        dialog.setVisible(true);
    }

    private void returnToMenu() {
        stopGameLoop();
        currentConsole = null;
        createMenu();
    }

    private JButton createPrimaryButton(String text) {
        Color base = ACCENT_PRIMARY;
        Color hover = new Color(Math.min(255, base.getRed() + 20), Math.min(255, base.getGreen() + 20), Math.min(255, base.getBlue() + 20));
        Color border = base.darker();
        return createStyledButton(text, base, hover, new Color(18, 22, 36), border, 18);
    }

    private JButton createGhostButton(String text) {
        Color base = new Color(44, 54, 92, 235);
        Color hover = new Color(58, 70, 112, 240);
        Color border = new Color(82, 102, 160, 200);
        return createStyledButton(text, base, hover, TEXT_PRIMARY, border, 16);
    }

    private JButton createStyledButton(String text, Color base, Color hover, Color textColor, Color borderColor, int padding) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(textColor);
        button.setBackground(base);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(8, padding, 8, padding)));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(base);
            }
        });
        return button;
    }

    private static void installGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                handleFatalException("Uncaught exception in " + thread.getName(), throwable));
    }

    private static void handleFatalException(String context, Throwable throwable) {
        if (throwable == null) {
            return;
        }

        File logFile = writeCrashLog(context, throwable);
        String message = "Retro Game encountered a fatal error.\n" +
                "Crash details were written to:\n" + (logFile != null ? logFile.getAbsolutePath() : "<console>");

        if (GraphicsEnvironment.isHeadless()) {
            System.err.println(message);
            throwable.printStackTrace();
            System.exit(1);
            return;
        }

        Runnable dialogTask = () -> {
            JOptionPane.showMessageDialog(null, message, "Retro Game Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        };

        if (SwingUtilities.isEventDispatchThread()) {
            dialogTask.run();
        } else {
            SwingUtilities.invokeLater(dialogTask);
        }
    }

    private static File writeCrashLog(String context, Throwable throwable) {
        try {
            File logFile = AppDirectories.getLogFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
                writer.println("[" + LocalDateTime.now().format(LOG_TIMESTAMP) + "] " + context);
                throwable.printStackTrace(writer);
                writer.println();
            }
            return logFile;
        } catch (IOException e) {
            System.err.println("Failed to write crash log: " + e.getMessage());
            throwable.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        installGlobalExceptionHandler();
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) ->
                handleFatalException("Main thread", throwable));

        SwingUtilities.invokeLater(() -> {
            try {
                new RetroConsole();
            } catch (Throwable t) {
                handleFatalException("UI initialization", t);
            }
        });
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
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
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

        GlassPanel(LayoutManager layout, Color top, Color bottom, Color border, boolean translucent) {
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color gradientTop = translucent ? top : new Color(top.getRed(), top.getGreen(), top.getBlue());
            Color gradientBottom = translucent ? bottom : new Color(bottom.getRed(), bottom.getGreen(), bottom.getBlue());
            g2.setPaint(new GradientPaint(0, 0, gradientTop, 0, getHeight(), gradientBottom));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);
            Color outline = translucent ? border : new Color(border.getRed(), border.getGreen(), border.getBlue());
            g2.setColor(outline);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 26, 26);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}