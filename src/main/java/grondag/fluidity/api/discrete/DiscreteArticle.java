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
/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
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

package grondag.fluidity.api.discrete;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public final class DiscreteArticle<V> extends AbstractDiscreteArticle<V> {
    public DiscreteArticle(CompoundTag tag) {
        super(tag);
    }

    public DiscreteArticle(PacketByteBuf buffer) {
        super(buffer);
    }

    public DiscreteArticle(V article, long count) {
        super(article, count);
    }

    public DiscreteArticle(AbstractDiscreteArticle<V> template) {
        super(template);
    }

    @Override
    public final DiscreteArticle<V> toImmutable() {
        return this;
    }

    public static <T> DiscreteArticle<T> of(T article, long count) {
        return new DiscreteArticle<T>(article, count);
    }

    @Override
    public long capacity() {
        return count();
    }
}