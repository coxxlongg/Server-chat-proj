import kz.ori.ClientHandler;
import kz.ori.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServerTest {
    private Server server;
    private ClientHandler clientHandler1;
    private ClientHandler clientHandler2;

    @BeforeEach
    public void setUp() {
        server = new Server(8189);
        clientHandler1 = Mockito.mock(ClientHandler.class);
        clientHandler2 = Mockito.mock(ClientHandler.class);
        when(clientHandler1.getUsername()).thenReturn("User1");
        when(clientHandler2.getUsername()).thenReturn("User2");
    }

    @Test
    public void testSubscribe() {
        server.subscribe(clientHandler1);
        List<ClientHandler> clients = server.getClientHandlers();
        assertEquals(1, clients.size());
        assertTrue(clients.contains(clientHandler1));
    }

    @Test
    public void testUnsubscribe() {
        server.subscribe(clientHandler1);
        server.unsubscribe(clientHandler1);
        List<ClientHandler> clients = server.getClientHandlers();
        assertFalse(clients.contains(clientHandler1));
    }

    @Test
    public void testBroadcastMessage() {
        server.subscribe(clientHandler1);
        server.subscribe(clientHandler2);
        server.broadcastMessage("Hello");
        verify(clientHandler1, times(1)).sendMessage("Hello");
        verify(clientHandler2, times(1)).sendMessage("Hello");
    }

    @Test
    public void testSendPrivateMsg() {
        server.subscribe(clientHandler1);
        server.subscribe(clientHandler2);
        server.sendPrivateMsg(clientHandler1, "User2", "Hello User2");
        verify(clientHandler2, times(1)).sendMessage("From: User1 Message: Hello User2");
        verify(clientHandler1, times(1)).sendMessage("Receiver: User2 Message: Hello User2");
    }

    @Test
    public void testIsUserOnline() {
        server.subscribe(clientHandler1);
        assertTrue(server.isUserOnline("User1"));
        assertFalse(server.isUserOnline("User3"));
    }
}
