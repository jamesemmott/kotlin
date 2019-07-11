/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvmhost.jsr223

import org.jetbrains.kotlin.cli.common.repl.InvokeWrapper
import java.io.*
import java.nio.charset.Charset
import javax.script.Bindings
import javax.script.ScriptContext
import javax.script.SimpleBindings
import kotlin.script.experimental.jvmhost.jsr223.fromCommonsIo.ReaderInputStream
import kotlin.script.experimental.jvmhost.jsr223.fromCommonsIo.WriterOutputStream

fun makeBestIoTrappingInvoker(context: ScriptContext): InvokeWrapper {
    // for recognizable things that are NOT looping back to possible our own thread aware
    // IO trapper, we can do nice thread specific capturing, otherwise assume the worst and
    // use old style unsafe IO trapping.  A caller can easily tag their reader/writers with
    // a marker interface to tell us to do otherwise.
    return if (isFriendly(context.reader) &&
            isFriendly(context.writer) &&
            isFriendly(context.errorWriter)
    ) {
        ContextRerouteScriptIoInvoker(context)
    } else {
        UnfriendlyContextRerouteScriptIoInvoker(context)
    }
}

class UnfriendlyContextRerouteScriptIoInvoker(val context: ScriptContext) : InvokeWrapper {
    override fun <T> invoke(body: () -> T): T {
        // TODO: this is so unsafe for multi-threaded environment, we don't know what each
        // thread is capturing too, but it is likely ok since most threads in a script context
        // would be using the same input and output streams.  The bigger problem is that
        // we might leave it set to something other than the original stdin/out

        // TODO: alternative is that since most people do not change stdin/out we could always
        // set back to the original stdin/out seen at the time of the first call.  But it is hard
        // to determine the best default behavior.

        // The user can wrap their reader/writer with the IoCaptureFriendly interface and then
        // none of this evil will happen.

        val (oldIn, oldOut, oldErr) = synchronized(this.javaClass) {
            val oldIn = System.`in`
            val oldOut = System.out
            val oldErr = System.err
            System.setIn(ReaderInputStream(context.reader, Charset.defaultCharset()))
            System.setOut(PrintStream(WriterOutputStream(context.writer, Charset.defaultCharset()), true))
            System.setErr(PrintStream(WriterOutputStream(context.errorWriter, Charset.defaultCharset()), true))
            Triple(oldIn, oldOut, oldErr)
        }
        try {
            return body()
        } finally {
            synchronized(this.javaClass) {
                System.setIn(oldIn)
                System.setOut(oldOut)
                System.setErr(oldErr)
            }
        }
    }
}

class ContextRerouteScriptIoInvoker(val context: ScriptContext)
    : RerouteScriptIoInvoker(
    wrapFriendlyReader(context.reader),
    wrapFriendlyWriter(context.writer),
    wrapFriendlyWriter(context.errorWriter)
)

fun isFriendly(reader: Reader): Boolean {
    return reader is IoCaptureFriendly
            || reader is StringReader
            || reader is CharArrayReader
            || reader is FileReader
}

fun isFriendly(writer: Writer): Boolean {
    return writer is IoCaptureFriendly
            || writer is StringWriter
            || writer is CharArrayWriter
            || writer is FileWriter
}

fun wrapFriendlyReader(reader: Reader): InputStream {
    return if (isFriendly(reader)) MarkedFriendlyReaderInputStream(reader)
    else ReaderInputStream(reader, Charset.defaultCharset())
}

fun wrapFriendlyWriter(writer: Writer): PrintStream {
    return if (isFriendly(writer)) MarkedFriendlyPrintStream(WriterOutputStream(writer), true)
    else PrintStream(WriterOutputStream(writer, Charset.defaultCharset()), true)
}

interface IoCaptureFriendly

