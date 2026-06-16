# HsMap

A high-performance indoor map rendering library for Android, optimized for retail environments with shelf navigation, element selection, route display, and real-time positioning.

## Features

- **Viewport cache optimization** — Renders only visible elements + padding, auto-rebuilds on pan/zoom/rotation
- **Device-adaptive memory** — Dynamically adjusts cache ratio based on device RAM (high/mid/low)
- **LOD (Level of Detail)** — 5-level rendering detail (FINE → OVERVIEW) for smooth zoom experience
- **Element selection** — Multi-select with custom styles, max count limit, and replace/block overflow modes
- **Element overlay** — Persistent overlay styles independent of selection state
- **Route navigation** — Customizable route drawing with direction arrows, corner radius, and border
- **Real-time positioning** — Position icon with heading angle animation and FOLLOW/FREE modes
- **Gesture control** — Independently toggle scroll, zoom, and rotation gestures
- **Background map** — Supports JSON-only, background-only, or combined rendering modes
- **Render callback** — One-time callback when the map completes its first render

## Installation

### 1. Add the AAR

Place `hsmap-1.0.0.aar` in your app module's `libs/` directory, then add to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(files("libs/hsmap-1.0.0.aar"))
}
```

### 2. Add required dependencies

HsMap depends on Gson and AndroidX Core. Add these to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
}
```

## Quick Start

### 1. Add HsMap to your layout

```xml
<com.hanshow.hsmap.HsMap
    android:id="@+id/hsMap"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:mapShowType="backgroundAndJson" />
```

XML attribute `mapShowType` options:

| Value            | Enum                        | Description                              |
|------------------|-----------------------------|------------------------------------------|
| `json`           | `SHOW_JSON` (0)             | Render only JSON elements                |
| `background`     | `SHOW_BACKGROUND` (1)       | Render only background image             |
| `backgroundAndJson` | `SHOW_JSON_AND_BACKGROUND` (2) | Render both (default)                 |

### 2. Initialize the map

```kotlin
val mapJson = readJsonFromAssets(context, "map.json") // your JSON file
hsMap.initMapData(mapJson)
hsMap.setShapeType(MapShowType.SHOW_JSON_AND_BACKGROUND)
```

`initMapData` accepts a raw JSON string and parses it internally via Gson into `FloorMapData`.

### 3. Render complete callback

```kotlin
hsMap.setOnRenderCompleteListener {
    // Map has completed its first onDraw
}
```

The callback fires once after the first successful render. It resets when `clear()` is called.

## Map Data Format

HsMap expects a JSON string that deserializes into `FloorMapData`. Required fields:

```json
{
  "width": 8000,
  "height": 6000,
  "mapElementList": [
    {
      "shapeType": "MapShelf",
      "x": 100, "y": 200, "width": 300, "height": 60,
      "code": "Shelf-1-001",
      "visible": true,
      "fillColor": "rgba(200,220,255,1)",
      "strokeColor": "rgba(0,0,0,1)"
    }
  ]
}
```

Supported `shapeType` values:

| ShapeType          | Description                                  |
|--------------------|----------------------------------------------|
| `MapShelf`         | Retail shelf with subsections                |
| `MapWall` / `Line` | Wall segments and lines                      |
| `MapCross`         | Channel/cross line (start→end with lineWidth)|
| `Rect`             | Generic rectangle                            |
| `MapPillar`        | Pillar/column                                |
| `MapTableArea`     | Table area (rect with fill + stroke)         |
| `MapSelfArea`      | Self-service area                            |
| `MapTable` / `MapTableFeature` | Table with feature display     |
| `Circle`           | Circle shape                                 |
| `MapMark`          | Icon/text marker                             |
| `MapRoadPoint`     | Precision point marker                       |

Color format: supports `rgba(r,g,b,a)`, `rgb(r,g,b)`, `#RGB`, `#RRGGBB`, `#RRGGBBAA`.

## Element Selection

### Select elements

```kotlin
// Select with default style
hsMap.selectElement("Shelf-1-001")

// Select with custom style
hsMap.selectElement("Shelf-1-001", SelectionStyle(
    borderColor = 0xFFFF0000.toInt(),   // red border
    fillColor = 0x40FF0000,             // semi-transparent red fill
    borderWidth = 2f,                   // border width in dp
    borderCornerRadius = 4f             // corner radius in dp
))

// Listen for selection events
hsMap.onElementSelected = { elements ->
    // List<ElementBean> — all currently selected elements
}

hsMap.onElementUnselected = { element ->
    // ElementBean — the removed element
}
```

### Selection limits

