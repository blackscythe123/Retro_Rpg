# Retro Gaming Console

A glassmorphic Java arcade cabinet that bundles three fully featured minigames—Snake, Flappy Bird, and Space Shooter—inside a single Swing application. Each title includes modernized physics, buff stacking, score combos, and a shared pause/restart system.

## Arcade Lineup

### Snake
- **Core loop:** Weave through a neon grid, collect food to grow, and chain combos by eating quickly.
- **Hazards & debuffs:** Toxic crystal obstacles spawn dynamically, the arena walls are lethal, and self-collision ends the run. Missed combos decay over time and streaks reset when crystals despawn off-screen.
- **Buffs & boosts:**

| Power-up | Effect | Stack/Duration |
| --- | --- | --- |
| Double Score Chip | Adds +1 to the score multiplier (up to x5) and extends timer; countdown shown in HUD. | Timers stack additively and decay one at a time. |
| Quantum Shield | Queues a 7-second shield charge that blocks a crystal or body hit; consumes one charge per collision. | Charges are stored in a deque so you can carry multiple safeties. |
| Chrono Berry | Slows the entire board, reduces snake speed, and eases control for tight corridors. | Duration accumulates up to 15 seconds and instantly re-tunes movement speed. |

### Flappy Bird
- **Core loop:** Thread the bird through reactive pillars, chase precision passes, and maintain combo chains for bonus points.
- **Hazards & debuffs:** Moving pillars close gaps, vertical boundaries are lethal, and falling out of rhythm drains combo streaks. Difficulty ramps every 480 frames.
- **Buffs & boosts:**

| Power-up | Effect | Stack/Duration |
| --- | --- | --- |
| Prism Shield | Grants a 7-second barrier that vaporizes the next pillar and logs a “shield save.” | Multiple charges queue; HUD shows remaining time for the foremost shield. |
| Score Multiplier Orb | Adds +1 to the active score multiplier (capped at x5) and refreshes its timer. | Timers add together; multiplier decays only when the clock reaches zero. |
| Time Warp Rune | Slows pillars, power-ups, and gravity, also dampens current velocity for emergency recovery. | Stacks up to 16 seconds while adjusting gravity/jump force each frame. |
- **Combo Frenzy:** Every fifth perfect gap triggers a temporary “frenzy” state with hotter physics and audio feedback.

### Space Shooter
- **Core loop:** Pilot the starfighter across the sector, shred asteroids and enemy saucers, and juggle power-ups to escalate firepower.
- **Threats & debuffs:**
   - **Asteroids** with variable size, armor, and drift.
   - **Enemy saucers** that chase and fire predictive shots.
   - **Enemy projectiles** that punish stationary play.
   - Failing to destroy hazards lowers streaks, reducing passive rewards.
- **Power-ups:**

| Power-up | Effect | Stack/Duration |
| --- | --- | --- |
| Shield Matrix | Adds a 8-second shield charge; absorbs any damage source. | Charges queue and decay sequentially. |
| Triple Shot | Fires a spread of three bolts. | Duration stacks up to 30 seconds. |
| Rapid Fire | Halves fire delay; stacks with overdrive. | Durations accumulate (cap ~30s). |
| Temporal Drag | Slows world speed (enemies, asteroids, drops). | Timer stacks to 20 seconds. |
| Hull Repair | Restores up to 3 hull or, when full, increases max hull by 1. | Instant effect. |
| Heart Core | Big heal scaling with difficulty; drops from elite kills. | Instant effect. |
| Overdrive | Grants rapid fire, speed boost, and empowered bullet visuals. | 6-second base, timers stack. |
| Drone Wing *(new)* | Summons orbiting drones that auto-fire angled shots. | 12-second timer with additive stacking; drones stay active while any time remains. |
| Phase Shift *(new)* | Makes the ship intangible to damage while active. | Duration stacks to 15 seconds; all damage ignored. |
| Nova Burst *(new)* | Detonates a screen-wide blast clearing enemies and shots. | Instant—adds streak rewards and screen flash feedback. |

