/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jun 23, 2022
 *     Author: epitgio
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 
 */
public class LocalReplyMappingDefaults
{

    // Mapping to cause attribute in ProblemDetail json element according to 3GPP
    private static final String MANDATORY_IE_INCORRECT = "MANDATORY_IE_INCORRECT";
    private static final String UNSPECIFIED_MSG_FAILURE = "UNSPECIFIED_MSG_FAILURE";
    private static final String MANDATORY_IE_MISSING = "MANDATORY_IE_MISSING";
    private static final String NF_FAILOVER = "NF_FAILOVER";
    private static final String NF_CONGESTION_RISK = "NF_CONGESTION_RISK";
    private static final String TARGET_NF_NOT_REACHABLE = "TARGET_NF_NOT_REACHABLE";
    private static final String INSUFFICIENT_RESOURCES = "INSUFFICIENT_RESOURCES";
    private static final String SYSTEM_FAILURE = "SYSTEM_FAILURE";

    // Routing behavior
    private static final String STRICT_ROUTING = "STRICT";
    private static final String ROUND_ROBIN = "ROUND_ROBIN";
    private static final String PREFERRED_ROUTING = "PREFERRED";
    private static final String DYNAMIC_FWD = "STRICT_DFP";

    /*
     * Based on the data filtering of each case the following filter combinations
     * are used:
     * 
     * 0: No action: In case screening action or routing rule is configured then the
     * result code and message body is kept unchanged.
     * 
     * 1: Status code filter
     * 
     * 2: And filter with Status code filter + Response flag filter
     * 
     * 3: And filter with Status code filter + Response flag filter + Metadata
     * filter (routing behavior)
     * 
     * 4: And filter with Status code filter + Metadata filter (routing behavior)
     * 
     * 5: Response flag filter
     * 
     * 6: No action: In case screening action or routing rule is configured then the
     * result code and message body is kept unchanged.
     * 
     */
    private static final int NO_ACTION = 0;
    private static final int STATUS_CODE_FILTER = 1;
    private static final int STATUS_FLAG_FILTER = 2;
    private static final int STATUS_FLAG_META_FILTER = 3;
    private static final int STATUS_META_FILTER = 4;
    private static final int RESPONSE_FLAG_FILTER = 5;

    // Response flags
    private static final String UMSDR_FLAG = "UMSDR";

    List<ProxyLocalReplyMapping> filters = new ArrayList<>();

    public LocalReplyMappingDefaults()
    {
        createDefaultMapping();
    }

