import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ServerExample extends Application {
    ExecutorService executorService; //스레드 풀을 생성하기 위해 선언
    ServerSocket serverSocket;       //클라이언트의 연결 요청을 수락하기 위해 선언
    List<Client> connections = new Vector<Client>();  //벡터로 객체를 생성한다. 서버쪽에서 클라이언트를 관리하기 위해 리스트에 저장해서 관리한다. 벡터는 멀티쓰레드에서 쓰레드에 안전하기 떄문에 상ㅇ했다.

    void startServer() { // 서버 시작 코드/ start버튼을 눌렀을 때 동작한다.
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); //코어의 수만큼 스레드를 생성해서 사용한다.

        try {
            serverSocket = new ServerSocket(); //서버 소켓 생성
            serverSocket.bind(new InetSocketAddress("localhost", 5001)); //포트에 바인딩하기 .
        } catch (Exception e) {
            if (!serverSocket.isClosed()) { //예외가 발생하면 안전하게 서버를 닫아준다.
                stopServer();               //밑에 있는 메소드를 호출한다.
            }
            return;                         //startServer()를 종료한다.
        }

        Runnable runnable = new Runnable() {          //연결 수락작업을 수행할 수 있게끔 ! 연결 수락작업객체라고 보면됨.
            @Override
            public void run() {
                Platform.runLater(() -> {              //밑에 있는 두 코드를 실행하는 것은 (람다식) 스레드 풀의 스레드가 아니라 자바 fx어플리케이션 쓰레드가 실행한다고 보면 된다.
                    displayText("[서버 시작]");          //그냥 쉽게 UI변경을 하려면 Platform.runlater~~~ 로 이 방법으로 만든다고 보면 된다. 스레드끼리의 문제떄문인걸로 알고 있다. 자세한건 javafx스레드 공부하고 나면 이해될듯.
                    btnStartStop.setText("stop");
                });
                while (true) {//클라이언트의 연결 수락 작업을 계속할 수 있도록 무한루프로 만든다.
                    try {
                        Socket socket = serverSocket.accept();  //클라이언트가 연결 요청할때까지 대기하다가(블로킹) 연결요청이 오면 socket을 리턴한다.
                        String message = "[연결 수락: " + socket.getRemoteSocketAddress() + ": " //클라이언트의 ip주소와 포트에 대한 정보를 얻는다.toString()... 이라고 보면 됨.
                                + Thread.currentThread().getName() + "]";                      //스레드 풀의 어떤 스레드가 실행하고 있는지 보여준다.
                        Platform.runLater(() -> displayText(message)); //로그를 출력한다.

                        Client client = new Client(socket);                  //클라이언트 객체를 만든다. 매개값으로 socket을 제공한다.
                        connections.add(client);						     //벡터에 저장한다.
                        Platform.runLater(() -> displayText("[연결 개수: " + connections.size() + "]")); //또 로그를 출력한다. 몇개가 연결되었는지 보여준다.
                    } catch (Exception e) {
                        if (!serverSocket.isClosed()) { //예외가 발생하면 안전하게 종료한다.
                            stopServer();
                        }
                        break;                          //더 이상 while문이 실행하지 않도록 break 한다. 즉 예외가 발생하면 무한루프를 빠져나온다.
                    }
                }
            }
        };
        executorService.submit(runnable);               //StartServer()의 마지막 코드. Runnable runnable에서 생성한 연결 수락 작업 객체를 스레드 풀에 스레드에서 처리할 수 있기 위해서 submit()을 통해 작업 객체 runnable을 매개값으로 전달해준다.
    }

    void stopServer() { //start 버튼을 누르고 나면 버튼이 stop으로 변하는데 그때 stop버튼을 누르면 실행된다.
        try {
            Iterator<Client> iterator = connections.iterator(); // 반복자를 얻어낸다.
            while (iterator.hasNext()) {                        //이터레이터에서 클라이언트를 하나 가져오기 위해서... 가져올게 있다면?
                Client client = iterator.next();                //하나 가져온다.
                client.socket.close();                          //가져온 클라리언트의 socket을 닫는다. 예외처리도 해준다.
                iterator.remove();								//연결을 끊었으니 클라이언트를 컬렉션에서 제거해야 해서 모든 클라이언트를 리스트에서 제거해줬다.
            }
            //이제 서버 소켓을 닫아주자.
            if (serverSocket != null && !serverSocket.isClosed()) {  //서버 소켓을 닫아준다.
                serverSocket.close();
            }
            if (executorService != null && !executorService.isShutdown()) { //executorService도 닫아준다.
                executorService.shutdown();
            }

            //로그 출력해주기.
            Platform.runLater(() -> {
                displayText("[서버 멈춤]");
                btnStartStop.setText("start"); //버튼은 다시 start로 바꿔야 한다.
            });
        } catch (Exception e) {
        }
    }

    class Client { //클라이언트 통신 코드 / 데이터 통신 코드
        Socket socket;  //필드

        Client(Socket socket) {
            this.socket = socket; //사용할 소켓이다. 대입해준다.
            receive();            //클라이언트에게 데이터를 받을 준비를 항상 해놓는다.
        }

        void receive() { //클라이언트가 보낸 데이터를 받기 위해 정의됨. 항상 서버쪽의 통신 소켓을 이용해서 읽는다. 무한루프를 돌면서!클라이언트가 정상/비정상 종료하면 catch로 이동해서 무한루프가 종료된다.
            Runnable runnable = new Runnable() { //별도의 스레드에서 클라이언트의 데이터를 받아야 한다.
                @Override
                public void run() { //클라이언트가 보낸 데이터를 받을 코드를 작성한다.
                    try {
                        while (true) { //무한루프 생성. 계속 클라이언트에서 보낸 데이터를 받기 위해서
                            byte[] byteArr = new byte[100]; //클라이언트에서 보낸 데이터를 일단 저장할 바이트배열
                            InputStream inputStream = socket.getInputStream();

                            // 클라이언트가 비정상 종료를 했을 경우 IOException 발생
                            int readByteCount = inputStream.read(byteArr); //클라이언트가 보낸 데이터를 받는다.

                            // 클라이언트가 정상적으로 Socket의 close()를 호출했을 경우
                            if (readByteCount == -1) {
                                throw new IOException(); //강제적으로 IOException을 발생시켜서 밑에 있는 데이터를 받는 코드가 실행되지 않도록 catch로 이동한다.왜냐면 정상적으로 종료된거니까! 어차피 비정상 종료면 IOException 발생해서 밑에 예외로 가게 되어있음.
                            }

                            String message = "[요청 처리: " + socket.getRemoteSocketAddress() + ": " //어떤클라이언트가 보낸 데이터냐.. 를 출력하기 위해서 .. ! 클라이언트 ip주소 출력함.
                                    + Thread.currentThread().getName() + "]";                      //어떤 스레드가 요청을 처리했는지를 알기 위해서 !
                            Platform.runLater(() -> displayText(message));                         //로그 출력

                            String data = new String(byteArr, 0, readByteCount, "UTF-8");          //읽은 데이터를 문자열로 변환함.

                            for (Client client : connections) {                                    //우리가 받은 데이터를 모든 클라이언트에게 전송을 할려고 한다. 채팅이니까. 그니까 하나의 클라이언트가 보낸 데이터를 모든 클라이언트에게 전달한다.
                                client.send(data);													//send() 메소드 호출
                            }
                        }
                    } catch (Exception e) {    //read()메소드를 호출하게 되면은 (114줄에서)  클라이언트가 비정상적으로 종료가 되면 IOException이 발생한다. 정상적으로 종료하면 read는 -1을 리턴한다. 그러므로 비정상/정상 인경우 둘다 이 코드로 오게 된다.
                        try {
                            connections.remove(Client.this);          //클라이언트가 더 이상 연결되어 있지 않기 때문에 해당 클라이언트에 해당되는 클라이언트 객체를 컬렉션에서 제거한다./ this라고만 적으면 runnable객체을 의미한다. 그래서 바깥 클라이언트 클래스의 인스턴스다 라는 뜻에서 Client.this로 적는다.
                            String message = "[클라이언트 통신 안됨: " + socket.getRemoteSocketAddress() + ": " //로그 생성
                                    + Thread.currentThread().getName() + "]";
                            Platform.runLater(() -> displayText(message)); //로그 출력한다.
                            socket.close();                                //이제 닫아준다. 클라이언트가 통신 안되니까 -> 예외가 발생했으니까 ->클라이언트가 소켓을 close했기 때문에 예외가 발생했으니 ->  socket을 닫아준다.
                        } catch (IOException e2) { //close()에 대한 예외 처리임.
                        }
                    }
                }
            };
            executorService.submit(runnable);     //스레드풀이 작업큐에 위 작업객체를 저장할 수 있도록 runnable을 매개값으로 준다. executorService내부의 스레드가 결국 run작업을 실행한다.
        }

        void send(String data) { //클라이언트로 데이터를 보낸다. 매개값으로 문자열을 받아서 이걸 클라이언트로 전송한다. 얘도 스레드풀의 스레드가 처리해야 한다.
            Runnable runnable = new Runnable() { //작업을 정의한다.
                @Override
                public void run() { //클라이언트로 보내는 코드를 작성해보자.
                    try {
                        byte[] byteArr = data.getBytes("UTF-8"); //보내고자 하는 데이터를 바이트 배열로 만든다. 문자셋을 utf로 한다. 여기서 발생하는 예외를 처리해준다.
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(byteArr); //여기서도 IOException이 발생할 수 있기 때문에 예외를 Exception으로 잡아준다. (위에 있는 예외랑 같이 사용하기 위해서 )
                        outputStream.flush();
                    } catch (Exception e) { //getBytes랑 write에서 예외가 발생해서 여기서 잡아주는데, 아마 getBytes는 정확하게 UTF-8로 줬기 때문에 예외가 안생길거다. 아마 write에서 발생하는 것일 거다. write에서 발생하는 경우는 클라이언트가 통신이 안되는 경우일 것이다.
                        try {
                            String message = "[클라이언트 통신 안됨: " + socket.getRemoteSocketAddress() + ": " //통신이 안되므로 다음과 같이 작성.
                                    + Thread.currentThread().getName() + "]";
                            Platform.runLater(() -> displayText(message));
                            connections.remove(Client.this);                         //클라이언트가 통신이 안되기 때문에 더 이상 Client객체는 필요가 없다. 그러므로 컬렉션으로 삭제한다. Client객체의 참조인 this
                            socket.close();                                       //아울러 소켓도 닫아준다. 여기서도 예외가 발생할 수 있으므로 예외를 잡아준다.
                        } catch (IOException e2) {
                        }
                    }
                }
            };
            executorService.submit(runnable); //스레드풀의 작업큐에 저장하기 위해서 다음과 같이 작성한다.
        }
    }

    //여기서부터는 ui관련 코드이다.
    //////////////////////////////////////////////////////
    TextArea txtDisplay;
    Button btnStartStop;

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        root.setPrefSize(500, 300);

        txtDisplay = new TextArea();
        txtDisplay.setEditable(false);
        BorderPane.setMargin(txtDisplay, new Insets(0, 0, 2, 0));
        root.setCenter(txtDisplay);

        btnStartStop = new Button("start");
        btnStartStop.setPrefHeight(30);
        btnStartStop.setMaxWidth(Double.MAX_VALUE);
        btnStartStop.setOnAction(e -> {
            if (btnStartStop.getText().equals("start")) {
                startServer();
            } else if (btnStartStop.getText().equals("stop")) {
                stopServer();
            }
        });
        root.setBottom(btnStartStop);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Server");
        primaryStage.setOnCloseRequest(event -> stopServer());
        primaryStage.show();
    }

    void displayText(String text) {
        txtDisplay.appendText(text + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}