/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.minecraft.client.Minecraft;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class cz
extends Thread {
    public File a;
    private Minecraft b;
    private boolean c = false;

    public cz(File file, Minecraft minecraft) {
        this.b = minecraft;
        this.setName("Resource download thread");
        this.setDaemon(true);
        this.a = new File(file, "resources/");
        if (!this.a.exists() && !this.a.mkdirs()) {
            throw new RuntimeException("The working directory could not be created: " + this.a);
        }
    }

    public void run() {
        try {
            URL uRL = new URL("http://s3.amazonaws.com/MinecraftResources/");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(uRL.openStream());
            NodeList nodeList = document.getElementsByTagName("Contents");
            for (int i2 = 0; i2 < 2; ++i2) {
                for (int i3 = 0; i3 < nodeList.getLength(); ++i3) {
                    Node node = nodeList.item(i3);
                    if (node.getNodeType() != 1) continue;
                    Element element = (Element)node;
                    String string = ((Element)element.getElementsByTagName("Key").item(0)).getChildNodes().item(0).getNodeValue();
                    long l2 = Long.parseLong(((Element)element.getElementsByTagName("Size").item(0)).getChildNodes().item(0).getNodeValue());
                    if (l2 <= 0L) continue;
                    this.a(uRL, string, l2, i2);
                    if (!this.c) continue;
                    return;
                }
            }
        }
        catch (Exception exception) {
            this.a(this.a, "");
            exception.printStackTrace();
        }
    }

    public void a() {
        this.a(this.a, "");
    }

    private void a(File file, String string) {
        File[] fileArray = file.listFiles();
        for (int i2 = 0; i2 < fileArray.length; ++i2) {
            if (fileArray[i2].isDirectory()) {
                this.a(fileArray[i2], string + fileArray[i2].getName() + "/");
                continue;
            }
            try {
                this.b.a(string + fileArray[i2].getName(), fileArray[i2]);
                continue;
            }
            catch (Exception exception) {
                System.out.println("Failed to add " + string + fileArray[i2].getName());
            }
        }
    }

    private void a(URL uRL, String string, long l2, int n2) {
        try {
            int n3 = string.indexOf("/");
            String string2 = string.substring(0, n3);
            if (string2.equals("sound") || string2.equals("newsound") ? n2 != 0 : n2 != 1) {
                return;
            }
            File file = new File(this.a, string);
            if (!file.exists() || file.length() != l2) {
                file.getParentFile().mkdirs();
                String string3 = string.replaceAll(" ", "%20");
                this.a(new URL(uRL, string3), file, l2);
                if (this.c) {
                    return;
                }
            }
            this.b.a(string, file);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void a(URL uRL, File file, long l2) {
        byte[] byArray = new byte[4096];
        DataInputStream dataInputStream = new DataInputStream(uRL.openStream());
        DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file));
        int n2 = 0;
        while ((n2 = dataInputStream.read(byArray)) >= 0) {
            dataOutputStream.write(byArray, 0, n2);
            if (!this.c) continue;
            return;
        }
        dataInputStream.close();
        dataOutputStream.close();
    }

    public void b() {
        this.c = true;
    }
}

