/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Oct 20, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.configuration;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.EnumUtils;

import com.ericsson.esc.bsf.load.server.InvalidParameter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Defines the configuration for exporting metrics.
 */
@JsonDeserialize(builder = MetricsConfiguration.Builder.class)
public class MetricsConfiguration
{
    /**
     * Exporting methods for metrics.
     */
    public enum ExportMetrics
    {
        /**
         * Export metrics in CSV files.
         */
        CSV_FILE,

        /**
         * Export metrics in LOG files.
         */
        LOG_FILE,

        /**
         * Export PROMETHEUS metrics.
         */
        PROMETHEUS;
    }

    private final Set<ExportMetrics> exportMetrics;

    private final String csvConvertDurationsTo;
    private final String csvConvertRatesTo;
    private final String csvMetricsDirectory;
    private final String csvNamePrefix;
    private final Integer csvPollInterval;

    private final String logConvertDurationsTo;
    private final String logConvertRatesTo;
    private final String logMetricsDirectory;
    private final String logNamePrefix;
    private final Integer logPollInterval;

    private final Boolean pmEnablePercentiles;
    private final List<String> pmTargetPercentiles;
    private final Integer pmPercentilePrecision;
    private final List<String> pmPercentileMetrics;
    private final Boolean pmEnableHistogramBuckets;
    private final List<String> pmHistogramBucketsMetrics;

    private MetricsConfiguration(Builder builder)
    {
        this.exportMetrics = builder.exportMetrics;
        this.csvConvertDurationsTo = builder.csvConvertDurationsTo;
        this.csvConvertRatesTo = builder.csvConvertRatesTo;
        this.csvMetricsDirectory = builder.csvMetricsDirectory;
        this.csvNamePrefix = builder.csvNamePrefix;
        this.csvPollInterval = builder.csvPollInterval;

        this.logConvertDurationsTo = builder.logConvertDurationsTo;
        this.logConvertRatesTo = builder.logConvertRatesTo;
        this.logMetricsDirectory = builder.logMetricsDirectory;
        this.logNamePrefix = builder.logNamePrefix;
        this.logPollInterval = builder.logPollInterval;

        this.pmEnablePercentiles = builder.pmEnablePercentiles;
        this.pmTargetPercentiles = builder.pmTargetPercentiles;
        this.pmPercentilePrecision = builder.pmPercentilePrecision;
        this.pmPercentileMetrics = builder.pmPercentileMetrics;
        this.pmEnableHistogramBuckets = builder.pmEnableHistogramBuckets;
        this.pmHistogramBucketsMetrics = builder.pmHistogramBucketsMetrics;
    }

