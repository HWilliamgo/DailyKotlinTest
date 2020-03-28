
import com.sun.deploy.trace.Trace.flush
import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException

/**
 * date: 2019/12/16
 * author: hwj
 * description:
 */
fun getStackTraceString(tr: Throwable?): String {
    if (tr == null) {
        return ""
    }

    // This is to reduce the amount of log spew that apps do in the non-error
    // condition of the network being unavailable.
    var t = tr
    while (t != null) {
        if (t is UnknownHostException) {
            return ""
        }
        t = t.cause
    }

    val sw = StringWriter()
    val pw = PrintWriter(sw)
    tr.printStackTrace(pw)
    pw.flush()
    return sw.toString()
}