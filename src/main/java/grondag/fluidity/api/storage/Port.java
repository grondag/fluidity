/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package grondag.fluidity.api.storage;

import grondag.fluidity.api.bulk.BulkStorage;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public interface Port {
    static int NORMAL = 0;
    static int EXACT = 1;
    static int SIMULATE = 2;

    default Identifier id() {
        return BulkStorage.ANONYMOUS_ID;
    }

    default Direction side() {
        return null;
    }

    default boolean canAccept() {
        return true;
    }

    default boolean canSupply() {
        return true;
    }
}
