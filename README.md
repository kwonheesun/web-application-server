# 실습을 위한 개발 환경 세팅
* https://github.com/slipp/web-application-server 프로젝트를 자신의 계정으로 Fork한다. Github 우측 상단의 Fork 버튼을 클릭하면 자신의 계정으로 Fork된다.
* Fork한 프로젝트를 eclipse 또는 터미널에서 clone 한다.
* Fork한 프로젝트를 eclipse로 import한 후에 Maven 빌드 도구를 활용해 eclipse 프로젝트로 변환한다.(mvn eclipse:clean eclipse:eclipse)
* 빌드가 성공하면 반드시 refresh(fn + f5)를 실행해야 한다.

# 웹 서버 시작 및 테스트
* webserver.WebServer 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다. 
* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.
* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답
* favicon을 얻기 위해 서버와 웹은 여러번 통신한다.
* log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
*	logging은 화면에 출력됨
* 12:01:24.608 [DEBUG] [Thread-1] [webserver.RequestHandler] - New Client Connect! Connected IP : /0:0:0:0:0:0:0:1, Port : 50702
* 12:01:24.608 [DEBUG] [Thread-2] [webserver.RequestHandler] - New Client Connect! Connected IP : /0:0:0:0:0:0:0:1, Port : 50703
* 12:01:24.609 [DEBUG] [Thread-3] [webserver.RequestHandler] - New Client Connect! Connected IP : /0:0:0:0:0:0:0:1, Port : 50704
*  포트 번호가 모두 다르다. 왜?
* showWeb(out, url);		//순서가 상관 있는가? 요청을 받고 바로 보내버리면 전체의 http를 출력할 수 없다.


### 요구사항 2 - get 방식으로 회원가입
* static method는 클래스에 바로 접속하여 사용할 수 있음
* while (!"".equals(str)){
		log.debug("header : {}", str);
		str = inBuf.readLine();
		if (str == null) {
			return;
		}
	}
	
* String url = takeURL(str);		//좀더 깔끔하게 값을 받아오는 것?
	if(url.startsWith("/create?")){
		addUser(url);
		
		//default page 설정
		url = "/index.html";
	}

* private String getQueryString(String url){
		int index = url.indexOf("?");
		String requestPath = url.substring(0, index);
		return url.substring(index+1);
	}

### 요구사항 3 - post 방식으로 회원가입
* post 방식과 get 방식의 차이
* if GET 방식인 경우 -> 요구사항 2 처럼 URL을 걸러내고 계산함
* if POST 방식인 경우 -> 다른 방식으로 변경됨

* GET과 POST의 차이점?
	get : 서버에 보낼 때 사이즈 제한이 있음
			서버에 있는 정보를 얻어오는 경우
	post : 글을 써서 db 등 서버에 영향을 미치는 경우
			  삭제, 수정 등의 상황
	html에서는 get과 post만 지원가능
	서버에서는 그 이상 가능
	

### 요구사항 4 - redirect 방식으로 이동
* if(str.contains("Content-Length")){
		length = getContentLength(str);
	}

### 요구사항 5 - cookie
* 

### 요구사항 6 - stylesheet 적용
* 

### heroku 서버에 배포 후
* 