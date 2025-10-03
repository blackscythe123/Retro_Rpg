# Space Shooter Console Deep Dive

The Space Shooter mini-game is the most system-heavy mode in the Retro Gaming Console. This guide explains spawn logic, enemy behaviour, power-ups, combo rewards, and the HUD so you can iterate confidently.

## Systems Overview

- `SpaceShooterConsole.initializeGame()` instantiates a `Spacecraft`, the supporting `GameObjectList` collections (asteroids, bullets, saucers, enemy projectiles, power-ups), and resets pacing variables.
- Each update frame:
  1. Maintenance timers tick (`fireCooldown`, `waveTimer`, `slowTimeTimer`, etc.).
  2. Player input may request firing via `tryShoot()`.
  3. `handleDroneWingSupport()` auto-fires drone shots when active.
  4. `syncObjectSpeeds()` applies speed multipliers to every object group based on active buffs and profile settings.
  5. `super.update()` performs the standard object update → spawn → collision → cleanup cycle.

## Player Ship (`Spacecraft`)

- Moves freely within a padded 800×600 field, clamping at 10 px margins horizontally and 24/20 px vertically.
- Maintains buff timers:
  - Shield stacks (`Deque<Integer>`)
  - Triple shot, rapid fire, speed boost, overdrive
  - Drone wing and phase shift (recent additions)
- Damage resolution checks phase shift immunity first, then shield stacks, finally hull integrity.

## Weapons & Firing

- `tryShoot()` respects `fireCooldown` derived from active buffs (overdrive > rapid fire > base).
- Bullets are instances of `gameobjects.Bullet`; spread fire clones adjust lateral velocity.
- Drone wing shots spawn from `handleDroneWingSupport()` at adjustable offsets and velocities.

## Enemy & Hazard Spawning

- **Asteroids** spawn every 30–60 frames depending on difficulty, with size-based health and drift. Destroying them rewards points and increments kill streak.
- **Enemy saucers** emerge from left/right/top spawn vectors with predictive aiming and their own fire cooldown. Saucer speed scales with difficulty and streak bonuses.
- **Enemy projectiles** track the player position; speed clamps ensure vertical pressure from top spawns.
- **Power-ups** appear every 360–600 frames with weighted randomness (see table below). Heart cores also drop opportunistically from destroyed saucers through `maybeSpawnHeart()`.

## Combo & Difficulty Mechanics

- `streak` increases on kills (asteroids +1, saucers +2) and decays slowly when no enemies are defeated (`comboDecayTimer`).
- Milestones every 6/10/15 streak grant automatic buffs (speed boost, shield charge, hull heal).
- `waveTimer` raises `difficultyMeter` periodically, which accelerates spawns, saucer aggression, and drop rates.

## Power-Up Reference

| Power-up | Weight | Effect | Stack Behaviour |
| --- | --- | --- | --- |
| Shield Matrix | 18% | Adds an 8-second shield charge. | Queue stored in spacecraft deque. |
| Triple Shot | 16% | Fires a three-bolt spread; duration stacks to 30s. | Additive timer. |
| Rapid Fire | 14% | Shortens fire delay (to 8 frames). | Additive timer. |
| Temporal Drag | 14% | Slows the game world (55% speed). | Timer stacks to 20 seconds. |
| Hull Repair | 14% | Repairs up to 3 hull; if full, increases max hull by 1. | Instant effect. |
| Heart Core | 10% | Restores a large heal scaled by difficulty. | Instant effect. |
| Overdrive | 7% | Grants rapid fire, speed boost, and offensive glow for 6+ seconds. | Timer stacks. |
| Drone Wing | 4.5% | Summons orbiting drones that auto-fire angled bolts. | Timer stacks to 25 seconds; drones persist while any time remains. |
| Phase Shift | 1.5% | Makes the ship intangible to damage. | Duration stacks to 15 seconds; collisions ignored. |
| Nova Burst | 1% | Instant screen-clear: destroys all asteroids and saucers, removes enemy shots, flashes the screen, and announces the number of threats removed. | Instant effect; also resets damage tint. |

Weights are cumulative thresholds from `spawnPowerUp()`. Adjust them there when rebalancing.

## Collision Handling

- Player vs projectile/asteroid/saucer: checks phase shift → shield → hull. Successful shield usage reduces streak but prevents damage tint.
- Bullets vs enemies: when health reaches zero, `rewardKill()` updates score/streak and may trigger automatic buffs.
- Power-up pickups immediately call `applyPowerUp()`; nova burst triggers `triggerNovaBurst()` for special handling.

## HUD & Visual Feedback

- Left HUD block: score, streak, best streak.
- Right HUD: hull meter with segments (max hull), plus buff timers for shields, triple shot, rapid fire, overdrive, speed boost, drone wing, phase shift, and slow time.
- Banner messages highlight important events (sector intensity increase, buff activation, nova clear counts).
- Background layers (stars, nebula, parallax grid) scroll based on `starfieldTick`; damage tint and nova flash overlays communicate hits and detonations.

## Extensibility Tips

- New enemy types should live in `gameobjects/` and be managed with additional `GameObjectList` collections similar to asteroids/saucers.
- To add a power-up: extend `SpacePowerUp.Type`, implement icon art in `SpacePowerUp.draw()`, handle behaviour in `applyPowerUp()`, and update spawn weights.
- Keep balancing constants (spawn intervals, buff durations) in the console so tuning does not leak into unrelated classes.
