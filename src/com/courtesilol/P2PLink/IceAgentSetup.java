package com.courtesilol.P2PLink;

import java.net.InetAddress;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.ice.Agent;
import org.ice4j.ice.harvest.StunCandidateHarvester;

/**
 *
 * @author javier
 */
public class IceAgentSetup {

    public static Agent createAgentWithStun() throws Exception {
        Agent agent = new Agent();
        agent.setControlling(true); // Uno de los pares debe ser el controlador

        // Agregar un cosechador de candidatos STUN
        String stunServer = "stun.l.google.com";
        int stunPort = 19302;
        TransportAddress stunAddress = new TransportAddress(InetAddress.getByName(stunServer), stunPort, Transport.UDP);
        agent.addCandidateHarvester(new StunCandidateHarvester(stunAddress));

        // Los componentes se crean automáticamente al agregar cosechadores
        return agent;
    }

}
