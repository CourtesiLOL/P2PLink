package com.courtesilol.P2PLink;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.message.Message;
import org.ice4j.message.MessageFactory;
import org.ice4j.stack.StunStack;

/**
 *
 * @author javier
 */
public class NetUtils {

    public static Optional<String> getPublicIp(HttpClient client) {
        
        Optional<String> resp;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://ifconfig.me/"))
                .header("User-Agent", "curl/7.64.1") // Simula curl
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();
        
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            resp = Optional.of(response.body());
        } catch (IOException | InterruptedException ex) {
            resp = Optional.empty();
        }
        
        
        return resp;
    }
    
    // Método para obtener la extensión del archivo
    public static String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    
}
