/*
 */
package com.courtesilol.P2PLink;

import com.courtesilol.P2PLink.server.FileReceiverServer;
import com.courtesilol.P2PLink.client.FileSenderClient;
import com.courtesilol.P2PLink.client.FileSenderClientIce;
import com.courtesilol.P2PLink.enums.Protocol;
import com.courtesilol.P2PLink.records.Candidate;
import com.courtesilol.P2PLink.records.FileMetadata;
import com.courtesilol.P2PLink.records.ServerKeyInfo;
import com.courtesilol.P2PLink.server.FileReceiverServerIce;
import java.io.File;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.ice4j.ice.Agent;
import org.ice4j.ice.CandidateType;
import org.ice4j.ice.Component;
import org.ice4j.ice.IceMediaStream;
import org.ice4j.ice.LocalCandidate;

/**
 *
 * @author javier
 */
public class P2PLink {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            return;
        }

        switch (args[0].toLowerCase()) {
            case "send":
                HttpClient globalClient = HttpClient.newHttpClient();
                Optional<String> publicIP = NetUtils.getPublicIp(globalClient);

                if (publicIP.isPresent()) {

                    File exampleFile = new File(args[1]);

                    var fileMetadata = new FileMetadata(
                            exampleFile.getName(),
                            exampleFile.length(),
                            NetUtils.getFileExtension(exampleFile)
                    );

                    FileSenderClient sender = new FileSenderClient("127.0.0.1", 40001);
                    sender.SendFile(fileMetadata, exampleFile.getAbsolutePath(), 40002);
                }
                break;

            case "sendice":
                System.out.println("SendIceClient");
                System.out.println("------------");
                if (args.length < 4) {
                    System.out.println("Missing args");
                    System.out.println("sendice <filePath> <remoteIP> <remotePort>");
                    return;
                }

                File fileToSend = new File(args[1]);

                if (fileToSend == null) {
                    System.out.println("File not exist");
                    return;
                }

                var fileMetaData = new FileMetadata(
                        fileToSend.getName(),
                        fileToSend.length(),
                        NetUtils.getFileExtension(fileToSend));

                var fileSender = new FileSenderClientIce();
            //fileSender.sendFile(fileMetaData, fileToSend.getAbsolutePath(), args[2], args[3], List.of(""));

            case "get":

                FileReceiverServer reciber = new FileReceiverServer(40002);
                reciber.RecibeFile();
                break;

            case "getice":
                System.out.println("GetIceServer");
                System.out.println("------------");
                var serverIce = new FileReceiverServerIce();
                serverIce.RecibeFile();
                break;

            case "test":

                //Test get Ufrag and localPwd
                Agent agent = IceAgentSetup.createAgentWithStun();
                String localUfrag = agent.getLocalUfrag();
                String localPwd = agent.getLocalPassword();

                System.out.println("LocalUfrag: " + localUfrag);
                System.out.println("LocalPwd: " + localPwd);

                // Crear un flujo de medios para transferir archivos (en lugar de usar RTP)
                IceMediaStream stream = agent.createMediaStream("data");
                Component dataComponent = stream.getComponent(Component.RTP); // Usar el componente RTP pero lo usare para archivos

                if (dataComponent != null) {
                    List<LocalCandidate> localCandidates = dataComponent.getLocalCandidates();
                    System.out.println("Total Candidates: " + localCandidates.size());
                } else {
                    System.out.println("Data component is not available.");
                }
            case "test2":
                System.out.println("Test 2");

                List<Candidate> candidateList = new ArrayList();

                candidateList.add(new Candidate("192.0.2.1", 50000,CandidateType.HOST_CANDIDATE, Protocol.UDP));
                candidateList.add(new Candidate("192.0.2.2", 50001,CandidateType.PEER_REFLEXIVE_CANDIDATE, Protocol.TCP));
                candidateList.add(new Candidate("192.0.2.3", 50002,CandidateType.RELAYED_CANDIDATE, Protocol.UDP));
                candidateList.add(new Candidate("192.0.2.4", 50003,CandidateType.HOST_CANDIDATE, Protocol.TCP));
                candidateList.add(new Candidate("192.0.2.5", 50004,CandidateType.SERVER_REFLEXIVE_CANDIDATE, Protocol.UDP));

                var key = new ServerKeyInfo("ufragFicticio", "passFicticia", candidateList);
                key.exportToXML("/home/javier/Escritorio/Codigo/java/pruebas P2PTransfer/test");

        }

        System.out.println("Fin");

    }

}
