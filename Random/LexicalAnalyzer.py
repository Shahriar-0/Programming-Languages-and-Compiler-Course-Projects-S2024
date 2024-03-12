import re
from enum import Enum


class ErrorHandlingMethod(Enum):
    PANIC_MODE = 1
    # TODO: implement the following error handling methods
    # SYNCHRONIZATION = 2
    # EXTENDING_RULES = 3


class Lexical:
    def __init__(self, input):
        self.input = input
        self.index = 0
        self.current: str = input[0]
        self.__tokens_init()
        self.error_handling_method = ErrorHandlingMethod.PANIC_MODE

    def __tokens_init(self):
        # FIXME: the following tokens are just for testing purposes
        # self.tokens = [
        #     ("INTEGER", r"\d+"),
        #     ("LPAREN", r"\("),
        #     ("RPAREN", r"\)"),
        #     ("WS", r"\s+")
        #     ("ID", r"[a-zA-Z_][a-zA-Z0-9_]*")
        #     ("OPERATOR", r"\+|\-|\*|\/"),
        #     ("ASSIGN", r"\="),
        #     ("SEMICOLON", r"\;")
        #     ("FLOAT", r"\d+\.\d+")
        #     ("STRING", r"\".*?\"")
        #     ("KEYWORD", r"if|else|while|for|def|return|class|import|from|as|elif|try|except|finally|raise|assert|pass|break|continue|del|global|nonlocal|lambda|yield|with|in|is|not|and|or|True|False|None")
        #     ("COMPARISON", r"\<|\>|\=\=|\!\=|\>\=|\<\=")
        # ]
        
        self.tokens = (
            ("T1", r"(a?)(b|c)*a"),
            ("T2", r"(b?)(a|c)*b"),
            ("T3", r"(c?)(a|b)*c"),
        )

    def advance(self):
        self.index += 1
        if self.index < len(self.input):
            self.current = self.input[self.index]
        else:
            self.current = None

    def peek(self):
        if self.index + 1 < len(self.input):
            return self.input[self.index + 1]
        else:
            return None

    def tokenize(self):
        """the algorithm works like this:
        it starts from our current index and tries to match the input with the regular expression
        it stores the longest match in all tokens
        and if it finds a match, it advances the index by the length of the match
        if it find multiple match with the same length, it takes the first one
        if it doesn't find any match, it advances the index by 1 (PANIC MODE)"""
        self.output = dict()
        tokens = []
        while self.current is not None:
            max_length = 0
            max_token = None
            for token_regex in self.tokens:
                name, regex = token_regex
                pattern = re.compile(regex)
                match = pattern.match(self.input, self.index)
                if match and match.end() - match.start() > max_length:
                    max_length = match.end() - match.start()
                    max_token = name
            if max_token is not None:
                tokens.append((max_token, self.input[self.index : self.index + max_length]))
                self.output.update({self.input[self.index : self.index + max_length] : max_token})
                self.index += max_length
            else:
                if self.error_handling_method == ErrorHandlingMethod.PANIC_MODE:
                    self.index += 1
            if self.index < len(self.input):
                self.current = self.input[self.index]
            else:
                self.current = None
        return tokens
    
    def show_result(self):
        lengths = [max(len(key), len(value)) for key, value in self.output.items()]
        for key, length in zip(self.output.keys(), lengths):
            print(f"{key:>{length}}| ", end="")
        print()
        for value, length in zip(self.output.values(), lengths):
            print(f"{value:>{length}}| ", end="")
            

if __name__ == "__main__":
    input = "bbaacdabcaaabaa"
    lexer = Lexical(input)
    tokens = lexer.tokenize()
    lexer.show_result()