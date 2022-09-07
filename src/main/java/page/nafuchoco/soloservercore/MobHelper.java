/*
 * Copyright 2022 NAFU_at
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

package page.nafuchoco.soloservercore;

import org.bukkit.entity.*;

import java.util.List;

public class MobHelper {

    public static List<Mob> getOffensive(List<Entity> entities) {
        return entities.stream()
                .filter(MobHelper::isOffensive)
                .map(Mob.class::cast)
                .toList();
    }

    public static boolean isOffensive(Entity entity) {
        return entity instanceof Monster
                || entity instanceof Phantom
                || entity instanceof PolarBear
                || entity instanceof Dolphin
                || entity instanceof Hoglin
                || entity instanceof Wolf
                || entity instanceof Llama
                || entity instanceof Panda;
    }

    public static boolean isHostile(Entity entity) {
        return entity instanceof Monster
                || entity instanceof Phantom;
    }
}
