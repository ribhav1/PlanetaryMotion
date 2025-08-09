# 🌌 Realtime Gravity Simulation

A visually interactive, real-time 2D gravity simulation written in JavaFX. This application models celestial mechanics using Newtonian physics, enabling users to explore orbital dynamics, planetary motion, and gravitational forces through an intuitive desktop interface.

<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-brightgreen" />
  <img src="https://img.shields.io/badge/JavaFX-Supported-blue" />
  <img src="https://img.shields.io/badge/Platform-Desktop-lightgrey" />
  <img src="https://img.shields.io/badge/License-MIT-yellow" />
</p>

---

## 🧠 Overview

This simulator provides a real-time physics engine for gravitational interaction between massive bodies. It supports:
- Precise time scaling (days/months/years per second)
- Real-world physics constants and units
- Interactive planet creation with mass, radius, velocity, and position
- CSV-based batch loading of planetary systems
- A modular, extensible JavaFX architecture

---

## 📦 Features

- ✅ **Newtonian Gravity Engine** – simulates force interactions using ![gravity equation](https://latex.codecogs.com/png.image?\dpi{120}F=G\frac{m_1m_2}{r^2})
- 🎨 **JavaFX UI** – responsive layout with interactive canvas
- 🛠️ **Dynamic Planet Creation** – add planets during simulation with user-defined attributes
- ⏱️ **Time Control** – toggle between day/month/year time scales, and control simulation speed
- 📄 **CSV Import Support** – load custom planetary systems from `.csv` files
- 🔄 **Pause, Resume, Reset, Reverse** – control flow of the simulation without restarting the app

---

## 📁 File Structure

```
src/
├── com.example.demo/
│   ├── Application.java          # JavaFX application launcher
│   ├── Body.java                 # Gravitational body model and physics logic
│   ├── MainController.java       # Main UI navigation controller
│   ├── SimController.java        # Simulation logic, canvas rendering, interactivity
│   ├── TimeUnits.java            # Time scaling constants (day, month, year)
│   ├── Units.java                # Distance unit conversion and constants
├── resources/
│   ├── main.fxml                 # Main layout (menus, navigation)
│   ├── sim.fxml                  # Simulation layout (canvas and controls)
│   ├── home.fxml                 # Welcome screen
```

---

## 🛠️ Installation & Usage

### 📦 Prerequisites

- Java **17+**
- JavaFX SDK (version compatible with your JDK)
- IDE with JavaFX support (IntelliJ IDEA recommended)

### ▶️ Run the App

#### Option 1: From IDE

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/realtime-gravity-sim.git
   cd realtime-gravity-sim
   ```

2. Open the project in IntelliJ or your preferred IDE.
3. Configure JavaFX library path (if not bundled).
4. Run `Application.java`.

---

## 🧪 Simulation Controls

| Feature         | Description |
|----------------|-------------|
| `Start/Pause`  | Toggle simulation state |
| `Reset`        | Restore to initial planet configuration |
| `Reverse`      | Reverse time in the simulation, rewinding in real simulation time |
| `Add Planet`   | Define a planet with radius, mass, and velocity, then click on canvas |
| `Time Scale`   | Choose between Day, Month, or Year as time unit |
| `Slider`       | Adjust time acceleration multiplier (0.1x to 5x) |
| `Load CSV`     | Import custom planet configurations |

---

## 📂 CSV File Format

You can load your own planet systems from `.csv` files. Each row represents a body:

```
radius,mass,pos_x,pos_y,color,vel_x,vel_y
```

**Example:**
```
10,5.972e24,1.0,0.0,Color.BLUE,0.0,29780
```

- `radius`: in pixels (for visualization)
- `mass`: in kg
- `pos_x, pos_y`: relative simulation coordinates (in AU)
- `color`: JavaFX-compatible color string (e.g., `Color.RED`, `Color.BLUE`)
- `vel_x, vel_y`: in m/s (converted internally to simulation units)

---

## 🧠 Physics & Units

- **Gravity Formula:** ![gravity equation](https://latex.codecogs.com/png.image?\dpi{120}F=G\frac{m_1m_2}{r^2})
- **Distance Units:** Astronomical Units (AU) → 1 AU = `1.496e11` meters
- **Velocity Units:** User-input in m/s, internally converted to scaled sim units
- **Time Units:**
  - Day = 86,400 seconds
  - Month ≈ 2.628e6 seconds
  - Year ≈ 3.154e7 seconds

---

## 🧑‍💻 Contributing

Pull requests are welcome! If you’d like to contribute:
1. Fork this repository
2. Create a feature branch (`git checkout -b feature/PlanetTrails`)
3. Commit your changes (`git commit -m 'Add orbit trails'`)
4. Push to the branch (`git push origin feature/PlanetTrails`)
5. Open a Pull Request

---

## 📜 License

This project is licensed under the MIT License.  
See the [LICENSE](LICENSE) file for details.

---

## 🙋‍♂️ Author

Created by [Ribhav Malhotra](https://github.com/your-username)

For questions, feedback, or collaboration ideas, feel free to reach out!
