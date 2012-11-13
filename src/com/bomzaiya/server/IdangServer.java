package com.bomzaiya.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

public class IdangServer {

	private static final String SOCKET_REQUEST_CONNECTION = "SOCKET_REQUEST_CONNECTION";
	private static final String SOCKET_REQUEST_LOGIN = "SOCKET_REQUEST_LOGIN";
	private static final String SOCKET_REQUEST_END = "SOCKET_REQUEST_END";

	private static final String SOCKET_EVENT = "event";
	private static final String SOCKET_DATA = "data";

	private static final String SOCKET_EVENT_LOGIN = "SOCKET_EVENT_LOGIN";
	private static final String SOCKET_EVENT_MESSAGE = "SOCKET_EVENT_MESSAGE";
	private static final String SOCKET_EVENT_CONNECTION = "SOCKET_EVENT_CONNECTION";
	private static final String SOCKET_EVENT_END = "SOCKET_EVENT_END";
	private static final String SOCKET_EVENT_CONNECTED = "SOCKET_EVENT_CONNECTED";
	private static final String SOCKET_EVENT_PINGPONG = "SOCKET_EVENT_PINGPONG";
	private static final String SOCKET_EVENT_UPDATE = "SOCKET_EVENT_UPDATE";
	private static final String SOCKET_EVENT_COMMAND = "SOCKET_EVENT_COMMAND";

	private static final int STATE_CONNECTION = 1;
	private static final int STATE_LOGIN = 2;
	private static final int STATE_CONNECTED = 3;
	private static final int STATE_PINGPONG = 4;
	private static final int STATE_UPDATE = 5;

	private static int mState = 0;

	public static Charset charset = Charset.forName("UTF-8");
	public static CharsetEncoder encoder = charset.newEncoder();
	public static CharsetDecoder decoder = charset.newDecoder();
	private static boolean mNeedLogin;

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException {
		IdangServer IdangServer = new IdangServer();
		IdangServer.Server();
	}

	public static void Server() throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		Selector selector = Selector.open();

		ServerSocketChannel server1 = ServerSocketChannel.open();
		server1.configureBlocking(false);
		server1.socket().bind(new InetSocketAddress(40000));
		server1.register(selector, SelectionKey.OP_ACCEPT);

		ServerSocketChannel server2 = ServerSocketChannel.open();
		server2.configureBlocking(false);
		server2.socket().bind(new InetSocketAddress(40001));
		server2.register(selector, SelectionKey.OP_ACCEPT);

		while (true) {
			selector.select();
			Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
			while (iter.hasNext()) {
				SocketChannel client;
				SelectionKey key = iter.next();
				iter.remove();
				switch (key.readyOps()) {
				case SelectionKey.OP_ACCEPT:
					client = ((ServerSocketChannel) key.channel()).accept();
					client.configureBlocking(false);
					client.register(selector, SelectionKey.OP_READ);
					mState = STATE_CONNECTION;
					break;

				case SelectionKey.OP_READ:
					client = (SocketChannel) key.channel();

					buffer.clear();
					if (client.read(buffer) != -1) {
						buffer.flip();
						String line = new String(buffer.array(),
								buffer.position(), buffer.remaining());

						try {
							JSONObject jsonObject = new JSONObject(line);
							String event = jsonObject.getString("event");
							String data = jsonObject.getString("data");
							System.out.println(event + ":" + data);
							if (event.equals(SOCKET_REQUEST_CONNECTION)) {
								mState = STATE_LOGIN;
								System.out.println(mState + "\r\n");
								client.register(selector, SelectionKey.OP_WRITE);
							} else if (event.equals(SOCKET_EVENT_LOGIN)) {
								mState = STATE_CONNECTED;
								System.out.println(mState + "\r\n");
								client.register(selector, SelectionKey.OP_WRITE);
							} else if (event.equals(SOCKET_EVENT_MESSAGE)) {
								mState = STATE_UPDATE;
								System.out.println(mState + "\r\n");
								client.register(selector, SelectionKey.OP_WRITE);
							} else if (event.equals(SOCKET_EVENT_COMMAND)) {
								mState = STATE_UPDATE;
								System.out.println(mState + "\r\n");
								client.register(selector, SelectionKey.OP_WRITE);
							}
						} catch (JSONException e) {
						}

					} else {
						key.cancel();
					}

					break;

				case SelectionKey.OP_WRITE:
					client = (SocketChannel) key.channel();
					switch (mState) {
					case STATE_LOGIN:
						emit(SOCKET_REQUEST_LOGIN, "", client);
						client.register(selector, SelectionKey.OP_READ);
						break;

					case STATE_CONNECTED:
						emit(SOCKET_EVENT_CONNECTED, "", client);
						client.register(selector, SelectionKey.OP_READ);
						break;

					case STATE_UPDATE:
						emit(SOCKET_EVENT_UPDATE, "MANY DATA", client);
						client.register(selector, SelectionKey.OP_READ);
						break;

					default:
						break;
					}

					break;

				default:
					System.out.println("unhandled " + key.readyOps());
					break;
				}
			}
		}
	}

	private static void emit(String event, Object data, SocketChannel client) {
		JSONObject jso = new JSONObject();
		try {
			jso.put(SOCKET_EVENT, event);
			jso.put(SOCKET_DATA, data);
			try {
				client.write(encoder.encode(CharBuffer.wrap(jso.toString()
						+ "\r\n")));

			} catch (CharacterCodingException e) {
			} catch (IOException e) {
			}
		} catch (JSONException e) {
		}

	}

}