    /**
     * Validates and logs all errors in the configuration. Does not stop at the
     * first invalid configuration option.
     * 
     * @return Returns false when there is at least one invalid configuration
     *         option, otherwise true.
     */
    public List<InvalidParameter> validate()
    {
        final var cv = new ConfigurationValidator();

        final var validCsvConvertDurationsTo = csvConvertDurationsTo != null && EnumUtils.isValidEnumIgnoreCase(TimeUnit.class, csvConvertDurationsTo);
        final var validCsvConvertRatesTo = csvConvertRatesTo != null && EnumUtils.isValidEnumIgnoreCase(TimeUnit.class, csvConvertRatesTo);
        final var validLogConvertDurationsTo = logConvertDurationsTo != null && EnumUtils.isValidEnumIgnoreCase(TimeUnit.class, logConvertDurationsTo);
        final var validLogConvertRatesTo = logConvertRatesTo != null && EnumUtils.isValidEnumIgnoreCase(TimeUnit.class, logConvertRatesTo);

        var validPmTargetPercentiles = true;
        try
        {
            // Check if it is in range (0, 1).
            if (this.pmTargetPercentiles.stream() //
                                        .map(Double::parseDouble)
                                        .map(val -> val <= 0 || val >= 1.0)
                                        .anyMatch(x -> x))
            {
                validPmTargetPercentiles = false;
            }
        }
        catch (NumberFormatException e)
        {
            // Check if it is a valid double number.
            validPmTargetPercentiles = false;
        }

        cv.checkNonNull(exportMetrics, "export-metrics", "Parameter 'export-metrics' must not be null");

        cv.check(validCsvConvertDurationsTo, "csv-convert-durations-to", "Parameter 'csv-convert-durations-to' must be a valid time unit and must not be null");
        cv.check(validCsvConvertRatesTo, "csv-convert-rates-to", "Parameter 'csv-convert-rates-to' must be a valid time unit and must not be null");
        cv.checkNonNull(csvMetricsDirectory, "csv-metrics-directory", "Parameter 'csv-metrics-directory must not be null");
        cv.checkNonNull(csvNamePrefix, "csv-name-prefix", "Parameter 'csv-name-prefix' must not be null");
        cv.checkNonNullPositive(csvPollInterval, "csv-poll-interval", "A positive number value is required for 'csv-poll-interval'");

        cv.check(validLogConvertDurationsTo, "log-convert-durations-to", "Parameter 'log-convert-durations-to' must be a valid time unit and must not be null");
        cv.check(validLogConvertRatesTo, "log-convert-rates-to", "Parameter 'log-convert-rates-to' must be a valid time unit and must not be null");
        cv.checkNonNull(logMetricsDirectory, "log-metrics-directory", "Parameter 'log-metrics-directory must not be null");
        cv.checkNonNull(logNamePrefix, "log-name-prefix", "Parameter 'log-name-prefix' must not be null");
        cv.checkNonNullPositive(logPollInterval, "log-poll-interval", "A positive number value is required for 'log-poll-interval'");

        cv.checkNonNull(pmEnablePercentiles, "pm-enable-percentiles", "Parameter 'pm-enable-percentiles' must not be null");
        cv.checkNonNull(pmTargetPercentiles, "pm-target-percentiles", "Parameter 'pm-target-percentiles' must not be null");
        cv.check(validPmTargetPercentiles, "pm-target-percentiles", "Parameter 'pm-target-percentiles' must be a valid decimal nubmer in range (0, 1)");
        cv.checkNonNullPositive(pmPercentilePrecision, "pm-percentile-precision", "A positive number value is required for 'pm-percentile-precision'");
        cv.checkNonNull(pmPercentileMetrics, "pm-percentile-metrics", "Parameter 'pm-percentile-metrics must not be null");
        cv.checkNonNull(pmEnableHistogramBuckets, "pm-enable-histogram-buckets", "Parameter 'pm-enable-histogram-buckets' must not be null.");
        cv.checkNonNull(pmHistogramBucketsMetrics, "pm-histogram-buckets-metrics", "Parameter 'pm-histogram-buckets-metrics' must not be null.");

        return cv.getInvalidParam();
    }

    /**
     * @return the exportMetrics
     */
    @JsonGetter("export-metrics")
    public Set<ExportMetrics> getExportMetrics()
    {
        return exportMetrics;
    }

    /**
     * Get the time unit for durations in CSV files.
     * 
     * @return the csvConvertDurationsTo
     */
    @JsonGetter("csv-convert-durations-to")
    public String getCsvConvertDurationsTo()
    {
        return csvConvertDurationsTo;
    }

    /**
     * Get the time unit for rates in CSV files.
     * 
     * @return the csvConvertRatesTo
     */
    @JsonGetter("csv-convert-rates-to")
    public String getCsvConvertRatesTo()
    {
        return csvConvertRatesTo;
    }

    /**
     * Get the directory where the CSV files are stored.
     * 
     * @return the csvMetricsDirectory
     */
    @JsonGetter("csv-metrics-directory")
    public String getCsvMetricsDirectory()
    {
        return csvMetricsDirectory;
    }

    /**
     * Get the CSV file name prefix.
     * 
     * @return the csvNamePrefix
     */
    @JsonGetter("csv-name-prefix")
    public String getCsvNamePrefix()
    {
        return csvNamePrefix;
    }

    /**
     * Get the polling interval of metrics in the CSV files.
     * 
     * @return the csvPollInterval
     */
    @JsonGetter("csv-poll-interval")
    public Integer getCsvPollInterval()
    {
        return csvPollInterval;
    }

    /**
     * Get the time unit for durations in log files.
     * 
     * @return the logConvertDurationsTo
     */
    @JsonGetter("log-convert-durations-to")
    public String getLogConvertDurationsTo()
    {
        return logConvertDurationsTo;
    }

    /**
     * Get the time unit for rates in log files.
     * 
     * @return the logConvertRatesTo
     */
    @JsonGetter("log-convert-rates-to")
    public String getLogConvertRatesTo()
    {
        return logConvertRatesTo;
    }

    /**
     * Get the directory where the log files are stored.
     * 
     * @return the logMetricsDirectory
     */
    @JsonGetter("log-metrics-directory")
    public String getLogMetricsDirectory()
    {
        return logMetricsDirectory;
    }

