package cn.jony.libutil.io;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import okio.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static cn.jony.libutil.Constants.UTF_8;


/**
 * General IO stream manipulation utilities.
 * <p/>
 * This class provides static utility methods for input/output operations.
 * <ul>
 * <li>closeQuietly - these methods close a stream ignoring nulls and exceptions
 * <li>toXxx/read - these methods read data from a stream
 * <li>write - these methods write data to a stream
 * <li>copy - these methods copy all the data from one stream to another
 * <li>contentEquals - these methods compare the content of two streams
 * </ul>
 * <p/>
 * The byte-to-char methods and char-to-byte methods involve a conversion step.
 * Two methods are provided in each case, one that uses the platform default
 * encoding and the other which allows you to specify an encoding. You are
 * encouraged to always specify an encoding because relying on the platform
 * default can lead to unexpected results, for example when moving from
 * development to production.
 * <p/>
 * All the methods in this class that read a stream are buffered internally.
 * This means that there is no cause to use a <code>BufferedInputStream
 * or <code>BufferedReader. The default buffer size of 4K has been shown
 * to be efficient in tests.
 * <p/>
 * Wherever possible, the methods in this class do <em>not flush or close
 * the stream. This is to avoid making non-portable assumptions about the
 * streams' origin and further use. Thus the caller is still responsible for
 * closing streams after use.
 * <p/>
 * Origin of code: Excalibur.
 * <p>
 * jony modify: add okio dependency for this class
 *
 * @author Peter Donald
 * @author Jeff Turner
 * @author Matthew Hawthorne
 * @author Stephen Colebourne
 * @author Gareth Davis
 * @author Ian Springer
 * @author Niall Pemberton
 * @author Sandy McArthur
 * @author jony
 * @version $Id: IOUtils.java 2016-11-03 $
 */
@SuppressWarnings("unused")
public class IOUtils {
    // NOTE: This class is focussed on InputStream, OutputStream, Reader and
    // Writer. Each method should take at least one of these as a parameter,
    // or return one of them.

    /**
     * The Unix directory separator character.
     */
    public static final char DIR_SEPARATOR_UNIX = '/';
    /**
     * The Windows directory separator character.
     */
    public static final char DIR_SEPARATOR_WINDOWS = '\\';
    /**
     * The system directory separator character.
     */
    public static final char DIR_SEPARATOR = File.separatorChar;
    /**
     * The Unix line separator string.
     */
    public static final String LINE_SEPARATOR_UNIX = "\n";
    /**
     * The Windows line separator string.
     */
    public static final String LINE_SEPARATOR_WINDOWS = "\r\n";
    /**
     * The system line separator string.
     */
    public static final String LINE_SEPARATOR;

    static {
        // avoid security issues
        StringBuilderWriter buf = new StringBuilderWriter(4);
        PrintWriter out = new PrintWriter(buf);
        out.println();
        LINE_SEPARATOR = buf.toString();
        out.close();
    }

    /**
     * The default buffer size to use for
     * {@link #copyLarge(InputStream, OutputStream)}
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * The default buffer size to use for the skip() methods.
     */
    private static final int SKIP_BUFFER_SIZE = 2048;

    // Allocated in the skip method if necessary.
    private static char[] SKIP_CHAR_BUFFER;
    private static byte[] SKIP_BYTE_BUFFER;

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public IOUtils() {
        super();
    }

    // -----------------------------------------------------------------------

    /**
     * Unconditionally close an <code>Reader.
     * <p/>
     * Equivalent to {@link Reader#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p/>
     * Example code:
     * <pre>
     *   char[] data = new char[1024];
     *   Reader in = null;
     *   try {
     *       in = new FileReader("foo.txt");
     *       in.read(data);
     *       in.close(); //close errors are handled
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(in);
     *   }
     * </pre>
     *
     * @param input the Reader to close, may be null or already closed
     */
    public static void closeQuietly(Reader input) {
        closeQuietly((Closeable) input);
    }

    /**
     * Unconditionally close a <code>Writer.
     * <p/>
     * Equivalent to {@link Writer#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p/>
     * Example code:
     * <pre>
     *   Writer out = null;
     *   try {
     *       out = new StringWriter();
     *       out.write("Hello World");
     *       out.close(); //close errors are handled
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(out);
     *   }
     * </pre>
     *
     * @param output the Writer to close, may be null or already closed
     */
    public static void closeQuietly(Writer output) {
        closeQuietly((Closeable) output);
    }

