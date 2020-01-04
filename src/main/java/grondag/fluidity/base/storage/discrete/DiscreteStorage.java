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
package grondag.fluidity.base.storage.discrete;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.storage.ArticleFunction;
import grondag.fluidity.api.storage.Storage;

@API(status = Status.EXPERIMENTAL)
public interface DiscreteStorage extends Storage {
	@Override
	default FractionView amount() {
		return Fraction.of(count());
	}

	@Override
	default FractionView volume() {
		return Fraction.of(capacity());
	}

	public interface DiscreteArticleFunction extends ArticleFunction {
		@Override
		default FractionView apply(Article item, FractionView volume, boolean simulate) {
			return volume.whole() == 0 ? Fraction.ZERO : Fraction.of(apply(item, volume.whole(), simulate));
		}

		@Override
		default long apply(Article item, long numerator, long divisor, boolean simulate) {
			final long whole = numerator / divisor;
			return whole == 0 ? 0 : apply(item, whole, simulate) * divisor;
		}
	}
}
