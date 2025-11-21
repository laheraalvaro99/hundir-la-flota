/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.hundirlaflota;

/**
 *
 * @author Diurno
 */
import java.io.*;
import static java.lang.Integer.SIZE;
import java.net.*;

public class PlayerHandler {
    Socket socket;
        BufferedReader in;
        PrintWriter out;
        int id;
        char[][] board = new char[SIZE][SIZE];

        PlayerHandler(Socket s, int id) throws IOException {
            this.socket = s;
            this.id = id;
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream(), true);
        }

        void send(String msg) {
            out.println(msg);
        }

        void readBoard() throws IOException {
            send("INFO_SEND_10_LINES");
            for (int i = 0; i < SIZE; i++) {
                String line = in.readLine();
                if (line == null) throw new IOException("cliente desconectado");
                board[i] = line.toCharArray();
            }
            send("BOARD_OK");
        }

        boolean allShipsSunk() {
            for (int i = 0; i < SIZE; i++)
                for (int j = 0; j < SIZE; j++)
                    if (board[i][j] == 'B')
                        return false;
            return true;
        }
    }

