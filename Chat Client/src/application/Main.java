package application;
	
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;


public class Main extends Application {
	
	Socket socket;
	TextArea textArea;
	
	// 클라이언트 프로그램 동작 메소드
	public void startClient(String IP, int port) {
		// 클라이언트는 서버랑 달리 여러 쓰레드가 동시에 실행될 필요가 없으므로 ThreadPool이 아니라 그냥 Thread 사용
		Thread thread = new Thread() { // 서버로부터 메시지를 전달받는 쓰레드
			public void run() {
				try {
					socket = new Socket(IP, port); // 소켓 초기화
					receive();
				} catch (Exception e) {
					if(!socket.isClosed()) {
						stopClient();
						System.out.println("[서버 접속 실패]");
						Platform.exit();  // 프로그램 자체를 종료
					}
				}
			}
		};
		thread.start(); // 쓰레드 실행
	};
	
	// 클라이언트 프로그램 종료 메소드
	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 서버로부터 메시지를 전달받는 메소드
	public void receive() {
		// 서버로부터 계속 메시지를 전달받을 수 있도록 반복
		while(true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[512];
				int length = in.read(buffer);
				if(length == -1) throw new IOException();
				
				String message = new String(buffer, 0 , length, "UTF-8");
				Platform.runLater(() -> {
					textArea.appendText(message); // 메시지 출력
				});
			} catch (Exception e) {
				stopClient();
				break;
			}
		}
	}
	
	// 서버로 메시지를 전송하는 메소드
	public void send(String message) {
		Thread thread = new Thread() { // 서버로 메세지를 전달하는 쓰레드
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}
	
	// 실제로 프로그램을 동작시키는 메소드
	@Override
	public void start(Stage primaryStage) {
		
	}
	
	// 프로그램의 진입점
	public static void main(String[] args) {
		launch(args);
	}
}