    public static void closeQuietly(Source source) {
        closeQuietly((Closeable) source);
    }

    public static void closeQuietly(Sink sink) {
        closeQuietly((Closeable) sink);
    }

    /**
     * Unconditionally close an <code>InputStream.
     * <p/>
     * Equivalent to {@link InputStream#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p/>
     * Example code:
     * <pre>
     *   byte[] data = new byte[1024];
     *   InputStream in = null;
     *   try {
     *       in = new FileInputStream("foo.txt");
     *       in.read(data);
     *       in.close(); //close errors are handled
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(in);
     *   }
     * </pre>
     *
     * @param input the InputStream to close, may be null or already closed
     */
    public static void closeQuietly(InputStream input) {
        closeQuietly((Closeable) input);
    }

    /**
     * Unconditionally close an <code>OutputStream.
     * <p/>
     * Equivalent to {@link OutputStream#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p/>
     * Example code:
     * <pre>
     * byte[] data = "Hello, World".getBytes();
     *
     * OutputStream out = null;
     * try {
     *     out = new FileOutputStream("foo.txt");
     *     out.write(data);
     *     out.close(); //close errors are handled
     * } catch (IOException e) {
     *     // error handling
     * } finally {
     *     IOUtils.closeQuietly(out);
     * }
     * </pre>
     *
     * @param output the OutputStream to close, may be null or already closed
     */
    public static void closeQuietly(OutputStream output) {
        closeQuietly((Closeable) output);
    }

    /**
     * Unconditionally close a <code>Closeable.
     * <p/>
     * Equivalent to {@link Closeable#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p/>
     * Example code:
     * <pre>
     *   Closeable closeable = null;
     *   try {
     *       closeable = new FileReader("foo.txt");
     *       // process closeable
     *       closeable.close();
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(closeable);
     *   }
     * </pre>
     *
     * @param closeable the object to close, may be null or already closed
     * @since Commons IO 2.0
     */
    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    /**
     * Unconditionally close a <code>Socket.
     * <p/>
     * Equivalent to {@link Socket#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p/>
     * Example code:
     * <pre>
     *   Socket socket = null;
     *   try {
     *       socket = new Socket("http://www.foo.com/", 80);
     *       // process socket
     *       socket.close();
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(socket);
     *   }
     * </pre>
     *
     * @param sock the Socket to close, may be null or already closed
     * @since Commons IO 2.0
     */
    public static void closeQuietly(Socket sock) {
        if (sock != null) {
            try {
                sock.close();
            } catch (IOException ioe) {
                // ignored
            }
        }
    }

    // read toByteArray
    // -----------------------------------------------------------------------

    /**
     * Get the contents of an <code>InputStream as a byte[].
     * <p/>
     *
     * @param input the <code>InputStream to read from
     * @return the requested byte array
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     */
    public static byte[] toByteArray(InputStream input) throws IOException {
        return Okio.buffer(Okio.source(input)).readByteArray();
    }

    // read char[]
    // -----------------------------------------------------------------------

    /**
     * Get the contents of an <code>InputStream as a character array
     * using the default character encoding of the platform.
     * <p/>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream.
     *
     * @param is the <code>InputStream to read from
     * @return the requested character array
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static char[] toCharArray(InputStream is) throws IOException {
        return toCharArray(is, UTF_8);
    }

    /**
     * Get the contents of an <code>InputStream as a character array
     * using the specified character encoding.
     * <p/>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA.
     * <p/>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream.
     *
     * @param is       the <code>InputStream to read from
     * @param encoding the encoding to use, null means platform default
     * @return the requested character array
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static char[] toCharArray(InputStream is, String encoding) throws IOException {
        return Okio.buffer(Okio.source(is)).readString(Charset.forName(encoding)).toCharArray();
    }

    // read toString
    // -----------------------------------------------------------------------

    /**
     * Get the contents of an <code>InputStream as a String
     * using the default character encoding of the platform.
     * <p/>
     *
     * @param input the <code>InputStream to read from
     * @return the requested String
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     */
    public static String toString(InputStream input) throws IOException {
        return Okio.buffer(Okio.source(input)).readUtf8();
    }

