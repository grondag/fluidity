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
package grondag.fluidity.impl;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.IdentityHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.server.MinecraftServer;

import grondag.fluidity.api.transact.Transaction;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.api.transact.TransactionParticipant;
import grondag.fluidity.api.transact.TransactionParticipant.TransactionDelegate;

@Internal
public final class TransactionImpl implements Transaction {

	private final class ContextImpl implements TransactionContext {
		private ContextImpl() {
		}

		@Override
		public <T> void setState(T state) {
			stateStorage.put(contextDelegate, state);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getState() {
			return (T) stateStorage.get(contextDelegate);
		}

		@Override
		public boolean isCommited() {
			return isCommited;
		}
	}

	private final ContextImpl context = new ContextImpl();
	private boolean isOpen = true;
	private boolean isCommited = false;
	private final IdentityHashMap<TransactionDelegate, Consumer<TransactionContext>> participants = new IdentityHashMap<>();
	private final IdentityHashMap<TransactionDelegate, Object> stateStorage = new IdentityHashMap<>();
	private TransactionDelegate contextDelegate;

	private TransactionImpl() {
	}

	@Override
	public void close() {
		if (isOpen) {
			rollback();
		}
	}

	private void clear() {
		participants.clear();
		stateStorage.clear();
		contextDelegate = null;
		isOpen = false;
		isCommited = false;
	}

	private void validate() {
		if (!isOpen) {
			throw new IllegalStateException("Encountered transaction operation for closed transaction.");
		}

		if (STACK.get(stackPointer) != this) {
			throw new IndexOutOfBoundsException("Transaction operations must apply to most recent open transaction.");
		}

		if (!innerLock.isHeldByCurrentThread()) {
			throw new ConcurrentModificationException("Attempt to modify transaction status from foreign thread");
		}
	}

	@Override
	public void rollback() {
		close(false);
	}

	@Override
	public void commit() {
		close(true);
	}

	private void close(boolean isCommited) {
		validate();
		this.isCommited = isCommited;

		participants.forEach((c, r) -> {
			contextDelegate = c;
			r.accept(context);
		});

		clear();

		final boolean root = --stackPointer == -1;

		innerLock.unlock();

		// non-server threads have an additional lock we must release.
		if (Thread.currentThread() != serverThread) {
			outerLock.unlock();

			if (root) {
				// Give other threads (like the server thread) that want to do transactions a
				// chance to jump in.
				Thread.yield();
			}
		}
	}

	@Override
	public <T extends TransactionParticipant> T enlistSelf(T container) {
		validate();
		final TransactionDelegate d = container.getTransactionDelegate();

		if (!participants.containsKey(d)) {
			contextDelegate = d;
			participants.put(d, defaultRollback(d.prepareRollback(context)));
			contextDelegate = null;
		}

		return container;
	}

	private Consumer<TransactionContext> defaultRollback(Consumer<TransactionContext> consumer) {
		return consumer;
	}

	///// STATIC MEMBERS FOLLOW /////

	public static void setServerThread(MinecraftServer server) {
		serverThread = Thread.currentThread();
	}

	private static Thread serverThread;

	/** Held by thread of active transaction, regardless of thread */
	static final ReentrantLock innerLock = new ReentrantLock();

	/**
	 * Must be held if thread is not the server thread - ensures non-server threads
	 * contend with each other before contending with server thread itself.
	 */
	static final ReentrantLock outerLock = new ReentrantLock();

	private static final ArrayList<TransactionImpl> STACK = new ArrayList<>();
	private static int stackPointer = -1;

	public static TransactionImpl open() {
		// non-server threads must take turns
		if (Thread.currentThread() != serverThread) {
			outerLock.lock();
		}

		innerLock.lock();

		final TransactionImpl result;

		if (STACK.size() > ++stackPointer) {
			result = STACK.get(stackPointer);
			result.isOpen = true;
		} else {
			assert STACK.size() == stackPointer;
			result = new TransactionImpl();
			STACK.add(result);
		}

		return result;
	}

	public static TransactionImpl current() {
		return stackPointer == -1 ? null : STACK.get(stackPointer);
	}
}
