package com.courtesilol.P2PLink.server;

import com.courtesilol.P2PLink.IceAgentSetup;
import com.courtesilol.P2PLink.enums.Protocol;
import com.courtesilol.P2PLink.records.FileMetadata;
import com.courtesilol.P2PLink.records.Candidate;
import com.courtesilol.P2PLink.records.ServerKeyInfo;
import org.ice4j.TransportAddress;
import org.ice4j.ice.*;
import org.ice4j.socket.IceSocketWrapper;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class FileReceiverServerIce {

    private ServerKeyInfo serverInfo;
    private int bufferSize;

    public FileReceiverServerIce() {
        this.bufferSize = 32768; // 32 KB por bloque
    }

    public boolean RecibeFile() throws Exception {

        // Crear el agente ICE con STUN UDP
        Agent agent = IceAgentSetup.createAgentWithStun();

        // Crear flujo de medios para datos
        IceMediaStream stream = agent.createMediaStream("data");

        // Crear componente para enviar datos
        Component component = agent.createComponent(stream,
                KeepAliveStrategy.SELECTED_ONLY, true);

        // Imprimir credenciales ICE locales
        System.out.println("=== CREDENCIALES ICE ===");
        System.out.println("Local ufrag: " + agent.getLocalUfrag());
        System.out.println("Local pwd  : " + agent.getLocalPassword());

        // Esperar a que los candidatos se generen
        while (component.getLocalCandidates().isEmpty()) {
            System.out.println("Esperando candidatos ICE...");
            Thread.sleep(500);
        }

        List<Candidate> candidates = new ArrayList();
        // Imprimir candidatos locales
        System.out.println("\n=== CANDIDATOS LOCALES ===");
        for (LocalCandidate candidate : component.getLocalCandidates()) {
            if (candidate == null) continue;
            TransportAddress addr = candidate.getTransportAddress();
            Candidate tempCandidate = new Candidate(
                    addr.getHostAddress(),
                    addr.getPort(),
                    candidate.getType(),
                    Protocol.valueOf(addr
                            .getTransport()
                            .toString()
                            .toUpperCase()
                    )
            );
            
            candidates.add(tempCandidate);            
            System.out.printf("IP: %s, Puerto: %d, Tipo: %s, Protocolo: %s%n",
                    tempCandidate.ip(),
                    tempCandidate.port(),
                    tempCandidate.type(),
                    tempCandidate.protocol());
        }
        
        serverInfo = new ServerKeyInfo(
                agent.getLocalUfrag(),
                agent.getLocalPassword(),
                candidates
        );
        
        serverInfo.exportToXML(new File("").getAbsolutePath());
        
        // Esperar conexión ICE
        CountDownLatch latch = new CountDownLatch(1);
        agent.addStateChangeListener(evt -> {
            IceProcessingState newState = (IceProcessingState) evt.getNewValue();
            if (newState == IceProcessingState.COMPLETED
                    || newState == IceProcessingState.FAILED) {
                latch.countDown();
            }
        });

        // Iniciar ICE
        agent.startConnectivityEstablishment();
        latch.await();

        CandidatePair selectedPair = component.getSelectedPair();
        if (selectedPair == null) {
            System.err.println("No se pudo establecer una conexión ICE.");
            return false;
        }

        // Obtener socket final negociado
        IceSocketWrapper socketWrapper = selectedPair.getIceSocketWrapper();
        Socket socket = socketWrapper.getTCPSocket();

        System.out.println("\n✅ Conexión ICE establecida con: " + socket.getInetAddress());

        try (DataInputStream dataIn = new DataInputStream(socket.getInputStream())) {
            // Leer metadatos
            String fileName = dataIn.readUTF();
            long fileSize = dataIn.readLong();
            String fileFormat = dataIn.readUTF();
            FileMetadata metadata = new FileMetadata(fileName, fileSize, fileFormat);

            System.out.println("📥 Recibiendo archivo: " + metadata.fileName());
            System.out.println("Tamaño: " + metadata.fileSize() + " bytes");
            System.out.println("Formato: " + metadata.fileFormat());

            // Guardar archivo
            try (FileOutputStream fileOut = new FileOutputStream(metadata.fileName())) {
                byte[] buffer = new byte[bufferSize];
                int bytesRead;
                long totalRead = 0;

                while (totalRead < metadata.fileSize() && (bytesRead = dataIn.read(buffer)) != -1) {
                    fileOut.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                }

                System.out.println("✅ Archivo recibido correctamente.");
                return true;
            }

        } catch (IOException e) {
            System.err.println("❌ Error al recibir archivo: " + e.getMessage());
            return false;
        }
    }


}
