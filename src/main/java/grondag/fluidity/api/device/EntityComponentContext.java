/*******************************************************************************
 * Copyright 2019, 2020 grondag
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
package grondag.fluidity.api.device;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.entity.Entity;

/**
 * Sub-type of {@code ComponentContext} for block devices,
 * needed to carry block-specific information.
 */
@Experimental
public interface EntityComponentContext extends ComponentContext {
	/**
	 * {@code Entity} instance that may contain device component
	 *
	 * @return {@code Entity} instance that may contain device component
	 */
	<E extends Entity> E entity();
}
