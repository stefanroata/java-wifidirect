package com.example.wifidirect;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerAsyncTask extends AsyncTask<Void, Void, Void> {
    public ServerSocket serverSocket;
    public Socket client;

    public ServerAsyncTask(int port) throws IOException{
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            Log.v("Waiting for client on port ", serverSocket.getLocalPort() + "...");
            client = serverSocket.accept(); // this call blocks until a connection is accepted by the client
            Log.v("JUST CONNECTED TO: ", client.getRemoteSocketAddress() + "...");
            BufferedReader bufferedReader = new  BufferedReader( new InputStreamReader(client.getInputStream()));
            Log.v("The message received is: ", bufferedReader.readLine());
        }
        catch (SocketTimeoutException ignored){
            Log.v("SOCKET TIMED OUT!", "");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute() {
        Log.v("COMPLETED THREAD", "");
    }
}
