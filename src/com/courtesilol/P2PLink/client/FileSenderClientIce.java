package com.courtesilol.P2PLink.client;

import com.courtesilol.P2PLink.IceAgentSetup;
import com.courtesilol.P2PLink.records.FileMetadata;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.ice.*;
import org.ice4j.socket.IceSocketWrapper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class FileSenderClientIce {

    private final int bufferSize;

    public FileSenderClientIce() {
        this.bufferSize = 32768; // 32 KB
    }

    /**
     * Enviar archivo usando ICE con STUN.
     *
     * @param fileMetadata    Metadatos del archivo
     * @param filePath        Ruta del archivo a enviar
     * @param remoteUfrag     ufrag remoto del receptor
     * @param remotePwd       password ICE del receptor
     * @param remoteCandidates Lista de candidatos remotos
     */
    public boolean sendFile(FileMetadata fileMetadata, String filePath,
                            String remoteUfrag,
                            String remotePwd,
                            List<TransportAddress> remoteCandidates) throws Exception {

        // Crear agente ICE con STUN
        Agent agent = IceAgentSetup.createAgentWithStun();
        agent.setControlling(true); // El cliente es controlador en esta lógica

        // Crear media stream
        IceMediaStream stream = agent.createMediaStream("data");

        // Crear componente (UDP)
        Component component = agent.createComponent(stream,
                KeepAliveStrategy.SELECTED_ONLY, true);

        // Establecer credenciales ICE remotas
        stream.setRemoteUfrag(remoteUfrag);
        stream.setRemotePassword(remotePwd);

        // Agregar candidatos remotos recibidos desde el receptor
        for (TransportAddress address : remoteCandidates) {
            RemoteCandidate remoteCandidate = new RemoteCandidate(
                    address,
                    component,
                    CandidateType.HOST_CANDIDATE,
                    null, 0, null
            );
            component.addRemoteCandidate(remoteCandidate);
        }

        // Esperar conexión ICE
        CountDownLatch latch = new CountDownLatch(1);

        agent.addStateChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                IceProcessingState state = (IceProcessingState) evt.getNewValue();
                if (state == IceProcessingState.COMPLETED) {
                    latch.countDown();
                } else if (state == IceProcessingState.FAILED) {
                    System.err.println("ICE connection failed.");
                    latch.countDown();
                }
            }
        });

        // Iniciar conexión ICE
        agent.startConnectivityEstablishment();

        // Esperar finalización
        latch.await();

        CandidatePair selectedPair = component.getSelectedPair();
        if (selectedPair == null) {
            System.err.println("No se pudo establecer la conexión ICE.");
            return false;
        }

        // Obtener socket negociado
        IceSocketWrapper socketWrapper = selectedPair.getIceSocketWrapper();
        Socket socket = socketWrapper.getTCPSocket(); // o getUDPSocket() según tu configuración

        // Enviar archivo
        try (
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                FileInputStream fileInputStream = new FileInputStream(filePath)
        ) {
            dataOut.writeUTF(fileMetadata.fileName());
            dataOut.writeLong(fileMetadata.fileSize());
            dataOut.writeUTF(fileMetadata.fileFormat());

            System.out.println("Enviando archivo...");

            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dataOut.write(buffer, 0, bytesRead);
            }

            System.out.println("Archivo enviado exitosamente.");
        } catch (IOException e) {
            System.err.println("Error al enviar archivo: " + e.getMessage());
            return false;
        }

        return true;
    }
}
