/*
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 * User: eliranm
 * Date: 4/23/13
 * Time: 7:22 PM
 */
@SuppressWarnings("serial")
@Entity
public class ServerNodeEvent extends Model {

    @Id
    private Long id;

    public static enum Type{
        DONE, ERROR, INFO
    }

    @Enumerated(EnumType.STRING)
    private Type eventType;

    private Long eventTimestamp = System.currentTimeMillis();

    @Column(length = 512)
    private String msg = "";

    @ManyToOne
    private ServerNode serverNode;

    public ServerNodeEvent setEventType(Type type) {
        this.eventType = type;
        return this;
    }

    public ServerNodeEvent setEventTimestamp(Long timestamp) {
        this.eventTimestamp = timestamp;
        return this;
    }

    public ServerNodeEvent setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public ServerNodeEvent setServerNode(ServerNode serverNode) {
        this.serverNode = serverNode;
        return this;
    }

    public Long getId() {
        return id;
    }

    public Type getEventType() {
        return eventType;
    }

    public Long getEventTimestamp() {
        return eventTimestamp;
    }

    public String getMsg() {
        return msg;
    }

    public ServerNode getServerNode() {
        return serverNode;
    }
}
