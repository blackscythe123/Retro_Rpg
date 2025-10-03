# Power-Ups & Buff System

All three games share the same design philosophy for buffs: abilities stack instead of replacing each other, timers are frame-based, and HUD labels show exact durations. This document catalogues every buff, debuff, and helper, describing where they live in the code and how they interact.

## Shared Design Rules

- **Deque timers:** Shield-like effects store remaining frames in a `Deque<Integer>`. The front item decrements each frame; when it hits zero, it is removed and the next charge activates automatically.
- **Additive timers:** Duration-based buffs (multipliers, slow time, rapid fire, etc.) increase their timers instead of resetting, allowing back-to-back pickups to extend effects.
- **Frame-to-second conversion:** HUD strings divide frames by 60 to display seconds, keeping UI accurate regardless of frame rate.
- **Profile aware:** `GamingConsole.GameSpeedProfile` scaling feeds into each console’s speed calculations, ensuring buffs feel consistent across RELAXED, STANDARD, and TURBO profiles.

## Snake Buffs (`SnakeConsole` + `SnakePowerUp`)

| Name | Enum | Source Code | Effect | Notes |
| --- | --- | --- | --- | --- |
| Double Score Chip | `SnakePowerUp.PowerUpType.DOUBLE_SCORE` | `SnakeConsole.applyPowerUp()` | Increments `scoreMultiplier` (cap x5) and extends `multiplierTimer` by 360 frames. | Timer capped at 1,200 frames (~20s). |
| Quantum Shield | `SnakePowerUp.PowerUpType.SHIELD` | `Snake.activateShield()` | Adds 420-frame shield charge; saves on obstacle or self collision and deletes offending obstacle. | Shield queue drawn in HUD with count and primary time. |
| Chrono Berry | `SnakePowerUp.PowerUpType.SLOW_TIME` | `SnakeConsole.applyPowerUp()` | Adds 260 frames of slow-time; recomputes snake speed via `updateActiveSpeed()`. | Cap 900 frames; ensures minimum speed of 4 px/tick. |

## Flappy Bird Buffs (`FlappyBirdConsole` + `FlappyPowerUp`)

| Name | Enum | Effect | Implementation Details |
| --- | --- | --- | --- |
| Prism Shield | `FlappyPowerUp.Type.SHIELD` | Adds 420-frame shield charge to `Bird`; removes collided pillar and logs “SHIELD_SAVE”. | `Bird.activateShield()`, `FlappyBirdConsole.checkCollisions()`.
| Score Multiplier Orb | `FlappyPowerUp.Type.DOUBLE_SCORE` | Adds +1 multiplier (cap x5) and extends timer by 600 frames. | Timer capped at 1,200 frames; multiplier fades with banner alert.
| Time Warp Rune | `FlappyPowerUp.Type.SLOW_TIME` | Adds 320 frames of slow motion; slows pillars and power-ups, dampens velocity. | `FlappyBirdConsole.applyPowerUp()` recalibrates gravity and jump force.
| Frenzy Boost | Combo reward | 240-frame period of heightened gravity/jump speed triggered every 5 perfect passes. | Not queued but tracked via `frenzyTimer`; interacts with Time Warp for layered feel.

## Space Shooter Buffs (`SpaceShooterConsole` + `SpacePowerUp`)

| Name | Enum | Effect | Additional Behaviour |
| --- | --- | --- | --- |
| Shield Matrix | `SpacePowerUp.Type.SHIELD` | Adds 480-frame shield charge; blocks next damage source. | Shields queue; HUD displays count + primary duration. |
| Triple Shot | `SpacePowerUp.Type.TRIPLE_SHOT` | Adds 600 frames of triple-bolt firing. | Works with Overdrive and Rapid Fire simultaneously. |
| Rapid Fire | `SpacePowerUp.Type.RAPID_FIRE` | Adds 520 frames of shorter fire delay. | Influences drone wing cooldown as well. |
| Temporal Drag | `SpacePowerUp.Type.TIME_SLOW` | Adds 360 frames of global slow motion (0.55x). | Caps at 1,200 frames; affects enemy spawn counters. |
| Hull Repair | `SpacePowerUp.Type.HULL_REPAIR` | Restores up to 3 hull; if full, increases max hull by 1. | Instant effect; announces reinforcement. |
| Heart Core | `SpacePowerUp.Type.HEART_CORE` | Restores 2–4 hull based on difficulty. | Drops from saucers via `maybeSpawnHeart()`. |
| Overdrive | `SpacePowerUp.Type.OVERDRIVE` | Adds 360 frames of overdrive: rapid fire, speed boost, empowered bullets. | Stacks with Rapid Fire and Speed Boost timers. |
| Drone Wing | `SpacePowerUp.Type.DRONE_WING` | Adds 720 frames of autonomous drones that fire angled shots periodically. | `handleDroneWingSupport()` manages firing cadence. |
| Phase Shift | `SpacePowerUp.Type.PHASE_SHIFT` | Adds 360 frames of intangibility; all collision damage ignored. | Collisions early-return before applying damage while active. |
| Nova Burst | `SpacePowerUp.Type.NOVA_BURST` | Instant screen-clear; destroys asteroids/saucers, removes enemy shots, flashes the screen. | Announces number of threats cleared; resets damage tint. |
| Streak Rewards | Kill streak milestones | Auto-granted on `rewardKill()`: heal (15), shield (10), speed boost (6). | Encourages chaining kills between pickups. |

## Debuffs & Difficulty Scaling

- **Obstacles/Walls:** All games use `CollisionException` to stop gameplay on fatal contact unless a shield charge is active.
- **Combo decay:** Snake and Space Shooter reduce streaks when timers expire; Flappy reduces combo after delays or wide clearances.
- **Spawn acceleration:** Each console increases hazard spawn rates based on score/difficulty meters, which indirectly offsets powerful buff stacking.

## HUD Integration

- Every buff has a dedicated HUD entry with colour coding and `Locale.US` formatting for seconds.
- Shield stacks show as `Shield xN (S.Ss)` when more than one charge is active; single charges show remaining time.
- Temporary overlays (damage tint, frenzy banners, nova flash) communicate debuff states or powerful activations.

## Implementation References

- `gameobjects/Bird.java` & `gameobjects/Spacecraft.java`: Deque shield handling and buff timers.
- `consoles/SnakeConsole.java`, `FlappyBirdConsole.java`, `SpaceShooterConsole.java`: Buff application, HUD messaging, spawn cadence.
- `gameobjects/SnakePowerUp.java`, `FlappyPowerUp.java`, `SpacePowerUp.java`: Rendering and enum definitions for collectible sprites.
