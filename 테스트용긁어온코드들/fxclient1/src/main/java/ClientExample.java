import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ClientExample extends Application { //javafx 실행클래스로 만들기 위해 Application클래스를  상속받았다.
    Socket socket; //필드

    void startClient() { //연결 시작 코드
        //소켓 객체를 생성해서 서버에 연결 요청하는 코드를 작성을 하면 된다. 근데 하나 고려해야 할점은 서버와 연결이 되기 전까지 블로킹 즉 대기상태가 되기 때문에 자바 fx 어플리케이션 스레드가 연결을 요청하는 코드를 실행하지 않도록 하는게 좋다.
        //그래서 별도의 스레드를 생성해서 연결 요청을 하도록 만들었다. 그래서 스레드 객체를 생성을 했다.
        Thread thread = new Thread() {   //익명객체로 만들자.
            @Override
            public void run() {
                try {
                    socket = new Socket(); //소켓 객체를 생성한다.
                    socket.connect(new InetSocketAddress("localhost", 5001)); //연결을 한다. 여기서도 예외가 발생한다.
                    Platform.runLater(()->{                                                //이제 로그에 출력하자.
                        displayText("[연결 완료: "  + socket.getRemoteSocketAddress() + "]"); //어떤 서버에 연결이 완료됬는지 출력해줌. (서버의 ip주소와 포트번호)
                        btnConn.setText("stop");                                            //start버튼의 글자를 바꿔준다.
                        btnSend.setDisable(false);                                          //send버튼을 활성화시킨다. 이제 보낼 수 있다.
                    });
                } catch(Exception e) {  //5001에서 서버가 실행하고 있지 않다면 예외가 발생한다. 그러니 예외처리를 해준다.
                    Platform.runLater(()->displayText("[서버 통신 안됨]"));
                    if(!socket.isClosed()) { stopClient(); }   //소켓을 안전하게 닫아준다.
                    return;                                   //run메소드를 종료해준다.
                }
                receive(); //예외가 발생하지 않았다면 연결이 성공한 것이므로, 여기서 receive()메소드를 통해 항상 서버가 보낸 데이터를 받도록 한다.
            }
        };
        thread.start();     //이제 스레드를 시작한다.
    }

    void stopClient() {//연결 끊기 코드이다.
        try {
            Platform.runLater(()->{ //로그 출력해준다.
                displayText("[연결 끊음]");
                btnConn.setText("start");   //글자를 start로 바꿔주고,
                btnSend.setDisable(true);   //send버튼을 다시 비활성화 시킨다. 보낼 수 없게.
            });
            if(socket!=null && !socket.isClosed()) { //이제 소켓도 닫아주자.
                socket.close();                      //소켓을 닫는다.
            }
        } catch (IOException e) {}    //close()의 예외를 잡아준다.
    }

    void receive() { //서버에서 데이터를 보내게 되면 그 데이터를 받아서 로그창에 출력을 해준다.
        while(true) { //항상 서버의 데이터를 받아야 하므로 무한루프로 작성한다.
            try {
                byte[] byteArr = new byte[100]; //데이터를 받을 바이트 배열 생성
                InputStream inputStream = socket.getInputStream();

                //서버가 비정상적으로 종료했을 경우 IOException 발생
                int readByteCount = inputStream.read(byteArr); //read()는 서버가 데이터를 보내기 전까지는 블로킹된다. 서버가 소켓을 정상 닫으면 -1 비정상 종료하면 IOExeption이 발생한다.

                //서버가 정상적으로 Socket의 close()를 호출했을 경우
                if(readByteCount == -1) { throw new IOException(); } //강제적으로 발생시킨다.

                String data = new String(byteArr, 0, readByteCount, "UTF-8"); //정상적으로 데이터를 읽었을 경우에만 이 코드를 실행한다. 문자열로 변환한다.

                Platform.runLater(()->displayText("[받기 완료] "  + data));  //출력해준다.
            } catch (Exception e) {       //예외가 발생했다면,
                Platform.runLater(()->displayText("[서버 통신 안됨]"));
                stopClient();                     //클라이언트의 연결을 끊어준다.
                break;                            // 무한루프를 빠져나간다.
            }
        }
    }

    void send(String data) { 	//데이터를 입력하고 send버튼을  클릭하면 이게 실행된다. 서버로 데이터를 보내는 역할을 한다. 데이터를 보낼 때도 별도의 스레드를 만들어서 보내주는게 좋다.
        //왜냐면 데이터를 보내는 시간이 오래 걸리면 UI가 멈춰있기 때문이다. 가능하면 javafx 어플리케이션 스레드가 통신코드를 실행하지 않도록 해주는게 좋다.
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    byte[] byteArr = data.getBytes("UTF-8"); //매개값으로 받은 문자열을 바이트 배열로 변환
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(byteArr);
                    outputStream.flush();
                    Platform.runLater(()->displayText("[보내기 완료]")); //로그에 출력해준다.
                } catch(Exception e) {//write에서 예외가 발생할 경우 -> 서버가 통신이 안될 경우
                    Platform.runLater(()->displayText("[서버 통신 안됨]"));
                    stopClient();    //안전하게 소켓을 닫아준다.
                }
            }
        };
        thread.start(); //스레드 시작
    }

    //여기는 ui코드이다.
    //////////////////////////////////////////////////////
    TextArea txtDisplay;
    TextField txtInput;
    Button btnConn, btnSend;

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        root.setPrefSize(500, 300);

        txtDisplay = new TextArea();
        txtDisplay.setEditable(false);
        BorderPane.setMargin(txtDisplay, new Insets(0,0,2,0));
        root.setCenter(txtDisplay);

        BorderPane bottom = new BorderPane();
        txtInput = new TextField();
        txtInput.setPrefSize(60, 30);
        BorderPane.setMargin(txtInput, new Insets(0,1,1,1));

        btnConn = new Button("start");
        btnConn.setPrefSize(60, 30);
        btnConn.setOnAction(e->{
            if(btnConn.getText().equals("start")) {
                startClient();
            } else if(btnConn.getText().equals("stop")){
                stopClient();
            }
        });

        btnSend = new Button("send");
        btnSend.setPrefSize(60, 30);
        btnSend.setDisable(true);
        btnSend.setOnAction(e->send(txtInput.getText()));

        bottom.setCenter(txtInput);
        bottom.setLeft(btnConn);
        bottom.setRight(btnSend);
        root.setBottom(bottom);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Client");
        primaryStage.setOnCloseRequest(event->stopClient());
        primaryStage.show();
    }

    void displayText(String text) {
        txtDisplay.appendText(text + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}