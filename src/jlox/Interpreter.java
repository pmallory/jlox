package jlox;

import java.util.ArrayList;
import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment environment = new Environment();
    private boolean interactive = false;

    void interpret(List<Stmt> statements, boolean interactive) {
        this.interactive = interactive;

        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                    return (double)left + (double)right;
                if (left instanceof String && right instanceof String)
                    return (String)left + (String)right;
                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if ((double)right == 0)
                    throw new RuntimeError(expr.operator,
                            "Division by zero.");
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);
        }

        return null; //Unreachable
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = expr.right;

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            case BANG:
                return !isTruthy(right);
        }

        return null; //unreachable
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object a, Object b) {
        if (a instanceof Double && b instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean) return (boolean)obj;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private String stringify(Object obj) {
        if (obj == null) return "nil";
        return obj.toString();
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private void executeBlock(List<Stmt> stmts, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt stmt: stmts) {
                execute(stmt);
            }
        } finally {
            this.environment = previous;
        }

    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        Object value = evaluate(stmt.expression);
        if (this.interactive)
            System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Object visitVarExpr(Expr.Var expr) {
        return environment.get(expr.name);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);

        environment.assign(expr.name, value);
        return value;
    }
}
