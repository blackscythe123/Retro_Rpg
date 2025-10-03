# Snake Console Deep Dive

This page documents the design of the Snake mini-game, including spawning logic, progression systems, power-ups, and HUD feedback. Use it alongside the source in `consoles/SnakeConsole.java` and `gameobjects/Snake*.java` when modifying gameplay.

## Loop Overview

1. `SnakeConsole.initializeGame()` seeds the random generator, creates a `Snake`, spawns the initial `Food`, and resets timers.
2. Each frame `GamingConsole.update()` drives four phases:
   - Update all `GameObject` instances (snake movement, power-up animations, obstacle pulses).
   - `spawnObjects()` advances timers, introduces obstacles/power-ups, and decrements buff queues.
   - `checkCollisions()` resolves food pickups, power-ups, and lethal collisions.
   - `cleanupObjects()` removes expired power-ups or off-screen hazards.

## Movement & Difficulty

- **Base speed** starts at 8 pixels per tick and climbs every five foods consumed (capped at 20). Slow-time reductions and profile scaling are applied via `updateActiveSpeed()`.
- **Speed profiles** (Relaxed/Standard/Turbo) scale movement with `resolveProfileSpeedScale()` so accessibility settings propagate without rewriting game logic.
- **Slow time buff** adjusts `baseSpeed` when active, ensuring a comfortable pace even at higher scores.

## Scoring & Combos

- Eating food increments `foodsEaten`, grows the snake, and awards points: `10 * comboMultiplier * max(1, scoreMultiplier)`.
- The **combo timer** lasts 180 frames. Consecutive pickups before it expires grow the combo count, increasing both point payout and max combo statistics.
- Combos decay gracefully—when `comboTimer` reaches zero the counter resets to zero.
- Every fifth food speeds up the snake slightly, contributing to escalating difficulty.

## Obstacles & Hazards

- **SnakeObstacle** crystals spawn every 90–220 frames (adaptive). Placement logic retries up to 40 times to avoid overlapping existing pieces.
- Colliding with a crystal without an active shield triggers a `CollisionException` and ends the run. Shielded collisions remove the obstacle instead.
- Walls (0–800 / 0–600 bounds) and self-collision (head against body segments) are lethal.

## Power-Ups

| Power-up | Source | Effect | Stacking behaviour |
| --- | --- | --- | --- |
| Food | Continuous | Grows the snake, awards score, feeds combo system. | — |
| Double Score | `SnakePowerUp.PowerUpType.DOUBLE_SCORE` | Increases `scoreMultiplier` (max x5) and extends timer by 360 frames. | Timer accumulates (up to 1,200 frames). |
| Quantum Shield | `SnakePowerUp.PowerUpType.SHIELD` | Adds a 7-second shield charge to the deque; absorbs one lethal hit and clears the obstacle. | Each pickup appends a new charge; timers tick one-at-a-time. |
| Chrono Berry | `SnakePowerUp.PowerUpType.SLOW_TIME` | Adds 260 frames to the slow-time timer; reduces snake speed and recalculates movement after each stack. | Cap of 900 frames; speed recalculated every frame. |

Power-up spawn cadence adapts with score (`Math.max(240, 480 - score/3)`), and pickups expire automatically after 600 frames.

## Buff Queue Mechanics

- **Shield timers** live in `Deque<Integer> shieldTimers`; each frame the leading timer decrements. When it reaches zero the charge disappears without affecting others.
- **Score multiplier** and **slow time** use additive timers with caps (`1200` and `900` frames, respectively). HUD strings convert frames to seconds for readability.

## HUD Elements

- Left column shows score, snake length, speed, and combo data.
- Right column lists active buffs with colour-coding: gold (multiplier), blue (shield), green (slow time).
- Game over panel displays final score, max combo, and restart instructions.

## Key Extension Points

- `spawnObstacle()` and `spawnPowerUp()` are the primary places to adjust difficulty curves or add new collectibles.
- `applyPowerUp()` centralises effect application; extend the `SnakePowerUp.PowerUpType` enum and handle new cases here.
- Movement tuning lives in `updateActiveSpeed()` and `resolveProfileSpeedScale()`, keeping speed logic isolated from collision handling.
