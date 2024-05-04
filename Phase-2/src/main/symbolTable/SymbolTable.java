package main.symbolTable;

import java.util.HashMap;
import java.util.Map;
import main.symbolTable.exceptions.ItemAlreadyExists;
import main.symbolTable.exceptions.ItemNotFound;
import main.symbolTable.item.SymbolTableItem;
import main.symbolTable.utils.Stack;

public class SymbolTable {

	public static SymbolTable top;
	public static SymbolTable root;
	private static Stack<SymbolTable> stack = new Stack<>();
	private Map<String, SymbolTableItem> items;

	public static void push(SymbolTable symbolTable) {
		if (top != null) stack.push(top);
		top = symbolTable;
	}

	public static void pop() {
		top = stack.pop();
	}

	public SymbolTable() {
		this.items = new HashMap<>();
	}

	public void put(SymbolTableItem item) throws ItemAlreadyExists {
		if (items.containsKey(item.getKey())) throw new ItemAlreadyExists();
		items.put(item.getKey(), item);
	}

	public SymbolTableItem getItem(String key) throws ItemNotFound {
		SymbolTableItem symbolTableItem = this.items.get(key);
		if (symbolTableItem != null) {
			return symbolTableItem;
		}
		throw new ItemNotFound();
	}

	@Override
	public String toString() {
		return items.keySet().toString();
	}
}
