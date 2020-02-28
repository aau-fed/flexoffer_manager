
/*
 * Created by bijay
 * GOFLEX :: WP2 :: core-api
 * Copyright (c) 2018 The GoFlex Consortium
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining  a copy of this software and associated documentation
 *  files (the "Software") to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify, merge,
 *  publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions: The above copyright notice and
 *  this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON
 *  INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 *  Last Modified 3/23/18 1:19 PM
 */

package org.goflex.wp2.fman.flexoffer;

/*-
 * #%L
 * GOFLEX::WP2::FlexOfferManager Backend
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


import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.FlexOfferState;
import org.goflex.wp2.core.util.FlexOfferJsonConvertor;
import org.goflex.wp2.fman.user.UserT;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * Created by bijay on 11/27/17.
 * Table to store flex_offer data
 */
@Entity
@Table(name = "flexoffer",
       indexes = { @Index(columnList = "status")})
public class FlexOfferT implements Serializable{

    private static final long serialVersionUID = -1690738940421286346L;

    //@GeneratedValue(strategy = GenerationType.IDENTITY)


//    @GeneratedValue(generator = "uuid2")
//    @GenericGenerator(name = "uuid2", strategy = "uuid2")
//    @Column(columnDefinition = "BINARY(16)")
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch=FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name="userId", nullable = false)
    private UserT user;

    private Date creationTime;

    private FlexOfferState status;

    @Column(name = "FlexOffer", nullable = false,length=100000)
    @Convert(converter = FlexOfferJsonConvertor.class)
    private FlexOffer flexoffer;

    public FlexOfferT(){

    }

    public FlexOfferT(UserT user, Date creationTime, FlexOfferState status,
                      FlexOffer flexoffer){
        this.user = user;
        this.creationTime = creationTime;
        this.status = status;
        this.flexoffer = flexoffer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserT getUser() {
        return this.user;
    }

    public void setUser(UserT user) {
        this.user = user;
    }

    public FlexOfferState getStatus() {
        return status;
    }

    public void setStatus(FlexOfferState status) {
        this.status = status;
    }

    public FlexOffer getFlexoffer() {
        return flexoffer;
    }

    public void setFlexoffer(FlexOffer flexoffer) {
        this.flexoffer = flexoffer;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlexOfferT that = (FlexOfferT) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(id, that.id) &&
                Objects.equals(creationTime, that.creationTime) &&
                status == that.status &&
                Objects.equals(flexoffer, that.flexoffer);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, user, creationTime, status, flexoffer);
    }

}
