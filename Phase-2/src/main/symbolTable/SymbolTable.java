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

	public SymbolTable(SymbolTable pre) {
		this.items = new HashMap<>(pre.items);
	}

	public void merge(SymbolTable symbolTable) {
		this.items.putAll(symbolTable.items);
	}

	public void put(SymbolTableItem item) throws ItemAlreadyExists {
		if (items.containsKey(item.getKey())) throw new ItemAlreadyExists();
		items.put(item.getKey(), item);
	}

	public void removeItem(String key) {
		items.remove(key);
	}

	public SymbolTableItem getItem(String key) throws ItemNotFound {
		if (this.items.containsKey(key)) {
			return this.items.get(key);
		}
		throw new ItemNotFound();
	}

	@Override
	public String toString() {
		return items.keySet().toString();
	}
}
