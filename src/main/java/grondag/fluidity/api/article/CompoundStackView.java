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
package grondag.fluidity.api.article;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.fraction.MutableFraction;
import grondag.fluidity.api.item.BulkItem;
import grondag.fluidity.api.item.StackHelper;

@API(status = Status.EXPERIMENTAL)
public class CompoundStackView implements ItemArticleView, BulkArticleView, FractionView {
	protected Item item;
	protected CompoundTag tag;
	protected boolean isBulk;
	protected MutableFraction fraction;
	protected int slot;

	public CompoundStackView() {
	}

	public CompoundStackView(ItemStack stack, int slot) {
		prepare(stack, slot);
	}

	public CompoundStackView prepare(ItemStack stack, int slot) {
		item = stack.getItem();
		tag = stack.getTag();

		if (tag != null) {
			tag = tag.copy();
		}

		this.slot = slot;
		isBulk = item instanceof BulkItem;

		if (isBulk) {
			fraction.readTag(tag);
			fraction.multiply(stack.getCount());
		} else {
			fraction.set(stack.getCount());
		}

		return this;
	}

	@Override
	public long count() {
		return fraction.whole();
	}

	@Override
	public int slot() {
		return slot;
	}

	@Override
	public Item item() {
		return item;
	}

	@Override
	public CompoundTag tag() {
		return tag == null ? null : (CompoundTag) tag.copy();
	}

	@Override
	public boolean hasTag() {
		return tag != null;
	}

	@Override
	public ItemStack toStack() {
		final ItemStack result = new ItemStack(item);
		result.setTag(tag.copy());
		return result;
	}

	@Override
	public boolean isEmpty() {
		return fraction.isZero();
	}

	@Override
	public boolean isBulk() {
		return isBulk;
	}

	@Override
	public FractionView volume() {
		return fraction;
	}

	@Override
	public long whole() {
		return count();
	}

	@Override
	public long numerator() {
		return fraction.numerator();
	}

	@Override
	public long divisor() {
		return fraction.divisor();
	}

	@Override
	public BulkItem bulkItem() {
		return isBulk ? (BulkItem) item : null;
	}

	@Override
	public BulkArticleView toBulkView() {
		return isBulk ? this : null;
	}

	@Override
	public boolean isItemEqual(ItemStack stack) {
		return StackHelper.areItemsEqual(item, tag, stack);
	}

	public static CompoundStackView of(ItemStack stack) {
		return new  CompoundStackView().prepare(stack, 0);
	}
}