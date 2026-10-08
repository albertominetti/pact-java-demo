# pact-java-demo

A didactic demo of **consumer-driven contract testing** with
[Pact](https://docs.pact.io/) (**pact-jvm 4.7.5**), between a Spring Boot
**producer** and **two consumers** that consume *different subsets* of the same
contract, with a **Pact Broker** as the contract registry.

The project is hosted on GitHub at <https://github.com/albertominetti/pact-java-demo>.
All project files live on the `feature/pact-java-demo` branch, which is proposed
to `main` through a pull request; the `main` branch is intentionally kept empty.

> **Design note (please read):** this project is **voluntarily simple**: it is a
> didactic demo, not a production template. In particular, it
> **uses Maven multi-module for simplicity**, so that the producer, the two
> consumers and the contract verification live in a single repository that can be
> built and tested with one command. In a real-world setup you would typically keep
> the services in separate repositories with separate pipelines: the *contract*,
> not the build layout, is the point of this demo.

---

## Versions & compatibility

| Component | Version | Notes |
|---|---|---|
| JDK / bytecode (`maven.compiler.release`) | **25** | Spring Boot 3.5.5+ is Java 25 ready |
| Spring Boot | **3.5.16** | last release of the 3.5.x line |
| pact-jvm | **4.7.5** | latest pact-jvm release |
| JUnit Jupiter | **5.12.2** | shared by both stacks (managed by Spring Boot) |
| Spring Framework | **6.2.19** | managed by Spring Boot 3.5.16 |

**Why not Spring Boot 4.x?** The latest Spring Boot line (4.1.1) ships
**JUnit Jupiter 6.0.3** and **Spring Framework 7.0.9**, while the latest
pact-jvm (4.7.5) still builds on **JUnit Jupiter 5.12.2** and the **Spring 6**
provider modules (`junit5spring` / `spring6`). The two stacks are therefore
**incompatible**: pact's JUnit 5 `TestTemplate` extension providers and its
Spring 6 test-context support cannot run on JUnit Jupiter 6 + Spring Framework 7,
so **Spring Boot 4.x + pact-jvm 4.7.5 does not work today** (pact has no release
for that stack yet).

So `Spring Boot 3.5.16 + pact-jvm 4.7.5` is the **most recent mutually
compatible set**: both resolve to **JUnit Jupiter 5.12.2** (and Spring Framework
6.2.19), while still running on **JDK 25**. If/when pact-jvm ships a JUnit 6 /
Spring 7 based release, moving to Spring Boot 4.x becomes an option.

---

## 1. What contract testing is (and why Pact)

In a microservices architecture, **classic integration tests** require every
service to be running together: slow, fragile and hard to maintain.

**Contract testing** moves the problem: the *consumer* declares **what it expects**
from the producer's API (expected request + response), obtains a JSON file called a
**pact** (the "contract"), and the *producer* **verifies** that it satisfies every
published contract. You validate the integration without having to boot every
service at the same time.

Key points demonstrated by this demo:

1. **Consumer-driven**: the consumers define the contract. The producer does not
   "invent" the JSON: it discovers it by verifying the pacts.
2. **Different subsets**: `trading-dashboard` wants **live prices**,
   `settlement-service` wants **master data and the maturity date**. The producer's
   JSON is a **superset** that satisfies both.
3. **Provider states**: the contracts declare the required state of the world
   (`@State`); the producer "seeds" it before every verification.
4. **Pact Broker**: a central contract registry with versions and tags (`main`):
   in real teams this is the piece that enables *can-i-deploy*.
5. **Immediate value**: `make demo-break` breaks a field on the producer and the
   verification **fails right away**, before any deploy.

---

## 2. Architecture

```
   ┌────────────────────────────┐      ┌─────────────────────────────────┐
   │ consumer-trading           │      │ consumer-settlement             │
   │ consumer: trading-dashboard│      │ consumer: settlement-service    │
   │                            │      │                                 │
   │ InstrumentClient (Rest)    │      │ InstrumentClient (Rest)         │
   │ wants: id, venue, currency,│      │ wants: id, isin, mic, venue,    │
   │   lastQuote.price/bid/ask/ │      │   instrumentType, maturityDate  │
   │   currency/timestamp       │      │   (no prices at all!)           │
   │                            │      │                                 │
   │ @Pact + @PactTestFor       │      │ @Pact + @PactTestFor            │
   │   (mock server + client)   │      │   (mock server + client)        │
   └─────────────┬──────────────┘      └──────────────┬──────────────────┘
                 │ generates                          │ generates
                 ▼                                     ▼
   consumer-trading/target/pacts/            consumer-settlement/target/pacts/
   trading-dashboard-instrument-service.json settlement-service-instrument-service.json
                 │                                     │
                 └──────────── copy (maven) ────────────┘
                                 ▼
                     <root>/pacts/   (shared contract folder)
                          │                        │
          make publish    │                        │  default verification
          (optional)      ▼                        ▼  @PactFolder("${pact.folder}")
                 ┌────────────────────────────────────────────┐
                 │ PACT BROKER  http://localhost:9292         │
                 │ pactfoundation/pact-broker + postgres:16   │
                 │ basic auth pact/pact - consumer versions   │
                 │ and "main" tag                             │
                 └────────────────────┬───────────────────────┘
                        broker mode   │ ( @PactBroker, selector tag=main )
                                      ▼
                 ┌────────────────────────────────────────────┐
                 │ instrument-service   (PRODUCER)            │
                 │ Spring Boot REST  GET /api/instruments/{id}│
                 │                  GET /api/instruments?mic= │
                 │ in-memory repository (5 seed instruments)  │
                 │                                            │
                 │ Provider verification (@Provider):         │
                 │   @State(...)  -> seeds the repository     │
                 │   verifies BOTH contracts: its superset    │
                 │   JSON must satisfy all of them            │
                 └────────────────────────────────────────────┘
```

### Domain

The producer's `Instrument` model:

| field | example | used by |
|---|---|---|
| `id`, `isin` | `IT0005443456` | settlement (and routing) |
| `name` | `BTP 4.40% 2033` | (superset, nobody asks for it yet) |
| `mic`, `venue` | `XMIL`, `Borsa Italiana` | settlement (listing by MIC) |
| `instrumentType` | `BOND`/`EQUITY`/`ETF` | settlement |
| `currency` | `EUR` | trading |
| `maturityDate` | `2033-09-15` (null for equity/etf) | settlement |
| `lastQuote` | `price,bid,ask,volume,currency,timestamp` | trading |

Seed data: BTP (XMIL), Bund (XETR), Enel (XMIL), iShares ETF (XETR), ASML (AEX).
`GET /api/instruments?mic=XMIL` returns **exactly 2** instruments (BTP + Enel),
with `maturityDate` set for the bond and `null` for the equity.

---

## 3. Project layout

```
pact-contract-demo/
├── pom.xml                    # parent (packaging pom, dependencyManagement)
├── docker-compose.yml         # pact-broker + postgres (port 9292)
├── Makefile                   # build, consumer-tests, publish, verify, ...
├── README.md
├── pacts/                     # SHARED contract folder (generated by the build)
├── scripts/
│   ├── publish-pacts.sh       # publishes the pacts to the broker (maven plugin)
│   ├── verify-provider.sh     # verification: folder (default) or --broker
│   ├── broker-up.sh           # docker compose up -d, or docker run equivalents
│   ├── broker-down.sh         # stops the broker
│   └── demo-break.sh          # demonstrative breaking change (restores itself)
├── consumer-trading/          # CONSUMER 1 - trading dashboard (live prices)
│   └── src/main/.../InstrumentClient.java
│   └── src/test/.../InstrumentQuotePactTest.java
├── consumer-settlement/       # CONSUMER 2 - back-office (master data/maturity)
│   └── src/main/.../InstrumentClient.java
│   └── src/test/.../InstrumentsByMicPactTest.java
└── instrument-service/        # PRODUCER (Spring Boot)
    └── src/main/.../Instrument{ServiceApplication,Repository,Controller}
    └── src/test/.../contract/
        ├── AbstractPactVerificationTest.java        # @TestTemplate + @State
        ├── InstrumentServicePactVerificationTest    # @PactFolder (default)
        ├── InstrumentServiceBrokerPactVerificationTest # @PactBroker (opt-in)
        └── PactVerificationMode.java                # folder/broker gate
```

**Stack**: Java 25 (`maven.compiler.release=25`, built on JDK 25), Maven 3.9,
Spring Boot **3.5.16**, pact-jvm **4.7.5**, JUnit Jupiter **5.12.2**, Docker (optional).

### Why `<root>/pacts` (a note on pact-jvm 4.7.x)

In pact-jvm 4.7.x the `@PactFolder` annotation is **not repeatable**: a verification
class can point at **one single** folder. Each consumer therefore writes its pact to
its own `target/pacts` (a demo criterion) and the build copies it into the **shared**
folder `<root>/pacts` (a `maven-resources-plugin` execution bound to the `test`
phase, which runs after surefire). The provider verification then uses a single:

```java
@PactFolder("${pact.folder:../pacts}")   // pact.folder = absolute path, set by surefire
```

which both consumers contribute to. `mvn clean` also empties `<root>/pacts`
(an antrun execution on the parent): pacts are build artifacts, not sources.

---

## 4. How to run it (sequence)

```bash
# 1) build the three modules (no tests)
mvn -q clean install -DskipTests

# 2) consumer tests: they generate the two pacts
#    -> consumer-trading/target/pacts/*.json
#    -> consumer-settlement/target/pacts/*.json
#    -> copied into <root>/pacts/
mvn -q -pl consumer-trading,consumer-settlement test

# 3) (OPTIONAL, requires Docker) Pact Broker + publish the pacts
docker compose up -d                # or: make broker-up  (uses docker run if the plugin is missing)
./scripts/publish-pacts.sh          # or: make publish

# 4) provider verification in folder mode (default, no broker)
mvn -q -pl instrument-service test

# 4b) or in broker mode (requires step 3)
mvn -q -pl instrument-service test -Dpact.broker.enabled=true
#     or: ./scripts/verify-provider.sh --broker   /   make verify-broker
```

All in one go: `mvn -q clean install` (build + consumer tests + producer
verification), or via Make: `make build && make consumer-tests && make verify`.

### Make targets

| target | command |
|---|---|
| `make build` | `mvn -q clean install -DskipTests` |
| `make consumer-tests` | `mvn -q -pl consumer-trading,consumer-settlement test` |
| `make publish` | `./scripts/publish-pacts.sh` |
| `make verify` | `mvn -q -pl instrument-service test` (folder mode) |
| `make verify-broker` | verification in broker mode |
| `make broker-up` / `broker-down` | start/stop the broker (docker) |
| `make demo-break` | demonstrative breaking change |
| `make clean` | `mvn clean` + `rm -rf pacts` |

---

## 5. Verification modes (folder vs broker)

| | folder (DEFAULT) | broker |
|---|---|---|
| pact source | `<root>/pacts` (`@PactFolder`) | Pact Broker (`@PactBroker`) |
| selector | - | `@VersionSelector(tag = "main")` |
| activation | always | `-Dpact.broker.enabled=true` (or `PACT_BROKER_ENABLED=true`) |
| dependencies | none | reachable broker with pacts tagged `main` |
| typical use | local development, simple CI | continuous integration, can-i-deploy |

The two verification classes are mutually exclusive: each one has an `@EnabledIf`
that checks `PactVerificationMode` (`instrument-service/src/test/...`).

The verification boots a **real application** (`@SpringBootTest`,
`WebEnvironment.RANDOM_PORT`) and the pact verifier talks to it over **real HTTP**:
controllers, Jackson serialization and 404 handling are all exercised, not just the
Java methods. The `@State` methods re-seed the in-memory repository so that the data
is deterministic.

---

## 6. Pact Broker (registry)

```bash
docker compose up -d      # or: make broker-up / scripts/broker-up.sh
# UI:      http://localhost:9292          (basic auth: pact / pact)
# Posting: PUT /pacts/provider/{p}/consumer/{c}/version/{v}
```

`docker-compose.yml` defines `postgres:16` + `pactfoundation/pact-broker`
(on 9292) with `PACT_BROKER_BASIC_AUTH_USERNAME/PASSWORD=pact` and
`PACT_BROKER_ALLOW_PUBLIC_READ=true`.

> **Environment note**: in this container the `docker compose` CLI plugin may be
> missing (and the Docker daemon may not be running): in that case
> `scripts/broker-up.sh`/`broker-down.sh` start and stop the same services with
> equivalent `docker run` commands (network `pact-demo-net`, containers
> `pact-demo-postgres` and `pact-demo-broker`), and the `docker compose config`
> criterion is considered degraded (the YAML file is still validated at build time).

Publishing (`scripts/publish-pacts.sh`) uses the maven plugin
`au.com.dius.pact.provider:maven:4.7.5:publish` with:

- `-Dpact.pactDirectory=pacts` (the shared folder)
- `-Dpact.broker.url/username/password` (from the environment)
- `-Dpact.projectVersion=$PACT_CONSUMER_VERSION` (default `1.0.0`)
- `-Dpact.consumer.tags=$PACT_CONSUMER_TAG` (default `main`)

### Environment variables

| variable | default | used by |
|---|---|---|
| `PACT_BROKER_BASE_URL` | `http://localhost:9292` | publish, verify-provider, broker verification (`pactbroker.url`) |
| `PACT_BROKER_USERNAME` | `pact` | publish / broker verification |
| `PACT_BROKER_PASSWORD` | `pact` | publish / broker verification |
| `PACT_CONSUMER_VERSION` | `1.0.0` | publish (consumer version) |
| `PACT_CONSUMER_TAG` | `main` | publish (tag of the consumer version) |
| `PACT_BROKER_ENABLED` | `false` | broker-mode gate (`pact.broker.enabled`) |

---

## 7. demo-break: watching a contract fail

```bash
make consumer-tests     # if not done yet (the pacts are required)
make demo-break
```

The script:

1. renames `lastQuote.price` -> `lastQuote.px` in the producer's `Quote` record
   (file: `instrument-service/.../domain/Instrument.java`),
2. runs `mvn -pl instrument-service test` **expecting it to fail**,
3. **always restores** the file (an `EXIT` trap).

The verification fails with an error like *expected field "price" but not found in
actual*: the trading dashboard would no longer be able to read the prices, and the
contract catches it **during the producer's build**, not in production. To repeat it
manually: apply the change, run `make verify`, watch it fail, revert the change.

---

## 8. Troubleshooting / notes

- **No pact found during verification** (`No Pact files were found`): the pacts are
  missing in `<root>/pacts` -> run `make consumer-tests`.
- **The copy into `pacts/` does not run**: the `copy-pact-to-shared-folder`
  execution is bound to the `test` phase and declared *after* surefire in the
  consumer poms: pacts are only copied when the tests actually run.
- **IDE**: the `../pacts` fallback of `@PactFolder` works if the IDE uses the
  `instrument-service` module folder as the working directory.
- **No network / no Docker**: folder mode (the default) is fully offline; broker and
  publishing are optional, documented, and not required by the main acceptance
  criteria.
- **Cleanup**: `make clean` (or `mvn clean` on the parent) also empties `<root>/pacts`.

---

## 9. Acceptance criteria -> how they are satisfied

1. `mvn -q clean install` → **BUILD SUCCESS**: reactor ordered
   consumers → provider; the producer verification reads the pacts generated within
   the same build (shared folder).
2. Pact JSON files in `consumer-trading/target/pacts/` and
   `consumer-settlement/target/pacts/`: written by `@PactDirectory("target/pacts")`.
3. `mvn -q -pl instrument-service test` → **BUILD SUCCESS** (default folder mode,
   no broker required).
4. `docker-compose.yml` syntactically valid (YAML); `docker compose config` degraded
   if the CLI plugin is missing (see the note in §6).
5. README with architecture, ASCII diagram, execution sequence and the concept of
   pact/contract testing (this file).
6. Makefile: `make build`, `make consumer-tests`, `make verify`, ...
