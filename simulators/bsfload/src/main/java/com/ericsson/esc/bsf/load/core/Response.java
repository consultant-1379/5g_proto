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
 * Created on: Jul 14, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.core;

import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;

/**
 * 
 */
public class Response
{
    final String error;
    final String resourceId;
    final boolean success;
    final Throwable t;

    public Response(Throwable t)
    {

        this.error = null;
        this.resourceId = null;
        this.success = false;
        this.t = t;
    }

    public Response(HttpResponse<Buffer> resp)
    {
        this.t = null;
        if (resp.statusCode() > 299)
        {
            this.error = resp.bodyAsString();
            this.resourceId = null;
            this.success = false;
        }
        else
        {
            this.error = null;
            success = true;

            var location = resp.getHeader("Location");
            if (location != null)
            {
                var index = location.lastIndexOf("/");
                this.resourceId = location.substring(index + 1, location.length());
            }
            else
            {
                this.resourceId = "";
            }
        }
    }

    public String getErrorMsg()
    {
        StringBuilder builder = new StringBuilder();
        if (t != null)
        {
            builder.append("Client error={");
            builder.append(t);
            builder.append("} ");
        }
        if (error != null)
        {
            builder.append("Failed request=");
            builder.append(error);
        }
        return builder.toString();
    }

    @Override
    public String toString()
    {
        return "Response [error=" + error + ", resourceId=" + resourceId + ", success=" + success + ", t=" + t + "]";
    }
}
