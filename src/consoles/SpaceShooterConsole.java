package consoles;

import exceptions.CollisionException;
import gameobjects.Asteroid;
import gameobjects.Bullet;
import gameobjects.EnemyProjectile;
import gameobjects.EnemySaucer;
import gameobjects.SpacePowerUp;
import gameobjects.Spacecraft;
import utils.GameObjectList;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.Random;

/**
 * Console implementation for Space Shooter game
 */
public class SpaceShooterConsole extends GamingConsole {
    private static final int FIELD_WIDTH = 800;
    private static final int FIELD_HEIGHT = 600;

    private Spacecraft spacecraft;
    private GameObjectList<Asteroid> asteroids;
    private GameObjectList<Bullet> bullets;
    private GameObjectList<EnemyProjectile> enemyProjectiles;
    private GameObjectList<EnemySaucer> saucers;
    private GameObjectList<SpacePowerUp> powerUps;

    private Random random;
    private boolean firing;

    private int asteroidSpawnTimer;
    private int saucerSpawnTimer;
    private int powerUpSpawnTimer;
    private int starfieldTick;
    private int waveTimer;
    private int fireCooldown;
    private int difficultyMeter;
    private int streak;
    private int bestStreak;
    private int comboDecayTimer;
    private int damageTintTimer;
    private int slowTimeTimer;
    private int heartDropCooldown;
    private int droneWingCooldown;
    private int novaFlashTimer;

    private String bannerText = "";
    private int bannerTimer;

    @Override
    protected void initializeGame() {
        random = new Random();
        spacecraft = new Spacecraft(FIELD_WIDTH / 2 - 20, FIELD_HEIGHT - 100);
        asteroids = new GameObjectList<>();
        bullets = new GameObjectList<>();
        enemyProjectiles = new GameObjectList<>();
        saucers = new GameObjectList<>();
        powerUps = new GameObjectList<>();

        firing = false;
        asteroidSpawnTimer = 0;
        saucerSpawnTimer = 90;
        powerUpSpawnTimer = 0;
        starfieldTick = 0;
        waveTimer = (int) Math.round(900 * getSpawnPacingScale());
        fireCooldown = 0;
        difficultyMeter = 0;
        streak = 0;
        bestStreak = 0;
        comboDecayTimer = 0;
        damageTintTimer = 0;
        slowTimeTimer = 0;
        heartDropCooldown = 0;
        droneWingCooldown = 0;
        novaFlashTimer = 0;
        bannerText = "";
        bannerTimer = 0;

        gameObjects.add(spacecraft);
        spacecraft.setSpeedScale(getProfileMovementScale());
    }

