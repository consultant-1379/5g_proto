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
 * Created on: Jul 26, 2021
 *     Author: epitgio
 */

package com.ericsson.sc.glue;

import com.ericsson.utilities.common.IfNamedListItem;

/**
 * 
 */
public interface IfEgressConnectionProfile extends IfNamedListItem
{
    Integer getHpackTableSize();

    Integer getMaxRequests();

    Integer getMaxPendingRequests();

    Integer getMaxConnections();

    Integer getMaxConcurrentStreams();

    IfTcpKeepalive getTcpKeepalive();

    Integer getConnectionIdleTimeout();

    Integer getMaxConnectionDuration();

    Integer getTcpConnectTimeout();

    Boolean getEnableMessageSizeMeasurement();

    Integer getDscpMarking();
}
