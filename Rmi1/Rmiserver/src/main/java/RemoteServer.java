import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class RemoteServer extends UnicastRemoteObject implements RemoteInterface{

    //객체 생성 시점에도 문제가 발생할 수 있다 => RemoteServer클래스에 빨간줄 뜸
    //생성자에도 throws 해주면 빨간줄 사라짐
    protected RemoteServer() throws RemoteException {
        super();
    }

    public static void main(String[] args) {
        try {
            //1.구현한 RMI용 객체를 클라이언트에서 사용할 수 있도록
            //RMI서버 (Registry)에 등록한다.

            //1-1.RMI용 인터페이스를 구현한 원격객체 생성하기
            RemoteInterface inf = new RemoteServer();

            //2. 구현한 객체를 클라이언트에서 찾을 수 있도록
            //   'Registry객체'를 생성해서 등록한다.

            //2-1. 포트번호를 지정하여 Registry객체 생성(기본포트값 : 1099)
            Registry reg = LocateRegistry.createRegistry(8888);

            //3. Registry서버에 제공하는 객체 등록
            //형식) Registry객체변수.rebind("객체의 Alias", 원격객체) Alias:식별자
            // Client는 서버에서 '원격 객체'를 바인딩 시키기 위해서 식별자(Alias) 이용해서 Client에서 lookup함
            reg.rebind("server", inf);

            System.out.println("RMI서버가 준비되었습니다.");

        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int doRemotePrint(String str) throws RemoteException {
        int length = str.length();
        System.out.println("클라이언트에서 보내온 메시지 : "+str);
        System.out.println("출력 끝...");
        return length;
    }

    @Override
    public void doPrintList(List<String> list) throws RemoteException {
        System.out.println("클라이어트에서 보낸 List값들...");
        for (int i = 0; i < list.size(); i++) {
            System.out.println((i+1) + "번째 : "+ list.get(i));
        }
        System.out.println("List 출력 끝..");
    }

    @Override
    public void doPrintVO(TestVO vo) throws RemoteException {
        System.out.println("클라이언트에서 보내온 TestVO객체의 값 출력");
        System.out.println("testId : "+ vo.getTestId());
        System.out.println("testNum : "+vo.getTestNum());
        System.out.println("TestVO객체 출력 끝...");
    }

    @Override
    public List<String> sendstringlistmsg() throws RemoteException {
        List<String> ls=new ArrayList<String>();
        ls.add("a");
        ls.add("b");
        ls.add("c");

        return ls;
    }

    @Override
    public void setFiles(FileInfoVO[] fInfo) throws RemoteException {
        FileOutputStream fos = null;
        String dir = "F:\\JavaProjects\\Rmi1";//파일이 저장될 위치
        System.out.println("파일 저장 시작...");

        for (int i = 0; i < fInfo.length; i++) {
            try {
                fos = new FileOutputStream(dir+fInfo[i].getFileName());

                //클라이언트에서 전달한 파일데이터(byte[])를 서버측에 저장한다.
                fos.write(fInfo[i].getFileData());
                fos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("파일 저장 완료...");
    }
}
