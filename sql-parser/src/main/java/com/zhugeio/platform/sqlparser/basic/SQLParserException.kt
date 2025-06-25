package com.zhugeio.platform.sqlparser.basic

/**
 *  on 2017/3/16.
 */

class SQLParserException(message: String?) : RuntimeException(message) {

    constructor(message: String?, exception: Exception?) : this(message)
}