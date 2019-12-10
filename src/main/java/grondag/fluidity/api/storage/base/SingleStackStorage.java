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
package grondag.fluidity.api.storage.base;

import java.util.function.Consumer;

import com.google.common.base.Preconditions;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.item.base.BulkItem;
import grondag.fluidity.api.item.base.ItemStackView;
import grondag.fluidity.api.item.base.StackHelper;
import grondag.fluidity.api.storage.BulkStorage;
import grondag.fluidity.api.storage.ItemStorage;
import grondag.fluidity.api.storage.view.ArticleView;
import grondag.fluidity.api.transact.TransactionContext;

public class SingleStackStorage extends AbstractStorage implements ItemStorage, BulkStorage {
	protected ItemStack stack = ItemStack.EMPTY;
	protected final ItemStackView view = new ItemStackView();

	@Override
	public int slotCount() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public boolean hasDynamicSlots() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ArticleView> T view(int slot) {
		Preconditions.checkElementIndex(slot, 1, "Invalid slot number");
		return (T) view.prepare(stack, 0);
	}

	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		context.setState(stack.copy());
		return rollbackHandler;
	}

	@Override
	protected void handleRollback(TransactionContext context) {
		stack = context.getState();
	}

	@Override
	public int getInvSize() {
		return 1;
	}

	@Override
	public boolean isInvEmpty() {
		return stack.isEmpty();
	}

	@Override
	public ItemStack getInvStack(int slot) {
		Preconditions.checkElementIndex(slot, 1, "Invalid slot number");

		return stack;
	}

	@Override
	public ItemStack takeInvStack(int slot, int count) {
		Preconditions.checkElementIndex(slot, 1, "Invalid slot number");

		if (stack.isEmpty() || count == 0) {
			return ItemStack.EMPTY;
		}

		final int n = Math.min(count, stack.getCount());
		final ItemStack result = stack.copy();
		result.setCount(n);
		stack.decrement(n);
		notifyListeners(view.prepare(stack, 0));
		markDirty();

		return result;
	}

	@Override
	public ItemStack removeInvStack(int slot) {
		Preconditions.checkElementIndex(slot, 1, "Invalid slot number");

		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		final ItemStack result = stack;
		stack = ItemStack.EMPTY;
		notifyListeners(view.prepare(stack, 0));

		return result;
	}

	@Override
	public void setInvStack(int slot, ItemStack itemStack) {
		Preconditions.checkElementIndex(slot, 1, "Invalid slot number");

		if (ItemStack.areItemsEqual(itemStack, stack) && itemStack.getCount() == stack.getCount()) {
			return;
		}

		stack = itemStack;
		notifyListeners(view.prepare(stack, 0));
		markDirty();
	}

	@Override
	public void markDirty() {
		//NOOP - this implementation doesn't support vanilla inventory listeners
	}

	@Override
	public void clear() {
		if (!stack.isEmpty()) {
			stack = ItemStack.EMPTY;
			notifyListeners(view.prepare(stack, 0));
			markDirty();
		}
	}

	@Override
	public long accept(Item item, CompoundTag tag, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);

		final int n;
		final boolean keepStack;

		if (stack.isEmpty()) {
			n = (int) Math.min(item.getMaxCount(), count);
			keepStack = false;
		} else if (StackHelper.areItemsEqual(item, tag, stack)) {
			n = (int) Math.min(item.getMaxCount() - stack.getCount(), count);
			keepStack = true;
		} else {
			n = 0;
			keepStack = false;
		}

		if (!simulate && n != 0) {
			if (keepStack) {
				stack.increment(n);
			} else {
				stack = StackHelper.newStack(item, tag, n);
			}

			notifyListeners(view.prepare(stack, 0));
		}

		return n;
	}

	@Override
	public long supply(Item item, CompoundTag tag, long count, boolean simulate) {
		Preconditions.checkArgument(count >= 0, "Request to supply negative items. (%s)", count);

		final int n;

		if (!stack.isEmpty() && StackHelper.areItemsEqual(item, tag, stack)) {
			n = (int) Math.min(stack.getCount(), count);
		} else {
			n = 0;
		}

		if (!simulate && n != 0) {
			stack.decrement(n);
			notifyListeners(view.prepare(stack, 0));
		}

		return n;
	}

	@Override
	public FractionView accept(BulkItem item, FractionView volume, boolean simulate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FractionView supply(BulkItem item, FractionView volume, boolean simulate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long accept(BulkItem item, long numerator, long divisor) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long supply(BulkItem item, long numerator, long divisor) {
		// TODO Auto-generated method stub
		return 0;
	}
}