    private List<ProxyLocalReplyMapping> createDefaultMapping()
    {

        // Filters according to
        // https://ericsson.sharepoint.com/:x:/r/sites/5GSCDevelopment/_layouts/15/doc2.aspx?sourcedoc=%7B69b54a40-c701-4d3c-97dc-fa8c0bc9c03a%7D&action=edit&activeCell=%27Error%20triggers%20and%20codes%27!A121&wdinitialsession=e95e3775-1150-4718-8ac5-702a406b5cfa&wdrldsc=8&wdrldc=1&wdrldr=AccessTokenExpiredWarning%2CRefreshingExpiredAccessT
        filters.add(new ProxyLocalReplyMapping(0, 0, "", List.of(), null, NO_ACTION)); // Configured - No action
        filters.add(new ProxyLocalReplyMapping(418, 400, UNSPECIFIED_MSG_FAILURE, null, null, STATUS_CODE_FILTER)); // Manager send a dummy status code 418 for
        // the case "Route not found (No VHost
        // match)"

        filters.add(new ProxyLocalReplyMapping(0, 500, SYSTEM_FAILURE, List.of("NC"), null, RESPONSE_FLAG_FILTER)); // row114

        filters.add(new ProxyLocalReplyMapping(400, 400, MANDATORY_IE_INCORRECT, List.of("E_IVH"), null, STATUS_FLAG_FILTER)); // row4
        filters.add(new ProxyLocalReplyMapping(400, 400, MANDATORY_IE_MISSING, List.of(), null, STATUS_CODE_FILTER)); // row3

        filters.add(new ProxyLocalReplyMapping(403, 400, UNSPECIFIED_MSG_FAILURE, List.of(), null, STATUS_CODE_FILTER)); // row6

        filters.add(new ProxyLocalReplyMapping(404, 400, UNSPECIFIED_MSG_FAILURE, List.of("E_APR", "NR"), null, STATUS_FLAG_FILTER)); // row8-9
        filters.add(new ProxyLocalReplyMapping(404, 400, MANDATORY_IE_MISSING, List.of("E_MPR"), null, STATUS_FLAG_FILTER)); // row7
        filters.add(new ProxyLocalReplyMapping(404, 400, UNSPECIFIED_MSG_FAILURE, List.of(), null, STATUS_CODE_FILTER)); // row113

        filters.add(new ProxyLocalReplyMapping(408, 500, NF_FAILOVER, List.of("UT", UMSDR_FLAG, "DT", "SI"), DYNAMIC_FWD, STATUS_FLAG_META_FILTER));// row19&21&24
        filters.add(new ProxyLocalReplyMapping(408, 500, NF_FAILOVER, List.of("UT", UMSDR_FLAG, "DT", "SI"), STRICT_ROUTING, STATUS_FLAG_META_FILTER));// row19&21&24

        filters.add(new ProxyLocalReplyMapping(408,
                                               504,
                                               TARGET_NF_NOT_REACHABLE,
                                               List.of("UT", UMSDR_FLAG, "DT", "SI"),
                                               PREFERRED_ROUTING,
                                               STATUS_FLAG_META_FILTER));// row20&22&25
        filters.add(new ProxyLocalReplyMapping(408, 504, TARGET_NF_NOT_REACHABLE, List.of("UT", UMSDR_FLAG, "DT", "SI"), ROUND_ROBIN, STATUS_FLAG_META_FILTER));

        filters.add(new ProxyLocalReplyMapping(413, 413, null, List.of("E_PTL"), null, STATUS_FLAG_FILTER)); // row26

        filters.add(new ProxyLocalReplyMapping(426, 400, UNSPECIFIED_MSG_FAILURE, List.of(), null, STATUS_CODE_FILTER)); // row 28

        filters.add(new ProxyLocalReplyMapping(429, 429, NF_CONGESTION_RISK, List.of("RL"), null, STATUS_FLAG_FILTER)); // row 30

        filters.add(new ProxyLocalReplyMapping(500, 500, INSUFFICIENT_RESOURCES, List.of("E_PTL"), null, STATUS_FLAG_FILTER)); // row 31
        filters.add(new ProxyLocalReplyMapping(500, 500, SYSTEM_FAILURE, List.of("RLSE"), null, STATUS_FLAG_FILTER)); // row 32

        filters.add(new ProxyLocalReplyMapping(502, 500, NF_FAILOVER, List.of("UPE"), STRICT_ROUTING, STATUS_FLAG_META_FILTER)); // row 33 & 37
        filters.add(new ProxyLocalReplyMapping(502, 500, NF_FAILOVER, List.of("UPE"), DYNAMIC_FWD, STATUS_FLAG_META_FILTER)); // row 33 & 37
        filters.add(new ProxyLocalReplyMapping(502, 504, TARGET_NF_NOT_REACHABLE, List.of("UPE"), ROUND_ROBIN, STATUS_FLAG_META_FILTER)); // row 34 & 38
        filters.add(new ProxyLocalReplyMapping(502, 504, TARGET_NF_NOT_REACHABLE, List.of("UPE"), PREFERRED_ROUTING, STATUS_FLAG_META_FILTER)); // row 34 & 38
        filters.add(new ProxyLocalReplyMapping(502, 500, SYSTEM_FAILURE, List.of(), null, STATUS_CODE_FILTER)); // row 41

        filters.add(new ProxyLocalReplyMapping(503, 500, NF_FAILOVER, List.of("UH"), STRICT_ROUTING, STATUS_FLAG_META_FILTER)); // row 44
        filters.add(new ProxyLocalReplyMapping(503, 500, NF_FAILOVER, List.of("UH"), DYNAMIC_FWD, STATUS_FLAG_META_FILTER)); // row 44

        filters.add(new ProxyLocalReplyMapping(503, 504, TARGET_NF_NOT_REACHABLE, List.of("UH"), ROUND_ROBIN, STATUS_FLAG_META_FILTER)); // row 45
        filters.add(new ProxyLocalReplyMapping(503, 504, TARGET_NF_NOT_REACHABLE, List.of("UH"), PREFERRED_ROUTING, STATUS_FLAG_META_FILTER)); // row 45
        filters.add(new ProxyLocalReplyMapping(503,
                                               500,
                                               NF_FAILOVER,
                                               List.of("UF", "UC", "LR", "UO", "UR", "OM", "URX"),
                                               STRICT_ROUTING,
                                               STATUS_FLAG_META_FILTER)); // row 35
        filters.add(new ProxyLocalReplyMapping(503,
                                               500,
                                               NF_FAILOVER,
                                               List.of("UF", "UC", "LR", "UO", "UR", "OM", "URX"),
                                               DYNAMIC_FWD,
                                               STATUS_FLAG_META_FILTER)); // row 35
        filters.add(new ProxyLocalReplyMapping(503,
                                               504,
                                               TARGET_NF_NOT_REACHABLE,
                                               List.of("UF", "UC", "LR", "UO", "UR", "OM", "URX"),
                                               ROUND_ROBIN,
                                               STATUS_FLAG_META_FILTER)); // row 36
        filters.add(new ProxyLocalReplyMapping(503,
                                               504,
                                               TARGET_NF_NOT_REACHABLE,
                                               List.of("UF", "UC", "LR", "UO", "UR", "OM", "URX"),
                                               PREFERRED_ROUTING,
                                               STATUS_FLAG_META_FILTER)); // row 36
        filters.add(new ProxyLocalReplyMapping(503,
                                               500,
                                               NF_FAILOVER,
                                               List.of("UF", "UC", "LR", "UO", "UR", "OM", "URX"),
                                               STRICT_ROUTING,
                                               STATUS_FLAG_META_FILTER)); // row 41
        filters.add(new ProxyLocalReplyMapping(503,
                                               500,
                                               NF_FAILOVER,
                                               List.of("UF", "UC", "LR", "UO", "UR", "OM", "URX"),
                                               DYNAMIC_FWD,
                                               STATUS_FLAG_META_FILTER)); // row 41
        filters.add(new ProxyLocalReplyMapping(503, 500, SYSTEM_FAILURE, List.of("DPE"), null, STATUS_FLAG_FILTER)); // row 52
        filters.add(new ProxyLocalReplyMapping(503, 429, NF_CONGESTION_RISK, List.of("RLSE"), null, STATUS_FLAG_FILTER)); // row 77
        filters.add(new ProxyLocalReplyMapping(503, 500, SYSTEM_FAILURE, List.of(), DYNAMIC_FWD, STATUS_META_FILTER)); // row 54-55 DYNAMIC FWD

        filters.add(new ProxyLocalReplyMapping(504, 500, NF_FAILOVER, List.of("UT"), STRICT_ROUTING, STATUS_FLAG_META_FILTER)); // row78
        filters.add(new ProxyLocalReplyMapping(504, 500, NF_FAILOVER, List.of("UT"), DYNAMIC_FWD, STATUS_FLAG_META_FILTER)); // row78
        filters.add(new ProxyLocalReplyMapping(504, 504, TARGET_NF_NOT_REACHABLE, List.of("UT"), ROUND_ROBIN, STATUS_FLAG_META_FILTER)); // row79
        filters.add(new ProxyLocalReplyMapping(504, 504, TARGET_NF_NOT_REACHABLE, List.of("UT"), PREFERRED_ROUTING, STATUS_FLAG_META_FILTER)); // row79

        return filters;

    }

