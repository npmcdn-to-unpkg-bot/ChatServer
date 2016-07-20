import spark.Spark.*
import com.google.gson.Gson
import org.eclipse.jetty.websocket.api.*
import org.eclipse.jetty.websocket.api.annotations.*
import java.util.*

fun toJson(message: String): String =  Gson().toJson(message)

val sessions: MutableMap<Session, String> = HashMap<Session, String>()

fun create(user: String = "User", text: String): String = "$user: $text"

fun broadcast(message:  String) {
    sessions.keys.filter { s -> s.isOpen }.forEach { s ->
        try {
            s.remote.sendString(toJson(message))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@WebSocket
class WebSocketHandler {

    @OnWebSocketConnect
    fun connect(session: Session) {
        sessions.put(session, "User")
        broadcast(create("User", "has joined the fun"))
    }

    @OnWebSocketMessage
    fun message(message: String) {
        broadcast(message)
    }

    @OnWebSocketClose
    fun close(session: Session, state: Int, reason: String) {
        val username = sessions[session]
        broadcast(create("User", "has left the chat"))
    }
}

fun main(args: Array<String>) {

    staticFileLocation("/public")

    webSocket("/chat", WebSocketHandler::class.java)

    init()
}