class MarkedFriendlyReaderInputStream(reader: Reader) : ReaderInputStream(reader), IoCaptureFriendly
class MarkedFriendlyInputStreamReader(inputStream: InputStream) : InputStreamReader(inputStream), IoCaptureFriendly
class MarkedFriendlyPrintWriter(outputStream: OutputStream) : PrintWriter(outputStream), IoCaptureFriendly
class MarkedFriendlyPrintStream(outputStream: OutputStream, autoFlush: Boolean) : PrintStream(outputStream, autoFlush), IoCaptureFriendly


open class IoFriendlyScriptContext : ScriptContext {
    protected var _writer: Writer = MarkedFriendlyPrintWriter(InOutTrapper.originalSystemOut)
    protected var _errorWriter: Writer = MarkedFriendlyPrintWriter(InOutTrapper.originalSystemErr)
    protected var _reader: Reader = MarkedFriendlyInputStreamReader(InOutTrapper.originalSystemIn)

    protected var _engineScope: Bindings = SimpleBindings()
    protected var _globalScope: Bindings? = null

    override fun setBindings(bindings: Bindings?, scope: Int) {
        when (scope) {
            ScriptContext.ENGINE_SCOPE -> {
                if (bindings == null) {
                    throw NullPointerException("Engine scope cannot be null.")
                }
                _engineScope = bindings
            }
            ScriptContext.GLOBAL_SCOPE -> _globalScope = bindings
            else -> throw IllegalArgumentException("Invalid scope value.")
        }
    }

    override fun getAttribute(name: String): Any? {
        checkName(name)
        return _engineScope.get(name) ?: _globalScope?.get(name)
    }

    override fun getAttribute(name: String, scope: Int): Any? {
        checkName(name)
        return when (scope) {
            ScriptContext.ENGINE_SCOPE -> _engineScope[name]
            ScriptContext.GLOBAL_SCOPE -> _globalScope?.get(name)
            else -> throw IllegalArgumentException("Illegal scope value.")
        }
    }

    override fun removeAttribute(name: String, scope: Int): Any? {
        checkName(name)
        return when (scope) {
            ScriptContext.ENGINE_SCOPE -> _engineScope.remove(name)
            ScriptContext.GLOBAL_SCOPE -> _globalScope?.remove(name)
            else -> throw IllegalArgumentException("Illegal scope value.")
        }
    }

    override fun setAttribute(name: String, value: Any, scope: Int) {
        checkName(name)
        when (scope) {
            ScriptContext.ENGINE_SCOPE -> _engineScope.put(name, value)
            ScriptContext.GLOBAL_SCOPE -> _globalScope?.put(name, value)
            else -> throw IllegalArgumentException("Illegal scope value.")
        }
    }

    override fun getWriter(): Writer {
        return _writer
    }

    override fun getReader(): Reader {
        return _reader
    }

    override fun setReader(reader: Reader) {
        _reader = reader
    }

    override fun setWriter(writer: Writer) {
        _writer = writer
    }

    override fun getErrorWriter(): Writer {
        return _errorWriter
    }

    override fun setErrorWriter(writer: Writer) {
        _errorWriter = writer
    }

    override fun getAttributesScope(name: String): Int {
        checkName(name)
        if (_engineScope.containsKey(name)) {
            return ScriptContext.ENGINE_SCOPE
        } else if (_globalScope?.containsKey(name) ?: false) {
            return ScriptContext.GLOBAL_SCOPE
        } else {
            return -1
        }
    }

    override fun getBindings(scope: Int): Bindings? {
        if (scope == ScriptContext.ENGINE_SCOPE) {
            return _engineScope
        } else if (scope == ScriptContext.GLOBAL_SCOPE) {
            return _globalScope
        } else {
            throw IllegalArgumentException("Illegal scope value.")
        }
    }

    override fun getScopes(): List<Int> {
        return _scopes
    }

    private fun checkName(name: String) {
        if (name.isEmpty()) {
            throw IllegalArgumentException("name cannot be empty")
        }
    }

    companion object {
        private var _scopes: List<Int> = listOf(ScriptContext.ENGINE_SCOPE, ScriptContext.GLOBAL_SCOPE)
    }
}

