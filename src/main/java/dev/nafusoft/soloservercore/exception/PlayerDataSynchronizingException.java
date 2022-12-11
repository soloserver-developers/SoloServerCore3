/*
 * Copyright 2022 Nafu Satsuki
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

package dev.nafusoft.soloservercore.exception;

public class PlayerDataSynchronizingException extends Exception {
    public PlayerDataSynchronizingException() {
    }

    public PlayerDataSynchronizingException(String message) {
        super(message);
    }

    public PlayerDataSynchronizingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlayerDataSynchronizingException(Throwable cause) {
        super(cause);
    }

    public PlayerDataSynchronizingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
