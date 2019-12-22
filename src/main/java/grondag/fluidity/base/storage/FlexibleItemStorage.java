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
package grondag.fluidity.base.storage;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import grondag.fluidity.api.article.DiscreteArticleView;
import grondag.fluidity.api.item.DiscreteItem;
import grondag.fluidity.api.storage.DiscreteStorage;
import grondag.fluidity.api.storage.DiscreteStorageListener;
import grondag.fluidity.base.article.DiscreteArticle;

@API(status = Status.EXPERIMENTAL)
public class FlexibleItemStorage extends AbstractLazyRollbackStorage<DiscreteArticleView,  DiscreteStorageListener, DiscreteItem> implements DiscreteStorage {
	protected Predicate<DiscreteItem> filter = Predicates.alwaysTrue();
	protected final FlexibleArticleManager<DiscreteItem, DiscreteArticle> articles;
	protected final DiscreteItemNotifier notifier;

	public FlexibleItemStorage(int startingHandleCount, long capacity, @Nullable Predicate<DiscreteItem> filter) {
		articles = new FlexibleArticleManager<>(startingHandleCount, DiscreteArticle::new);
		notifier = new DiscreteItemNotifier(capacity, this, articles);
		filter(filter);
	}

	public FlexibleItemStorage(int capacity) {
		this(32, capacity, null);
	}

	public void filter(Predicate<DiscreteItem> filter) {
		this.filter = filter == null ? Predicates.alwaysTrue() : filter;
	}

	@Override
	public long accept(DiscreteItem item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to accept null item");

		if (item.isEmpty()) {
			return 0;
		}

		final long result = Math.min(count, notifier.capacity - notifier.count);

		if(result > 0 && !simulate) {
			final DiscreteArticle article = articles.findOrCreateArticle(item);
			article.count += result;
			notifier.notifyAccept(article, result);
		}

		return result;
	}

	@Override
	public long supply(DiscreteItem item, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to supply negative items. (%s)", count);
		Preconditions.checkNotNull(item, "Request to supply null item");

		if (item.isEmpty() || articles.isEmpty()) {
			return 0;
		}

		final DiscreteArticle article = articles.get(item);

		if(article == null || article.isEmpty()) {
			return 0;
		}

		final long result = Math.min(count, article.count);

		if(result > 0 && !simulate) {
			notifier.notifySupply(article, result);
			article.count -= result;
		}

		return result;
	}

	@Override
	public long count() {
		return notifier.count;
	}

	@Override
	public long capacity() {
		return notifier.capacity;
	}

	@Override
	public void clear() {
		if(isEmpty()) {
			return;
		}

		final int limit = articles.handleCount();

		for (int i = 0; i < limit; i++) {
			final DiscreteArticle a = articles.get(i);

			if(!a.isEmpty()) {
				notifier.notifySupply(a, a.count);
				a.item = DiscreteItem.EMPTY;
				a.count = 0;
			}
		}

		articles.articles.clear();
		articles.emptyHandleCount = 0;
		articles.nextUnusedHandle = 0;
	}

	@Override
	public int handleCount() {
		return articles.handleCount();
	}

	@Override
	public DiscreteArticleView view(int handle) {
		return articles.get(handle);
	}

	@Override
	protected Object createRollbackState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void applyRollbackState(Object state) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void sendFirstListenerUpdate(DiscreteStorageListener listener) {
		notifier.sendFirstListenerUpdate(listener);
	}

	@Override
	public CompoundTag writeTag() {
		final CompoundTag result = new CompoundTag();
		result.putLong("cap", notifier.capacity);

		if(!isEmpty()) {
			final ListTag list = new ListTag();
			final int limit = articles.handleCount();

			for (int i = 0; i < limit; i++) {
				final DiscreteArticle a = articles.get(i);

				if(!a.isEmpty()) {
					list.add(a.toTag());
				}
			}

			result.put("items", list);
		}

		return result;
	}

	@Override
	public void readTag(CompoundTag tag) {
		clear();

		notifier.setCapacity(tag.getLong(TAG_CAPACITY));

		if(tag.contains(TAG_ITEMS)) {
			final ListTag list = tag.getList(TAG_ITEMS, 10);
			final int limit = list.size();
			final DiscreteArticle lookup = new DiscreteArticle();

			for(int i = 0; i < limit; i++) {
				lookup.readTag(list.getCompound(i));

				if(!lookup.isEmpty()) {
					accept(lookup.item(), lookup.count, false);
				}
			}
		}
	}
}
