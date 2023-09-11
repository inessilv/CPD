import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

/**
 * This program demonstrates a simple TCP/IP socket server.
 * It uses a thread pool to handle multiple clients connected at the same time.
 * Each client needs to write their username upon connection to save it on the server.
 * Every message sent by the client will be displayed on the server terminal.
 * The server will save the client's username, rank, position x, and position y.
 * If the client disconnects and reconnects, the server will still have their information.
 *
 * To stop the server, type "exit" in the server terminal.
 *
 */
public class TimeServer {
    private static final String KEYSTORE_PATH = "../doc/file_key.jks";
    private static final String KEYSTORE_PASSWORD = "keystore_password";
    private static final String KEY_ALIAS = "server_alias";
    private static final String KEY_PASSWORD = "server_password";
    private static final String TRUSTSTORE_PATH = "../doc/file_trust.jks";
    private static final String TRUSTSTORE_PASSWORD = "password";
    
    private static HashMap<String, ClientInfo> clients = new HashMap<>();
    private static Lock clientsLock = new ReentrantLock(); // Lock for clients HashMap
    private static List<ClientInfo> searchSimple = new ArrayList<>();
    private static List<ClientInfo> searchRanked = new ArrayList<>();
    private static List<ClientInfo> playSimple = new ArrayList<>();
    private static List<ClientInfo> playRanked = new ArrayList<>();
    public static boolean gameSimple_playing = false;
    public static boolean gameRanked_playing = false;
    public static Game simple = null;
    public static Game ranked = null;

    public static void main(String[] args) {
        if (args.length < 1) return;

        int port = Integer.parseInt(args[0]);

        AtomicBoolean isRunning = new AtomicBoolean(true);
        Thread consoleThread = null;

    try {
            // Generate the server's self-signed certificate
            //generateSelfSignedCertificate(KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD);

            // Load the server's KeyStore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            FileInputStream file1 = new FileInputStream(KEYSTORE_PATH);
            keyStore.load(file1, KEYSTORE_PASSWORD.toCharArray());

            // Create KeyManagerFactory with the server's KeyStore
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, KEY_PASSWORD.toCharArray());

            KeyStore trustStore = KeyStore.getInstance("JKS");
            FileInputStream file2 = new FileInputStream(TRUSTSTORE_PATH);
            trustStore.load(file2, TRUSTSTORE_PASSWORD.toCharArray());

            TrustManagerFactory  trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);


