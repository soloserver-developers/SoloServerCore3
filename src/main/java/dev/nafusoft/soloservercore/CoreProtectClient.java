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

package dev.nafusoft.soloservercore;

import net.coreprotect.CoreProtectAPI;
import org.bukkit.block.Block;

import java.util.List;

public class CoreProtectClient {
    private final CoreProtectAPI coreProtectAPI;

    public CoreProtectClient(CoreProtectAPI coreProtectAPI) {
        this.coreProtectAPI = coreProtectAPI;
    }

    public String getAction(Block block, int time) {
        List<String[]> lookup = coreProtectAPI.blockLookup(block, time);
        if (!lookup.isEmpty()) {
            var result = coreProtectAPI.parseResult(lookup.get(0));
            if (result.getActionId() == 1)
                return result.getPlayer();
        }
        return null;
    }
}
