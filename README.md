# GitHub Copilot Metrics App

A modular Java application designed to retrieve and analyze GitHub Copilot usage metrics for an organization. The application follows a clean architecture with single-responsibility components that communicate through well-defined interfaces. It fetches data from the GitHub API, processes the JSON response, and generates a consolidated report of code suggestions and acceptance rates by programming language.

## Features

- **Modular Architecture**: Cleanly separated components for configuration, API communication, data processing, and output formatting.
- **Fetch Metrics**: Connects to the GitHub API to retrieve Copilot metrics for a specified organization with proper authentication.
- **Data Analysis**: Aggregates daily metrics across multiple editors and models, including total code suggestions and acceptances.
- **Language Breakdown**: Provides detailed statistics per programming language with acceptance rate calculations.
- **Robust Error Handling**: Configuration validation, HTTP status checking, and exception management at each layer.
- **Modern Java**: Built with Java 21 features (Records, Text Blocks) and uses Jackson for efficient JSON parsing.

## Prerequisites

- **Java 21**: The application requires Java 21 to run.
- **GitHub Access**: A GitHub Personal Access Token (PAT) with appropriate permissions (usually `manage_billing:copilot` or organization read access) is required.
- **Organization**: You must have access to a GitHub Organization with Copilot Business enabled.

## Configuration

The application requires a configuration file to connect to GitHub.