    /**
     * Get the contents of an <code>InputStream as a String
     * using the specified character encoding.
     * <p/>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA.
     * <p/>
     *
     * @param input    the <code>InputStream to read from
     * @param encoding the encoding to use, null means platform default
     * @return the requested String
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     */
    public static String toString(InputStream input, String encoding) throws IOException {
        return Okio.buffer(Okio.source(input)).readString(Charset.forName(encoding));
    }

    // readLines
    // -----------------------------------------------------------------------

    /**
     * Get the contents of an <code>InputStream as a list of Strings,
     * one entry per line, using the default character encoding of the platform.
     * <p/>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream.
     *
     * @param input the <code>InputStream to read from, not null
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static List<String> readLines(InputStream input) throws IOException {
        InputStreamReader reader = new InputStreamReader(input);
        return readLines(reader);
    }

    /**
     * Get the contents of an <code>InputStream as a list of Strings,
     * one entry per line, using the specified character encoding.
     * <p/>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA.
     * <p/>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream.
     *
     * @param input    the <code>InputStream to read from, not null
     * @param encoding the encoding to use, null means platform default
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static List<String> readLines(InputStream input, String encoding) throws IOException {
        if (encoding == null) {
            return readLines(input);
        } else {
            InputStreamReader reader = new InputStreamReader(input, encoding);
            return readLines(reader);
        }
    }

    /**
     * Get the contents of a <code>Reader as a list of Strings,
     * one entry per line.
     * <p/>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedReader.
     *
     * @param input the <code>Reader to read from, not null
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static List<String> readLines(Reader input) throws IOException {
        BufferedReader reader = new BufferedReader(input);
        List<String> list = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            list.add(line);
            line = reader.readLine();
        }
        return list;
    }

    // lineIterator
    // -----------------------------------------------------------------------

    /**
     * Return an Iterator for the lines in a <code>Reader.
     * <p/>
     * <code>LineIterator holds a reference to the open
     * <code>Reader specified here. When you have finished with the
     * iterator you should close the reader to free internal resources.
     * This can be done by closing the reader directly, or by calling
     * {@link LineIterator#close()} or {@link LineIterator#closeQuietly(LineIterator)}.
     * <p/>
     * The recommended usage pattern is:
     * <pre>
     * try {
     *   LineIterator it = IOUtils.lineIterator(reader);
     *   while (it.hasNext()) {
     *     String line = it.nextLine();
     *     /// do something with line
     *   }
     * } finally {
     *   IOUtils.closeQuietly(reader);
     * }
     * </pre>
     *
     * @param reader the <code>Reader to read from, not null
     * @return an Iterator of the lines in the reader, never null
     * @throws IllegalArgumentException if the reader is null
     * @since Commons IO 1.2
     */
    public static LineIterator lineIterator(Reader reader) {
        return new LineIterator(reader);
    }

    /**
     * Return an Iterator for the lines in an <code>InputStream, using
     * the character encoding specified (or default encoding if null).
     * <p/>
     * <code>LineIterator holds a reference to the open
     * <code>InputStream specified here. When you have finished with
     * the iterator you should close the stream to free internal resources.
     * This can be done by closing the stream directly, or by calling
     * {@link LineIterator#close()} or {@link LineIterator#closeQuietly(LineIterator)}.
     * <p/>
     * The recommended usage pattern is:
     * <pre>
     * try {
     *   LineIterator it = IOUtils.lineIterator(stream, "UTF-8");
     *   while (it.hasNext()) {
     *     String line = it.nextLine();
     *     /// do something with line
     *   }
     * } finally {
     *   IOUtils.closeQuietly(stream);
     * }
     * </pre>
     *
     * @param input    the <code>InputStream to read from, not null
     * @param encoding the encoding to use, null means platform default
     * @return an Iterator of the lines in the reader, never null
     * @throws IllegalArgumentException if the input is null
     * @throws IOException              if an I/O error occurs, such as if the encoding is invalid
     * @since Commons IO 1.2
     */
    public static LineIterator lineIterator(InputStream input, String encoding) throws IOException {
        Reader reader;
        if (encoding == null) {
            reader = new InputStreamReader(input);
        } else {
            reader = new InputStreamReader(input, encoding);
        }
        return new LineIterator(reader);
    }

