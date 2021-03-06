package intelligent.dormitory.system;

public class PersonChat {
    private int id;
    private String name;
    private String chatMessage;
    private boolean isMeSend;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChatMessage() {
        return this.chatMessage;
    }

    public void setChatMessage(String chatMessage) {
        this.chatMessage = chatMessage;
    }

    public boolean isMeSend() {
        return this.isMeSend;
    }

    public void setMeSend(boolean isMeSend) {
        this.isMeSend = isMeSend;
    }

    public PersonChat(int id, String name, String chatMessage, boolean isMeSend) {
        this.id = id;
        this.name = name;
        this.chatMessage = chatMessage;
        this.isMeSend = isMeSend;
    }

    public PersonChat() {
    }
}