```kotlin
// Max 5 selections, replace oldest when exceeded
hsMap.setMaxSelectionCount(5, SelectionMode.REPLACE_EARLIEST)

// Max 3 selections, block new when limit reached
hsMap.setMaxSelectionCount(3, SelectionMode.BLOCK_NEW)
```

### Clear selection

```kotlin
hsMap.clearSelection()
```

## Element Overlay

Overlay styles are persistent visual highlights independent of selection state.

```kotlin
// Apply overlay to an element
hsMap.setElementOverlay("Shelf-1-001", SelectionStyle(
    borderColor = 0xFFFFFF00.toInt(),   // yellow border
    fillColor = 0x40FF0000,
    borderWidth = 1f,
    borderCornerRadius = 2f
))

// Clear all overlays
hsMap.clearElementOverlays()
```

## Route Navigation

```kotlin
// Add a route path (list of map coordinate pairs)
hsMap.addRoutePath(listOf(
    Pair(100f, 200f),
    Pair(300f, 400f),
    Pair(500f, 600f)
))

// Update route path (replaces existing)
hsMap.updateRoutePath(newPath)

// Update route and position simultaneously
hsMap.updateRoutePathAndCurrentPosition(newPath, positionX, positionY, headingAngle)

// Clear route
hsMap.clearRoutePath()

// Customize route style
hsMap.routeStyle = RouteStyle(
    color = "#5391FC".toColorInt(),
    strokeWidth = 6f,
    cornerRadius = 3f,
    borderColor = "#0143A6".toColorInt(),
    borderWidth = 1f,
    arrowColor = Color.WHITE,
    arrowSpacing = 19f,
    arrowLength = 3f
)
```

## Positioning & Icons

### Show position icon

```kotlin
val shapeInfo = ShapeInfo(
    positionX = 500,
    positionY = 300,
    width = 24,
    height = 24,
    bitmap = positionBitmap,
    headingAngle = 90f
)

hsMap.updateStartIcon(shapeInfo)
hsMap.showCurrentPosition()
```

### Position animation

```kotlin
hsMap.setPositionAnimationEnabled(true)
hsMap.setPositionAnimationDuration(800) // milliseconds
```

### Target icons

```kotlin
hsMap.updateTargetIcons(targetShapeInfoList)
hsMap.clearIcons()
```

### Location mode

```kotlin
// Free: map stays fixed, position marker moves
// Follow: position stays centered, map pans underneath
```

## Gestures & Transform

### Gesture toggles

```kotlin
hsMap.scrollEnabled = true   // enable/disable scroll (pan)
hsMap.zoomEnabled = true     // enable/disable zoom (pinch)
hsMap.rotateEnabled = false  // enable/disable rotation
```

### Scale & zoom

```kotlin
// Set scale so N meters fills the screen width
hsMap.setScaleToMeters(3)  // 3 meters = screen width

// Set scale range
hsMap.setScaleRange(0.5f, 5.0f)

// Manually set scale at a focal point (map coordinates)
hsMap.setScaleSize(focusX, focusY, scaleFactor)

// Get current scale
val scale = hsMap.getCurrentScaleSize()
```

### Rotation

```kotlin
hsMap.updateRotationAngle(45f)  // rotate to 45 degrees
```

### Reset

```kotlin
hsMap.resetTransformData()  // reset to initial scale/position/rotation
```

## Scale Bar

```kotlin
val scaleBarInfo = hsMap.getScaleBarInfo()
// scaleBarInfo.meters  → e.g. 5 (meters)
// scaleBarInfo.pixels  → e.g. 120.0 (screen pixels for that distance)
```

Use this to draw a scale bar overlay on your layout.

## Precision Points

```kotlin
val points = hsMap.getPercisionPointList()
// Returns List<PercisionPoint> with x, y, code, distance metrics
```

## ElementBean (Selection Event Data)

When an element is selected/unselected, the callback receives `ElementBean`:

| Field        | Type         | Description            |
|--------------|--------------|------------------------|
| `name`       | String       | Element name           |
| `code`       | String       | Element code (unique)  |
| `x`          | Float        | Map X coordinate       |
| `y`          | Float        | Map Y coordinate       |
| `width`      | Int?         | Element width          |
| `height`     | Int?         | Element height         |
| `rotation`   | Int?         | Rotation angle         |
| `crossCode`  | String?      | Channel code           |
| `rowFlag`    | String?      | Row flag               |
| `type`       | String?      | Shape type             |

## Cleanup

```kotlin
hsMap.clear()  // Release all resources, caches, and animations
```

Call this when the view is no longer needed (e.g., in `onDestroy`).

## License

Proprietary — © Hanshow
