/*
 * Copyright 2021 NAFU_at
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package page.nafuchoco.soloservercore.data;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class TeamMessage {
    private final UUID id;
    private final UUID senderPlayer;
    private final UUID targetTeam;
    private final Date sentDate;
    private final String subject;
    private final List<String> message;

    public TeamMessage(UUID id, UUID senderPlayer, UUID targetTeam, Date sentDate, String subject, List<String> message) {
        this.id = id;
        this.senderPlayer = senderPlayer;
        this.targetTeam = targetTeam;
        this.sentDate = sentDate;
        this.subject = subject;
        this.message = message;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSenderPlayer() {
        return senderPlayer;
    }

    public UUID getTargetTeam() {
        return targetTeam;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public String getSubject() {
        return subject;
    }

    public List<String> getMessage() {
        return message;
    }

    public static class TeamMessageBuilder {
        private final UUID id;
        private final UUID senderPlayer;
        private final List<String> message;
        private PlayersTeam targetTeam;
        private String subject;

        public TeamMessageBuilder(UUID senderPlayer) {
            this.id = UUID.randomUUID();
            this.senderPlayer = senderPlayer;
            message = new LinkedList<>();
        }

        public UUID getId() {
            return id;
        }

        public UUID getSenderPlayer() {
            return senderPlayer;
        }

        public PlayersTeam getTargetTeam() {
            return targetTeam;
        }

        public TeamMessageBuilder setTargetTeam(PlayersTeam playersTeam) {
            targetTeam = playersTeam;
            return this;
        }

        public String getSubject() {
            return subject;
        }

        public TeamMessageBuilder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public List<String> getMessage() {
            return message;
        }

        public TeamMessageBuilder addMessageLine(String messageLine) {
            this.message.add(messageLine);
            return this;
        }

        public TeamMessageBuilder removeMessageLine(int line) {
            message.remove(line);
            return this;
        }

        public TeamMessage build() {
            return new TeamMessage(id,
                    senderPlayer,
                    targetTeam.getId(),
                    new Date(),
                    subject,
                    message);
        }
    }
}
