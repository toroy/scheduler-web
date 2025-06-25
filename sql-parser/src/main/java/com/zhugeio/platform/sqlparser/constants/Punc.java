package com.zhugeio.platform.sqlparser.constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Punc {

    public static final String DOT = ".";
    public static final String EQ = "=";
    public static final String EQ2 = "==";
    public static final Set<String> EQ_SET = new HashSet<>(Arrays.asList(EQ, EQ2));

    public static final String NQ = "!=";
    public static final String DECR = "-";
    public static final String ADD = "+";
    public static final String COMMA = ",";
    public static final String COLON = ":";
    public static final String UNDERLINE = "_";
    public static final String ASTERISK = "*";

}
