package main.ast.type;

public class NoReturn extends Type {
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return 37;
    }

    @Override
    public String toString() {
        return "NoReturn";
    }

}