    @Override
    public void handleKeyPress(int keyCode) {
        if (handleCommonKeyPress(keyCode)) return;

        switch (keyCode) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                spacecraft.moveLeft();
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                spacecraft.moveRight();
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                spacecraft.moveUp();
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                spacecraft.moveDown();
                break;
            case KeyEvent.VK_SPACE:
                firing = true;
                break;
        }
    }

    @Override
    public void handleKeyRelease(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_A:
            case KeyEvent.VK_D:
                spacecraft.stopHorizontalMoving();
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_W:
            case KeyEvent.VK_S:
                spacecraft.stopVerticalMoving();
                break;
            case KeyEvent.VK_SPACE:
                firing = false;
                break;
        }
    }

    @Override
    public void handleMousePressed(int button, int x, int y) {
        if (button == MouseEvent.BUTTON1) {
            firing = true;
        }
    }

    @Override
    public void handleMouseReleased(int button, int x, int y) {
        if (button == MouseEvent.BUTTON1) {
            firing = false;
        }
    }

    @Override
    public void update() {
        if (gameOver || paused) return;

        starfieldTick++;
        if (fireCooldown > 0) fireCooldown--;
        if (bannerTimer > 0) bannerTimer--;
        if (damageTintTimer > 0) damageTintTimer--;
        if (slowTimeTimer > 0) slowTimeTimer--;
        if (heartDropCooldown > 0) heartDropCooldown--;
    if (novaFlashTimer > 0) novaFlashTimer--;
        if (comboDecayTimer > 0) {
            comboDecayTimer--;
        } else if (streak > 0) {
            streak = Math.max(0, streak - 1);
            comboDecayTimer = streak > 0 ? 90 : 0;
        }

        if (waveTimer > 0 && --waveTimer == 0) {
            difficultyMeter = Math.min(12, difficultyMeter + 1);
            waveTimer = (int) Math.round(Math.max(420, 900 - difficultyMeter * 40) * getSpawnPacingScale());
            announce("Sector intensity +" + difficultyMeter, 110);
        }

        if (slowTimeTimer > 0) {
            if (asteroidSpawnTimer % 2 == 0) {
                asteroidSpawnTimer++;
                saucerSpawnTimer++;
                powerUpSpawnTimer++;
            }
        }

        if (firing) {
            tryShoot();
        }

        handleDroneWingSupport();

        syncObjectSpeeds();

        super.update();
    }

    private void tryShoot() {
        if (fireCooldown > 0) return;

        int fireDelay = spacecraft.getFireDelay();
        fireCooldown = fireDelay;

        int centerX = spacecraft.getX() + spacecraft.getWidth() / 2;
        int noseY = spacecraft.getY() - 8;

        Bullet main = new Bullet(centerX - 3, noseY, 0, -14, spacecraft.isOverdriveActive() ? 3 : 2);
        main.applySpeedMultiplier(getProjectileSpeedScale());
        if (spacecraft.isOverdriveActive()) {
            main.setColors(new Color(255, 240, 180), new Color(255, 90, 90));
        }
        bullets.add(main);
        gameObjects.add(main);

        if (spacecraft.hasTripleShot()) {
            Bullet left = new Bullet(centerX - 14, noseY + 4, -2, -13, 2);
            Bullet right = new Bullet(centerX + 8, noseY + 4, 2, -13, 2);
            left.applySpeedMultiplier(getProjectileSpeedScale());
            right.applySpeedMultiplier(getProjectileSpeedScale());
            bullets.add(left);
            bullets.add(right);
            gameObjects.add(left);
            gameObjects.add(right);
        }
    }

    private void syncObjectSpeeds() {
        double slowFactor = slowTimeTimer > 0 ? 0.55 : 1.0;
        double profileScale = getProfileMovementScale();
        spacecraft.setSpeedScale(profileScale);
        double asteroidMultiplier = slowFactor * profileScale;
        for (Asteroid asteroid : asteroids) {
            asteroid.applySpeedMultiplier(asteroidMultiplier);
        }
        double saucerBase = Math.max(0.35, profileScale);
        double saucerBoost = 1.0 + difficultyMeter * 0.05 + Math.min(0.4, streak * 0.02);
        for (EnemySaucer saucer : saucers) {
            saucer.applySpeedMultiplier(Math.max(0.35, slowFactor * saucerBase * saucerBoost));
        }
        double projectileMultiplier = slowFactor * profileScale;
        for (EnemyProjectile projectile : enemyProjectiles) {
            projectile.applySpeedMultiplier(projectileMultiplier);
        }
        for (SpacePowerUp powerUp : powerUps) {
            powerUp.applySpeedModifier(projectileMultiplier);
        }
    }

    @Override
    protected void spawnObjects() {
        asteroidSpawnTimer++;
        saucerSpawnTimer++;
        powerUpSpawnTimer++;

        int asteroidIntervalBase = Math.max(24, 60 - difficultyMeter * 4 - score / 50);
        int asteroidInterval = (int) Math.round(asteroidIntervalBase * getSpawnPacingScale());
        asteroidInterval = Math.max(30, asteroidInterval);
        if (asteroidSpawnTimer >= asteroidInterval) {
            spawnAsteroid();
            asteroidSpawnTimer = 0;
        }

        int saucerIntervalBase = Math.max(160, 360 - difficultyMeter * 24 - score / 30);
        int saucerInterval = (int) Math.round(saucerIntervalBase * getSpawnPacingScale());
        saucerInterval = Math.max(180, saucerInterval);
        if (saucerSpawnTimer >= saucerInterval) {
            spawnSaucer();
            saucerSpawnTimer = 0;
        }

        int powerUpInterval = (int) Math.round((360 + random.nextInt(240)) * Math.max(0.9, getSpawnPacingScale()));
        if (powerUpSpawnTimer >= powerUpInterval) {
            spawnPowerUp();
            powerUpSpawnTimer = 0;
        }

        for (EnemySaucer saucer : saucers) {
            if (saucer.readyToFire()) {
                fireEnemyShot(saucer);
            }
        }
    }

    private void spawnAsteroid() {
        int size = random.nextInt(3);
        int width = size == 0 ? 26 : size == 1 ? 36 : 48;
        int health = size + 1;
        int x = random.nextInt(FIELD_WIDTH - width - 40) + 20;
        int velocityX = random.nextInt(5) - 2;
        int velocityY = 2 + size + random.nextInt(2 + difficultyMeter / 3);

        Asteroid asteroid = new Asteroid(x, -width, velocityX, velocityY, width, health);
        asteroids.add(asteroid);
        gameObjects.add(asteroid);
        asteroid.applySpeedMultiplier(getProfileMovementScale() * (slowTimeTimer > 0 ? 0.55 : 1.0));
    }

    private void spawnSaucer() {
        EnemySaucer.SpawnVector origin;
        double roll = random.nextDouble();
        if (roll < 0.45) {
            origin = EnemySaucer.SpawnVector.LEFT;
        } else if (roll < 0.9) {
            origin = EnemySaucer.SpawnVector.RIGHT;
        } else {
            origin = EnemySaucer.SpawnVector.TOP;
        }

        int x;
        int y;
        switch (origin) {
            case LEFT:
                x = -60;
                y = 110 + random.nextInt(260);
                break;
            case RIGHT:
                x = FIELD_WIDTH + 20;
                y = 110 + random.nextInt(260);
                break;
            case TOP:
            default:
                x = 80 + random.nextInt(FIELD_WIDTH - 160);
                y = -60;
                break;
        }

        EnemySaucer saucer = new EnemySaucer(x, y, origin, spacecraft);
        saucers.add(saucer);
        gameObjects.add(saucer);
        double initialMultiplier = getProfileMovementScale() * (slowTimeTimer > 0 ? 0.55 : 1.0);
        saucer.applySpeedMultiplier(Math.max(0.35, initialMultiplier));
    }

    private void spawnPowerUp() {
        SpacePowerUp.Type type;
        float roll = random.nextFloat();
        if (roll < 0.18f) {
            type = SpacePowerUp.Type.SHIELD;
        } else if (roll < 0.34f) {
            type = SpacePowerUp.Type.TRIPLE_SHOT;
        } else if (roll < 0.48f) {
            type = SpacePowerUp.Type.RAPID_FIRE;
        } else if (roll < 0.62f) {
            type = SpacePowerUp.Type.TIME_SLOW;
        } else if (roll < 0.76f) {
            type = SpacePowerUp.Type.HULL_REPAIR;
        } else if (roll < 0.86f) {
            type = SpacePowerUp.Type.HEART_CORE;
        } else if (roll < 0.93f) {
            type = SpacePowerUp.Type.OVERDRIVE;
        } else if (roll < 0.975f) {
            type = SpacePowerUp.Type.DRONE_WING;
        } else if (roll < 0.99f) {
            type = SpacePowerUp.Type.PHASE_SHIFT;
        } else {
            type = SpacePowerUp.Type.NOVA_BURST;
        }

        int y = 140 + random.nextInt(260);
        SpacePowerUp powerUp = new SpacePowerUp(FIELD_WIDTH + 40, y, type, 2 + difficultyMeter / 4);
        powerUps.add(powerUp);
        gameObjects.add(powerUp);
        powerUp.applySpeedModifier(getProfileMovementScale() * (slowTimeTimer > 0 ? 0.55 : 1.0));
    }

    private void fireEnemyShot(EnemySaucer saucer) {
        double originX = saucer.getFireX() - 3;
        double originY = saucer.getFireY();
        double targetX = spacecraft.getX() + spacecraft.getWidth() / 2.0;
        double targetY = spacecraft.getY() + spacecraft.getHeight() / 2.0;

        double dx = targetX - originX;
        double dy = targetY - originY;
        double distance = Math.hypot(dx, dy);
        if (distance < 1.0) distance = 1.0;
        double baseSpeed = 5.6 + difficultyMeter * 0.4 + Math.min(2.5, streak * 0.08);
        double slowFactor = slowTimeTimer > 0 ? 0.85 : 1.0;
        baseSpeed *= slowFactor;

        int velocityX = (int) Math.round(dx / distance * baseSpeed);
        int velocityY = (int) Math.round(dy / distance * baseSpeed);
        if (saucer.getOrigin() == EnemySaucer.SpawnVector.TOP && velocityY < 3) velocityY = 3;
        if (velocityY < 2) velocityY = 2;
        if (velocityX == 0) {
            velocityX = dx > 0 ? 1 : -1;
        }

        EnemyProjectile projectile = new EnemyProjectile((int) originX, (int) originY, velocityX, velocityY, 2);
        enemyProjectiles.add(projectile);
        gameObjects.add(projectile);
        projectile.applySpeedMultiplier(getProfileMovementScale() * (slowTimeTimer > 0 ? 0.55 : 1.0));

        int cooldown = (int) Math.max(52, 140 - difficultyMeter * 8 - Math.min(12, streak * 0.6));
        saucer.resetFireCooldown(cooldown);
    }

    @Override
    protected void checkCollisions() throws CollisionException {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            for (int j = asteroids.size() - 1; j >= 0; j--) {
                Asteroid asteroid = asteroids.get(j);
                if (bullet.collidesWith(asteroid)) {
                    asteroid.damage(bullet.getDamage());
                    gameObjects.remove(bullet);
                    bullets.remove(bullet);
                    if (asteroid.isDestroyed()) {
                        rewardKill(asteroid.getScoreValue(), 1);
                        gameObjects.remove(asteroid);
                        asteroids.remove(asteroid);
                    }
                    break;
                }
            }

            for (int j = saucers.size() - 1; j >= 0; j--) {
                EnemySaucer saucer = saucers.get(j);
                if (bullet.collidesWith(saucer)) {
                    saucer.damage(bullet.getDamage());
                    gameObjects.remove(bullet);
                    bullets.remove(bullet);
                    if (saucer.isDestroyed()) {
                        int centerX = saucer.getX() + saucer.getWidth() / 2;
                        int centerY = saucer.getY() + saucer.getHeight() / 2;
                        rewardKill(saucer.getScoreValue(), 2);
                        gameObjects.remove(saucer);
                        saucers.remove(saucer);
                        maybeSpawnHeart(centerX, centerY);
                    }
                    break;
                }
            }
        }

        for (int i = enemyProjectiles.size() - 1; i >= 0; i--) {
            EnemyProjectile projectile = enemyProjectiles.get(i);
            if (spacecraft.collidesWith(projectile)) {
                if (spacecraft.isPhaseShiftActive()) {
                    continue;
                }
                boolean shielded = spacecraft.hasShield();
                boolean destroyed = spacecraft.applyDamage(1);
                if (destroyed) {
                    throw new CollisionException("Hit by enemy fire!");
                }
                if (!shielded) {
                    damageTintTimer = 50;
                    streak = Math.max(0, streak - 2);
                }
                gameObjects.remove(projectile);
                enemyProjectiles.remove(projectile);
            }
        }

        for (int i = asteroids.size() - 1; i >= 0; i--) {
            Asteroid asteroid = asteroids.get(i);
            if (spacecraft.collidesWith(asteroid)) {
                if (spacecraft.isPhaseShiftActive()) {
                    continue;
                }
                boolean shielded = spacecraft.hasShield();
                boolean destroyed = spacecraft.applyDamage(2);
                if (destroyed) {
                    throw new CollisionException("Spacecraft hit an asteroid!");
                }
                if (!shielded) {
                    damageTintTimer = 60;
                    streak = Math.max(0, streak - 3);
                }
                asteroid.damage(3);
                if (asteroid.isDestroyed()) {
                    rewardKill(asteroid.getScoreValue(), 1);
                    gameObjects.remove(asteroid);
                    asteroids.remove(asteroid);
                }
            }
        }

        for (int i = saucers.size() - 1; i >= 0; i--) {
            EnemySaucer saucer = saucers.get(i);
            if (spacecraft.collidesWith(saucer)) {
                if (spacecraft.isPhaseShiftActive()) {
                    continue;
                }
                boolean shielded = spacecraft.hasShield();
                boolean destroyed = spacecraft.applyDamage(2);
                if (destroyed) {
                    throw new CollisionException("Collided with saucer!");
                }
                if (!shielded) {
                    damageTintTimer = 60;
                    streak = Math.max(0, streak - 3);
                }
                gameObjects.remove(saucer);
                saucers.remove(saucer);
                maybeSpawnHeart(saucer.getX() + saucer.getWidth() / 2, saucer.getY() + saucer.getHeight() / 2);
            }
        }

        for (int i = powerUps.size() - 1; i >= 0; i--) {
            SpacePowerUp powerUp = powerUps.get(i);
            if (spacecraft.collidesWith(powerUp)) {
                SpacePowerUp.Type type = powerUp.getType();
                applyPowerUp(type);
                if (type != SpacePowerUp.Type.HULL_REPAIR && type != SpacePowerUp.Type.NOVA_BURST) {
                    announce(type.name().replace('_', ' ') + " ready!", 110);
                }
                gameObjects.remove(powerUp);
                powerUps.remove(powerUp);
            }
        }
    }

    private void rewardKill(int value, int comboWeight) {
        score += value + streak * 2;
        streak += comboWeight;
        bestStreak = Math.max(bestStreak, streak);
        comboDecayTimer = 120;
        if (streak > 0 && streak % 15 == 0) {
            spacecraft.heal(1);
            announce("Nanites restored hull integrity!", 90);
        } else if (streak > 0 && streak % 10 == 0) {
            spacecraft.activateShield(240);
            announce("Defense bonus!", 90);
        } else if (streak > 0 && streak % 6 == 0) {
            spacecraft.enableSpeedBoost(220);
            announce("Afterburners engaged!", 80);
        }
    }

    private void maybeSpawnHeart(int centerX, int centerY) {
        double baseChance = 0.18 + difficultyMeter * 0.02 + Math.min(0.15, streak * 0.01);
        if (heartDropCooldown > 0) {
            baseChance *= 0.4;
        }
        if (random.nextDouble() <= baseChance) {
            spawnHeartPowerUp(centerX - 14, centerY - 14);
            heartDropCooldown = 180;
        }
    }

    private void spawnHeartPowerUp(int x, int y) {
        int spawnX = Math.max(32, Math.min(x, FIELD_WIDTH - 60));
        int spawnY = Math.max(40, Math.min(y, FIELD_HEIGHT - 140));
        SpacePowerUp heart = new SpacePowerUp(spawnX, spawnY, SpacePowerUp.Type.HEART_CORE, 2 + difficultyMeter / 5);
        powerUps.add(heart);
        gameObjects.add(heart);
        heart.applySpeedModifier(getProfileMovementScale() * (slowTimeTimer > 0 ? 0.55 : 1.0));
        announce("Heart core recovered!", 90);
    }

    private void applyPowerUp(SpacePowerUp.Type type) {
        switch (type) {
            case SHIELD:
                spacecraft.activateShield(480);
                int charges = spacecraft.getShieldCharges();
                double shieldSeconds = spacecraft.getPrimaryShieldSeconds();
                announce(charges > 1
                        ? String.format(Locale.US, "Shield stacked x%d (%.1fs)", charges, shieldSeconds)
                        : String.format(Locale.US, "Shield ready (%.1fs)", shieldSeconds), 100);
                break;
            case TRIPLE_SHOT:
                spacecraft.enableTripleShot(600);
                announce(String.format(Locale.US, "Triple shot %.1fs", spacecraft.getTripleShotSeconds()), 90);
                break;
            case RAPID_FIRE:
                spacecraft.enableRapidFire(520);
                announce(String.format(Locale.US, "Rapid fire %.1fs", spacecraft.getRapidFireSeconds()), 90);
                break;
            case HULL_REPAIR:
                int missing = spacecraft.getMaxHealth() - spacecraft.getHealth();
                if (missing <= 0) {
                    spacecraft.boostMaxHealth(1);
                    spacecraft.heal(1);
                    announce("Hull plating reinforced!", 110);
                } else {
                    spacecraft.heal(Math.min(3, missing));
                    announce("Hull patched!", 90);
                }
                break;
            case TIME_SLOW:
                slowTimeTimer = Math.min(1200, slowTimeTimer + 360);
                announce(String.format(Locale.US, "Temporal drag %.1fs", slowTimeTimer / 60.0), 90);
                break;
            case HEART_CORE:
                spacecraft.heal(Math.min(4, 2 + difficultyMeter / 2));
                announce("Hull surge from heart core!", 90);
                break;
            case DRONE_WING:
                spacecraft.enableDroneWing(720);
                droneWingCooldown = 0;
                announce(String.format(Locale.US, "Wing drones %.1fs", spacecraft.getDroneWingSeconds()), 90);
                break;
            case PHASE_SHIFT:
                spacecraft.enablePhaseShift(360);
                announce(String.format(Locale.US, "Phase shift %.1fs", spacecraft.getPhaseShiftSeconds()), 90);
                break;
            case NOVA_BURST:
                triggerNovaBurst();
                break;
            case OVERDRIVE:
            default:
                spacecraft.enterOverdrive(360);
                spacecraft.enableRapidFire(360);
                spacecraft.enableSpeedBoost(360);
                announce(String.format(Locale.US, "Overdrive %.1fs", spacecraft.getOverdriveSeconds()), 100);
                break;
        }
    }

    @Override
    protected void onSpeedProfileChanged(GameSpeedProfile newProfile) {
        spacecraft.setSpeedScale(getProfileMovementScale());
        rescalePlayerProjectiles();
        syncObjectSpeeds();
    }

    private void rescalePlayerProjectiles() {
        double scale = getProjectileSpeedScale();
        for (Bullet bullet : bullets) {
            bullet.applySpeedMultiplier(scale);
        }
    }

    private double getProfileMovementScale() {
        switch (getSpeedProfile()) {
            case RELAXED:
                return 0.7;
            case TURBO:
                return 1.25;
            case STANDARD:
            default:
                return 1.0;
        }
    }

    private double getProjectileSpeedScale() {
        switch (getSpeedProfile()) {
            case RELAXED:
                return 0.75;
            case TURBO:
                return 1.15;
            case STANDARD:
            default:
                return 1.0;
        }
    }

    private double getSpawnPacingScale() {
        switch (getSpeedProfile()) {
            case RELAXED:
                return 1.25;
            case TURBO:
                return 0.9;
            case STANDARD:
            default:
                return 1.0;
        }
    }

    @Override
    protected void cleanupObjects() {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            if (bullet.getY() + bullet.getHeight() < -20 || bullet.isExpired()) {
                gameObjects.remove(bullet);
                bullets.remove(bullet);
            }
        }

        for (int i = asteroids.size() - 1; i >= 0; i--) {
            Asteroid asteroid = asteroids.get(i);
            if (asteroid.getY() > FIELD_HEIGHT + 60) {
                gameObjects.remove(asteroid);
                asteroids.remove(asteroid);
                streak = 0;
            }
        }

        for (int i = enemyProjectiles.size() - 1; i >= 0; i--) {
            EnemyProjectile projectile = enemyProjectiles.get(i);
            if (projectile.getY() > FIELD_HEIGHT + 20 || projectile.isExpired()) {
                gameObjects.remove(projectile);
                enemyProjectiles.remove(projectile);
            }
        }

        for (int i = saucers.size() - 1; i >= 0; i--) {
            EnemySaucer saucer = saucers.get(i);
            if (saucer.isOutOfBounds()) {
                gameObjects.remove(saucer);
                saucers.remove(saucer);
            }
        }

        for (int i = powerUps.size() - 1; i >= 0; i--) {
            SpacePowerUp powerUp = powerUps.get(i);
            if (powerUp.getX() + powerUp.getWidth() < -40) {
                gameObjects.remove(powerUp);
                powerUps.remove(powerUp);
            }
        }
    }

    @Override
    protected void drawUI(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Consolas", Font.BOLD, 26));
        g2d.drawString(String.format("SCORE %06d", score), 24, 40);

        g2d.setFont(new Font("Consolas", Font.PLAIN, 16));
        g2d.drawString(String.format("STREAK %02d (BEST %02d)", streak, bestStreak), 24, 64);

        int hullBarWidth = 220;
        int hullBarHeight = 18;
        int hullX = FIELD_WIDTH - hullBarWidth - 36;
        int hullY = 50;

        g2d.setColor(new Color(22, 32, 56, 190));
        g2d.fillRoundRect(hullX, hullY - 30, hullBarWidth, hullBarHeight + 24, 16, 16);

        double hullRatio = spacecraft.getHealth() / (double) spacecraft.getMaxHealth();
        int fillWidth = (int) Math.round((hullBarWidth - 22) * hullRatio);
        fillWidth = Math.max(0, Math.min(hullBarWidth - 22, fillWidth));

        g2d.setColor(new Color(120, 230, 140));
        g2d.fillRoundRect(hullX + 11, hullY - 20, fillWidth, hullBarHeight, 12, 12);

        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.drawRoundRect(hullX + 11, hullY - 20, hullBarWidth - 22, hullBarHeight, 12, 12);

        g2d.setFont(new Font("Consolas", Font.BOLD, 14));
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.format("HULL %d / %d", spacecraft.getHealth(), spacecraft.getMaxHealth()), hullX + 20, hullY - 2);

        g2d.setColor(new Color(80, 130, 100, 140));
        int segments = spacecraft.getMaxHealth();
        if (segments > 1) {
            int segmentWidth = (hullBarWidth - 22) / segments;
            for (int s = 1; s < segments; s++) {
                int markerX = hullX + 11 + s * segmentWidth;
                g2d.drawLine(markerX, hullY - 20, markerX, hullY - 2);
            }
        }

        int statusY = 88;
        g2d.setFont(new Font("Arial", Font.PLAIN, 15));
        if (spacecraft.hasShield()) {
            g2d.setColor(new Color(120, 210, 255));
            g2d.drawString(String.format(Locale.US, "Shield x%d (%.1fs)", spacecraft.getShieldCharges(), spacecraft.getPrimaryShieldSeconds()), 24, statusY);
            statusY += 18;
        }
        if (spacecraft.hasTripleShot()) {
            g2d.setColor(new Color(255, 215, 120));
            g2d.drawString(String.format(Locale.US, "Triple shot %.1fs", spacecraft.getTripleShotSeconds()), 24, statusY);
            statusY += 18;
        }
        if (spacecraft.hasRapidFire()) {
            g2d.setColor(new Color(255, 150, 150));
            g2d.drawString(String.format(Locale.US, "Rapid fire %.1fs", spacecraft.getRapidFireSeconds()), 24, statusY);
            statusY += 18;
        }
        if (spacecraft.isOverdriveActive()) {
            g2d.setColor(new Color(255, 120, 200));
            g2d.drawString(String.format(Locale.US, "Overdrive %.1fs", spacecraft.getOverdriveSeconds()), 24, statusY);
            statusY += 18;
        }
        if (spacecraft.getSpeedBoostSeconds() > 0) {
            g2d.setColor(new Color(140, 220, 255));
            g2d.drawString(String.format(Locale.US, "Thrusters %.1fs", spacecraft.getSpeedBoostSeconds()), 24, statusY);
            statusY += 18;
        }
        if (spacecraft.hasDroneWing()) {
            g2d.setColor(new Color(110, 250, 210));
            g2d.drawString(String.format(Locale.US, "Wing drones %.1fs", spacecraft.getDroneWingSeconds()), 24, statusY);
            statusY += 18;
        }
        if (spacecraft.isPhaseShiftActive()) {
            g2d.setColor(new Color(190, 205, 255));
            g2d.drawString(String.format(Locale.US, "Phase shift %.1fs", spacecraft.getPhaseShiftSeconds()), 24, statusY);
            statusY += 18;
        }
        if (slowTimeTimer > 0) {
            g2d.setColor(new Color(170, 160, 255));
            g2d.drawString(String.format(Locale.US, "Time warp %.1fs", slowTimeTimer / 60.0), 24, statusY);
            statusY += 18;
        }

        g2d.setColor(new Color(220, 220, 220));
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
    g2d.drawString("MOVE: Arrow Keys / WASD | FIRE: SPACE or LEFT CLICK | P: Pause | R: Restart | ESC: Exit", 20, FIELD_HEIGHT - 20);

        if (bannerTimer > 0 && bannerText != null && !bannerText.isEmpty()) {
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(bannerText);
            int x = (FIELD_WIDTH - textWidth) / 2;
            int y = 80;
            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRoundRect(x - 16, y - 28, textWidth + 32, 44, 18, 18);
            g2d.setColor(Color.WHITE);
            g2d.drawString(bannerText, x, y);
        }

        if (gameOver) {
            g2d.setColor(new Color(255, 120, 120));
            g2d.setFont(new Font("Arial", Font.BOLD, 42));
            g2d.drawString("MISSION FAILED", 210, 250);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("Score: " + score, 324, 288);
            g2d.drawString("Best Streak: " + bestStreak, 314, 316);
            g2d.drawString("Press R to restart", 296, 346);
            g2d.drawString("ESC to exit to menu", 282, 374);
        }
    }

    @Override
    protected void drawBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint sky = new GradientPaint(0, 0, new Color(8, 10, 30), 0, FIELD_HEIGHT, new Color(20, 30, 70));
        g2d.setPaint(sky);
        g2d.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);

        drawStars(g2d);
        drawNebula(g2d);
        drawParallaxGrid(g2d);

        if (damageTintTimer > 0) {
            int alpha = Math.min(120, damageTintTimer * 4);
            g2d.setColor(new Color(255, 80, 80, alpha));
            g2d.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        }

        if (novaFlashTimer > 0) {
            int alpha = Math.min(180, novaFlashTimer * 8);
            g2d.setColor(new Color(255, 245, 200, alpha));
            g2d.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        }

        g2d.dispose();
    }

    private void drawStars(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 255, 180));
        for (int i = 0; i < 60; i++) {
            int x = (i * 127 + starfieldTick * 3) % FIELD_WIDTH;
            int y = (i * 83) % FIELD_HEIGHT;
            g2d.fillRect(x, y, 2, 2);
        }

        g2d.setColor(new Color(255, 255, 255, 110));
        for (int i = 0; i < 40; i++) {
            int x = (i * 211 + starfieldTick * 2) % FIELD_WIDTH;
            int y = (i * 167) % FIELD_HEIGHT;
            g2d.fillRect(x, y, 1, 1);
        }
    }

    private void drawNebula(Graphics2D g2d) {
        g2d.setColor(new Color(80, 30, 120, 90));
        for (int i = 0; i < 5; i++) {
            int x = (i * 240 + starfieldTick) % FIELD_WIDTH;
            g2d.fillOval(x - 120, 120 + i * 60, 220, 140);
        }

        g2d.setColor(new Color(40, 120, 160, 60));
        for (int i = 0; i < 4; i++) {
            int x = (i * 260 - starfieldTick * 2) % FIELD_WIDTH;
            g2d.fillOval(x - 130, 240 + i * 50, 240, 150);
        }
    }

    private void drawParallaxGrid(Graphics2D g2d) {
        g2d.setColor(new Color(40, 70, 120, 60));
        int offset = starfieldTick % 40;
        for (int x = -offset; x < FIELD_WIDTH; x += 40) {
            g2d.drawLine(x, FIELD_HEIGHT - 200, x + 40, FIELD_HEIGHT);
        }
        for (int y = FIELD_HEIGHT - 200; y <= FIELD_HEIGHT; y += 40) {
            g2d.drawLine(0, y, FIELD_WIDTH, y);
        }
    }

    private void announce(String message, int durationFrames) {
        bannerText = message;
        bannerTimer = durationFrames;
    }

    private void handleDroneWingSupport() {
        if (gameOver || !spacecraft.hasDroneWing()) {
            if (!spacecraft.hasDroneWing()) {
                droneWingCooldown = 0;
            }
            return;
        }
        if (droneWingCooldown > 0) {
            droneWingCooldown--;
            return;
        }
        spawnDroneWingShot(-18, -3);
        spawnDroneWingShot(18, 3);
        int baseDelay = spacecraft.isOverdriveActive() ? 10 : 16;
        if (spacecraft.hasRapidFire()) {
            baseDelay = Math.max(8, baseDelay - 2);
        }
        droneWingCooldown = baseDelay;
    }

    private void spawnDroneWingShot(int offsetX, int velocityX) {
        int spawnX = spacecraft.getX() + spacecraft.getWidth() / 2 + offsetX;
        int spawnY = spacecraft.getY() + 2;
        Bullet support = new Bullet(spawnX, spawnY, velocityX, -12, 2);
        support.setColors(new Color(120, 255, 220), new Color(40, 190, 170));
        support.applySpeedMultiplier(getProjectileSpeedScale());
        bullets.add(support);
        gameObjects.add(support);
    }

    private void triggerNovaBurst() {
        int destroyed = 0;
        for (int i = asteroids.size() - 1; i >= 0; i--) {
            Asteroid asteroid = asteroids.get(i);
            rewardKill(asteroid.getScoreValue(), 1);
            gameObjects.remove(asteroid);
            asteroids.remove(asteroid);
            destroyed++;
        }
        for (int i = saucers.size() - 1; i >= 0; i--) {
            EnemySaucer saucer = saucers.get(i);
            rewardKill(saucer.getScoreValue(), 2);
            int centerX = saucer.getX() + saucer.getWidth() / 2;
            int centerY = saucer.getY() + saucer.getHeight() / 2;
            gameObjects.remove(saucer);
            saucers.remove(saucer);
            maybeSpawnHeart(centerX, centerY);
            destroyed++;
        }
        for (int i = enemyProjectiles.size() - 1; i >= 0; i--) {
            EnemyProjectile projectile = enemyProjectiles.get(i);
            gameObjects.remove(projectile);
            enemyProjectiles.remove(projectile);
        }
        if (destroyed > 0) {
            announce(String.format(Locale.US, "Nova cleared %d threats!", destroyed), 100);
        } else {
            announce("Nova burst discharged!", 80);
        }
        novaFlashTimer = 24;
        damageTintTimer = 0;
    }
}