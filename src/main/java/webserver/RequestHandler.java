package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	
	private Socket connection;
	
	private boolean logined = false;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
		
		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
			
			//http header 읽어오기 & 출력하기
			BufferedReader inBuf = new BufferedReader(new InputStreamReader(in));	
			String str = inBuf.readLine();
			
			
			//url 받아오기, webapp 디렉토리에 읽어 전달하기
			String url = takeString(str, " ", 1);
			
			
			//버퍼에서 값 받아오기
			int length = 0;
			while (!"".equals(str)){
				log.debug("header : {}", str);
				str = inBuf.readLine();

				if(str.startsWith("Content-Length")){
					length = Integer.parseInt(takeString(str, ": ", 1));
				}
				if (str == null) {
					return;
				}
			}
						
			if(url.equals("/create")){
				IOUtils ioUtil = new IOUtils();
				addUser(ioUtil.readData(inBuf, length));
			}
			
			System.out.println("\turl = " + url);
			String userInfo = takeString(url, " ", 0);
			System.out.println(userInfo);
//			if(url.startsWith("/login")){
//			}
			
			showWeb(out, url);
			
			
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	

	private String takeString(String str, String sp, int i){
		String[] tokens = str.split(sp);
		return tokens[i];
	}
	
	private void showWeb(OutputStream out, String url) throws IOException{
		DataOutputStream dos = new DataOutputStream(out);
		
		if(url.equals("/create")){
			response302Header(dos);
		}
		else{
			// file은 현재 존재하는 것(form.html, index.html 등)만 불러올 수 있다.
			// 따라서 현재 존재하지 않는 파일 /create는 불러올 수 없기 때문에 위에 올려놓을 경우 에러가 발생할 수밖에 없다.
			byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
			response200Header(dos, body.length);
			responseBody(dos, body);
		}
	}
	
	private void addUser(String queryString){
		HttpRequestUtils re = new HttpRequestUtils();
		Map<String, String> userInf  = re.parseQueryString(queryString);
		
		User user = new User(userInf.get("password"), userInf.get("name"), userInf.get("userId"), userInf.get("email"));
		DataBase.addUser(user);
		
		log.debug("user : {}", user);
		logined = true;
	}
	
	private boolean userCheck(String userId, String password){
		return DataBase.userCheck(userId, password);
	}
	
	
	
	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			// 이 header 정보를 보내줌으로써 여기에 맞춰서 출력
			// 브라우저가 그냥 아는 것이 아니다
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void response302Header(DataOutputStream dos) {
		try {
			// 이동만 알려주는 것이므로 단지 주소만 가지고 body는 없음
			dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
			dos.writeBytes("Location: /index.html \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.writeBytes("\r\n");
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
