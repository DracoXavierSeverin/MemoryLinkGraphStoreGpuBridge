# MemoryLinkGraphStoreGpuBridge (MLGSGB)

一个为 **Android 5.0+ (API 21)** 设计的 Java 库，为项目提供底层基础设施：

- 🧠 **内存连接** — 模块间轻量对象连接
- 📡 **模块通信** — 点对点消息 + 广播通知，跨线程安全投递
- 💾 **四存储矩阵** — 临时/永久 × 全局/局域，带容量控制与权限
- 🚚 **信息搬运** — 跨存储批量搬运，支持打包名称与删除源
- 🎨 **噪声库** — Perlin / Worley / Wave / FBM，全部 CPU 实现
- ⚡ **GPU 加速** — 基于 OpenGL ES 3.0 + EGL 离线上下文，5~35× 加速

## 快速开始

### 初始化

在 Application.onCreate() 中调用一次：

MLGSGB.get().init(this);

### 使用示例

MLGSGB lib = MLGSGB.get();

// 模块通信
lib.bus().registerModule("producer");
lib.bus().registerModule("consumer");
lib.bus().subscribe("consumer", "ping", (from, topic, payload) ->
        Log.i("TAG", "收到: " + payload));
lib.bus().send("producer", "consumer", "ping", "hello");

// 存储
Store store = lib.store().globalTemp();
store.put("moduleA", "user:profile", "Alice");
Object v = store.get("moduleA", "user:profile");

// 噪声（CPU）
float[] perlin = lib.noise().perlin()
        .seed(42L).frequency(0.03f).octaves(4)
        .size(256, 256).generate();

// GPU 加速
GpuContext ctx = new GpuContext();
ctx.init();
GpuCompute compute = new GpuCompute(ctx);
float[] gpuResult = compute.executeWithUniforms(
        GpuShaders.perlin(), 512, 512,
        new String[]{"uFrequency", "uOctaves", "uSeed"},
        new float[]{0.03f, 4f, 42f});
ctx.release();

## 核心能力

### 1. 内存连接 MemoryLink

轻量对象连接表，标签 → 对象。

lib.memory().connect("tag", obj);
Object o = lib.memory().get("tag");
lib.memory().disconnect("tag");

### 2. 模块通信 MessageBus

点对点消息 + 广播通知。

lib.bus().registerModule("A");
lib.bus().subscribe("A", "topic", listener);
lib.bus().send(from, to, topic, payload);    // 点对点
lib.bus().notify(from, topic, payload);      // 广播

### 3. 四存储矩阵 Store

            临时（内存）        永久（闪存）
全局    globalTemp()        globalPersist()
局域    localTemp(id)       localPersist(id)

默认容量：24MB / 8MB / 128MB / 32MB，可通过 StoreBuilder 调整。

Store s = lib.store().globalTemp();
s.put("moduleA", "user:profile", "Alice");
Object v = s.get("moduleA", "user:profile");
s.remove("moduleA", "user:profile");
List<String> mods = s.modules();
List<String> tags = s.tags("moduleA");

### 4. 信息搬运 StoreMover

把源 Store 中选中的一批 tag，打包成 packName，搬进目标 Store。

MoveResult r = lib.store().mover().move(
        sourceStore, "moduleA",
        new String[]{"user:profile", "user:avatar"},
        targetStore, "backup");

// 可选：搬运后删除源
mover.move(src, "moduleA", tags, dst, "backup", true);

## 噪声库

四种噪声，统一链式 API。

// Perlin 云雾
float[] a = lib.noise().perlin()
        .seed(42).frequency(0.03f).octaves(4)
        .size(256, 256).generate();

// Worley 细胞
float[] b = lib.noise().worley()
        .seed(42).frequency(0.08f).octaves(3)
        .size(256, 256).generate();

// Wave 条纹
float[] c = lib.noise().wave()
        .seed(42).frequency(0.05f).octaves(5)
        .size(256, 256).generate();

// FBM 分形叠加（地形基础）
float[] d = lib.noise().fbm()
        .seed(42).frequency(0.008f).octaves(6)
        .power(2.5f)
        .size(256, 256).generate();

## GPU 加速

基于 OpenGL ES 3.0 + EGL 离线上下文，无需 SurfaceView。

GpuContext ctx = new GpuContext();
ctx.init();
GpuCompute compute = new GpuCompute(ctx);

float[] gpuPerlin = compute.executeWithUniforms(
        GpuShaders.perlin(), 512, 512,
        new String[]{"uFrequency", "uOctaves", "uSeed"},
        new float[]{0.03f, 4f, 42f});

compute.release();
ctx.release();

可用的内置 shader：

- GpuShaders.perlin()
- GpuShaders.worley()
- GpuShaders.wave()
- GpuShaders.fbm()

## 性能对比（512×512，实测）

| 噪声     | CPU     | GPU    | 加速比 |
|----------|---------|--------|--------|
| Perlin   | 364 ms  | 22 ms  | ~17×   |
| Worley   | 261 ms  | 60 ms  | ~4×    |
| Wave     | 328 ms  | 16 ms  | ~20×   |
| FBM      | 530 ms  | 15 ms  | ~35×   |

## 项目结构

MemoryLinkGraphStoreGpuBridge/
├── mlgsgb-core/                 # 纯库模块
│   └── src/main/java/...
│       └── memorylinkgraphstoregpubridge/
│           ├── MLGSGB.java              # 统一门面
│           ├── MemoryLink.java          # 内存连接
│           ├── GraphStore.java          # 图形存储
│           ├── GpuBridge.java           # GPU 调用
│           ├── noise/                   # 噪声库
│           ├── gpu/                     # GPU 计算框架
│           └── comm/                    # 通信体系
│               ├── MessageBus           # 消息总线
│               └── store/               # 四存储矩阵
└── app/                         # 测试壳

## 构建

终端执行：

    ./gradlew assembleDebug

或使用 AndroidIDE 的"小绿按钮"编译安装。

## 环境要求

- Android 5.0+ (API 21)
- Java 11
- Android Studio / AndroidIDE

## License

MIT
