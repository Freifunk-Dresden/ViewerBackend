/*
 * The MIT License
 *
 * Copyright 2018 Niklas Merkelt.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.freifunkdresden.viewerbackend.dataparser;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.jsoup.nodes.Element;

public class DataParserRegister extends DataParser {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final Element tr;

    static {
        sdf.setTimeZone(TimeZone.getDefault());
    }

    public DataParserRegister(Element tr) {
        this.tr = tr;
    }

    @Override
    public Long getFirstseen() throws Exception {
        String firstseen = tr.child(6).child(0).getElementsByTag("td").get(0).text();
        return firstseen.isEmpty() ? null : sdf.parse(firstseen).getTime();
    }

    @Override
    public Long getLastseen() throws Exception {
        String lastseen = tr.child(6).child(0).getElementsByTag("td").get(1).text();
        return lastseen.isEmpty() ? null : sdf.parse(lastseen).getTime();
    }

    @Override
    public Boolean isGateway() throws Exception {
        return tr.child(3).child(1).attr("alt").equals("Ja");
    }
}
