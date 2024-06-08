package main.ast.type.primitiveType;

import main.ast.type.Type;

public class IntType extends Type {

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return 3;
	}

	@Override
	public String toString() {
		return "IntType";
	}
}
