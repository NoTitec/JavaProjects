import java.io.File;
import java.io.FileInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RemoteClient {
    public static void main(String[] args) {
        try {
            //1.등록된 서버를 찾기 위해 Registry객체를 생성한 후
            //  사용할 객체를 불러온다. => 서버측의 IP와 포트번호, 디폴트시 localhost RMI 기본 포트: 1099
            Registry reg = LocateRegistry.getRegistry("127.0.0.1", 8888);

            RemoteInterface inf = (RemoteInterface) reg.lookup("server");
            Scanner sc=new Scanner(System.in);
            boolean prgramexit=false;
            while(prgramexit!=true) {
                //이제부터는 불러온 원격객체의 메서드를 호출해서 사용할 수 있다.
                int a = inf.doRemotePrint("안녕하세요");
                System.out.println("반환값 => " + a);
                System.out.println("------------------------------------------");

                List<String> list = new ArrayList<String>();
                list.add("대전");
                list.add("대구");
                list.add("광주");
                list.add("인천");
                inf.doPrintList(list);
                System.out.println("List호출 끝");
                System.out.println("------------------------------------------");

                TestVO vo = new TestVO();
                vo.setTestId("dditMan");
                vo.setTestNum(123456);
                inf.doPrintVO(vo);
                System.out.println("VO 출력 메서드 호출 끝...");
                System.out.println("------------------------------------------");

                List<String> get;
                get = inf.sendstringlistmsg();
                for (String data : get) {
                    System.out.println(data);
                }
                System.out.println("서버가보낸 문자들 출력 끝...");
                System.out.println("would you wan't to stop program? y/n");
                String input=sc.next();
                if(input=="y"){
                    prgramexit=true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
