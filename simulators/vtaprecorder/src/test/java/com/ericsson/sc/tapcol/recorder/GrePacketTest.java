package com.ericsson.sc.tapcol.recorder;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.testng.annotations.Test;

import reactor.core.publisher.Flux;

public class GrePacketTest
{

    @Test
    void greDecodingTest() throws IOException
    {

        final var gres = IntStream.range(1, 11)
                                  .mapToObj(i -> new File(String.format("src/test/resources/udpPayload%s.bin", i)))
                                  .map(GrePacket::fromFile)
                                  .collect(Collectors.toList());
        assertEquals(gres.get(0).getPayloadType(), GrePacket.PayloadType.JSON);
        assertEquals(gres.get(1).getPayloadType(), GrePacket.PayloadType.PCAPNG_HEADERS);
        assertEquals(gres.get(2).getPayloadType(), GrePacket.PayloadType.PCAPNG_DATA);
        assertEquals(gres.get(3).getPayloadType(), GrePacket.PayloadType.PCAPNG_DATA);
        assertEquals(gres.get(4).getPayloadType(), GrePacket.PayloadType.PCAPNG_DATA);
        assertEquals(gres.get(5).getPayloadType(), GrePacket.PayloadType.PCAPNG_DATA);
        assertEquals(gres.get(6).getPayloadType(), GrePacket.PayloadType.JSON);
        assertEquals(gres.get(7).getPayloadType(), GrePacket.PayloadType.PCAPNG_HEADERS);
        assertEquals(gres.get(8).getPayloadType(), GrePacket.PayloadType.PCAPNG_DATA);
        assertEquals(gres.get(9).getPayloadType(), GrePacket.PayloadType.PCAPNG_DATA);

        final var gresFlux = Flux.fromIterable(gres);
        Files.createTempFile("testgre", ".pcapng");
        final var writer = new GrePacketWriter(gresFlux, "/tmp/gres.pcapng", null);
        writer.start();
        writer.onFinished().block();
    }
}
