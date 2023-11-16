package com.ieseljust.psp.client.communications;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import com.ieseljust.psp.client.CurrentConfig;
import com.ieseljust.psp.client.Message;
import com.ieseljust.psp.client.ViewModel;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class serverListener implements Runnable {

    /*
     * Aquesta classe s'encarrega de gestionar els broadcasts que fa el servidor
     * cap als clients subscrits a les seues publicacions.
     * Implementarà doncs un servei que escoltarà en un port aleatori el que
     * li envia el servidor de missatgeria i ho processarà de forma adeqüada.
     * 
     */
    public static ViewModel vm;

    public serverListener(ViewModel vm) {
        this.vm = vm;
    }

    int listenerPort = CurrentConfig.listenPort();

    @Override
    public void run() {
        // 1. Crear un socket de tipus servidor que escolte pel port de recepció de
        // missatges
        ServerSocket listener = null;
        try {
            // Creem el socket en un  port determinat pel sistema
            // i el guardem a listenPort.
            listener = new ServerSocket(0);
            CurrentConfig.setlistenPort(listener.getLocalPort());

            while (true) {
                Socket clientSocket = listener.accept(); // Accepta la connexió entrant

                // Llegir el missatge rebut del client
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                String clientMessage = reader.readLine();

                // Processar el missatge rebuda
                processMessage(clientMessage);

                // Tancar el socket del client
                writer.write("{'status':'ok'}");
                writer.flush();

                writer.close();
                reader.close();

                clientSocket.close();
            }

        } catch (IOException e) {
            System.out.println("El port " + listenerPort + " està ocupat o és inaccessible.");
            return;
        }

        // TO-DO
        // 2. Iniciem un bucle infinit a l'espera de rebre connexions
        // Quan arribe una connexió la processrem de manera adeqüada
        // Les peticions que podme rebre seran de tipus:
        // {"type": "userlist", "content": [Llista d'usuaris]}, amb un JSONArray amb la llista d'usuaris.
        // {"type": "message", "user":"usuari", "content": "Contingut del missatge" }
        // És interessant implementar un mètode a banda per processat aquestes línies
        // però no cal que creem un fil a propòsit per atendre cada missatge, ja que
        // no som un servidor com a tal, i el que estem fent aci, és mantindre un 
        // canal de recepció només amb el servidor.
//        ViewModel.llistaUsuaris;
    }

    private static void processMessage(String message) {
        try {
            System.out.println("message" + message);
            JSONObject jsonObject = new JSONObject(message);
            String messageType = jsonObject.getString("type");

            // Processar segons el tipus de missatge
            if (messageType.equals("userlist")) {
                JSONArray userList = jsonObject.getJSONArray("content");
                ArrayList<String> users = new ArrayList();//null

                for (int i = 0; i < userList.length(); i++) {
                    // users.add(userList.toString(i));
                    String userName = userList.getString(i);
                    users.add(userName);

                }

                vm.updateUserList(users);

                // Processar la llista d'usuaris
            } else if (messageType.equals("message")) {
                String user = jsonObject.getString("user");
                String content = jsonObject.getString("content");
                // Processar el missatge i l'usuari
                Message msg = new Message(user, content);
                vm.addMessage(msg);
            } else {
                System.out.println("Tipus de missatge desconegut: " + messageType);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