            // Create SSLContext and initialize it with the KeyManagerFactory
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);


        try (SSLServerSocket serverSocket = (SSLServerSocket) sslContext.getServerSocketFactory().createServerSocket(port)) {

            System.out.println("Server is listening on port " + port);

            // Listen for console input to gracefully stop the server
            consoleThread = new Thread(() -> {
                BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                while (isRunning.get()) {
                    try {
                        String input = consoleReader.readLine();
                        if (input.equalsIgnoreCase("exit")) {
                            System.out.println("Shutting down server...");
                            isRunning.set(false);

                            // Notify all connected clients before shutting down
                            clientsLock.lock(); // Acquire lock before modifying clients HashMap
                            try {
                                for (ClientInfo client : clients.values()) {
                                    try {
                                        PrintWriter clientWriter = new PrintWriter(client.getSocket().getOutputStream(), true);
                                        clientWriter.println("Server is shutting down. Goodbye!");
                                        client.getSocket().close();
                                    } catch (IOException ex) {
                                        System.out.println("Error notifying client " + client.getUsername());
                                        ex.printStackTrace();
                                    }
                                }
                            } finally {
                                clientsLock.unlock(); // Release lock after modifying clients HashMap
                            }
                            System.exit(0); // Shut down the program
                        }

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            consoleThread.start();

            while (isRunning.get()) {
                SSLSocket socket = (SSLSocket) serverSocket.accept();
    
                // Perform SSL/TLS configuration
                socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
                

                Thread clientThread = new Thread(new ClientHandler(socket));
                clientThread.start();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            if (consoleThread != null) consoleThread.interrupt();
        }
    } catch (IOException ex) {
        System.out.println("Error generating self-signed certificate: " + ex.getMessage());
        ex.printStackTrace();
    } catch (KeyManagementException e) {
        e.printStackTrace();
    } catch (UnrecoverableKeyException e) {
        e.printStackTrace();
    } catch (KeyStoreException e) {
        e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
    } catch (Exception e) {
        e.printStackTrace();
    }finally {
    }
}
   /*  private static void generateSelfSignedCertificate(String keystorePath, String keystorePassword, String keyAlias, String keyPassword) throws Exception {
        // Generate a self-signed certificate using the 'keytool' utility
        ProcessBuilder processBuilder = new ProcessBuilder (
                "keytool",
                "-genkeypair",
                "-alias", keyAlias,
                "-keyalg", "RSA",
                "-keysize", "2048",
                "-validity", "365",
                "-keystore", keystorePath,
                "-storepass", keystorePassword,
                "-keypass", keyPassword,
                "-dname", "CN=localhost",
                "-ext", "SAN=dns:localhost"
        );
    
        Process process = processBuilder.start();
        System.out.println(process.toString());
    int exitCode = process.waitFor();
    if (exitCode != 0) {
        throw new Exception("Failed to generate self-signed certificate.");
    }
}*/



    private static class ClientHandler implements Runnable {
        private SSLSocket socket;

        public ClientHandler(SSLSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                // Get client username
                String username = reader.readLine();
                if (username == null) {
                    return;
                }

            clientsLock.lock(); // Acquire lock before modifying clients HashMap
            try {
                // urreCheck if client is already connected
                if (!clients.containsKey(username)) {
                    // Add client to HashMap
                    ClientInfo clientInfo = new ClientInfo(username, socket, Thread.currentThread());
                    clients.put(username, clientInfo);
                    System.out.println("New client connected: " + username);
                    clients.get(username).updateConnection();
                    
                    writer.println("Welcome to Summing " + username);
                    writer.println("Write 'S' to inicialize a normal game");
                    writer.println("Write 'R' to inicialize a ranked game");
                    writer.println("Write 'I' to learn about the game");
                    writer.println("Write 'Q' to quit the game");
                }else if(clients.containsKey(username) && !clients.get(username).getConnection()){
                    System.out.println("Client " + username + " has reconnected.");
                    writer.println("Welcome back " + username);
                    if(clients.get(username).getStatus()){
                        writer.println("You were in game when you disconnected");
                    }else if(!clients.get(username).getStatus() && searchSimple.contains(username)){
                        writer.println("You were searching for a simple game when you disconnected");
                    }else if(!clients.get(username).getStatus() && searchRanked.contains(username)){
                        writer.println("You were searching for a ranked game when you disconnected");
                    }else{
                        writer.println("Welcome back to Summing " + username);
                        writer.println("Write 'S' to inicialize a normal game");
                        writer.println("Write 'R' to inicialize a ranked game");
                        writer.println("Write 'I' to learn about the game");
                        writer.println("Write 'Q' to quit the game");
                    }
                }else{
                    writer.println("This player is already connected to the server.");
                    /*username = reader.readLine();
                    if (username == null) {
                        return;
                    }*/
                }
            } finally {
                clientsLock.unlock(); // Release lock after modifying clients HashMap
            }

            // Listen for messages from client
            String message;
            while ((message = reader.readLine()) != null) {

                System.out.println("Message from " + username + ": " + message);

                if(!clients.get(username).getStatus() && !clients.get(username).getSearching()){
                    if(message.equalsIgnoreCase("S") || message.equalsIgnoreCase("s")){
                        searchSimple.add(clients.get(username));
                        clients.get(username).updateSearching();
                        if(gameSimple_playing){
                            writer.println("A normal game is taking place. The queue could take some time");
                        }
                        writer.println("Searching for normal game...");
                        if(searchSimple.size() >= 4 && !gameSimple_playing){
                            for (ClientInfo p : searchSimple.subList(0, 4)) {
                                playSimple.add(p);
                            }
                            simple = new Game(playSimple, false);
                            for (ClientInfo p : playSimple) {
                                p.setGame(simple);
                            }
                            gameSimple_playing = true;
                            simple.start();
                        }
                    }else if(message.equalsIgnoreCase("R") || message.equalsIgnoreCase("r")){
                        searchRanked.add(clients.get(username));
                        clients.get(username).updateSearching();
                        if(gameRanked_playing){
                            writer.println("A ranked game is taking place. The queue could take some time");
                        }
                        writer.println("Searching for ranked game...");
                        if(searchRanked.size() >= 4 && !gameRanked_playing){
                            for (ClientInfo p : searchRanked.subList(0, 4)) {
                                playRanked.add(p);
                            }
                            ranked = new Game(playRanked, true);
                            for (ClientInfo p : playRanked) {
                                p.setGame(ranked);
                            }
                            gameRanked_playing = true;
                            ranked.start();
                        }        
                    }else if(message.equalsIgnoreCase("I") || message.equalsIgnoreCase("i")){
                        writer.println("The game consists of adding up numbers entered by players, in order to guess a random number.\nIf the sum goes beyond the elected number, a message will be displayed.\nIt has two distinct modes, Simple and Rank and it was designed to be played in two teams of two players each.\nThe game only ends when one of the teams guesses the number.");
                    }else if(message.equals("Q") || message.equals("q")){
                        writer.println("Good bye!");
                        break;
                    }else{
                        writer.println("Thats not an option");
                    }
                }else if(clients.get(username).getSearching()){
                    writer.println("You are searching for a game. Be patient!");
                }else if(clients.get(username).getTurn()){
                    try {
                        int num = Integer.parseInt(message);
                        clients.get(username).setNumber(num);
                        clients.get(username).updateTurn();
                        if(simple.players.contains(clients.get(username))){
                            simple.stuff(clients.get(username), num);
                            if (simple.checkWinner()) {
                                gameSimple_playing = false;
                                playSimple.clear();
                                searchSimple.removeAll(searchSimple.subList(0, 4));
                                simple = null;
                            }else{
                                simple.changeTurn();
                                simple.players.get(simple.playerTurn).updateTurn();
                            }

                        }else{
                            ranked.stuff(clients.get(username), num);
                            if (ranked.checkWinner()) {
                                gameRanked_playing = false;
                                searchRanked.removeAll(searchRanked.subList(0, 4));
                                ranked = null;
                            }else{
                                ranked.changeTurn();
                                ranked.players.get(ranked.playerTurn).updateTurn();
                            }
                        
                        }
                    } catch (NumberFormatException e) {
                        writer.println("Write a number.");
                    }
                }else if(clients.get(username).getStatus()){
                    writer.println("Its not your turn");
                }else{
                    writer.println("ERROR");
                }

                // TODO: Process client message here
            }

            clients.get(username).updateConnection();
            System.out.println("Client disconnected: " + username);

        } catch (IOException ex) {
            System.out.println("Error handling client: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
}