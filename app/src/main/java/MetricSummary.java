record MetricSummary(String language, long totalSuggestions, long totalAcceptances) {
    double acceptanceRate() {
        return totalSuggestions == 0 ? 0.0 : (double) totalAcceptances / totalSuggestions * 100.0;
    }

    MetricSummary merge(MetricSummary other) {
        return new MetricSummary(
                language,
                this.totalSuggestions + other.totalSuggestions,
                this.totalAcceptances + other.totalAcceptances);
    }
}
