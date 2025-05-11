package com.courtesilol.P2PLink.server;

import com.courtesilol.P2PLink.records.FileMetadata;
import com.courtesilol.P2PLink.records.FileMetadata;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author javier
 */
public class FileReceiverServer {

    private int port;
    private int bufferSize;

    public FileReceiverServer(int port) {
        this.port = port;
        this.bufferSize = 32768; // 32KB
    }

    public FileReceiverServer(int port, int bufferSize) {
        this.port = port;
        this.bufferSize = bufferSize;
    }

    public boolean RecibeFile() {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Esperando conexión...");

            // Espera una conexión entrante
            Socket clientSocket = serverSocket.accept();
            System.out.println("Conexión establecida con el cliente: " + clientSocket.getInetAddress());

            // Obtener los metadatos
            try (DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream())) {
                String fileName = dataIn.readUTF();  // Nombre del archivo
                long fileSize = dataIn.readLong();   // Tamaño del archivo
                String fileFormat = dataIn.readUTF(); // Formato del archivo

                // Crear el record con los metadatos recibidos
                FileMetadata fileReceivedInfo = new FileMetadata(fileName, fileSize, fileFormat);

                System.out.println("Recibiendo archivo: " + fileReceivedInfo.fileName());
                System.out.println("Tamaño: " + fileReceivedInfo.fileSize() + " bytes");
                System.out.println("Formato: " + fileReceivedInfo.fileFormat());

                // Preparar para recibir el archivo
                try (FileOutputStream fileOutputStream = new FileOutputStream(fileReceivedInfo.fileName())) {
                    byte[] buffer = new byte[bufferSize];
                    int bytesRead;
                    long totalBytesRead = 0;
                    while (totalBytesRead < fileReceivedInfo.fileSize() && (bytesRead = dataIn.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                    }

                    System.out.println("Archivo recibido correctamente!");
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Archivo no recibido!");
            e.printStackTrace();
        }
        
        return false;
    }

}
