# Object-Oriented Architecture Overview

This document highlights the object-oriented programming (OOP) techniques that power the Retro Gaming Console project. Each section points to the concrete classes inside `src/` so you can explore the implementation details directly. Use it as both a refresher on OOP theory and a field guide when you extend the codebase.

## Pillar Cheat Sheet

| Concept | What it means in theory | Where to spot it in the project | Why it matters |
| --- | --- | --- | --- |
| Abstraction | Hide internal complexity behind a common interface. | `GamingConsole`, `GameObject` hierarchies. | Keeps the menu/loop agnostic to concrete games. |
| Encapsulation | Keep state private and expose controlled behaviour. | Buff timers in `Spacecraft`, combo counters in consoles. | Prevents accidental cross-game mutations. |
| Inheritance | Share behaviour via parent classes. | `GameObject` → `MovableObject` → concrete actors. | Eliminates duplicate movement/rendering code. |
| Polymorphism | Treat different types through a unified contract. | `GameObjectList.updateAll/drawAll`. | Lets the loop update any object without `instanceof` checks. |
| Composition | Build complex objects from simpler collaborators. | Consoles + `GameObjectList`, `RetroConsole` + Swing panels. | Encourages modular, swappable components. |

## Core Principles

### Abstraction
- `GamingConsole` defines the lifecycle hooks (`initializeGame`, `spawnObjects`, `checkCollisions`, `drawUI`, etc.) that every mini-game must implement without exposing the internal Swing loop details.
- `GameObject`, `MovableObject`, and `ImmovableObject` abstract shared rendering and collision-friendly state so individual games focus on custom behaviour.
- **Why it matters:** Adding a new game only requires implementing the hooks; the rest of the engine stays untouched.
- **Try it yourself:** Inspect `consoles/SnakeConsole.java` and note that the class only describes Snake-specific logic—input, spawning, and HUD—because everything else is abstracted away.

### Encapsulation
- Game state such as `Spacecraft` health, shield stacks, and buff timers are hidden behind public methods (`activateShield`, `enablePhaseShift`, `getOverdriveSeconds`). External code can trigger abilities without mutating raw fields.
- Each console keeps score, combo streaks, and timers private, exposing them only through HUD rendering or helper getters, preventing accidental cross-game interference.
- **Anti-pattern to avoid:** Directly mutating `spacecraft.shieldStacks` from outside the class would break invariants (e.g., deque ordering). Keep helper methods authoritative.

### Inheritance
- All renderable entities extend `GameObject`; moving actors inherit velocity and motion helpers from `MovableObject`.
- Mini-games inherit from `GamingConsole`, automatically gaining pause handling, update sequencing, and drawing order.
- Exceptions (`CollisionException`, `InvalidGameStateException`, `GameInitializationException`) extend Java’s `Exception` hierarchy to provide semantic error types.
- **Visual map:**
  ```text
  GameObject
   ├─ MovableObject
   │   ├─ Snake, Bird, Spacecraft, Bullet, EnemySaucer, Asteroid, EnemyProjectile
   │   └─ ...
   └─ ImmovableObject
       ├─ Pillar, SnakeObstacle, Food
       └─ ...
  ```

### Polymorphism
- `GameObjectList` stores `GameObject` references. During each frame, the console invokes `update()` and `draw()` polymorphically, letting each object execute its own logic.
- Input handling and reset functionality call `handleKeyPress`/`handleKeyRelease` on the active console without knowing which game is currently running.
- **In practice:** When `SpaceShooterConsole` removes a destroyed asteroid it does not need to know which class produced the explosion; it simply asks the shared list to `remove()` the `GameObject` instance.

## Composition & Aggregation
- `GameObjectList` wraps a `CopyOnWriteArrayList` to manage game entities while iterating safely; consoles compose these lists to track asteroids, power-ups, and HUD effects.
- `SpaceShooterConsole` composes helper methods such as `spawnDroneWingShot` and `rewardKill` to build richer behaviour from smaller building blocks.
- `RetroConsole` aggregates the available consoles, swapping them into the active `GamePanel` through card layout switching.
- **Design guideline:** Prefer composition for feature toggles (e.g., adding a new HUD widget) rather than introducing more inheritance layers.