    /**
     * @return the filters
     */
    public List<ProxyLocalReplyMapping> getFilters()
    {
        return filters;
    }

    /**
     * @param filters the filters to set
     */
    public void setFilters(List<ProxyLocalReplyMapping> filters)
    {
        this.filters = filters;
    }

    class ProxyLocalReplyMapping
    {

        private final int envoyStatusCode;
        private final int threeGppStatusCode;
        private final String title;
        private final int status;
        private Optional<String> cause = Optional.empty();
        private List<String> envoyResponseFlag = new ArrayList<>();
        private final String routingType;
        private final int filterType;

        /**
         * 
         */
        public ProxyLocalReplyMapping(int envoyStatusCode,
                                      int threeGppStatusCode,
                                      String cause,
                                      List<String> envoyResponseFlag,
                                      String routingType,
                                      int filterType)
        {
            this.envoyStatusCode = envoyStatusCode;
            this.threeGppStatusCode = threeGppStatusCode;
            switch (threeGppStatusCode)
            {
                case 429:
                    this.title = "Too Many Requests";
                    break;
                case 504:
                    this.title = "Gateway Timeout";
                    break;
                case 400:
                    this.title = "Bad request";
                    break;
                case 413:
                    this.title = "Payload Too Large";
                    break;
                default:
                    this.title = "Internal Server Error";
            }
            this.status = threeGppStatusCode;
            this.cause = Optional.ofNullable(cause);
            this.envoyResponseFlag = envoyResponseFlag;
            this.routingType = routingType;
            this.filterType = filterType;
        }

        /**
         * @return the envoyStatusCode
         */
        public int getEnvoyStatusCode()
        {
            return envoyStatusCode;
        }

        /**
         * @return the threeGppStatusCode
         */
        public int getThreeGppStatusCode()
        {
            return threeGppStatusCode;
        }

        /**
         * @return the title
         */
        public String getTitle()
        {
            return title;
        }

        /**
         * @return the status
         */
        public int getStatus()
        {
            return status;
        }

        /**
         * @return the cause
         */
        public Optional<String> getCause()
        {
            return cause;
        }

        /**
         * @return the envoyResponseFlag
         */
        public List<String> getEnvoyResponseFlag()
        {
            return envoyResponseFlag;
        }

        /**
         * @return the routingTypes
         */
        public String getRoutingType()
        {
            return routingType;
        }

        /**
         * @return the filterType
         */
        public int getFilterType()
        {
            return filterType;
        }

    }

}
