from graphviz import Digraph
from Constants import *

class Node:
    def __init__(self, value: str = None, children: list["Node"] = [], parent: "Node" = None):
        self._value: str = value
        self._children: list["Node"] = children
        self._parent: "Node" = parent

    def __str__(self) -> str:
        return f"at address {id(self)}: {self._value} -> {', '.join([str(child) for child in self._children] if self._children else '')}"

    def __repr__(self) -> str:
        return self.__str__()

    def get_value(self) -> str:
        return self._value

    def get_children(self) -> list["Node"]:
        return self._children

    def get_parent(self) -> "Node":
        return self._parent

    def add_child(self, child: "Node") -> None:
        self._children.append(child)

    def set_parent(self, parent: "Node") -> None:
        self._parent = parent

    def set_value(self, value: str) -> None:
        self._value = value
    
    def draw(self, graph: Digraph, id: int = 0) -> int:
        label = f"{self._value}_{id}"
        for child in self._children:
            id += 1
            child_label = f"{child.get_value()}_{id}"
            graph.edge(label, child_label)
            id = child.draw(graph, id)
        return id

            