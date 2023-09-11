import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Game {
    public final int MIN_VALUE = 1;
    public final int MAX_VALUE = 100;

    public List<ClientInfo> players;
    public List<ClientInfo> team_1 = new ArrayList<>();
    public List<ClientInfo> team_2 = new ArrayList<>();
    public int targetNumber;
    public boolean gameStarted;
    public boolean isRanked;
    public Lock locale = new ReentrantLock();
    public int playerTurn = 0;

    public Game(List<ClientInfo> players, boolean isRanked) {

        this.players = players;
        this.isRanked = isRanked;

        if(isRanked){
            // sort the players list by rank
            Collections.sort(players, new Comparator<ClientInfo>() {
                @Override
                public int compare(ClientInfo p1, ClientInfo p2) {
                    return Integer.compare(p1.getRank(), p2.getRank());
                }
            });

            for (int i = 0; i < players.size(); i++) {
                if (i == 0 || i == 3) {
                    players.get(i).updateSearching();
                    players.get(i).entered();
                    team_1.add(players.get(i));
                } else {
                    players.get(i).updateSearching();
                    players.get(i).entered();
                    team_2.add(players.get(i));
                }
            }

        }else{
            // Embaralhar aleatoriamente os jogadores antes de atribuí-los aos times
            Collections.shuffle(players);

            for (int i = 0; i < players.size(); i++) {
                if (i == 0 || i == 3) {
                    players.get(i).updateSearching();
                    players.get(i).entered();
                    team_1.add(players.get(i));
                } else {
                    players.get(i).updateSearching();
                    players.get(i).entered();
                    team_2.add(players.get(i));
                }
            }
        }

        // Gerar um número aleatório entre MIN_VALUE e MAX_VALUE
        Random rand = new Random();
        targetNumber = rand.nextInt(MAX_VALUE - MIN_VALUE + 1) + MIN_VALUE;
        gameStarted = false;
    }

    public void start() throws IOException {
        if (!gameStarted) {
            gameStarted = true;
            if(!isRanked){
                for (ClientInfo player : players) {
                    OutputStream output = player.getSocket().getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);
                    writer.println("Starting a simple game");
                    if(team_1.contains(player)){
                        writer.println("You are in team 1");
                    }else if(team_2.contains(player)){
                        writer.println("You are in team 2");
                    }
                }
            }else{
                for (ClientInfo player : players) {
                    OutputStream output = player.getSocket().getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);
                    writer.println("Starting a ranked game");
                    if(team_1.contains(player)){
                        writer.println("You are in team 1");
                    }else if(team_2.contains(player)){
                        writer.println("You are in team 2");
                    }
                }
            }
            System.out.println("Starting the game with " + team_1.size() + " players in team 1 and " + team_2.size() + " players in team 2.");
            System.out.println("The target number is " + targetNumber);

            players.get(playerTurn).updateTurn();
            OutputStream output = players.get(playerTurn).getSocket().getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println("Its your turn");
           
        } else {
            System.out.println("The game has already started.");
        }
    }

    public boolean checkWinner() {
        if (team_1.get(0).getTotal() == targetNumber) {
            System.out.println("Team 1 won!");
            for (ClientInfo player : team_1) {
                OutputStream output;
                try {
                    output = player.getSocket().getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);
                    writer.println("Your team won the game!");
                    player.updateWins();
                    player.updateRank();
                    player.resetTotal();
                    player.finishGame();
                    writer.println("Going back to game menu...");
                    writer.println("Write 'S' to inicialize a normal game");
                    writer.println("Write 'R' to inicialize a ranked game");
                    writer.println("Write 'I' to learn about the game");
                    writer.println("Write 'Q' to quit the game");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            for (ClientInfo player : team_2) {
                OutputStream output;
                try {
                    output = player.getSocket().getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);
                    writer.println("Your team lost the game!");
                    player.updateLoses();
                    player.updateRank();
                    player.resetTotal();
                    player.finishGame();
                    writer.println("Going back to game menu...");
                    writer.println("Write 'S' to inicialize a normal game");
                    writer.println("Write 'R' to inicialize a ranked game");
                    writer.println("Write 'I' to learn about the game");
                    writer.println("Write 'Q' to quit the game");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return true;
        } else if (team_2.get(0).getTotal() == targetNumber) {
            System.out.println("Team 2 won!");
            for (ClientInfo player : team_2) {
                OutputStream output;
                try {
                    output = player.getSocket().getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);
                    writer.println("Your team won the game!");
                    player.updateWins();
                    player.updateRank();
                    player.resetTotal();
                    player.finishGame();
                    writer.println("Going back to game menu...");
                    writer.println("Write 'S' to inicialize a normal game");
                    writer.println("Write 'R' to inicialize a ranked game");
                    writer.println("Write 'I' to learn about the game");
                    writer.println("Write 'Q' to quit the game");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            for (ClientInfo player : team_1) {
                OutputStream output;
                try {
                    output = player.getSocket().getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);
                    writer.println("Your team lost the game!");
                    player.updateLoses();
                    player.updateRank();
                    player.resetTotal();
                    player.finishGame();
                    writer.println("Going back to game menu...");
                    writer.println("Write 'S' to inicialize a normal game");
                    writer.println("Write 'R' to inicialize a ranked game");
                    writer.println("Write 'I' to learn about the game");
                    writer.println("Write 'Q' to quit the game");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void stuff(ClientInfo p, int number){
        int i = players.indexOf(p);
        if (i == 0 || i == 3) {
            players.get(0).addNumber(number);
            players.get(3).addNumber(number);
        } else {
            players.get(1).addNumber(number);
            players.get(2).addNumber(number);
        }
        for (ClientInfo player : players) {
            OutputStream output;
            try {
                output = player.getSocket().getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                writer.println("Score Team 1 is " + team_1.get(0).getTotal());
                writer.println("Score Team 2 is " + team_2.get(0).getTotal());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }

    public void changeTurn(){
        playerTurn += 1;
        if(playerTurn == 4){
            playerTurn = 0;
        }
        OutputStream output;
        try {
            output = players.get(playerTurn).getSocket().getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println("Its your turn");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}