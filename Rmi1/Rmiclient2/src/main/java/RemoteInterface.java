import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteInterface extends Remote {
    //메서드 정의
    public int doRemotePrint(String str) throws RemoteException;

    public void doPrintList(List<String> list) throws RemoteException;

    public void doPrintVO(TestVO vo) throws RemoteException;

    //파일 전송을 위한 메서드
    public void setFiles(FileInfoVO[] fInfo) throws RemoteException;

    public List<String> sendstringlistmsg() throws  RemoteException;
}
