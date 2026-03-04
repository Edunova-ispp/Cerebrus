package com.cerebrus.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
public class CerebrusUtils {
    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final int CODE_LENGTH = 7;
    private static final SecureRandom RNG = new SecureRandom();

    public static String generateUniqueCode(){
        char[] code = new char[CODE_LENGTH];
        for (int i = 0; i < CODE_LENGTH; i++) {
            code[i] = ALPHABET[RNG.nextInt(ALPHABET.length)];
        }
        return new String(code);
    }

    public static <T> Collection<T> shuffleCollection(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return Collections.emptyList();
        }
        if (collection.size() == 1) {
            return new ArrayList<>(collection); 
        }

        List<T> copy = new ArrayList<>(collection); 
        Collections.shuffle(copy, RNG);              
        return copy;                                 
    }
    
}
