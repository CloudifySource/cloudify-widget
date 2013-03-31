/*
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * the license agreement granted to you by GigaSpaces.
 */

package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

/**
 * User: guym
 * Date: 3/29/13
 * Time: 10:00 PM
 */
@Entity
public class WidgetIcon extends Model {

    @Id
    public Long id;

    @Lob
    private byte[] data;

    private String name;

    private String contentType;

    @OneToOne(mappedBy = "icon")
    private Widget widget;

    public static Model.Finder<Long,WidgetIcon> find = new Model.Finder<Long,WidgetIcon>(Long.class, WidgetIcon.class);

    public static WidgetIcon findByWidgetApiKey( String apiKey ){
        return find.where(  ).eq( "widget.apiKey",apiKey ).findUnique();
    }

    public Long getId()
    {
        return id;
    }

    public byte[] getData(){
        return data;
    }

    public void setData( byte[] data ){
        this.data = data;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType( String contentType )
    {
        this.contentType = contentType;
    }
}
