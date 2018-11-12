package com.yumi.sspt.plugin.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * ByteArrayHttpResponse
 *
 * @author chenwenlong@foresee.com.cn
 * @version 1.0
 */
public class ByteArrayHttpResponse extends HttpServletResponseWrapper {

    private ByteArrayPrintWriter pw;
    /**
     * Constructs a response adaptor wrapping the given response.
     *
     * @param response
     * @throws IllegalArgumentException if the response is null
     */
    public ByteArrayHttpResponse(HttpServletResponse response, ByteArrayPrintWriter pw) {
        super(response);
        this.pw = pw;
    }

    public byte[] toByteArray() {
        return pw.toByteArray();
    }

    public ServletResponse getResponse() {
        return super.getResponse();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return this.pw.getStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return this.pw.getWriter();
    }
}
