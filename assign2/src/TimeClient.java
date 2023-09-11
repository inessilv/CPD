import java.net.*;
import java.io.*;
import java.util.Scanner;
import javax.net.ssl.*;
import java.security.KeyStore;

/**
 * This program demonstrates a simple TCP/IP socket client.
 */
public class TimeClient {
    private static final String KEYSTORE_PATH = "../doc/file_key.jks";
    private static final String KEYSTORE_PASSWORD = "server_password";
    private static final String TRUSTSTORE_PATH = "../doc/file_trust.jks";
    private static final String TRUSTSTORE_PASSWORD = "password";
    private static final String KEY_ALIAS = "client_alias";
    private static final String KEY_PASSWORD = "client_password";

    public static void main(String[] args) {
        if (args.length < 2) return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        Scanner scanner = new Scanner(System.in);

        for (int clientIndex = 1; clientIndex <= 5; clientIndex++) {
            try {
                // Run the Bash script with the client index as an argument
                String[] command = {"/bin/bash", "./script.sh", String.valueOf(clientIndex)};
                Process process = Runtime.getRuntime().exec(command);

                // Wait for the script to complete before proceeding
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("Bash script executed successfully for client " + clientIndex);
                } else {
                    System.out.println("Bash script execution failed for client " + clientIndex);
                }
            }

        try {
            // Generate the client's self-signed certificate
    
            
            KeyStore trustStore = KeyStore.getInstance("JKS");
            FileInputStream file1 = new FileInputStream(TRUSTSTORE_PATH);
            trustStore.load(file1, TRUSTSTORE_PASSWORD.toCharArray());

            KeyStore keyStore = KeyStore.getInstance("JKS");
            FileInputStream file2 = new FileInputStream(KEYSTORE_PATH);
            keyStore.load(file2, KEYSTORE_PASSWORD.toCharArray());

            TrustManagerFactory  trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, KEY_PASSWORD.toCharArray());

            // Create SSLContext and initialize it with the TrustManagerFactory
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            try (Socket socket = sslContext.getSocketFactory().createSocket(hostname, port)) {
                SSLSocket sslSocket = (SSLSocket) socket;

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                System.out.print("Enter your username: ");
                String username = scanner.nextLine();
                writer.println(username);

                Thread serverListener = new Thread(() -> {
                    try {
                        InputStream input = sslSocket.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                        String serverResponse;
                        while ((serverResponse = reader.readLine()) != null) {
                            System.out.println(serverResponse);
                            if (serverResponse.equals("Server is shutting down. Goodbye!") || serverResponse.equals("Good bye!")) {
                                System.exit(0); // Shut down the program
                            }
                        }
                        sslSocket.close();
                    } catch (IOException ex) {
                        System.out.println("Error reading from server");
                        System.exit(0); // Shut down the program
                    }
                });

                serverListener.start();

                while (true) {
                    String userInput = scanner.nextLine();
                    writer.println(userInput);
                }
            } catch (UnknownHostException ex) {
                System.out.println("Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O error: " + ex.getMessage());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            scanner.close();
        }
    
    }
   
}
