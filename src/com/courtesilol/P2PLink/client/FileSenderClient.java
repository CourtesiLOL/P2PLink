    package com.courtesilol.P2PLink.client;

    import com.courtesilol.P2PLink.records.FileMetadata;
    import java.io.DataOutputStream;
    import java.io.File;
    import java.io.FileInputStream;
    import java.io.IOException;
    import java.net.Socket;

    /**
     *
     * @author javier
     */
    public class FileSenderClient {

        private String serverAddress;
        private int port;
        private int bufferSize;

        public FileSenderClient(String serverAddress, int port) {
            this.serverAddress = serverAddress;
            this.port = port;
            bufferSize = 32768; // 32KB
        }

        public FileSenderClient(String serverAdderss, int port, int bufferSize) {
            this.serverAddress = serverAddress;
            this.port = port;
            this.bufferSize = bufferSize;
        }

        public boolean SendFile(FileMetadata fileMetadata, String filePath, int reciberPort) {

            try (Socket socket = new Socket(serverAddress, reciberPort);
                 DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                 FileInputStream fileInputStream = new FileInputStream(filePath)) {

                // Enviar metadatos
                dataOut.writeUTF(fileMetadata.fileName());  // Nombre del archivo
                dataOut.writeLong(fileMetadata.fileSize()); // Tamaño del archivo
                dataOut.writeUTF(fileMetadata.fileFormat()); // Formato del archivo

                System.out.println("Metadatos enviados. Enviando archivo...");

                // Enviar el archivo
                byte[] buffer = new byte[bufferSize];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    dataOut.write(buffer, 0, bytesRead);
                }

                System.out.println("Archivo enviado exitosamente!");
                return true;

            } catch (IOException e) {
                System.out.println("Error al enviar el archivo");
                return false;
            }
        }


    }
