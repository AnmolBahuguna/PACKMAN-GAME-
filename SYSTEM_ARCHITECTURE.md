# System Architecture

```mermaid
flowchart TD
    A[App / JFrame] --> B[GamePanel / JPanel]
    B --> C[Keyboard Input]
    B --> D[Swing Timer Game Loop]
    D --> E[Player Update]
    D --> F[Ghost AI Update]
    D --> G[Tile / Collision Logic]
    E --> G
    F --> G
    G --> H[Score, Lives, Power Mode, Levels]
    H --> I[HUD and Maze Rendering]
    I --> B
```

## Main modules
- **App**: application entry point and window creation.
- **GamePanel**: owns the map, timer, game state, camera, collision checks, HUD, and screens.
- **Player**: direction buffering, movement, and player rendering.
- **Ghost**: heuristic movement, chase/flee decisions, respawn, and rendering.
