/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.enums.autocomplete;

public enum CharacterSet {

    WINDOWS_874("windows-874"),
    WINDOWS_1250("windows-1250"),
    WINDOWS_1251("windows-1251"),
    WINDOWS_1252("windows-1252"),
    WINDOWS_1253("windows-1253"),
    WINDOWS_1254("windows-1254"),
    WINDOWS_1255("windows-1255"),
    WINDOWS_1256("windows-1256"),
    WINDOWS_1257("windows-1257"),
    WINDOWS_1258("windows-1258"),
    US_ASCII("US-ASCII"),
    ISO_8859_1("ISO-8859-1"),
    ISO_8859_2("ISO-8859-2"),
    ISO_8859_3("ISO-8859-3"),
    ISO_8859_4("ISO-8859-4"),
    ISO_8859_5("ISO-8859-5"),
    ISO_8859_6("ISO-8859-6"),
    ISO_8859_7("ISO-8859-7"),
    ISO_8859_8("ISO-8859-8"),
    ISO_8859_9("ISO-8859-9"),
    ISO_8859_10("ISO-8859-10"),
    ISO_2022_KR("ISO-2022-KR"),
    ISO_2022_JP("ISO-2022-JP"),
    ISO_2022_JP_2("ISO-2022-JP-2"),
    ISO_2022_CN("ISO-2022-CN"),
    ISO_2022_CN_EXT("ISO-2022-CN-EXT"),
    UTF_8("UTF-8"),
    ;

    private String name;

    CharacterSet(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
