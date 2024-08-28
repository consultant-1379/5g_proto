/**
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 27, 2022
 *     Author: echfari
 */
package com.ericsson.sc.tapcol;

import java.util.List;
import java.util.function.UnaryOperator;

import com.ericsson.sc.tapcol.pcap.TapPacket;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Consumes sequences of captured packets and notifies packet producer when
 * finished
 *
 */
public interface PacketSink
{
    /**
     * Define how each {@link PacketSink} sequence should be consumed
     * 
     * @param packetSequence The packet sequence to consume. Note that a single
     *                       PacketSink might consume multiple sequences in parallel
     * @return A Mono that indicates the completion of sequence consumption
     */
    Mono<Void> consumePacketStream(Flux<List<TapPacket>> packetSequence);

    /**
     * Combine two sinks. Both sinks will receive the same packets. Processing is
     * considered finished when both sinks complete
     * 
     * @param other The second packet sink to combine with this packet sink
     * @return The combined packet sink
     */
    default PacketSink combineWith(PacketSink other)
    {
        return inFlow ->
        {
            final var inFlowCommon = inFlow.publish().autoConnect();
            return consumePacketStream(inFlowCommon).or(other.consumePacketStream(inFlowCommon));
        };
    }

    /**
     * Modify packet sink input
     * 
     * @param sequenceTransformer A function that applies the wanted input
     *                            transformation
     * @return A new packet sink with modified behavior
     */
    default PacketSink modifyInput(UnaryOperator<Flux<List<TapPacket>>> sequenceTransformer)
    {
        return inFlow -> this.consumePacketStream(sequenceTransformer.apply(inFlow));
    }
}