1. Navigate to `app/src/main/resources/` (create the directory if it doesn't exist).
2. Create a file named `config.properties`.
3. Add the following properties:

```properties
GITHUB_TOKEN=your_github_pat_here
ORG_NAME=your_organization_name
```

> **Note**: Ensure `config.properties` is **NOT** committed to version control if it contains real secrets.

## Building the Application

The project uses Gradle for build automation. A wrapper is included explicitly.

To build the project, run:

```bash
# Windows
gradlew.bat build

# Linux/macOS
./gradlew build
```

## Running the Application

You can run the application directly using Gradle:

```bash
# Windows
gradlew.bat app:run

# Linux/macOS
./gradlew app:run
```

## Application Architecture

The application follows a layered, modular design where each component has a single responsibility:

```
┌─────────────────────────────────────────────────────────────┐
│         CopilotMetricsApp (Orchestrator)                    │
│  Coordinates workflow: Configure → Fetch → Process → Print  │
└────────────────┬────────────────────────────────────────────┘
                 │
      ┌──────────┼──────────┬──────────────┬─────────────────┐
      ▼          ▼          ▼              ▼                 ▼
┌─────────┐ ┌─────────┐ ┌──────────┐ ┌────────────┐ ┌──────────────┐
│  Config │ │ Copilot │ │ Metrics  │ │ Metric     │ │ Metrics      │
│uration │ │ Client  │ │Processor │ │ Summary    │ │ Printer      │
└─────────┘ └─────────┘ └──────────┘ └────────────┘ └──────────────┘
   (Load)    (GitHub    (Aggreg-      (Data         (Format &
   & Vali-   API Call)  ate JSON)     Model/        Display)
   date)                              Record)
```

### Core Components

- **`CopilotMetricsApp.java`**: Main orchestrator that coordinates the workflow and handles top-level exception management. Implements the sequential flow: load configuration → fetch metrics → process data → print results.

- **`Configuration.java`**: Immutable Java Record that manages application configuration. Loads and validates required properties (`GITHUB_TOKEN`, `ORG_NAME`) from `config.properties`. Throws exceptions for missing or blank required fields.

- **`CopilotClient.java`**: GitHub API integration layer. Uses Java's HttpClient to communicate with the GitHub API endpoint (`https://api.github.com/orgs/{orgName}/copilot/metrics`). Implements Bearer token authentication, connection timeouts, and HTTP status validation.

- **`MetricsProcessor.java`**: Core data processing component. Transforms raw GitHub API JSON responses into consolidated language metrics. Iterates through daily metrics, extracts editor and model information, and aggregates metrics by programming language using `Map.merge()`.

- **`MetricSummary.java`**: Immutable Java Record representing aggregated metrics for a single language. Contains `language`, `totalSuggestions`, and `totalAcceptances` fields. Provides an `acceptanceRate()` method for percentage calculation and a `merge()` method for combining metrics.

- **`MetricsPrinter.java`**: Output formatting component. Displays consolidated metrics to the console with filtering (removes languages with zero suggestions), sorting (by acceptance rate in descending order), and formatted presentation.

## Dependencies

- **Jackson Databind 2.16.1**: for JSON parsing and object mapping.
- **JUnit 5**: for unit testing framework.
- **jqwik**: for property-based testing support.
- **Java HttpClient**: built-in HTTP client (Java 11+) for API communication.

## Error Handling & Validation

The application implements multi-layered error handling:

- **Configuration Layer**: Validates that required properties exist and are not blank. Throws `IllegalArgumentException` if validation fails.
- **API Layer**: Validates HTTP response status codes and throws `IOException` on non-200 responses.
- **Processing Layer**: Uses Java's `Optional` pattern for safe navigation of nested JSON structures, gracefully handling missing data paths.
- **Application Layer**: Top-level exception handling in `CopilotMetricsApp` catches and logs all exceptions with informative messages.

## Testing

The project includes comprehensive unit tests for each component:

- **`ConfigurationTest.java`**: Tests configuration loading, validation, and error cases.
- **`CopilotClientTest.java`**: Tests HTTP client initialization, authentication, and API communication.
- **`MetricsProcessorTest.java`**: Tests JSON parsing and metrics aggregation logic.
- **`MetricSummaryTest.java`**: Tests the data model and acceptance rate calculations.
- **`MetricsPrinterTest.java`**: Tests output formatting and sorting behavior.

Run tests with:
```bash
# Windows
gradlew.bat test

# Linux/macOS
./gradlew test
```

## Sample Output

```text
--- Consolidated Copilot Acceptance Statistics by Language ---
🔹 **java**
  - Acceptance Rate: **28.50%**
  - Total Suggestions: 1500, Total Acceptances: 427
---
🔹 **python**
  - Acceptance Rate: **22.10%**
  - Total Suggestions: 800, Total Acceptances: 177
---
```
# GitHub Copilot Metrics App

A Java application designed to retrieve and analyze GitHub Copilot usage metrics for an organization. It fetches data from the GitHub API, processes the JSON response, and generates a consolidated report of code suggestions and acceptance rates by language.

## Features

- **Fetch Metrics**: Connects to the GitHub API to retrieve Copilot metrics for a specified organization.
- **Data Analysis**: Aggregates daily metrics including total code suggestions and acceptances.
- **Language Breakdown**: Provides detailed statistics per programming language.
- **Performance**: Built with Java 21 features (Records, Text Blocks, Virtual Threads readiness) and uses Jackson for efficient JSON parsing.

## Prerequisites

- **Java 21**: The application requires Java 21 to run.
- **GitHub Access**: A GitHub Personal Access Token (PAT) with appropriate permissions (usually `manage_billing:copilot` or organization read access) is required.
- **Organization**: You must have access to a GitHub Organization with Copilot Business enabled.

## Configuration

The application requires a configuration file to connect to GitHub.

1. Navigate to `app/src/main/resources/` (create the directory if it doesn't exist).
2. Create a file named `config.properties`.
3. Add the following properties:

```properties
GITHUB_TOKEN=your_github_pat_here
ORG_NAME=your_organization_name
```

> **Note**: Ensure `config.properties` is **NOT** committed to version control if it contains real secrets.

## Building the Application

The project uses Gradle for build automation. A wrapper is included explicitly.

To build the project, run:

```bash
# Windows
gradlew.bat build

# Linux/macOS
./gradlew build
```

## Running the Application

You can run the application directly using Gradle:

```bash
# Windows
gradlew.bat app:run

# Linux/macOS
./gradlew app:run
```

## Application Structure

- **`CopilotMetricsApp.java`**: Main entry point. Handles configuration loading, API requests, data processing, and output generation.
- **`CopilotClient`**: Handles HTTP requests to the GitHub API.
- **`MetricsProcessor`**: Parses and aggregates the JSON data using Jackson.
- **`MetricsPrinter`**: Formats and prints the results to the console.

## Dependencies

- **Jackson Databind**: for JSON processing.
- **JUnit**: for testing.

## Sample Output

```text
--- Consolidated Copilot Acceptance Statistics by Language ---
🔹 **java**
  - Acceptance Rate: **28.50%**
  - Total Suggestions: 1500, Total Acceptances: 427
---
🔹 **python**
  - Acceptance Rate: **22.10%**
  - Total Suggestions: 800, Total Acceptances: 177
---
```
