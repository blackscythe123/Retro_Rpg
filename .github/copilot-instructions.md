# Copilot Instructions for Retro_Rpg

## Repository Overview

**Project Name:** Retro_Rpg  
**Repository:** blackscythe123/Retro_Rpg  
**Current State:** Early-stage/minimal repository  
**Primary Purpose:** Development of a retro-style RPG game

This repository is currently in its initial state with minimal content. The project appears intended for developing a retro-style role-playing game.

### Repository Statistics
- **Size:** ~8KB (excluding .git)
- **Files:** 1 source file (README.md)
- **Languages:** Not yet determined
- **Framework:** Not yet determined

## Current Repository Structure

```
Retro_Rpg/
├── .github/
│   └── copilot-instructions.md (this file)
└── README.md (minimal description)
```

## Important Notes for Coding Agents

### Repository State
This repository is currently **minimal** and does not yet contain:
- Source code files
- Build configuration files (package.json, Makefile, etc.)
- Test files or test framework
- CI/CD pipelines or GitHub Actions workflows
- Dependencies or requirements files
- Linting or formatting configuration

### When Adding Code

Since no specific language, framework, or architecture has been established yet, agents should:

1. **Confirm Technology Stack**: Ask the user or check for specifications about:
   - Programming language (Python, JavaScript, Java, C++, C#, etc.)
   - Game framework/engine (Unity, Godot, Pygame, Phaser, LÖVE, etc.)
   - Target platform (web, desktop, mobile)

2. **Follow Best Practices**: When implementing code, use industry-standard patterns for the chosen stack:
   - Create appropriate directory structure (e.g., `src/`, `tests/`, `assets/`)
   - Add proper build/dependency management files
   - Include .gitignore for the chosen language/framework
   - Set up testing infrastructure if applicable

3. **Documentation**: Update README.md with:
   - Project description
   - Setup instructions
   - Build/run instructions
   - Dependencies and requirements

## Build and Validation Instructions

**Current State:** No build process exists yet.

### When Build Process is Added

Once code is added to this repository, the following should be documented here:

**Bootstrap Steps:**
- Installation of runtime/SDK requirements
- Dependency installation commands
- Environment setup steps

**Build Steps:**
- Commands to compile/build the project
- Expected build output location
- Build time expectations

**Test Steps:**
- Commands to run tests
- Expected test execution time
- How to run specific test suites

**Run Steps:**
- Commands to execute the application
- Required environment variables
- Expected runtime behavior

**Lint/Format Steps:**
- Linting commands and configuration file locations
- Auto-formatting commands
- Code style guidelines

### Validation Checklist (Update When Applicable)

- [ ] All dependencies installed
- [ ] Code builds without errors
- [ ] All tests pass
- [ ] Linting passes
- [ ] Application runs successfully
- [ ] No regressions introduced

## Project Architecture (To Be Determined)

This section should be updated once the project architecture is established. Typical RPG game architecture may include:

**Potential Components:**
- Game engine/core loop
- Character/entity management system
- Combat/battle system
- Inventory/item system
- Map/world rendering
- Save/load system
- UI/menu system
- Audio/music system

**Potential Directory Structure Examples:**

For Python + Pygame:
```
src/
  ├── main.py
  ├── engine/
  ├── entities/
  ├── systems/
  └── ui/
assets/
  ├── sprites/
  ├── sounds/
  └── maps/
tests/
```

For Unity (C#):
```
Assets/
  ├── Scripts/
  ├── Prefabs/
  ├── Scenes/
  ├── Sprites/
  └── Audio/
ProjectSettings/
```

For JavaScript + Phaser:
```
src/
  ├── scenes/
  ├── entities/
  ├── systems/
  └── utils/
assets/
  ├── sprites/
  ├── audio/
  └── tilemaps/
public/
```

## GitHub Workflows and CI/CD

**Current State:** No workflows configured.

When CI/CD is added, document here:
- Workflow file locations (`.github/workflows/`)
- Triggers (push, pull request, etc.)
- Build/test/deploy steps
- Required secrets or environment variables
- How to replicate CI checks locally

## Common Issues and Workarounds

**Current State:** No known issues yet.

This section should be updated as issues are discovered during development:
- Build failures and their solutions
- Test flakiness and workarounds
- Environment-specific problems
- Dependency conflicts and resolutions

## Files in Repository Root

Currently:
- `README.md` - Minimal project title only ("# Retro_Rpg")

## Key Configuration Files

**Current State:** None exist yet.

When added, document locations and purposes:
- Build configuration
- Dependency management
- Testing configuration
- Linting/formatting rules
- Editor settings

## Agent Guidelines

1. **Trust These Instructions**: These instructions are maintained to reflect the current state of the repository. Only search for additional information if these instructions are incomplete or incorrect.

2. **Minimal Changes**: Always make the smallest possible changes to accomplish the task. Don't refactor or modify unrelated code.

3. **Verify Before Committing**: 
   - When build process exists: Always build and test before committing
   - When tests exist: Ensure all tests pass
   - When linters exist: Ensure code is properly formatted

4. **Update Documentation**: When making structural changes (adding build system, tests, etc.), update this file and the README.md accordingly.

5. **Ask When Uncertain**: If the technology stack or architecture is not clear from the repository state, ask the user for clarification rather than making assumptions.

## README.md Contents

```markdown
# Retro_Rpg
```

(Currently minimal - should be expanded when project development begins)

---

**Last Updated:** When repository was onboarded to Copilot  
**Status:** Repository is in minimal/initialization state - most development infrastructure not yet established
