# Project Report — Pac-Man Reimagined

## 1. Project Overview
**Project title:** Pac-Man Reimagined: An Interactive 2D Arcade Game in Java  
**Technology:** Java 17+, Swing, AWT, Maven (optional)

This project develops a desktop arcade game that demonstrates object-oriented programming, event handling, timer-driven updates, collision detection, and 2D rendering without relying on a third-party game engine.

## 2. Motivation
Classic arcade gameplay provides a practical way to demonstrate computer-science concepts. The project translates Java concepts that are often taught theoretically into a visual, interactive application.

## 3. Objectives
- Build a playable Pac-Man-inspired maze game.
- Demonstrate Swing/AWT rendering and keyboard input.
- Implement scoring, lives, pellets, power mode, enemies, and restart behavior.
- Expand the original small map into a larger scrolling maze.
- Keep the code understandable and dependency-light for a student demonstration.

## 4. Technology Stack
- Java 17 or later
- Swing: JFrame, JPanel, Timer, KeyListener
- AWT: Graphics2D, Color, geometry and keyboard events
- Java NIO: local high-score persistence
- Maven: optional build automation

## 5. Design and Implementation
The application entry point creates a JFrame and attaches GamePanel. GamePanel owns the timer-driven update loop, map data, game state, collision checks, camera offset, HUD, and rendering. Player and Ghost classes encapsulate entity-specific movement and drawing.

The map is a character grid: `#` is a wall, `.` is a pellet, `o` is a power pellet, and spaces are walkable corridors. A viewport follows the player through the larger map. The game uses tile-based movement and wall checks; ghost behavior uses lightweight heuristic chase/flee logic.

## 6. Features
- Scrolling maze and tunnel wrapping
- Arrow/WASD movement
- Normal pellets and power pellets
- Temporary frightened ghost state
- Four ghost characters
- Score, high score, three lives, and level progression
- Bonus fruit, pause/resume, restart, and game-over screen
- Generated graphics and no external assets

## 7. Testing Plan
1. Launch the app and confirm the start screen appears.
2. Start using Enter and move using arrow keys and WASD.
3. Verify walls block movement and pellets increase the score.
4. Collect a power pellet and verify ghosts change appearance and can be eaten.
5. Touch a normal ghost and verify a life is lost and entities reset.
6. Pause and resume using P.
7. Verify bonus fruit adds 500 points.
8. Lose all lives and restart with Enter.
9. Close and relaunch; verify high score persists where the user home directory is writable.

## 8. Limitations and Future Enhancements
Current ghost AI is heuristic rather than a complete shortest-path algorithm. The M key toggles a status indicator only; audio playback is not implemented. Future improvements could add BFS/A* pathfinding, real sound effects, sprite assets, a level editor, mobile-friendly controls, accessibility settings, and saved statistics.

## 9. Conclusion
Pac-Man Reimagined provides a practical Java desktop project that demonstrates GUI programming, event-driven input, object-oriented design, game loops, map rendering, and collision handling. It extends the proposal with a larger scrolling map and additional gameplay systems while keeping the implementation easy to build and present.