    /**
     * Get the log file name prefix.
     * 
     * @return the logNamePrefix
     */
    @JsonGetter("log-name-prefix")
    public String getLogNamePrefix()
    {
        return logNamePrefix;
    }

    /**
     * Get the polling interval of metrics in the log files.
     * 
     * @return the logPollInterval
     */
    @JsonGetter("log-poll-interval")
    public Integer getLogPollInterval()
    {
        return logPollInterval;
    }

    /**
     * Get the Prometheus percentiles enable.
     * 
     * @return the pmEnablePercentiles
     */
    @JsonGetter("pm-enable-percentiles")
    public Boolean getPmEnablePercentiles()
    {
        return pmEnablePercentiles;
    }

    /**
     * Get the list of Prometheus target percentiles.
     * 
     * @return the pmTargetPercentiles
     */
    @JsonGetter("pm-target-percentiles")
    public List<String> getPmTargetPercentiles()
    {
        return pmTargetPercentiles;
    }

    /**
     * Get the Prometheus percentile precision.
     * 
     * @return the pmPercentilePrecision
     */
    @JsonGetter("pm-percentile-precision")
    public Integer getPmPercentilePrecision()
    {
        return pmPercentilePrecision;
    }

    /**
     * Get the Prometheus metrics for which percentiles are produced.
     * 
     * @return the pmPercentileMetrics
     */
    @JsonGetter("pm-percentile-metrics")
    public List<String> getPmPercentileMetrics()
    {
        return pmPercentileMetrics;
    }

    /**
     * Get the Prometheus histogram buckets enable.
     * 
     * @return the pmEnableHistogramBuckets
     */
    @JsonGetter("pm-enable-histogram-buckets")
    public Boolean getPmEnableHistogramBuckets()
    {
        return pmEnableHistogramBuckets;
    }

