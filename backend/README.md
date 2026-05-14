# Flight Information Tracker 后端

这是一个基于 Spring Boot 的航班信息跟踪后端，主要负责从 OpenSky Network 拉取指定地理范围内的实时航班状态，通过 REST API 和 WebSocket 提供给前端，并支持将航班轨迹写入 MySQL 后进行历史轨迹和统计查询。

## 技术栈

- Java 17
- Spring Boot 3.2.4
- Spring Web / WebFlux / WebSocket STOMP
- MyBatis
- MySQL
- Maven Wrapper

## 已实现功能

- OpenSky OAuth2 客户端凭证认证，自动缓存和提前刷新 Token。
- 定时轮询 OpenSky `/states/all` 接口，支持按经纬度边界框拉取实时航班。
- 对上游请求做简单重试、429 限流冷却和缓存降级。
- 通过 WebSocket STOMP 广播实时航班数据到 `/topic/flights`。
- 提供实时航班查询、历史轨迹查询、统计和系统状态接口。
- 提供 `flight_track` 历史轨迹表和 `user_favorite` 收藏表建表 SQL。

## 接口说明

### REST API

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/flights` | 查询缓存中的实时航班列表，可选参数：`callsign`、`icao24`、`country` |
| `GET` | `/api/tracks/{icao24}` | 查询指定飞机历史轨迹。可传 `start`、`end`（Unix 秒），或传 `seconds` 查询最近一段时间，默认 3600 秒 |
| `GET` | `/api/stats/summary` | 实时航班概况，包括总数、国家/地区分布、高度分布 |
| `GET` | `/api/stats/timeline?hours=1` | 查询最近 N 小时的航班记录数量时间线，按 10 分钟聚合 |
| `GET` | `/api/system/status` | 查询系统状态，包括缓存数量、最近更新时间、限流状态、Token 过期时间等 |

### WebSocket

- 连接端点：`/ws-flights`（启用 SockJS）
- 订阅主题：`/topic/flights`
- 服务端会按 `opensky.polling-interval-seconds` 配置的周期拉取并广播实时航班列表。

## 配置说明

主配置文件是 `src/main/resources/application.yml`，当前默认激活 `local` profile：

```yaml
spring:
  profiles:
    active: local
```

本地敏感配置应放在 `src/main/resources/application-local.yml`，该文件已在 `.gitignore` 中忽略。可以参考 `src/main/resources/application.yml.example` 创建本地配置，主要配置项包括：

- `server.port`：后端端口，默认可用 `8080`。
- `spring.datasource.*`：MySQL 数据库连接信息。
- `opensky.client-id` / `opensky.client-secret`：OpenSky API 凭据。
- `opensky.auth-url` / `opensky.api-url`：OpenSky 认证和 API 地址。
- `opensky.bbox`：查询航班的经纬度范围。
- `opensky.filter-on-ground`：是否过滤地面飞机。
- `opensky.polling-interval-seconds`：定时拉取和广播周期。
- `opensky.retry-*`、`rate-limit-pause-seconds`：重试和限流降级参数。

注意：不要把真实数据库密码和 OpenSky 密钥提交到仓库。

## 首次运行步骤

1. 安装并确认环境：
   - JDK 17+
   - MySQL 8.x 或兼容版本

2. 初始化数据库：
   - 登录 MySQL。
   - 执行 `sql/flight_tracker.sql`，创建 `flight_tracker` 数据库、`flight_track` 表和 `user_favorite` 表。

3. 创建本地配置：
   - 复制 `src/main/resources/application.yml.example` 为 `src/main/resources/application-local.yml`。
   - 修改数据库用户名、密码，以及 OpenSky `client-id`、`client-secret`。
   - 如需降低 OpenSky 调用频率，可适当调大 `opensky.polling-interval-seconds`。

4. 启动项目：

```bash
./mvnw spring-boot:run
```

Windows PowerShell 可使用：

```powershell
.\mvnw.cmd spring-boot:run
```

5. 验证服务：
   - 访问 `http://localhost:8080/api/system/status` 查看系统状态。
   - 等待一次轮询完成后，访问 `http://localhost:8080/api/flights` 查看实时航班缓存。

## 当前可改进点

- `TrackStorageService` 虽然实现了批量保存轨迹，但目前没有看到被定时广播流程调用；如果需要历史轨迹和时间线统计有数据，应在拉取航班后触发保存，并配置 `@EnableAsync` 和对应的 `taskExecutor`。
- 部分类同时使用了 `@RequiredArgsConstructor` 和字段 `@Autowired`，建议统一为构造器注入，便于测试和维护。
- `FlightTrack` 已使用 Lombok `@Data`，同时又手写了 getter/setter，可以二选一以减少冗余。
- 时间线统计复用了 `FlightTrack.callsign` 和 `FlightTrack.velocity` 承载聚合结果，可读性较弱，建议新增专门的统计 DTO。
- 目前缺少全局异常处理和参数校验，例如 `hours`、`seconds`、`start/end` 的范围校验。
- `WebSocketConfig` 允许所有来源连接，开发阶段方便，但生产环境建议限制为可信前端域名。
- `application-local.yml` 中若已经写入真实密钥，建议立即轮换密钥，并只保留在本地或环境变量中。
- SQL 中已创建 `user_favorite` 表，但后端暂未实现收藏相关接口。
