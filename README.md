# Programming-Languages-and-Compiler-Course-Projects

- [Programming-Languages-and-Compiler-Course-Projects](#programming-languages-and-compiler-course-projects)
  - [Intro](#intro)
  - [Phase 1](#phase-1)
  - [Phase 2](#phase-2)
  - [Phase 3](#phase-3)

## Intro

In this repository, we tried to implement a compiler for a simple language. The new language is called `FunctionCraft`, and it is a functional programming language. The formal documentation for language is available in the `FunctionCraft\Documentation` directory.

## Phase 1

In the first phase of the project, we implemented designed the grammar for the language and implemented the lexer and parser for the language. The lexer and parser are implemented using `ANTLR4`. The results of the lexer and parser are tested using the provided test cases, you can find tests and their parse tree in the respective directory of this phase. The whole structure is depicted using other graphical methods for better understanding.

## Phase 2

In the second phase of the project, first we implemented and generated an abstract syntax tree (AST) for the language. Then we implemented a part of semantic analysis called name analysis. The name analysis is implemented using the visitor pattern. The compiler is designed in a way to gather as much error as possible in one run. Other than normal name checking (for which you can find a list in the description of respective phase), a few additional checks are implemented, one of them is circular dependency check. Also in this phase we implemented a symbol table to store the information of the symbols in the program. The symbol table is implemented using a hash table.

## Phase 3

In the third phase of the project, we implemented the type checking part of the semantic analysis. The type checking is implemented using the visitor pattern. The compiler is designed in a way to gather as much error as possible in one run. The type checking is implemented for many different types of expressions and statements. The type checker checks for valid operations and expressions, and for type compatibility. The return type of the functions and the type of the arguments passed to the functions, the type of the variables and the type of the values assigned to the variables are also checked.
