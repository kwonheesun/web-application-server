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
			String url = takeURL(str, " ");
			int length = 0;
			
			//버퍼에서 값 받아오기
			while (!"".equals(str)){
				log.debug("header : {}", str);
				str = inBuf.readLine();
				
				if(str.startsWith("Content-Length")){
					length = Integer.parseInt(takeURL(str, ": "));
				}
				
				if (str == null) {
					return;
				}
			}
						
			if(url.equals("/create")){
				IOUtils ioUtil = new IOUtils();
				addUser(ioUtil.readData(inBuf, length));

				url = "/index.html";
			}
			
			showWeb(out, url);
			
			
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	

	private String takeURL(String str, String sp){
		String[] tokens = str.split(sp);
		return tokens[1];
	}
	
	private void showWeb(OutputStream out, String url) throws IOException{
		DataOutputStream dos = new DataOutputStream(out);
		byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
		
		response200Header(dos, body.length);
		responseBody(dos, body);
	}
	
	private String getQueryString(String url){
		int index = url.indexOf("?");
		String requestPath = url.substring(0, index);
		return url.substring(index+1);
	}
	
	private void addUser(String queryString){
		HttpRequestUtils re = new HttpRequestUtils();
		Map<String, String> userInf  = re.parseQueryString(queryString);
		
		User user = new User(userInf.get("password"), userInf.get("name"), userInf.get("userId"), userInf.get("email"));
		DataBase.addUser(user);
		
		log.debug("user : {}", user);
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
