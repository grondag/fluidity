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
package grondag.fluidity.wip.base.transport;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.transact.TransactionParticipant;
import grondag.fluidity.wip.api.transport.CarrierSession;

/**
 * Flexible storage interface for tanks, containers.
 * Interface supports both discrete items and bulk resources (such as fluids.)
 */
@API(status = Status.EXPERIMENTAL)
public interface CarrierCostFunction extends TransactionParticipant {
	/**
	 * Adds items to this storage. May return less than requested.
	 *
	 * @param item Item to add
	 * @param tag NBT if item has it, null otherwise.
	 * @param count How many to add. Must be >= 0;
	 * @param simulate If true, will forecast result without making changes.
	 * @return Count added, or that would be added if {@code simulate} = true.
	 */
	long apply(CarrierSession sender, Article item, long count, boolean simulate);


	default long apply(CarrierSession sender, Item item, @Nullable CompoundTag tag, long count, boolean simulate) {
		return apply(sender, Article.of(item, tag), count, simulate);
	}

	default long apply(CarrierSession sender, Item item, long count, boolean simulate) {
		return apply(sender, Article.of(item), count, simulate);
	}

	default long apply(CarrierSession sender, ItemStack stack, long count, boolean simulate) {
		return apply(sender, Article.of(stack), count, simulate);
	}

	default long apply(CarrierSession sender, ItemStack stack, boolean simulate) {
		return apply(sender, Article.of(stack), stack.getCount(), simulate);
	}

	FractionView apply(CarrierSession sender, Article item, FractionView volume, boolean simulate);

	long apply(CarrierSession sender, Article item, long numerator, long divisor, boolean simulate);

	CarrierCostFunction FREE = new CarrierCostFunction() {

		@Override
		public TransactionDelegate getTransactionDelegate() {
			return TransactionDelegate.IGNORE;
		}

		@Override
		public long apply(CarrierSession sender, Article item, long count, boolean simulate) {
			return count;
		}

		@Override
		public FractionView apply(CarrierSession sender, Article item, FractionView volume, boolean simulate) {
			return volume.toImmutable();
		}

		@Override
		public long apply(CarrierSession sender, Article item, long numerator, long divisor, boolean simulate) {
			return numerator;
		}
	};
}