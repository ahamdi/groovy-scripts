/**
 * Created by IntelliJ IDEA.
 * User: ali
 * Date: 3/4/14
 * Time: 11:07 AM
 * This code was deduced from an online code found on stackoverflow
 */
def traceMap = [:]

// Number of lines to keep in buffer
def BUFFER_SIZE = 100

// Pattern for stack trace line
def TRACE_LINE_PATTERN = '^[\\s\\t]+at .*$'

// Log line pattern between which we try to capture full trace. We assume that a log line starts with two digits
def LOG_LINE_PATTERN = '^([<#][^/]|\\d\\d).*$'

// List of patterns to replace in final captured stack trace line, like log timestamp
def REPLACE_PATTERNS = ['(^\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d,\\d\\d\\d)']

// The script looks for log files under the current folder and prints to the console just the error exceptions 
new File('.').eachFile { File file ->
  if (file.name.contains('.log') || file.name.contains('.out')) {
    def bufferLines = []
    file.withReader { Reader reader ->
      while (reader.ready()) {
        def String line = reader.readLine()
        // a String 'at' was found on the beginning of the line -> this is an exception log that we need to print 
        if (line.matches(TRACE_LINE_PATTERN)) {
          def trace = []
          for(def i = bufferLines.size() - 1; i >= 0; i--) {
            if (!bufferLines[i].matches(LOG_LINE_PATTERN)) {
              trace.add(0, bufferLines[i])
            } else {
              trace.add(0, bufferLines[i])
              break
            }
          }
          trace.add(line)
          if (reader.ready()) {
            line = reader.readLine()
            while (!line.matches(LOG_LINE_PATTERN)) {
              trace.add(line)
              if (reader.ready()) {
                line = reader.readLine()
              } else {
                break;
              }
            }
          }
          def traceString = trace.join("\n")
          // We replace unnecessary informations in the catched exception with empty string
          REPLACE_PATTERNS.each { pattern ->
            traceString = traceString.replaceAll(pattern, '')
          }
          //  if the exception is not in the map, we add it else we increase the number of occurences.
          if (traceMap.containsKey(traceString)) {
            traceMap.put(traceString, traceMap.get(traceString) + 1)
          } else {
            traceMap.put(traceString, 1)
          }
        }
        // Keep the buffer of last lines.
        bufferLines.add(line)
        if (bufferLines.size() > BUFFER_SIZE) {
          bufferLines.remove(0)
        }
      }
    }
  }
}

traceMap = traceMap.sort { it.value }

traceMap.reverseEach { trace, number ->
  println "-- Exception Occured $number times -----------------------------------------"
  println trace
}

