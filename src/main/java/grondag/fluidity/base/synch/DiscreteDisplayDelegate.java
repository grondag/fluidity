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
package grondag.fluidity.base.synch;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.impl.DiscreteDisplayDelegateImpl;

/**
 * Client-side representation of server inventory that supports
 * very large quantities and slotless/virtual containers via handles.
 */
@Experimental
public interface DiscreteDisplayDelegate extends DisplayDelegate {
	@Override
	long getCount();

	void setCount(long count);

	DiscreteDisplayDelegate set(Article article, long count, int handle);

	default DiscreteDisplayDelegate set(DiscreteDisplayDelegate from) {
		return set(from.article(), from.getCount(), from.handle());
	}

	DiscreteDisplayDelegate EMPTY = new DiscreteDisplayDelegateImpl(Article.NOTHING, 0, -1);

	static DiscreteDisplayDelegate create(Article stack, long count, int handle) {
		return new DiscreteDisplayDelegateImpl(stack, count, handle);
	}
}
