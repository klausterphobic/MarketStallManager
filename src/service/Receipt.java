package service;

public class Receipt {
    private final String text;
    private final String filePath;

    public Receipt(String text, String filePath) {
        this.text = text;
        this.filePath = filePath;
    }

    public String getText() { return text; }
    public String getFilePath() { return filePath; }
}