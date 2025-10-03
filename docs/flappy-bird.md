# Flappy Bird Console Deep Dive

This document outlines the gameplay systems behind the Flappy Bird mini-game. It covers spawning rules, combo logic, buffs, and HUD conventions for reference when tweaking balance or adding features.

## Frame Lifecycle

1. `FlappyBirdConsole.initializeGame()` spawns a `Bird`, seeds random generators, schedules the first pillar, and resets combo/buff state.
2. Every call to `GamingConsole.update()` executes in this order:
   - Apply per-frame tuning (`updateTimers()` and `applyFlightTuning()`).
   - Sync pillar/power-up speeds to active modifiers (`syncSpeedModifiers()`).
   - Delegate to `super.update()` to update objects, spawn new hazards, resolve collisions, and clean up.

## Movement & Tuning

- **Gravity & jump force**: Default jump force is `-8.8` and gravity scale `1.0`. Slow-time and frenzy modes adjust these values on the fly (slow time lowers gravity/jump, frenzy raises both).
- **Base pillar speed**: Driven by score, difficulty meter, and buff modifiers, clamped to 8 px/tick.
- **Difficulty meter**: Increases every 480 frames, tightening pillar gaps and boosting oscillation amplitude.

## Pillar Generation

- `spawnPillar()` calculates gap height (`105–180`) and vertical position while avoiding impossible setups.
- Each pillar may oscillate (35% chance after score > 6) using sine-wave offsets with configurable amplitude/period.
- Pillars track whether the bird has passed the gap to avoid double-counting combos.

## Combo & Scoring System

- Passing through a gap awards base points and checks horizontal alignment:
  - Perfect clearance (within `gapHeight/6`) increments `combo`, flashes the pillar, and may trigger frenzy every 5 perfects.
  - Loose clearance can reduce the combo.
- `comboDecayTimer` decrements each frame; if it reaches zero, the combo drops slowly, encouraging consistent precision.
- `scoreMultiplier` multiplies the base score; stacks via power-ups and decays when `multiplierTimer` hits zero.

## Power-Ups

Power-ups spawn every 260–620 frames and inherit speed modifiers to stay in sync with pillars.

| Power-up | Enum | Effect | Stack Behaviour |
| --- | --- | --- | --- |
| Prism Shield | `FlappyPowerUp.Type.SHIELD` | Adds a 7-second shield charge; the next pillar collision deletes the obstacle and keeps the run alive. | Charges queue in the bird’s shield deque. |
| Score Amplifier | `FlappyPowerUp.Type.DOUBLE_SCORE` | Adds +1 to the score multiplier (cap x5) and refreshes the multiplier timer. | Timer accumulates up to 1,200 frames. |
| Time Warp Rune | `FlappyPowerUp.Type.SLOW_TIME` | Adds 320 frames of slow time, lowers pillar speed, gravity scale, and dampens current velocity for smoother recovery. | Stacks to 960 frames and re-tunes physics each frame. |

All pickups call `bird.flash(24)` for visual feedback and resync pillar speeds after applying effects.

## Frenzy Mode

- Triggered every 5th perfect pass.
- Lasts 240 frames, slightly increases base speed and jump power, providing risk/reward tension.
- Cancelled if the player collides despite having a shield (shield saves do not end frenzy, but losing combos does).

## Collision Handling

- Ceiling or floor contact immediately throws a `CollisionException`.
- Pillar collisions call `bird.absorbHit()`: a successful shield consumes a charge, removes the pillar from play, and logs a “SHIELD_SAVE” event; otherwise the run ends.
- Power-up collisions call `applyPowerUp()` and remove the collectible from the game.

## HUD & Messaging

- Top-left displays score, combo, and multiplier status.
- Status column lists active buffs with durations (converted from frames to seconds).
- Banner messages (`announce()`) communicate state changes: multiplier fade, frenzy triggers, shield saves, etc.
- Game-over panel summarizes score, best combo, and restart hints.

## Extension Points

- Adjust spawn pacing via `BASE_PILLAR_INTERVAL`, `MIN_PILLAR_INTERVAL`, or the random window in `spawnPowerUp()`.
- Introduce new buffs by extending `FlappyPowerUp.Type`, `FlappyPowerUp.draw()`, and `FlappyBirdConsole.applyPowerUp()`.
- Modify flight feel within `applyFlightTuning()` or `Bird`’s physics parameters.
