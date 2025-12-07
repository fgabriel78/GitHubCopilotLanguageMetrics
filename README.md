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
