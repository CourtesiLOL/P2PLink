package com.courtesilol.P2PLink.records;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author javier
 */
public record ServerKeyInfo(String ufrag, String pwd, List<Candidate> candidates) {

    public boolean toFile(String outPath) throws IOException {

        //A editar, mejor usar XML
        File tempFile = new File(outPath + File.separator + "ServerKeyInfo.xml");
        if (!tempFile.exists()) {
            tempFile.createNewFile();
        }

        if (tempFile.canWrite()) {
            FileWriter fileWriter = new FileWriter(tempFile);
            fileWriter.write("ufrag=" + ufrag + "\n");
            fileWriter.write("pwd=" + pwd + "\n");

            for (int i = 0; i < candidates.size(); i++) {
                fileWriter.write("Candidate-" + i + "=" + candidates + "\n");
            }

            fileWriter.write("ufrag=" + ufrag + "\n");
            fileWriter.write("ufrag=" + ufrag + "\n");
        }

        return false;
    }

    public boolean exportToXML(String outPath) throws IOException {

        //A editar, mejor usar XML
        System.out.println("Entra a file export");

        File tempFile;
        FileWriter tempFileWriter;

        if (outPath.endsWith("/")) {
            tempFile = new File(outPath + "ServerKey.xml");
        } else {
            tempFile = new File(outPath + File.separator + "ServerKey.xml");
        }

        if (!tempFile.exists()) {
            tempFileWriter = new FileWriter(tempFile);
        } else {
            tempFileWriter = new FileWriter(tempFile, false);
        }

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer;
        try {
            writer = factory.createXMLStreamWriter(tempFileWriter);

            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement("ServerKeyInfo");

            writer.writeStartElement("ufrag");
            writer.writeCharacters(ufrag);
            writer.writeEndElement();

            writer.writeStartElement("pwd");
            writer.writeCharacters(pwd);
            writer.writeEndElement();

            writer.writeStartElement("candidates");
            for (Candidate candidate : candidates) {
                writer.writeStartElement("candidate");

                writer.writeStartElement("ip");
                writer.writeCharacters(candidate.ip());
                writer.writeEndElement();

                writer.writeStartElement("port");
                writer.writeCharacters(String.valueOf(candidate.port()));
                writer.writeEndElement();

                writer.writeStartElement("protocol");
                writer.writeCharacters(candidate.protocol().toString());
                writer.writeEndElement();

                writer.writeEndElement();
            }
            writer.writeEndElement();

            writer.writeEndElement();
            writer.writeEndDocument();

            writer.flush();
            writer.close();

            System.out.println("Archivo XML creado con XMLStreamWriter.");

            System.out.println("Sale de file export");
        } catch (XMLStreamException ex) {
            System.out.println("Can't write the file");
            return false;
        }

        return true;
    }

    public boolean importToXML(String inPath) {
        System.out.println("To Implement");
        return false;
    }
}
