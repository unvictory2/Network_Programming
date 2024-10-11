import java.io.Serializable;

public class CalcExpr implements Serializable {
    double operand1, operand2;
    char operator;

    public CalcExpr(double operand1, char operator, double operand2) {
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operator = operator;
    }
}
