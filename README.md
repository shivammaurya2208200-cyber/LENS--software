# LENS - Deep Search Timeline

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange.svg" alt="Java 17">
  <img src="https://img.shields.io/badge/JavaFX-Modern_UI-blue.svg" alt="JavaFX">
  <img src="https://img.shields.io/badge/Theme-Pitch_Black-black.svg" alt="Dark Theme">
</p>

**LENS** is an ultra-modern, high-performance desktop search application. It bypasses basic OS file name searches by directly parsing the **deep text content** of your `.pdf`, `.docx`, and `.txt` documents. 

Designed with a stunning, pitch-black "Dark Web" inspired minimalist UI, LENS presents your results on an interactive, dynamic horizontal timeline.

## 🌌 Features
* **Deep Content Parsing**: Uses Apache PDFBox and Apache POI to parse thousands of pages of raw text to find exact matches.
* **Pitch-Black Timeline UI**: A fully custom, glassmorphism-inspired JavaFX interface designed for zero eye-strain and extreme minimalism.
* **Interactive SVG Animations**: Engaging, cinematic knot-drawing animations respond instantly to your UI interactions.
* **Granular Limiting constraints**:
    * **Area Selection**: Force the engine to strictly search a specific folder or drive.
    * **Time Shifting**: Limit the search to files modified in the Past Week, Month, or Year.
    * **Depth Limiting**: Automatically cut off PDF scans at 10, 50, or 100 pages to save extreme amounts of processing time on massive textbooks.

## 🚀 Download & Run (For Users)
You do not need to compile the source code to run LENS.

1. Go to the [Releases](../../releases) tab on GitHub.
2. Download the `lens-1.0.0.zip` file.
3. Extract the ZIP file anywhere on your computer.
4. Inside the extracted folder, go to the `bin/` directory.
5. Double click the `lens` executable (or `lens.bat` on Windows).

## 🛠 Build from Source (For Developers)
If you want to modify the source code or build it yourself:

1. Clone this repository.
2. Ensure you have **Java 17+** installed.
3. Run the following command in the terminal to launch the app directly:
```bash
./gradlew run
```
4. To build the distribution ZIP yourself, run:
```bash
./gradlew distZip
```
The output will be located in `build/distributions/`.