    /**
     * convert inputStream to a bufferedSource
     *
     * @param inputStream
     * @return
     */
    public static BufferedSource toSource(InputStream inputStream) {
        return Okio.buffer(Okio.source(inputStream));
    }

    /**
     * Convert the specified CharSequence to an bufferedSource, encoded as bytes
     * using the specified character encoding.
     * <p/>
     * Character encoding names can be found at <a
     * href="http://www.iana.org/assignments/character-sets">IANA.
     *
     * @param input    the CharSequence to convert
     * @param encoding the encoding to use, null means platform default
     * @return an input stream
     * @throws IOException if the encoding is invalid
     * @since Commons IO 2.0
     */
    public static BufferedSource toSource(CharSequence input, String encoding) throws IOException {
        return toSource(input.toString(), encoding);
    }

    // -----------------------------------------------------------------------

    /**
     * Convert the specified string to a BufferedSource, encoded as bytes using
     * the default character encoding of the platform.
     *
     * @param input the string to convert
     * @return an input stream
     * @since Commons IO 1.1
     */
    public static BufferedSource toSource(String input) {
        byte[] bytes = input.getBytes();
        Buffer buffer = new Buffer();
        buffer.write(bytes);
        return buffer;
    }

    /**
     * Convert the specified string to a BufferedSource, encoded as bytes using
     * the default character encoding of the platform.
     *
     * @param input    the string to convert
     * @param encoding the encoding to use, null means platform default
     * @return an input stream
     * @since Commons IO 1.1
     */
    public static BufferedSource toSource(String input, String encoding) throws IOException {
        byte[] bytes = encoding != null ? input.getBytes(encoding) : input.getBytes();
        Buffer buffer = new Buffer();
        buffer.write(bytes);
        return buffer;
    }

    /**
     * convert outputStream to a bufferedSink
     *
     * @param outputStream
     * @return
     */
    public static BufferedSink toSink(OutputStream outputStream) {
        return Okio.buffer(Okio.sink(outputStream));
    }


    // write String
    // -----------------------------------------------------------------------

