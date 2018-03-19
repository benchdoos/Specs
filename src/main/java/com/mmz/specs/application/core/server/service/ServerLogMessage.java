/*
 * (C) Copyright 2018.  Eugene Zrazhevsky and others.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Contributors:
 * Eugene Zrazhevsky <eugene.zrazhevsky@gmail.com>
 */

package com.mmz.specs.application.core.server.service;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ServerLogMessage {
    private long time;
    private String message;
    private ServerLogMessageLevel level;

    public ServerLogMessage(String message, ServerLogMessageLevel level) {
        this.time = Calendar.getInstance().getTimeInMillis();
        this.message = message;
        this.level = level;
    }

    public String getFormattedMessage() {
        Date date = new Date(time);
        String dateString = new SimpleDateFormat("HH:mm:ss").format(date);
        return "[" + dateString + "]\t" + message.replace("\n", " ");
    }

    @Override
    public int hashCode() {
        int result = (int) (time ^ (time >>> 32));
        result = 31 * result + message.hashCode();
        result = 31 * result + level.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerLogMessage that = (ServerLogMessage) o;

        if (time != that.time) return false;
        if (!message.equals(that.message)) return false;
        return level == that.level;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("time", time)
                .append("message", message)
                .append("level", level)
                .toString();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ServerLogMessageLevel getLevel() {
        return level;
    }

    public void setLevel(ServerLogMessageLevel level) {
        this.level = level;
    }

    public long getTime() {
        return time;
    }

    public enum ServerLogMessageLevel {
        INFO, SUCCESS, WARN
    }
}
