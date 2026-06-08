# HsMap — 室内地图组件库

HsMap 是一个 Android 自定义 View 组件，用于渲染室内地图（商超、仓库等场景），支持 JSON 数据驱动的地图绘制、导航路线、位置图标动画、手势缩放旋转、元素选中交互等功能。

---

## 目录

- [功能概览](#功能概览)
- [快速集成](#快速集成)
- [核心 API](#核心-api)
  - [地图数据](#地图数据)
  - [位置图标](#位置图标)
  - [导航路线](#导航路线)
  - [缩放控制](#缩放控制)
  - [旋转控制](#旋转控制)
  - [元素选中](#元素选中)
  - [定位模式](#定位模式)
  - [比例尺](#比例尺)
  - [辅助方法](#辅助方法)
  - [生命周期](#生命周期)
- [数据模型](#数据模型)
  - [FloorMapData](#floormapdata)
  - [MapElement](#mapelement)
  - [ShapeInfo](#shapeinfo)
  - [其他 Bean](#其他-bean)
- [样式配置](#样式配置)
  - [RouteStyle](#routestyle)
  - [SelectionStyle](#selectionstyle)
- [XML 属性](#xml-属性)
- [渲染管线与性能优化](#渲染管线与性能优化)
- [异常体系](#异常体系)
- [架构概览](#架构概览)

---

## 功能概览

| 功能 | 说明 |
|------|------|
| **地图渲染** | JSON 数据驱动，支持货架(MapShelf)、墙壁(MapWall/Line)、圆形(Circle)、标记(MapMark)、矩形(Rect/MapPillar)、路点(MapRoadPoint)等多种图形类型 |
| **背景图叠加** | 支持 Base64 编码的背景图，三种显示模式：纯 JSON / 纯背景图 / JSON+背景图叠加 |
| **导航路线** | 绘制路线折线 + 方向箭头，可配置颜色、线宽、圆角、箭头样式 |
| **位置图标** | 支持当前位置图标 + 第二层方向指示图标，位置更新带平滑动画 |
| **手势交互** | 支持拖拽平移、双指缩放、双指旋转 |
| **元素选中** | 点击货架/标记选中，支持多选上限、两种溢出策略（替换最早/阻止新选） |
| **定位模式** | FREE（自由浏览）/ FOLLOW（跟随定位），FOLLOW 模式下图标居中、地图跟随移动 |
| **LOD 优化** | 5 级细节层次（FINE/NORMAL/SIMPLE/MINIMAL/OVERVIEW），缩放时自动降级 |
| **视口缓存** | 仅缓存可见区域 + 30% 留白，后台 HandlerThread 重建，主线程不阻塞 |
| **比例尺** | 自动从候选整数米数中选择合适的比例尺，像素长度在 50~200px 范围 |

---

## 快速集成

### 1. 布局中添加 HsMap

```xml
<com.hanshow.hsmap.HsMap
    android:id="@+id/hsMap"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:mapShowType="backgroundAndJson" />
```

`mapShowType` 可选值：

| XML 值 | 枚举 | 说明 |
|--------|------|------|
| `json` | `SHOW_JSON(0)` | 仅显示 JSON 绘制的地图元素 |
| `background` | `SHOW_BACKGROUND(1)` | 仅显示背景图片 |
| `backgroundAndJson` | `SHOW_JSON_AND_BACKGROUND(2)` | 同时显示 JSON 和背景图片 |

### 2. 初始化地图数据

```kotlin
// 从 JSON 字符串初始化（推荐）
val mapJson = readJsonFromAssets(this, "floor-map.json")
hsMap.initMapData(mapJson)

// 或直接传入 FloorMapData 对象
val floorMapData = gson.fromJson(mapJson, FloorMapData::class.java)
hsMap.setMapData(floorMapData)
```

> **注意**：`initMapData` 内部调用 Gson 解析 JSON 并自动调用 `setMapData`。JSON 字符串不能为空，否则抛出 `IllegalArgumentException`。

### 3. 设置显示类型（可选）

```kotlin
hsMap.setShapeType(MapShowType.SHOW_JSON_AND_BACKGROUND)
```

---

## 核心 API

### 地图数据

| 方法 | 说明 |
|------|------|
| `initMapData(mapJson: String)` | 解析 JSON 字符串并构建地图渲染缓存 |
| `setMapData(mapData: FloorMapData)` | 设置地图数据对象（IHsMap 接口方法） |
| `getMapData(): FloorMapData?` | 获取当前加载的地图数据 |
| `setShapeType(type: MapShowType)` | 设置显示类型（JSON / 背景图 / 混合） |
| `requestDraw()` | 请求刷新地图显示（IHsMap 接口方法） |

### 位置图标

位置图标是地图上的"当前位置"标记，支持双层图标（主图标 + 方向指示图标）。

```kotlin
// 创建位置图标
val shapeInfo = ShapeInfo(
    positionX = 3699,       // 地图 X 坐标（厘米单位）
    positionY = 3000,       // 地图 Y 坐标（厘米单位）
    width = 100,            // 图标显示宽度（像素）
    height = 100,           // 图标显示高度（像素）
    bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_location),
    onClickListener = { /* 点击回调 */ },
    headingAngle = 0f,      // 朝向角度（度数）
    shapeDesc = ShapeInfoDesc(  // 第二层方向指示图标（可选）
        bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_arrow),
        width = 40,
        height = 40,
        marginBottom = 60    // 第二层图标距主图标的偏移
    )
)

// 更新位置图标（距离 >10 时自动启动平滑动画）
hsMap.updateStartIcon(shapeInfo)
```

| 方法 | 说明 |
|------|------|
| `updateStartIcon(startShapeInfo: ShapeInfo)` | 更新当前位置图标，移动距离 >10 时启动平滑动画 |
| `setPositionAnimationEnabled(enabled: Boolean)` | 启用/禁用位置动画 |
| `setPositionAnimationDuration(duration: Long)` | 设置动画时长（默认 800ms） |
| `showCurrentPosition()` | 居中到当前位置（保持当前缩放） |
| `showCurrentPosition(mapScale: Float)` | 居中到当前位置（指定缩放比例） |
| `showCenterPosition(x, y, scale)` | 居中到指定位置（指定缩放比例） |

**动画行为**：

- 启用动画 + 移动距离 > 10 → 启动 ValueAnimator 平滑过渡（线性插值）
- 禁用动画 或 移动距离 ≤ 10 → 立即跳到新位置
- FOLLOW 模式下，动画会同步平移地图使图标始终居中
- FREE 模式下，动画结束后如果图标距屏幕边缘超出安全区域（1/4 ~ 3/4），自动飞回中心

### 导航路线

```kotlin
// 更新路线（替换当前路线）
hsMap.updateRoutePath(
    listOf(1191f to 574f, 1200f to 1998f, 2062f to 1998f)
)

// 添加路线 + 自动缩放使路线完整可见
hsMap.addRoutePath(
    listOf(1191f to 574f, 1200f to 1998f, 2062f to 1998f)
)

// 同时更新路线和当前位置
hsMap.updateRoutePathAndCurrentPosition(routePath, shapeInfo)

// 清除路线
hsMap.clearRoutePath()
```

| 方法 | 说明 |
|------|------|
| `updateRoutePath(routePathPoint)` | 更新路线路径（替换） |
| `addRoutePath(routePathPoint)` | 添加路线 + 自动缩放适配（飞行动画） |
| `updateRoutePathAndCurrentPosition(route, shape)` | 同时更新路线和位置 |
| `clearRoutePath()` | 清除路线路径 |

> `addRoutePath` 会自动计算路线 bounding box 并以飞行动画缩放到合适比例（留 35% 内边距）。

### 目标图标

```kotlin
// 在地图上显示多个目标标记
hsMap.updateTargetIcons(listOf(shapeInfo1, shapeInfo2, shapeInfo3))

// 清除所有图标（包括目标图标和普通图标）
hsMap.clearIcons()
```

### 缩放控制

```kotlin
// 设置 3 米铺满屏幕宽度
hsMap.setScaleToMeters(3)

// 设置 10 米铺满屏幕宽度
hsMap.setScaleToMeters(10)

// 自定义缩放范围（默认 1.0 ~ 10.0）
hsMap.setScaleRange(0.5f, 20f)

// 以指定焦点为中心缩放
hsMap.setScaleSize(focusX, focusY, scaleFactor)

// 获取当前缩放比例
val scale = hsMap.getCurrentScaleSize()
```

| 方法 | 说明 |
|------|------|
| `setScaleToMeters(meters: Int)` | 指定米数铺满屏幕宽度，如有位置图标则同时居中 |
| `setScaleRange(min, max)` | 设置缩放范围 |
| `setScaleSize(focusX, focusY, scaleFactor)` | 以指定焦点缩放 |
| `getCurrentScaleSize(): Float` | 获取当前缩放比例 |

> `setScaleToMeters` 如果有当前位置图标，会同时居中到图标位置；否则以屏幕中心为焦点。

### 旋转控制

```kotlin
// 直接设置旋转角度（angle 为导航方向，内部自动减去 90°）
hsMap.updateRotationAngle(angle)

// 带节流的旋转更新（推荐：传感器高频数据场景）
hsMap.updateRotationAngleWithThrottle(rotationAngle, orientationAngle)
```

| 方法 | 说明 |
|------|------|
| `updateRotationAngle(angle: Float)` | 更新旋转角度（实际旋转 = angle - 90） |
| `updateRotationAngleWithThrottle(rotation, orientation)` | 带双重过滤的旋转更新 |

**节流机制**（`updateRotationAngleWithThrottle`）：

1. **时间过滤**：500ms 内只处理一次更新
2. **角度过滤**：变化量 < 5° 的更新被忽略

### 元素选中

```kotlin
// 启用元素点击选中
hsMap.elementClickEnabled = true

// 设置多选上限（最多 3 个，达到上限后阻止新选）
hsMap.setMaxSelectionCount(3, SelectionMode.BLOCK_NEW)

// 设置多选上限（最多 3 个，超出时替换最早选中的）
hsMap.setMaxSelectionCount(3, SelectionMode.REPLACE_EARLIEST)

// 手动选中/取消选中
hsMap.selectElement("Shelf-108-1")
hsMap.unselectElement("Shelf-108-1")

// 清空所有选中
hsMap.clearSelection()

// 监听选中事件
hsMap.onElementSelected = { elements ->
    Log.d("Map", "选中: ${elements.map { it.code }}")
}
hsMap.onElementUnselected = { element ->
    Log.d("Map", "取消选中: ${element.code}")
}

// 或使用 Flow 监听（适合 ViewModel 场景）
hsMap.selectedElementsFlow.collect { elements -> ... }
```

| 方法/属性 | 说明 |
|-----------|------|
| `elementClickEnabled` | 点击选中开关（设为 false 时自动清除已选中） |
| `selectElement(code)` | 手动选中指定 code |
| `unselectElement(code)` | 手动取消选中 |
| `clearSelection()` | 清空所有选中 |
| `getSelectedElements(): List<ElementBean>` | 获取当前选中元素列表 |
| `setMaxSelectionCount(count, mode)` | 设置多选上限，0 = 无上限 |
| `onElementSelected` | 选中回调 |
| `onElementUnselected` | 取消选中回调 |
| `selectedElementsFlow` | 选中集合变化 Flow（SharedFlow） |

**SelectionMode 枚举**：

| 值 | 说明 |
|----|------|
| `REPLACE_EARLIEST` | 超过上限时，自动取消最早选中的项 |
| `BLOCK_NEW` | 达到上限后，不允许再选中新项 |

**选中检测机制**：

- 货架(MapShelf)：矩形 hit test，旋转货架反向旋转后再判断
- 标记(MapMark)：圆形 hit test，以 `(x, y)` 为中心，`size` 为半径

### 定位模式

```kotlin
// 切换到跟随模式（图标居中，地图跟随移动）
hsMap.setLocationMode(LocationMode.FOLLOW)

// 切换到自由浏览模式（地图不动，图标移动）
hsMap.setLocationMode(LocationMode.FREE)

// 监听模式切换（手势打断 FOLLOW 时会通知）
hsMap.onLocationModeChanged = { mode ->
    if (mode == LocationMode.FREE) {
        // 显示"回到跟随"按钮
    }
}
```

| LocationMode | 说明 |
|-------------|------|
| `FREE` | 自由浏览：图标移动，地图不动。图标超出安全区域时自动居中 |
| `FOLLOW` | 跟随定位：图标始终居中，地图跟随移动。用户拖拽时自动切换为 FREE |

> 切换到 FOLLOW 时会立即将地图居中到当前位置。用户拖拽手势会打断 FOLLOW 模式，触发 `onLocationModeChanged` 回调。

### 比例尺

```kotlin
val scaleBarInfo = hsMap.getScaleBarInfo()
// scaleBarInfo.meters → 比例尺代表的米数（如 1, 2, 5, 10, 20...）
// scaleBarInfo.pixels → 比例尺在屏幕上的像素长度（50~200px 范围）
```

### 辅助方法

```kotlin
// 获取 Mark 列表
val allMarks = hsMap.getMarkList()           // 所有 Mark
val iconMarks = hsMap.getMarkList(true)      // 仅带图标 Mark
val textMarks = hsMap.getMarkList(false)     // 仅无图标 Mark

// 获取精度定位点列表
val percisionPoints = hsMap.getPercisionPointList()

// 更新精度点（打点测试场景）
hsMap.updatePercisionList(percisionPoints)
hsMap.resetPercisionList(percisionPoints)

// 重置地图变换（缩放、偏移恢复初始状态）
hsMap.resetTransformData()
```

### 生命周期

```kotlin
// 清除地图数据和所有资源（释放 Bitmap、清除缓存、取消动画）
hsMap.clear()

// 仅释放所有 Bitmap（用后地图无法正常显示，需重新初始化）
hsMap.releaseAllBitmaps()
```

> `onDetachedFromWindow` 时会自动调用 `clear()`。

---

## 数据模型

### FloorMapData

楼层地图数据，对应 JSON 结构，字段名与 JSON 保持一致。

```kotlin
data class FloorMapData(
    var mapId: Long,           // 地图 ID
    var floorId: Long,         // 楼层 ID
    var floorIndex: Long,      // 楼层序号
    var floorName: String?,    // 楼层名称
    var width: Float,          // 地图宽度（厘米，必需）
    var height: Float,         // 地图高度（厘米，必需）
    var xScale: Float,         // X 缩放因子（默认 1）
    var yScale: Float,         // Y 缩放因子（默认 1）
    var xMove: Long,           // X 偏移
    var yMove: Long,           // Y 偏移
    var backgroundColor: String?,  // 背景颜色
    var backgroundImage: String?,  // 背景图片（Base64 编码）
    var mapElementList: List<MapElement>?,  // 地图元素列表
    var apList: List<ApPosition>?,          // AP 位置列表
    var positionsList: List<Positions>?     // 定位位置列表
)
```

### MapElement

地图元素数据类，包含所有图形类型的通用字段。字段按功能分组：

| 分组 | 关键字段 |
|------|---------|
| **基础属性** | `shapeType`, `locked`, `visible`, `name`, `code`, `display`, `pointType` |
| **位置大小** | `x`, `y`, `width`, `height`, `endX`, `endY`, `rotation` |
| **样式属性** | `fillColor`, `strokeColor`, `lineWidth`, `lineDash`, `fillImagePath` |
| **形状特定** | `points`(折线点), `size`(标记大小), `startAngle/endAngle`(弧形), `iconPath`(图标) |
| **文本属性** | `text`, `fontSize`, `fontFamily`, `textColor`, `bold`, `italic` |
| **货架属性** | `subsection`, `subsectionCount`, `rowFlag`, `focusShelf`, `eslIdList` |

**shapeType 对应的渲染行为**：

| shapeType | 渲染器方法 | 说明 |
|-----------|-----------|------|
| `MapShelf` | `drawShelf` | 货架：矩形 + 边框 + 分隔线，支持旋转和 LOD 降级 |
| `MapWall` / `Line` | `drawWall` | 墙壁/线段：points 折线绘制 |
| `Circle` | `drawCircle` | 圆形/弧形：椭圆弧绘制 |
| `MapMark` | `drawMark` | 标记：图标 + 文字组合绘制 |
| `Rect` / `MapPillar` | `drawArea` | 矩形区域/柱子 |
| `MapRoadPoint` | `drawPercision` | 路点（精度定位点），仅 FINE/NORMAL LOD 级别显示 |
| `MapTableFeature` / `MapTable` | `drawTabFeature` | 台面特征 |

### ShapeInfo

位置/图标信息，用于 `updateStartIcon` 和 `updateTargetIcons`。

```kotlin
open class ShapeInfo(
    var positionX: Int?,         // 地图 X 坐标（null = 未初始化）
    var positionY: Int?,         // 地图 Y 坐标
    var width: Int,              // 图标显示宽度（像素）
    var height: Int,             // 图标显示高度（像素）
    var bitmap: Bitmap,          // 图标图片
    var onClickListener: (() -> Unit)?,  // 点击回调
    var headingAngle: Float?,    // 朝向角度（度数）
    var shapeDesc: ShapeInfoDesc? // 第二层方向指示图标（可选）
)
```

**ShapeInfoDesc**（第二层图标）：

```kotlin
data class ShapeInfoDesc(
    var width: Int,            // 第二层图标宽度
    var height: Int,           // 第二层图标高度
    var bitmap: Bitmap,        // 第二层图标图片
    var marginBottom: Int      // 距主图标的偏移量
)
```

### 其他 Bean

| 类名 | 说明 |
|------|------|
| `ElementBean` | 选中结果：`name`, `code`, `x`, `y` |
| `MarkBean` | 标记信息：`name`, `code`, `text`, `iconName`, `x`, `y` |
| `PercisionPoint` | 精度定位点：`x`, `y`, `z`, `code`, `index`, `isRecord` + 各种距离度量 |
| `ScaleBarInfo` | 比例尺信息：`meters`(米数), `pixels`(像素长度) |
| `LocationMode` | 定位模式枚举：`FREE`, `FOLLOW` |
| `MapShowType` | 显示类型枚举：`SHOW_JSON(0)`, `SHOW_BACKGROUND(1)`, `SHOW_JSON_AND_BACKGROUND(2)` |
| `SelectionMode` | 选中上限模式：`REPLACE_EARLIEST`, `BLOCK_NEW` |

---

## 样式配置

### RouteStyle

路线样式，设置后下一帧自动生效。

```kotlin
hsMap.routeStyle = RouteStyle(
    color = Color.parseColor("#FF0000"),      // 路线颜色
    strokeWidth = 6f,                         // 路线线宽（dp）
    cornerRadius = 10f,                       // 路线圆角半径（dp）
    arrowColor = Color.parseColor("#FFFFFF"), // 方向箭头颜色
    arrowSpacing = 10f,                       // 箭头间隔（dp）
    arrowLength = 3f                          // 箭头长度（dp）
)
```

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `color` | `Color.BLUE` | 路线颜色 |
| `strokeWidth` | `6f` | 路线线宽（dp） |
| `cornerRadius` | `3f` | 路线圆角半径（dp），使用 CornerPathEffect |
| `arrowColor` | `Color.WHITE` | 方向箭头颜色 |
| `arrowSpacing` | `19f` | 方向箭头间隔（dp） |
| `arrowLength` | `3f` | 方向箭头长度（dp） |

### SelectionStyle

选中样式，设置后下一帧自动生效。

```kotlin
hsMap.selectionStyle = SelectionStyle(
    borderColor = Color.parseColor("#FF0000"),     // 选中边框颜色
    fillColor = Color.parseColor("#80FF0000"),     // 选中半透明填充颜色
    borderWidth = 1f,                              // 选中边框线宽（dp）
    borderCornerRadius = 1f                        // 选中边框圆角半径（dp）
)
```

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `borderColor` | `0xFF4D90E9` | 边框颜色（蓝色） |
| `fillColor` | `0x404D90E9` | 半透明填充颜色 |
| `borderWidth` | `2f` | 边框线宽（dp） |
| `borderCornerRadius` | `4f` | 边框圆角半径（dp） |

---

## XML 属性

```xml
<declare-styleable name="CustomHsMap">
    <attr name="mapShowType" format="enum">
        <enum name="json" value="0" />            <!-- 仅 JSON -->
        <enum name="background" value="1" />       <!-- 仅背景图 -->
        <enum name="backgroundAndJson" value="2" /> <!-- JSON + 背景图 -->
    </attr>
</declare-styleable>
```

使用示例：

```xml
<com.hanshow.hsmap.HsMap
    app:mapShowType="backgroundAndJson" />
```

---

## 渲染管线与性能优化

### LOD（细节层次）

根据 `rawScaleFactor`（像素/厘米）自动选择 LOD 级别，缩放越小细节越少：

| LOD 级别 | rawScaleFactor 阈值 | 货架边框 | 分隔线 | 文字 |
|----------|---------------------|---------|--------|------|
| FINE | ≥ 1.0 | 6px 侧框 + 2px 底框 | 显示 | 显示 |
| NORMAL | ≥ 0.5 | 同 FINE | 显示 | 显示 |
| SIMPLE | ≥ 0.3 | 2px 侧框 | 不显示 | 显示 |
| MINIMAL | ≥ 0.15 | 1px 侧框 | 不显示 | 不显示 |
| OVERVIEW | < 0.15 | 仅填充色 | 不显示 | 不显示 |

### 视口缓存

**核心思想**：只缓存可见区域 + 30% 留白，Bitmap 大小始终 ≈ 1.6× 屏幕尺寸。

- **触发重建条件**：
  - LOD 级别变化
  - 缩放偏离超过 20%
  - 旋转变化超过 5°
  - 视口超出缓存边界
- **重建策略**：
  - 无旧缓存 → 主线程立即重建
  - 有旧缓存 → 后台 HandlerThread 重建，主线程继续用旧缓存绘制
  - 视口接近缓存边界时提前重建（padding 消耗过半）
- **缓存最大尺寸**：4096×4096（Android Bitmap 安全上限）
- **OOM 兜底**：缓存 Bitmap 创建失败时回退到逐元素直接绘制

### 绘制调度

- `DrawScheduler`：50ms 绘制节流，避免同一帧多次 `invalidate()`
- 手势交互期间 (`isInteracting`) 跳过节流，确保实时响应
- `forceDrawMap()`：绕过节流，用于动画结束、选中变化等需要立即更新的场景

### 位置更新节流

- `MIN_UPDATE_INTERVAL = 50ms`：控制位置更新频率，避免动画频繁启停
- `ROTATION_UPDATE_INTERVAL = 500ms` + `ROTATION_MIN_ANGLE_DELTA = 5°`：旋转更新的双重过滤

---

## 异常体系

```
HsMapException (基类)
 ├── MapDataException     — 数据无效、缺失、解析失败
 ├── TransformException   — 缩放超范围、坐标转换失败
 ├── DrawException        — Canvas 锁定失败、Bitmap 已回收
 ├── IconException        — 位置未初始化、资源加载失败
 └── ResourceException    — 内存不足、资源释放失败
```

---

## 架构概览

```
HsMap (公共 API 协调器)
 ├── HsBaseMap (基类：组装管理器、处理 onDraw)
 │    ├── TransformManager     — 矩阵变换和坐标转换
 │    ├── MapDataManager       — 地图数据和初始化参数
 │    ├── GestureManager       — 触摸手势事件处理
 │    ├── TransformAnimationManager — 变换动画（缩放/偏移/旋转）
 │    └── DrawScheduler        — 绘制调度和节流
 │
 ├── 渲染器
 │    ├── MapRenderer          — 地图元素绘制（视口缓存 + LOD）
 │    ├── RouteRenderer        — 路线和箭头绘制
 │    ├── IconRenderer         — 图标绘制（双层图标支持）
 │    └── CoordinateTransformer — 坐标转换接口
 │
 ├── 管理器
 │    ├── IconManager          — 图标状态和 Bitmap 内存管理
 │    ├── AnimationManager     — 位置平滑动画（ValueAnimator）
 │    ├── SelectionManager     — 选中状态和点击检测
 │
 └── TransformGestureDetector  — 双指缩放/旋转手势检测
```

**职责分离原则**：

- `HsBaseMap`：协调各管理器，不包含业务逻辑
- `HsMap`：面向使用者的公共 API，协调渲染器和管理器
- 各 Manager：单一职责，通过回调与外部交互
- 各 Renderer：仅负责绘制，通过 `CoordinateTransformer` 接口获取坐标转换

**坐标转换公式**：

```
screenX = scaleTransformX(x) + marginTransformX()
screenY = scaleTransformY(y) + marginTransformY()
```

其中：
- `scaleTransformX/Y` = `x * mapScreenScaleFactor * saveScale`（地图坐标 → 屏幕坐标）
- `marginTransformX/Y` = 偏移量（居中定位 + 手势平移）
- `getRawScaleFactor` = `mapScreenScaleFactor * saveScale`（总缩放因子，像素/厘米）