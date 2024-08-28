package com.ericsson.esc.bsf.openapi.model;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.ericsson.esc.lib.ValidationException;
import com.google.common.net.InetAddresses;

public class Ipv6PrefixTest
{

    @Test
    public void positive()
    {
        var p = new Ipv6Prefix("FFFF:FFFF:1234:56FF:1234:5678:1111:FFFF/96");
        assertTrue(p.getPrefixAddress().equals(InetAddresses.forString("FFFF:FFFF:1234:56FF:1234:5678::")));
        assertTrue(p.getPrefixLength() == 96);
    }

    @Test
    public void negative()
    {
        var p = new Ipv6Prefix("FFFF:FFFF:1234:56FF:1234:5678:1111:FFFF/96");
        assertFalse(p.getPrefixAddress().equals(InetAddresses.forString("FFFF:FFFF:1234:56FF:1234:5678:1111:FFFF")));
        assertTrue(p.getPrefixLength() == 96);
    }

    @Test
    public void wrongIp()
    {
        try
        {
            new Ipv6Prefix("FFFF:FFFF:1234:56FF:1234:5678:1111:FFFQ/96");
        }
        catch (ValidationException e)
        {
            validateException(e);
        }
    }

    private void validateException(ValidationException e)
    {
        assertSame(e.getErrorType(), ValidationException.ErrorType.SYNTAX_ERROR);
        assertSame(e.getInvalidParams().size(), 1);
        assertTrue(e.getInvalidParams().get(0).getParam().equals("ipv6Prefix"));
    }
}
