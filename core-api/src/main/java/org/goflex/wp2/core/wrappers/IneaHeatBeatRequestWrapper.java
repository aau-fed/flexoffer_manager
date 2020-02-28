package org.goflex.wp2.core.wrappers;

/*-
 * #%L
 * GOFLEX::WP2::Core Data Structures
 * %%
 * Copyright (C) 2017 - 2020 The GOFLEX Consortium
 * %%
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
 * #L%
 */


/*
 * Created by Ivan Bizaca on 13/07/2017.
 */

/* FLEX OFFER MESSAGE FROM FOA TO FMAN */



import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;


@XmlAccessorType(XmlAccessType.FIELD)
public class IneaHeatBeatRequestWrapper {

    private IneaHeartBeatMessage heartbeat;


    public IneaHeartBeatMessage getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(IneaHeartBeatMessage heartbeat) {
        this.heartbeat = heartbeat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IneaHeatBeatRequestWrapper)) return false;

        IneaHeatBeatRequestWrapper that = (IneaHeatBeatRequestWrapper) o;

        return heartbeat.equals(that.heartbeat);
    }

    @Override
    public int hashCode() {
        return heartbeat.hashCode();
    }

    @Override
    public String toString() {
        return "IneaHeatBeatRequestWrapper{" +
                "heartbeat=" + heartbeat +
                '}';
    }
}
