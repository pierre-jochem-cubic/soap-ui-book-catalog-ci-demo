# Book Catalog – Spring Boot + SoapUI in GitHub Actions

A small Spring Boot 4 REST API whose end-to-end tests are a **SoapUI project** that runs
automatically in **GitHub Actions** on every push and pull request.

```
.
├── .github/workflows/ci.yml            # build → start app → run SoapUI → publish report
├── soapui/book-catalog-soapui-project.xml
├── scripts/run-api-tests.sh            # same flow as CI, for your machine
└── src/                                # Spring Boot app + JUnit (MockMvc) tests
```

## The API

| Method | Path                | Result                                |
|--------|---------------------|---------------------------------------|
| GET    | `/api/books`        | 200, list of books                    |
| GET    | `/api/books/{id}`   | 200, or 404 problem+json              |
| POST   | `/api/books`        | 201 + `Location`, or 400 problem+json |
| PUT    | `/api/books/{id}`   | 200, 400 or 404                       |
| DELETE | `/api/books/{id}`   | 204 or 404                            |
| GET    | `/actuator/health`  | 200 `{"status":"UP"}`                 |

Data is kept in memory; the app has no external dependencies.

## The SoapUI tests

Test suite **Book API** contains three test cases:

- **Health check** checks the HTTP status, runs a JsonPath match on `$.status`, and applies a response SLA.
- **Book lifecycle** runs create → read → update → list → delete → read (expects 404).
  - The generated `id` is passed between steps with a **Property Transfer** (JSONPath `$.id` → TestCase property `bookId`).
  - The request bodies and assertions use property expansion (`${#TestCase#bookId}`, `${#TestCase#title}`).
  - The list step uses a **Groovy Script Assertion** to check that the created book is in the list.
- **Error handling** covers validation errors (400) and unknown ids (404), both returned as problem+json.

The endpoint is not hard-coded. Every request uses `${#Project#baseUrl}`, which defaults to
`http://localhost:8080` and is overridden on the command line with `-PbaseUrl=...`. The same
project can therefore run against any environment.

## Run locally

```bash
# 1. Start the app
./mvnw spring-boot:run

# 2a. Open soapui/book-catalog-soapui-project.xml in the SoapUI desktop app and press Run, or
# 2b. run the whole flow headless, like CI does:
SOAPUI_HOME=~/Applications/SoapUI-5.9.1 ./scripts/run-api-tests.sh
```

On macOS the SoapUI home is inside the app bundle, e.g.
`/Applications/SoapUI-5.9.1.app/Contents/java/app`.

## How CI works

`.github/workflows/ci.yml` has two jobs:

1. **build** runs `./mvnw verify`, which compiles the app and runs the JUnit/MockMvc tests. It then uploads `target/app.jar` as an artifact.
2. **api-tests** does the following, in order:
   - downloads the jar and starts it in the background;
   - waits until `/actuator/health` responds;
   - installs SoapUI OSS (cached between runs);
   - runs `testrunner.sh -j -r -f reports/soapui -PbaseUrl=...`;
   - publishes the JUnit XML as a GitHub check ("SoapUI API tests") and uploads the reports and `app.log` as an artifact.

`testrunner.sh` exits non-zero when any assertion fails, so a broken API fails the pipeline.

## Adding tests

Edit the project in the SoapUI GUI, save it, and commit the XML. Keep these conventions:

- Use `${#Project#baseUrl}` as the endpoint for new requests.
- Put secrets such as tokens in project properties with empty defaults. Pass the real values from GitHub Secrets:
  `-PapiToken="${{ secrets.API_TOKEN }}"`.
- To run only part of the suite, use `-s "Book API"` (one suite) or `-c "Book lifecycle"` (one test case).
