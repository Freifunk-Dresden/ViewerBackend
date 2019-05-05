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

import de.freifunkdresden.viewerbackend.Link;
import de.freifunkdresden.viewerbackend.Location;
import de.freifunkdresden.viewerbackend.NodeType;
import java.util.HashSet;

public abstract class DataParser {

    public Boolean getAutoUpdate() throws Exception {
        return null;
    }

    public Short getClients() throws Exception {
        return null;
    }

    public String getCommunity() throws Exception {
        return null;
    }

    public String getEMail() throws Exception {
        return null;
    }

    public String getFirmwareBase() throws Exception {
        return null;
    }

    public String getFirmwareVersion() throws Exception {
        return null;
    }
    
    public Long getFirstseen() throws Exception {
        return null;
    }

    public String getGatewayIp() throws Exception {
        return null;
    }
    
    public Long getLastseen() throws Exception {
        return null;
    }

    public HashSet<Link> getLinkSet() throws Exception {
        return null;
    }

    public Float getLoadAvg() throws Exception {
        return null;
    }
    
    public Location getLocation() throws Exception {
        return null;
    }

    public Double getMemoryUsage() throws Exception {
        return null;
    }

    public String getModel() throws Exception {
        return null;
    }

    public String getName() throws Exception {
        return null;
    }

    public NodeType getRole() throws Exception {
        return null;
    }

    public Float getUptime() throws Exception {
        return null;
    }
    
    public Boolean isOnline() throws Exception {
        return null;
    }
    
    public Boolean isGateway() throws Exception {
        return null;
    }
    
    public Integer getCPUCount() throws Exception {
        return null;
    }
}