## Interfaces & Contracts
- `Drawable` and `Updatable` define the essential contracts required by the game loop.
- `GamingConsole` implements both interfaces and enforces that every subclass can be drawn and updated consistently.
- Power-up enums (`SnakePowerUp.PowerUpType`, `FlappyPowerUp.Type`, `SpacePowerUp.Type`) provide type-safe identifiers for gameplay effects instead of using magic strings.
- **Contract check:** If you create a new object type, implement `update()` so it remains compatible with `GameObjectList.updateAll()`—this is polymorphism through interface adherence.

## Reusable Patterns

### Template Method
- `GamingConsole.update()` orchestrates the frame pipeline (`updateAll` objects → `spawnObjects` → `checkCollisions` → `cleanupObjects`). Subclasses override the hooks to specialise behaviour while reusing the overall loop.
- **Snippet:**
  ```java
  @Override
  public void update() {
      if (gameOver || paused) return;
      try {
          gameObjects.updateAll();
          spawnObjects();
          checkCollisions();
          cleanupObjects();
      } catch (CollisionException e) {
          gameOver = true;
          handleGameOver();
      }
  }
  ```
- **Takeaway:** You override the hook methods, not the master algorithm, which keeps the frame flow consistent between games.

### Strategy via Composition
- Difficulty pacing is handled by injecting profile multipliers (`GameSpeedProfile`) into consoles. Changing the profile swaps the active speed strategy without altering the core loop.
- **Example:** `SnakeConsole.resolveProfileSpeedScale()` returns 0.6/1.0/1.25 multipliers; the same console method works regardless of which profile the player picks.

### Observer-esque Event Flow
- Swing key bindings forward events into `GamingConsole.handleCommonKeyPress`, decoupling input devices from game-specific responses. While not a formal observer implementation, it follows the same signalling idea.
- **Extending:** Register a new keybinding in `RetroConsole` to emit custom events without editing every console.

### Factory Helpers
- Each console acts as a focused factory, spawning obstacles and power-ups (`spawnPowerUp`, `spawnPillar`, `spawnAsteroid`). Encapsulating creation logic in one place simplifies balancing and future tweaks.
- **Guideline:** New collectibles should be manufactured inside the owning console to keep spawn rules discoverable.

## Advanced Topics

### Time-Based Buff Queues
- Snake, Flappy Bird, and Space Shooter store buff timers inside `Deque` instances. This queue-based approach demonstrates encapsulation (timers hidden inside the entity) and composition (deque + helper methods) while supporting stacking behaviour.
- **Why a deque?** It preserves FIFO ordering so the oldest shield expires first—matching player expectations.

### Concurrency Safety
- `GameObjectList` leverages `CopyOnWriteArrayList` to iterate safely while objects spawn or despawn during the same tick—a practical example of choosing the right collection to satisfy OOP design goals (thread safety and immutability semantics).
- **Trade-off:** Copy-on-write favours read-heavy workloads (like our draw/update loops) at the cost of slower writes, which is acceptable given the modest object creation rate.

### Extensibility
- Adding a new mini-game requires only a new subclass of `GamingConsole` plus supporting objects. Existing infrastructure (menu wiring, build scripts, pause system) remains untouched—a testament to modular OOP design.
- **Checklist:**
  1. Create `NewGameConsole` extending `GamingConsole`.
  2. Implement lifecycle hooks.
  3. Register the console button in `RetroConsole.createMenu()`.
  4. Build—no other files need modification.

### SOLID Snapshot
- **Single Responsibility:** Each console handles only its game rules; rendering/input orchestration lives in `GamingConsole` and `RetroConsole`.
- **Open/Closed:** Adding power-up types means updating enums plus `applyPowerUp()`—existing behaviour stays closed for modification elsewhere.
- **Liskov Substitution:** Any `GameObject` subclass can be inserted into `GameObjectList` without breaking the update loop.
- **Interface Segregation:** Minimal interfaces (`Drawable`, `Updatable`) prevent objects from implementing unused methods.
- **Dependency Inversion:** High-level modules depend on abstractions (`GamingConsole`, `GameObject`), not concrete implementations, allowing the menu to remain decoupled from specific games.

## Suggested Reading Order
1. Start with `utils.RetroConsole` to see how consoles are launched and switched.
2. Review `consoles/GamingConsole` to understand the lifecycle contract.
3. Dive into a specific console (e.g., `SpaceShooterConsole`) and its related game objects to see the patterns in action.
4. If you plan to extend the game, use this document as a checklist to stay aligned with the existing architecture.