    /**
     * Writes chars from a <code>String to a Writer.
     *
     * @param data   the <code>String to write, null ignored
     * @param output the <code>Writer to write to
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static void write(String data, Writer output) throws IOException {
        if (data != null) {
            output.write(data);
        }
    }

    /**
     * Writes chars from a <code>String to bytes on an
     * <code>OutputStream using the default character encoding of the
     * platform.
     * <p/>
     * This method uses {@link String#getBytes()}.
     *
     * @param data   the <code>String to write, null ignored
     * @param output the <code>OutputStream to write to
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static void write(String data, OutputStream output) throws IOException {
        if (data != null) {
            output.write(data.getBytes());
        }
    }

    /**
     * Writes chars from a <code>String to bytes on an
     * <code>OutputStream using the specified character encoding.
     * <p/>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA.
     * <p/>
     * This method uses {@link String#getBytes(String)}.
     *
     * @param data     the <code>String to write, null ignored
     * @param output   the <code>OutputStream to write to
     * @param encoding the encoding to use, null means platform default
     * @throws NullPointerException if output is null
     * @throws IOException          if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static void write(String data, OutputStream output, String encoding) throws IOException {
        if (data != null) {
            if (encoding == null) {
                write(data, output);
            } else {
                output.write(data.getBytes(encoding));
            }
        }
    }

    // writeLines
    // -----------------------------------------------------------------------

    /**
     * Writes the <code>toString() value of each item in a collection to
     * an <code>OutputStream line by line, using the default character
     * encoding of the platform and the specified line ending.
     *
     * @param lines      the lines to write, null entries produce blank lines
     * @param lineEnding the line separator to use, null is system default
     * @param output     the <code>OutputStream to write to, not null, not closed
     * @throws NullPointerException if the output is null
     * @throws IOException          if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static void writeLines(Collection<?> lines, String lineEnding, OutputStream output) throws IOException {
        if (lines == null) {
            return;
        }
        if (lineEnding == null) {
            lineEnding = LINE_SEPARATOR;
        }

        BufferedSink sink = toSink(output);

        for (Object line : lines) {
            if (line != null) {
                sink.write(line.toString().getBytes());
            }
            sink.write(lineEnding.getBytes());
        }
    }

    /**
     * Writes the <code>toString() value of each item in a collection to
     * an <code>OutputStream line by line, using the specified character
     * encoding and the specified line ending.
     * <p/>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA.
     *
     * @param lines      the lines to write, null entries produce blank lines
     * @param lineEnding the line separator to use, null is system default
     * @param output     the <code>OutputStream to write to, not null, not closed
     * @param encoding   the encoding to use, null means platform default
     * @throws NullPointerException if the output is null
     * @throws IOException          if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static void writeLines(Collection<?> lines, String lineEnding, OutputStream output, String encoding)
            throws IOException {
        if (encoding == null) {
            writeLines(lines, lineEnding, output);
        } else {
            if (lines == null) {
                return;
            }
            if (lineEnding == null) {
                lineEnding = LINE_SEPARATOR;
            }

            BufferedSink sink = toSink(output);

            for (Object line : lines) {
                if (line != null) {
                    sink.write(line.toString().getBytes(encoding));
                }
                sink.write(lineEnding.getBytes(encoding));
            }
        }
    }


    // copy from InputStream
    // -----------------------------------------------------------------------

    /**
     * Copy bytes from an <code>InputStream to an
     * <code>OutputStream.
     * <p/>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream.
     * <p/>
     * Large streams (over 2GB) will return a bytes copied value of
     * <code>-1 after the copy has completed since the correct
     * number of bytes cannot be returned as an int. For large streams
     * use the <code>copyLarge(InputStream, OutputStream) method.
     *
     * @param input  the <code>InputStream to read from
     * @param output the <code>OutputStream to write to
     * @return the number of bytes copied, or -1 if > Integer.MAX_VALUE
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    /**
     * Copy bytes from a large (over 2GB) <code>InputStream to an
     * <code>OutputStream.
     * <p/>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream.
     *
     * @param input  the <code>InputStream to read from
     * @param output the <code>OutputStream to write to
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since Commons IO 1.3
     */
    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        BufferedSink sink = toSink(output);
        BufferedSource source = toSource(input);
        return source.readAll(sink);
    }


    // content equals
    // -----------------------------------------------------------------------

    /**
     * Compare the contents of two Streams to determine if they are equal or
     * not.
     * <p/>
     * This method buffers the input internally using
     * <code>BufferedInputStream if they are not already buffered.
     *
     * @param input1 the first stream
     * @param input2 the second stream
     * @return true if the content of the streams are equal or they both don't
     * exist, false otherwise
     * @throws NullPointerException if either input is null
     * @throws IOException          if an I/O error occurs
     */
    public static boolean contentEquals(InputStream input1, InputStream input2) throws IOException {
        if (!(input1 instanceof BufferedInputStream)) {
            input1 = new BufferedInputStream(input1);
        }
        if (!(input2 instanceof BufferedInputStream)) {
            input2 = new BufferedInputStream(input2);
        }

        int ch = input1.read();
        while (-1 != ch) {
            int ch2 = input2.read();
            if (ch != ch2) {
                return false;
            }
            ch = input1.read();
        }

        int ch2 = input2.read();
        return (ch2 == -1);
    }

    /**
     * Compare the contents of two Readers to determine if they are equal or
     * not.
     * <p/>
     * This method buffers the input internally using
     * <code>BufferedReader if they are not already buffered.
     *
     * @param input1 the first reader
     * @param input2 the second reader
     * @return true if the content of the readers are equal or they both don't
     * exist, false otherwise
     * @throws NullPointerException if either input is null
     * @throws IOException          if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static boolean contentEquals(Reader input1, Reader input2) throws IOException {
        if (!(input1 instanceof BufferedReader)) {
            input1 = new BufferedReader(input1);
        }
        if (!(input2 instanceof BufferedReader)) {
            input2 = new BufferedReader(input2);
        }

        int ch = input1.read();
        while (-1 != ch) {
            int ch2 = input2.read();
            if (ch != ch2) {
                return false;
            }
            ch = input1.read();
        }

        int ch2 = input2.read();
        return (ch2 == -1);
    }

    /**
     * Skip bytes from an input byte stream.
     *
     * @param input  byte stream to skip
     * @param toSkip number of bytes to skip.
     * @throws IOException              if there is a problem reading the file
     * @throws IllegalArgumentException if toSkip is negative
     * @see InputStream#skip(long)
     * @since Commons IO 2.0
     */
    public static void skip(InputStream input, long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }

        toSource(input).skip(toSkip);
    }
}
