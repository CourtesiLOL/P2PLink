package com.courtesilol.P2PLink.records;

import com.courtesilol.P2PLink.enums.Protocol;
import org.ice4j.ice.CandidateType;

/**
 *
 * @author javier
 */
public record Candidate(String ip, int port,CandidateType type, Protocol protocol) {

}
