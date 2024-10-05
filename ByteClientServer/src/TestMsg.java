import java.io.Serializable;

public class TestMsg implements Serializable {
    String msg;

    TestMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "[" + msg + "]";
    }
}