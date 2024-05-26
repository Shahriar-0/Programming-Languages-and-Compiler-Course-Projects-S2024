package main.ast.type;

public abstract class Type {

	public boolean sameType(Type other) {
		return (
			(this.getClass().equals(other.getClass())) &&
			!((this instanceof NoType) || (other instanceof NoType))
		);
	}

	public boolean sameTypeConsideringNoType(Type other) {
		return this.getClass().equals(other.getClass());
	}

	public boolean isNoType() {
		return this instanceof NoType;
	}
}
