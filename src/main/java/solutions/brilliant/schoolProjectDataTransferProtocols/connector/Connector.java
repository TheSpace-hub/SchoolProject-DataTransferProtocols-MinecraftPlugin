package solutions.brilliant.schoolProjectDataTransferProtocols.connector;

import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import solutions.brilliant.schoolProjectDataTransferProtocols.data.DataIn;
import solutions.brilliant.schoolProjectDataTransferProtocols.data.DataOut;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

public class Connector implements Runnable {

    private final int port;
    private final String available;
    private final Plugin plugin;

    private BufferedReader reader;
    private BufferedWriter writer;

    public Connector(int port, String available, Plugin plugin) {
        this.port = port;
        this.available = available;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = getServerSocket(port);
        if (serverSocket == null) return;

        Bukkit.getLogger().log(Level.INFO, "Ожидание подключения...");
        Socket socket = acceptConnection(serverSocket);
        Bukkit.getLogger().log(Level.INFO, "Подключение установленно");
        if (socket == null) return;

        reader = getBufferedReader(socket);
        writer = getBufferedWriter(socket);

        if (reader == null || writer == null) {
            reader = null;
            writer = null;
            return;
        }

        Bukkit.getLogger().log(Level.FINEST, "Соединение успешно установленно");
    }

    public void openConnection() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this);
    }

    public boolean isConnected() {
        return !(reader == null || writer == null);
    }

    public DataIn getData() {
        return new GsonBuilder().create().fromJson(getReaderData(), DataIn.class);
    }

    public void sendData(DataOut data) {
        sendDataByWriter(
                new GsonBuilder().create().toJson(data)
        );
    }

    private @Nullable ServerSocket getServerSocket(int port) {
        try {
            return new ServerSocket(port);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Ошибка при создании сокета сервера (вероятно, порт занят)");
        }
        return null;
    }

    private @Nullable String getReaderData() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    private void sendDataByWriter(String data) {
        try {
            writer.write(data + "\n");
            writer.flush();
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Произошла ошибка при передаче данных");
        }
    }

    private static Socket acceptConnection(ServerSocket serverSocket) {
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Ошибка при принятии соединения");
        }
        return null;
    }

    private static BufferedReader getBufferedReader(Socket socket) {
        try {
            return new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Ошибка при создании buffer reader");
        }
        return null;
    }

    private static BufferedWriter getBufferedWriter(Socket socket) {
        try {
            return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Ошибка при создании buffer writer");
        }
        return null;
    }

    public int getPort() {
        return port;
    }

    public String getAvailable() {
        return available;
    }

}
