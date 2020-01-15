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
package grondag.fluidity.api.storage;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.ItemStack;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.impl.storage.AlwaysReturnRequestedImpl;
import grondag.fluidity.impl.storage.AlwaysReturnZeroImpl;

/**
 * Store with fixed handles - similar to slots but they don't have aribtrary limits
 * and request to accept or supply incompatible with existing content is rejected.
 */
@API(status = Status.EXPERIMENTAL)
public interface FixedArticleFunction extends ArticleFunction {
	/**
	 * Will return zero if handle slot is already occupied and different.
	 * @param item
	 * @param count
	 * @param simulate
	 * @param handle
	 * @return
	 */
	long apply(int handle, Article item, long count, boolean simulate);

	default long apply(int handle, ItemStack stack, long count, boolean simulate) {
		return apply(handle, Article.of(stack), count, simulate);
	}

	default long apply(int handle, ItemStack stack, boolean simulate) {
		return apply(handle, Article.of(stack), stack.getCount(), simulate);
	}

	Fraction apply(int handle, Article item, Fraction volume, boolean simulate);

	long apply(int handle, Article item, long numerator, long divisor, boolean simulate);

	FixedArticleFunction ALWAYS_RETURN_REQUESTED = AlwaysReturnRequestedImpl.INSTANCE;
	FixedArticleFunction ALWAYS_RETURN_ZERO = AlwaysReturnZeroImpl.INSTANCE;
}