    /**
     * Get the Prometheus metrics for which histogram buckets are produced.
     * 
     * @return the pmHistogramBucketsMetrics
     */
    @JsonGetter("pm-histogram-buckets-metrics")
    public List<String> getPmHistogramBucketsMetrics()
    {
        return pmHistogramBucketsMetrics;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((csvConvertDurationsTo == null) ? 0 : csvConvertDurationsTo.hashCode());
        result = prime * result + ((csvConvertRatesTo == null) ? 0 : csvConvertRatesTo.hashCode());
        result = prime * result + ((csvMetricsDirectory == null) ? 0 : csvMetricsDirectory.hashCode());
        result = prime * result + ((csvNamePrefix == null) ? 0 : csvNamePrefix.hashCode());
        result = prime * result + ((csvPollInterval == null) ? 0 : csvPollInterval.hashCode());
        result = prime * result + ((exportMetrics == null) ? 0 : exportMetrics.hashCode());
        result = prime * result + ((logConvertDurationsTo == null) ? 0 : logConvertDurationsTo.hashCode());
        result = prime * result + ((logConvertRatesTo == null) ? 0 : logConvertRatesTo.hashCode());
        result = prime * result + ((logMetricsDirectory == null) ? 0 : logMetricsDirectory.hashCode());
        result = prime * result + ((logNamePrefix == null) ? 0 : logNamePrefix.hashCode());
        result = prime * result + ((logPollInterval == null) ? 0 : logPollInterval.hashCode());
        result = prime * result + ((pmEnableHistogramBuckets == null) ? 0 : pmEnableHistogramBuckets.hashCode());
        result = prime * result + ((pmEnablePercentiles == null) ? 0 : pmEnablePercentiles.hashCode());
        result = prime * result + ((pmHistogramBucketsMetrics == null) ? 0 : pmHistogramBucketsMetrics.hashCode());
        result = prime * result + ((pmPercentileMetrics == null) ? 0 : pmPercentileMetrics.hashCode());
        result = prime * result + ((pmPercentilePrecision == null) ? 0 : pmPercentilePrecision.hashCode());
        result = prime * result + ((pmTargetPercentiles == null) ? 0 : pmTargetPercentiles.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MetricsConfiguration other = (MetricsConfiguration) obj;
        if (csvConvertDurationsTo == null)
        {
            if (other.csvConvertDurationsTo != null)
                return false;
        }
        else if (!csvConvertDurationsTo.equals(other.csvConvertDurationsTo))
            return false;
        if (csvConvertRatesTo == null)
        {
            if (other.csvConvertRatesTo != null)
                return false;
        }
        else if (!csvConvertRatesTo.equals(other.csvConvertRatesTo))
            return false;
        if (csvMetricsDirectory == null)
        {
            if (other.csvMetricsDirectory != null)
                return false;
        }
        else if (!csvMetricsDirectory.equals(other.csvMetricsDirectory))
            return false;
        if (csvNamePrefix == null)
        {
            if (other.csvNamePrefix != null)
                return false;
        }
        else if (!csvNamePrefix.equals(other.csvNamePrefix))
            return false;
        if (csvPollInterval == null)
        {
            if (other.csvPollInterval != null)
                return false;
        }
        else if (!csvPollInterval.equals(other.csvPollInterval))
            return false;
        if (exportMetrics == null)
        {
            if (other.exportMetrics != null)
                return false;
        }
        else if (!exportMetrics.equals(other.exportMetrics))
            return false;
        if (logConvertDurationsTo == null)
        {
            if (other.logConvertDurationsTo != null)
                return false;
        }
        else if (!logConvertDurationsTo.equals(other.logConvertDurationsTo))
            return false;
        if (logConvertRatesTo == null)
        {
            if (other.logConvertRatesTo != null)
                return false;
        }
        else if (!logConvertRatesTo.equals(other.logConvertRatesTo))
            return false;
        if (logMetricsDirectory == null)
        {
            if (other.logMetricsDirectory != null)
                return false;
        }
        else if (!logMetricsDirectory.equals(other.logMetricsDirectory))
            return false;
        if (logNamePrefix == null)
        {
            if (other.logNamePrefix != null)
                return false;
        }
        else if (!logNamePrefix.equals(other.logNamePrefix))
            return false;
        if (logPollInterval == null)
        {
            if (other.logPollInterval != null)
                return false;
        }
        else if (!logPollInterval.equals(other.logPollInterval))
            return false;
        if (pmEnableHistogramBuckets == null)
        {
            if (other.pmEnableHistogramBuckets != null)
                return false;
        }
        else if (!pmEnableHistogramBuckets.equals(other.pmEnableHistogramBuckets))
            return false;
        if (pmEnablePercentiles == null)
        {
            if (other.pmEnablePercentiles != null)
                return false;
        }
        else if (!pmEnablePercentiles.equals(other.pmEnablePercentiles))
            return false;
        if (pmHistogramBucketsMetrics == null)
        {
            if (other.pmHistogramBucketsMetrics != null)
                return false;
        }
        else if (!pmHistogramBucketsMetrics.equals(other.pmHistogramBucketsMetrics))
            return false;
        if (pmPercentileMetrics == null)
        {
            if (other.pmPercentileMetrics != null)
                return false;
        }
        else if (!pmPercentileMetrics.equals(other.pmPercentileMetrics))
            return false;
        if (pmPercentilePrecision == null)
        {
            if (other.pmPercentilePrecision != null)
                return false;
        }
        else if (!pmPercentilePrecision.equals(other.pmPercentilePrecision))
            return false;
        if (pmTargetPercentiles == null)
        {
            if (other.pmTargetPercentiles != null)
                return false;
        }
        else if (!pmTargetPercentiles.equals(other.pmTargetPercentiles))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "MetricsConfiguration [exportMetrics=" + exportMetrics + ", csvConvertDurationsTo=" + csvConvertDurationsTo + ", csvConvertRatesTo="
               + csvConvertRatesTo + ", csvMetricsDirectory=" + csvMetricsDirectory + ", csvNamePrefix=" + csvNamePrefix + ", csvPollInterval="
               + csvPollInterval + ", logConvertDurationsTo=" + logConvertDurationsTo + ", logConvertRatesTo=" + logConvertRatesTo + ", logMetricsDirectory="
               + logMetricsDirectory + ", logNamePrefix=" + logNamePrefix + ", logPollInterval=" + logPollInterval + ", pmEnablePercentiles="
               + pmEnablePercentiles + ", pmTargetPercentiles=" + pmTargetPercentiles + ", pmPercentilePrecision=" + pmPercentilePrecision
               + ", pmPercentileMetrics=" + pmPercentileMetrics + ", pmEnableHistogramBuckets=" + pmEnableHistogramBuckets + ", pmHistogramBucketsMetrics="
               + pmHistogramBucketsMetrics + "]";
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder
    {
        @JsonProperty("export-metrics")
        private Set<ExportMetrics> exportMetrics = Set.of();

        @JsonProperty("csv-convert-durations-to")
        private String csvConvertDurationsTo = "MILLISECONDS";
        @JsonProperty("csv-convert-rates-to")
        private String csvConvertRatesTo = "SECONDS";
        @JsonProperty("csv-metrics-directory")
        private String csvMetricsDirectory = "/opt/bsf-load/metrics";
        @JsonProperty("csv-name-prefix")
        private String csvNamePrefix = "metrics";
        @JsonProperty("csv-poll-interval")
        private Integer csvPollInterval = 1;

        @JsonProperty("log-convert-durations-to")
        private String logConvertDurationsTo = "MILLISECONDS";
        @JsonProperty("log-convert-rates-to")
        private String logConvertRatesTo = "SECONDS";
        @JsonProperty("log-metrics-directory")
        private String logMetricsDirectory = "/opt/bsf-load/metrics";
        @JsonProperty("log-name-prefix")
        private String logNamePrefix = "metrics";
        @JsonProperty("log-poll-interval")
        private Integer logPollInterval = 10;

        @JsonProperty("pm-enable-percentiles")
        private Boolean pmEnablePercentiles = false;
        @JsonProperty("pm-target-percentiles")
        private List<String> pmTargetPercentiles = List.of("0.75", "0.9");
        @JsonProperty("pm-percentile-precision")
        private Integer pmPercentilePrecision = 1;
        @JsonProperty("pm-percentile-metrics")
        private List<String> pmPercentileMetrics = List.of("vertx.http.client.response.time");
        @JsonProperty("pm-enable-histogram-buckets")
        private Boolean pmEnableHistogramBuckets = false;
        @JsonProperty("pm-histogram-buckets-metrics")
        private List<String> pmHistogramBucketsMetrics = List.of("vertx.http.client.response.time");

        /**
         * Set the method of exporting metrics.
         * 
         * @param exportMetrics The method according to {@link ExportMetrics}.
         * @return Builder The builder.
         */
        public Builder exportMetrics(List<ExportMetrics> exportMetrics)
        {
            this.exportMetrics = Set.copyOf(exportMetrics);
            return this;
        }

        /**
         * Convert durations to the given time unit. Applicable only if CSV_FILE
         * exporting is enabled.
         * 
         * @param csvConvertDurationsTo String corresponding to {@link TimeUnit}.
         * @return Builder The builder.
         */
        public Builder csvConvertDurationsTo(String csvConvertDurationsTo)
        {
            this.csvConvertDurationsTo = csvConvertDurationsTo;
            return this;
        }

        /**
         * Convert rates to the given time unit. Applicable only if CSV_FILE exporting
         * is enabled.
         * 
         * @param csvConvertRatesTo String corresponding to {@link TimeUnit}.
         * @return Builder The builder.
         */
        public Builder csvConvertRatesTo(String csvConvertRatesTo)
        {
            this.csvConvertRatesTo = csvConvertRatesTo;
            return this;
        }

        /**
         * Set the directory where the CSV files are stored. Applicable only if CSV_FILE
         * exporting is enabled.
         * 
         * @param csvMetricsDirectory The metrics directory.
         * @return Builder The builder.
         */
        public Builder csvMetricsDirectory(String csvMetricsDirectory)
        {
            this.csvMetricsDirectory = csvMetricsDirectory;
            return this;
        }

        /**
         * Set the prefix of the directory that contains the exported CSV files.
         * Applicable only if CSV_FILE exporting is enabled.
         * 
         * @param csvNamePrefix The String prefix.
         * @return Builder The builder.
         */
        public Builder csvNamePrefix(String csvNamePrefix)
        {
            this.csvNamePrefix = csvNamePrefix;
            return this;
        }

        /**
         * Set the polling interval in seconds of the exported values in the CSV files.
         * Applicable only if CSV_FILE exporting is enabled.
         * 
         * @param csvPollInterval The polling period in seconds.
         * @return Builder The builder.
         */
        public Builder csvPollInterval(Integer csvPollInterval)
        {
            this.csvPollInterval = csvPollInterval;
            return this;
        }

        /**
         * Convert durations to the given time unit. Applicable only if LOG_FILE
         * exporting is enabled.
         * 
         * @param logConvertDurationsTo String corresponding to {@link TimeUnit}.
         * @return Builder The builder.
         */
        public Builder logConvertDurationsTo(String logConvertDurationsTo)
        {
            this.logConvertDurationsTo = logConvertDurationsTo;
            return this;
        }

        /**
         * Convert rates to the given time unit. Applicable only if LOG_FILE exporting
         * is enabled.
         * 
         * @param logConvertRatesTo String corresponding to {@link TimeUnit}.
         * @return Builder The builder.
         */
        public Builder logConvertRatesTo(String logConvertRatesTo)
        {
            this.logConvertRatesTo = logConvertRatesTo;
            return this;
        }

        /**
         * Set the directory where the log files are stored. Applicable only if LOG_FILE
         * exporting is enabled.
         * 
         * @param logMetricsDirectory The metrics directory.
         * @return Builder The builder.
         */
        public Builder logMetricsDirectory(String logMetricsDirectory)
        {
            this.logMetricsDirectory = logMetricsDirectory;
            return this;
        }

        /**
         * Set the prefix of the directory that contains the exported log files.
         * Applicable only if LOG_FILE exporting is enabled.
         * 
         * @param logNamePrefix The String prefix.
         * @return Builder The builder.
         */
        public Builder logNamePrefix(String logNamePrefix)
        {
            this.logNamePrefix = logNamePrefix;
            return this;
        }

        /**
         * Set the polling interval in seconds of the exported values in the log files.
         * Applicable only if LOG_FILE exporting is enabled.
         * 
         * @param logPollInterval The polling period in seconds.
         * @return Builder The builder.
         */
        public Builder logPollInterval(Integer logPollInterval)
        {
            this.logPollInterval = logPollInterval;
            return this;
        }

        /**
         * Enable or disable percentiles. Produces an additional time series for each
         * requested percentile. This percentile is computed locally, and so it can't be
         * aggregated with percentiles computed across other dimensions (e.g. in a
         * different instance). Applicable only if PROMETHEUS exporting is enabled.
         * 
         * @param pmEnablePercentiles Set to true to enable, false otherwise.
         * @return Builder The builder.
         */
        public Builder pmEnablePercentiles(Boolean pmEnablePercentiles)
        {
            this.pmEnablePercentiles = pmEnablePercentiles;
            return this;
        }

        /**
         * Set the target percentiles. Applicable only if PROMETHEUS exporting is
         * enabled.
         * 
         * @param pmTargetPercentiles A list of Strings with the target percentiles.
         *                            Only decimal values in range (0, 1) are valid.
         * @return Builder The builder.
         */
        public Builder pmTargetPercentiles(List<String> pmTargetPercentiles)
        {
            this.pmTargetPercentiles = pmTargetPercentiles;
            return this;
        }

        /**
         * Determines the number of digits of precision to maintain on the dynamic range
         * histogram used to compute percentile approximations. The higher the degrees
         * of precision, the more accurate the approximation is at the cost of more
         * memory. Applicable only if PROMETHEUS exporting is enabled.
         * 
         * @param pmPercentilePrecision
         * @return Builder The builder.
         */
        public Builder pmPercentilePrecision(Integer pmPercentilePrecision)
        {
            this.pmPercentilePrecision = pmPercentilePrecision;
            return this;
        }

        /**
         * Set the name of the metrics for which percentiles are produced. Applicable
         * only if PROMETHEUS exporting is enabled.
         * 
         * @param pmPercentileMetrics A list of Strings of dot-separated metrics names.
         * @return Builder The builder.
         */
        public Builder pmPercentileMetrics(List<String> pmPercentileMetrics)
        {
            this.pmPercentileMetrics = pmPercentileMetrics;
            return this;
        }

        /**
         * Enable or disable the publication of a histogram that can be used to generate
         * aggregable percentile approximations on the Prometheus monitoring system.
         * Applicable only if PROMETHEUS exporting is enabled.
         * 
         * @param pmEnableHistogramBuckets Set to true to enable, false otherwise.
         * @return Builder The builder.
         */
        public Builder pmEnableHistogramBuckets(Boolean pmEnableHistogramBuckets)
        {
            this.pmEnableHistogramBuckets = pmEnableHistogramBuckets;
            return this;
        }

        /**
         * Set the name of the metrics for which histogram buckets are produced.
         * Applicable only if PROMETHEUS exporting is enabled.
         * 
         * @param pmHistogramBucketsMetrics A list of Strings of dot-separated metrics
         *                                  names.
         * @return Builder The builder.
         */
        public Builder pmHistogramBucketsMetrics(List<String> pmHistogramBucketsMetrics)
        {
            this.pmHistogramBucketsMetrics = pmHistogramBucketsMetrics;
            return this;
        }

        /**
         * Create the MetricsConfiguration object.
         * 
         * @return MetricsConfiguration The configuration options for the metrics.
         */
        public MetricsConfiguration build()
        {
            return new MetricsConfiguration(this);
        }
    }
}
