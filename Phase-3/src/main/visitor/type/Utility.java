package main.visitor.type;

import main.ast.nodes.statement.*;
import main.ast.type.*;
import java.util.List;
import java.util.logging.Logger;

import org.antlr.v4.runtime.atn.LookaheadEventInfo;

import java.util.AbstractMap;

public class Utility {
    
    private static final Type misMatchType = new MisMatchType();

    public static AbstractMap.SimpleEntry<Type, Boolean> isStillCompatible(List<Type> types, Type newType, Type oldType) {

        if (newType instanceof NoReturn) {
            return new AbstractMap.SimpleEntry<>(oldType, true);
        }

        if (newType instanceof MisMatchType || oldType instanceof MisMatchType) {
            types.add(newType);
            return new AbstractMap.SimpleEntry<>(misMatchType, false);
        }

        if (types.isEmpty()) {
            types.add(newType);
            oldType = newType;
            return new AbstractMap.SimpleEntry<>(newType, true);
        } 

        else {
            if (!oldType.sameTypeConsideringNoType(newType)) {
                types.add(newType);
                oldType = misMatchType;
                return new AbstractMap.SimpleEntry<>(misMatchType, false);
            }
            return new AbstractMap.SimpleEntry<>(newType, true);
        }
    }

    public static boolean mayContainReturn(Statement statement) {
        return statement instanceof ReturnStatement || 
               statement instanceof LoopDoStatement || 
               statement instanceof ForStatement || 
               statement instanceof IfStatement;
    }
}
