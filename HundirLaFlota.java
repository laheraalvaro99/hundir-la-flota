/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.hundirlaflota;

/**
 *
 * @author Diurno
 */

import java.io.*;
import java.net.*;

public class HundirLaFlota {

    private static final int PORT = 12345;
    private static final int SIZE = 10;

    private PlayerHandler player1;
    private PlayerHandler player2;
    // Indica si la partida está en curso
    private volatile boolean gameRunning = false;

    public static void main(String[] args) {
        new HundirLaFlota().start();
    }
    
    public void start() {
        System.out.println("Servidor iniciado en puerto " + PORT + "...");
        try (ServerSocket server = new ServerSocket(PORT)) {

            // Esperar jugador 1
            System.out.println("Esperando jugador 1...");
            Socket s1 = server.accept();
            player1 = new PlayerHandler(s1, 1);
            player1.send("WELCOME 1");
            System.out.println("Jugador 1 conectado");

            // Esperar jugador 2
            System.out.println("Esperando jugador 2...");
            Socket s2 = server.accept();
            player2 = new PlayerHandler(s2, 2);
            player2.send("WELCOME 2");
            System.out.println("Jugador 2 conectado");

            // Pedir tableros
            player1.send("SEND_BOARD");
            player2.send("SEND_BOARD");

            // Leer tableros
            player1.readBoard();
            player2.readBoard();

            System.out.println("Ambos tableros recibidos. Iniciando partida...");
            broadcast("START");

            gameRunning = true;
            runGame();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void runGame() {
        int turn = 1;

        while (gameRunning) {
            PlayerHandler current = (turn == 1 ? player1 : player2);
            PlayerHandler enemy = (turn == 1 ? player2 : player1);

            current.send("YOUR_TURN");
            enemy.send("ENEMY_TURN");

            try {
                String cmd = current.in.readLine();
                if (cmd == null) {
                    disconnect(current);
                    break;
                }

                if (cmd.startsWith("FIRE")) {
                    // FIRE x y
                    String[] p = cmd.split(" ");
                    int x = Integer.parseInt(p[1]);
                    int y = Integer.parseInt(p[2]);

                    if (enemy.board[x][y] == 'B') {
                        enemy.board[x][y] = 'X';
                        current.send("HIT " + x + " " + y);
                        enemy.send("GOT_HIT " + x + " " + y);

                        if (enemy.allShipsSunk()) {
                            current.send("YOU_WIN");
                            enemy.send("YOU_LOSE");
                            gameRunning = false;
                        }
                    } else {
                        current.send("MISS " + x + " " + y);
                        enemy.send("ENEMY_MISSED " + x + " " + y);
                    }

                    // Cambiar turno simple
                    turn = (turn == 1 ? 2 : 1);
                }

            } catch (IOException ex) {
                disconnect(current);
                break;
            }
        }

        System.out.println("Partida terminada.");
    }
    
    private void disconnect(PlayerHandler p) {
        System.out.println("Jugador " + p.id + " desconectado.");
        if (p == player1 && player2 != null) player2.send("OPPONENT_DISCONNECTED");
        if (p == player2 && player1 != null) player1.send("OPPONENT_DISCONNECTED");
        gameRunning = false;
    }

    private void broadcast(String msg) {
        player1.send(msg);
        player2.send(msg);
    }
    
}
