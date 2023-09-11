import java.net.*;
import java.util.Scanner;

public class ClientInfo {
    private String username;
    private int rank = 0;
    private int wins = 0;
    private int loses = 0;
    private int number = 0;
    private int total = 0;
    private boolean inGame = false;
    private Scanner scanner;
    private Socket socket;
    private Thread thread;
    private boolean hisTurn;
    private boolean isConnected = false;
    private boolean isSearching = false;
    private Game gg;

    public ClientInfo(String username, Socket socket, Thread thread) {
        this.username = username;
        this.socket = socket;
        this.thread = thread;
        this.hisTurn = false;
        this.gg = null;
    }

    public void setGame(Game g) {
        this.gg = g;
    }

    public Game getGame(){
        return gg;
    }

    public String getUsername() {
        return username;
    }

    public Socket getSocket() {
        return socket;
    }

    public Thread getThread() {
        return thread;
    }

    public int getRank() {
        return rank;
    }

    public void updateRank() {
        this.rank = wins - loses;
        if(this.rank < 0){
            this.rank = 0;
        }
    }

    public int getWins() {
        return wins;
    }

    public void updateWins() {
        this.wins++;
    }

    public int getLoses() {
        return loses;
    }

    public void updateLoses() {
        this.loses++;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int n) {
        this.number = n;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public int getTotal() {
        return total;
    }

    public void addNumber(int number) {
        this.total += number;
    }

    public void entered() {
        inGame = true;
    }

    public void finishGame() {
        inGame = false;
    }

    public boolean getStatus() {
        return inGame;
    }

    public void updateTurn(){
        if(hisTurn){
            hisTurn = false;
        }else{
            hisTurn = true;
        }
    }

    public boolean getTurn(){
        return hisTurn;
    }

    public boolean getSearching(){
        return isSearching;
    }

    public void updateSearching(){
        if(isSearching){
            isSearching = false;
        }else{
            isSearching = true;
        }
    }

    public void resetTotal(){
        this.total = 0;
    }

    public boolean getConnection(){
        return isConnected;
    }

    public void updateConnection(){
        if(isConnected){
            isConnected = false;
        }else{
            isConnected = true;
        }
    }
}
