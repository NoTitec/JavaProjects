import java.io.Serializable;

public class FileInfoVO implements Serializable {
    private String fileName;
    private byte[] fileData;

    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public byte[] getFileData() {
        return fileData;
    }
    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

}