## Buff System at a Glance
- All games share a **queue-based stacking model**: new pickups add time/charges to a deque rather than overwriting existing buffs.
- HUD overlays show both remaining duration (in seconds) and stack counts.
- Shield mechanics remove the nearest lethal obstacle (Flappy) or consume a charge without damage (Snake & Shooter).
- Slow-time effects rescale object speeds using profile-aware multipliers so difficulty profiles stay consistent across RELAXED/STANDARD/TURBO modes.

## Project Structure

```
src/
├── consoles/
│   ├── GamingConsole.java        # Template method skeleton for every mini-game
│   ├── SnakeConsole.java         # Snake rules, HUD, power-up logic
│   ├── FlappyBirdConsole.java    # Flappy pillars, combos, power-ups
│   └── SpaceShooterConsole.java  # Shooter waves, enemies, buff stacking
├── exceptions/
│   ├── CollisionException.java
│   ├── InvalidGameStateException.java
│   └── GameInitializationException.java
├── gameobjects/
│   ├── GameObject.java           # Base renderable entity
│   ├── MovableObject.java        # Adds velocity & movement helpers
│   ├── ImmovableObject.java      # Static obstacle base
│   ├── Bird.java, Pillar.java, FlappyPowerUp.java
│   ├── Snake.java, Food.java, SnakeObstacle.java, SnakePowerUp.java
│   ├── Spacecraft.java, SpacePowerUp.java, Bullet.java
│   ├── Asteroid.java, EnemySaucer.java, EnemyProjectile.java
│   └── Heart core & helper assets live alongside each game object
├── interfaces/
│   ├── Drawable.java             # Paint contract
│   └── Updatable.java            # Update tick contract
└── utils/
      ├── RetroConsole.java         # Launcher + glassmorphic menu
      ├── GamePanel.java            # Swing panel with timing + input
      └── GameObjectList.java       # Thread-safe CopyOnWriteArrayList wrapper
```

## Build & Run

### Prerequisites
- Java 11 or newer (tested up to 21)
- Windows users can run the included batch scripts; cross-platform builds work via `javac`

### Quick start (Windows PowerShell)

```powershell
cd "c:\Users\sam\OneDrive\one drive back up\OneDrive - SSN-Institute\Documents\projects\java_game"
.\build.bat
java -cp bin utils.RetroConsole
```

### Manual compilation (any platform)

```bash
javac -d bin $(find src -name "*.java")
java -cp bin utils.RetroConsole
```

## Controls

| Game | Movement | Action | System Keys |
| --- | --- | --- | --- |
| Snake | WASD | — | P = pause, R = restart, ESC = menu |
| Flappy Bird | Space / W / Up = flap | — | P, R, ESC |
| Space Shooter | Arrow keys or WASD = strafe | Space = fire | P, R, ESC |

## Additional Documentation
- [Snake Console Deep Dive](docs/snake.md) – movement tuning, combo math, obstacle spawning, and buff queues.
- [Flappy Bird Console Deep Dive](docs/flappy-bird.md) – pillar logic, precision scoring, and shield saves.
- [Space Shooter Console Deep Dive](docs/space-shooter.md) – enemy behaviours, power-up distribution, and HUD wiring.
- [Power-Ups & Buff System](docs/power-ups.md) – cross-game overview of buffs, debuffs, and stacking rules.
- [Object-Oriented Design Overview](docs/oop-overview.md) – inheritance, composition, and design patterns underpinning the project.

## Exception Handling
- `CollisionException` – bubbles up fatal collisions to trigger game over transitions.
- `InvalidGameStateException` – guards against inconsistent console state changes.
- `GameInitializationException` – signals failures during setup.

## Recent Highlights
- Shield, multiplier, slow-time, and drone timers now **stack** seamlessly across all games.
- Flappy shields vaporize the offending pillar instead of bouncing the bird backward.
- Space Shooter adds **drone wing**, **phase shift**, and **nova burst** power-ups alongside the existing arsenal.
- Shared `GameObjectList` leverages `CopyOnWriteArrayList` to avoid concurrent modification when iterating plus spawning.

Ready to add your own mini-game? Start by extending `GamingConsole`, drop new objects into `gameobjects/`, and wire the mode into `RetroConsole`’s menu.